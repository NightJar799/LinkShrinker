package miniLu.demo.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import miniLu.demo.InMemmory.MemoryStorage;
import miniLu.demo.dto.Analytics;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;

@Slf4j
@Component
public class AnalyticsBuffer {
    MemoryStorage memoryStorage;
    UserAgentAnalyzer userAgentAnalyzer;
    private final BlockingQueue<Analytics> buffer = new LinkedBlockingQueue<>(10000);
    private DatabaseReader geoIpReader;

    public AnalyticsBuffer(MemoryStorage memoryStorage) {
        this.memoryStorage = memoryStorage;
        this.userAgentAnalyzer = UserAgentAnalyzer.newBuilder().build();
    }
    
    public void add(String shortCode, HttpServletRequest request) {
    try {
        DatabaseReader dReader = getDatabaseReader();
        
        if (dReader == null) {
            log.warn("GeoIP database not available - skipping geolocation");
            Analytics event = buildBasicAnalytics(shortCode, request);
            buffer.add(event);
            return;
        }
        
        String agentString = request.getHeader("User-Agent");
        UserAgent parsed = userAgentAnalyzer.parse(agentString);
        
        String ip = request.getRemoteAddr().equals("127.0.0.1") ? "8.8.8.8" : request.getRemoteAddr();
        
        CityResponse response = dReader.city(InetAddress.getByName(ip));
        
        Analytics event = Analytics.builder()
            .ip(ip)
            .userAgent(agentString)
            .device(parsed.getValue("DeviceClass"))
            .agent(parsed.getValue("AgentClass"))
            .os(parsed.getValue("OperatingSystemClass"))
            .referer(request.getHeader("Referer"))
            .timeStamp(Instant.now().toString())
            .shortLink(shortCode)
            .country(response.getCountry() != null ? response.getCountry().getNames().get("en") : null)
            .city(response.getCity() != null ? response.getCity().getNames().get("en") : null)
            .build();
        
        buffer.add(event);
        
    } catch (Exception e) {
        log.error("Failed to process analytics: {}", e.getMessage(), e);
        Analytics event = buildBasicAnalytics(shortCode, request);
        buffer.add(event);
    }
}

@PostConstruct
private void initGeoIpReader() {
    try {
        InputStream inputStream = getClass().getResourceAsStream("/GeoLite2-City.mmdb");
        
        if (inputStream != null) {
            geoIpReader = new DatabaseReader.Builder(inputStream).build();
            log.info("GeoIP database loaded successfully from classpath");
        } else {
            File dbFile = new File("src/main/resources/GeoLite2-City.mmdb");
            if (dbFile.exists()) {
                geoIpReader = new DatabaseReader.Builder(dbFile).build();
                log.info("GeoIP database loaded successfully from: {}", dbFile.getAbsolutePath());
            } else {
                log.warn("GeoIP database not found at: {}", dbFile.getAbsolutePath());
                geoIpReader = null;
            }
        }
    } catch (Exception e) {
        log.warn("Failed to load GeoIP database: {}", e.getMessage());
        geoIpReader = null;
    }
}

private DatabaseReader getDatabaseReader() {
    return geoIpReader;
}

private Analytics buildBasicAnalytics(String shortCode, HttpServletRequest request) {
    String agentString = request.getHeader("User-Agent");
    UserAgent parsed = userAgentAnalyzer.parse(agentString);
    
    return Analytics.builder()
        .ip(request.getRemoteAddr())
        .userAgent(agentString)
        .device(parsed.getValue("DeviceClass"))
        .agent(parsed.getValue("AgentClass"))
        .os(parsed.getValue("OperatingSystemClass"))
        .referer(request.getHeader("Referer"))
        .timeStamp(Instant.now().toString())
        .shortLink(shortCode)
        .build();
}
    
    @Scheduled(fixedDelay = 3000)
    public void flush() {

        if (buffer.isEmpty()) return;

        List<Analytics> batch = new ArrayList<>();
        int drained = buffer.drainTo(batch, 1000);

        if (batch.isEmpty()) return;
        memoryStorage.putNewMetrics(batch);

        log.info("drained - " + drained);
    }
    
    @PreDestroy
    public void flushRemaining() {
        flush();
    }
}