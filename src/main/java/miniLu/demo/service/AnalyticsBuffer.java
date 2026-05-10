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
import miniLu.demo.Repository.LinkRepository;
import miniLu.demo.Repository.UserMetricRepository;
import miniLu.demo.entity.Link;
import miniLu.demo.entity.UserMetric;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;

@Slf4j
@Component
public class AnalyticsBuffer {
    private final UserMetricRepository userMetricRepository;
    private final LinkRepository linkRepository;
    private final UserAgentAnalyzer userAgentAnalyzer;
    private final BlockingQueue<UserMetric> buffer = new LinkedBlockingQueue<>(10000);
    private DatabaseReader geoIpReader;

    public AnalyticsBuffer(UserMetricRepository userMetricRepository, LinkRepository linkRepository) {
        this.userMetricRepository = userMetricRepository;
        this.linkRepository = linkRepository;
        this.userAgentAnalyzer = UserAgentAnalyzer.newBuilder().withCache(10000).build();
        log.info("AnalyticsBuffer initialized");
    }

    public String getFullLink(String shortCode) {
        return linkRepository.findByShortLink(shortCode).map(Link::getLink).orElse(null);
    }
    
    public void add(String shortCode, HttpServletRequest request) {
        try {
            log.debug("Adding analytics for shortCode: {}", shortCode);
            
            String agentString = request.getHeader("User-Agent");
            UserAgent parsed = userAgentAnalyzer.parse(agentString != null ? agentString : "");
            
            String ip = request.getRemoteAddr();
            if (ip == null || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
                ip = "8.8.8.8";
            }
            
            Long linkId = linkRepository.findByShortLink(shortCode)
                    .map(Link::getId)
                    .orElse(null);
            
            if (linkId == null) {
                log.error("Link not found for shortCode: {}", shortCode);
                return;
            }
            
            String country = "Unknown";
            String city = "Unknown";
            
            try {
                DatabaseReader dReader = getDatabaseReader();
                if (dReader != null) {
                    CityResponse response = dReader.city(InetAddress.getByName(ip));
                    country = response.getCountry() != null ? 
                              response.getCountry().getNames().get("en") : "Unknown";
                    city = response.getCity() != null ? 
                           response.getCity().getNames().get("en") : "Unknown";
                }
            } catch (Exception e) {
                log.debug("Geolocation failed: {}", e.getMessage());
            }
            
            UserMetric event = UserMetric.builder()
                .linkId(linkId)
                .ip(ip)
                .userAgent(agentString)
                .device(parsed.getValue("DeviceClass"))
                .agent(parsed.getValue("AgentClass"))
                .os(parsed.getValue("OperatingSystemClass"))
                .referer(request.getHeader("Referer"))
                .timeStamp(Instant.now().toString())
                .shortLink(shortCode)
                .country(country)
                .city(city)
                .build();
            
            buffer.offer(event);
            log.debug("Event added to buffer, size: {}", buffer.size());
            
        } catch (Exception e) {
            log.error("Failed to process analytics: {}", e.getMessage(), e);
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
                    log.warn("GeoIP database not found");
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
    
    @Scheduled(fixedDelay = 3000)
    public void flush() {
        log.debug("Flush triggered, buffer size: {}", buffer.size());
        
        if (buffer.isEmpty()) {
            return;
        }

        List<UserMetric> batch = new ArrayList<>();
        int drained = buffer.drainTo(batch, 1000);

        if (batch.isEmpty()) {
            return;
        }
        
        try {
            log.info("Saving {} metrics to database", drained);
            userMetricRepository.saveAll(batch);
            log.info("Successfully saved {} metrics", drained);
        } catch (Exception e) {
            log.error("Failed to save metrics: {}", e.getMessage(), e);
            buffer.addAll(batch);
        }
    }
    
    @PreDestroy
    public void flushRemaining() {
        log.info("Flushing remaining {} metrics", buffer.size());
        List<UserMetric> remaining = new ArrayList<>();
        buffer.drainTo(remaining);
        
        if (!remaining.isEmpty()) {
            try {
                userMetricRepository.saveAll(remaining);
                log.info("Saved {} remaining metrics", remaining.size());
            } catch (Exception e) {
                log.error("Failed to save remaining metrics: {}", e.getMessage(), e);
            }
        }
    }
}