package miniLu.demo.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import miniLu.demo.InMemmory.MemoryStorage;
import miniLu.demo.dto.Analytics;

@Slf4j
@Component
public class AnalyticsBuffer {
    MemoryStorage memoryStorage;
    private final BlockingQueue<Analytics> buffer = 
        new LinkedBlockingQueue<>(10000);

    public AnalyticsBuffer(MemoryStorage memoryStorage) {
        this.memoryStorage = memoryStorage;
    }
    
    public void add(String shortCode, HttpServletRequest request) {
        Analytics event = Analytics.builder()
            .ip(request.getRemoteAddr())
            .userAgent(request.getHeader("User-Agent"))
            .referer(request.getHeader("Referer"))
            .timeStamp(Instant.now().toString())
            .shortLink(shortCode)
            .build();
        
        buffer.add(event);
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