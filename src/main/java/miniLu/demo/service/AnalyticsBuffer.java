package miniLu.demo.service;

import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import miniLu.demo.dto.Analytics;

@Component
public class AnalyticsBuffer {
    private final ConcurrentLinkedQueue<Analytics> buffer = 
        new ConcurrentLinkedQueue<>();
    
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
       
    }
    
    @PreDestroy
    public void flushRemaining() {
        flush();
    }
}