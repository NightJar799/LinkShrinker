package miniLu.demo.serviceTest;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.maxmind.geoip2.exception.GeoIp2Exception;

import jakarta.servlet.http.HttpServletRequest;
import miniLu.demo.InMemmory.MemoryStorage;
import miniLu.demo.dto.Analytics;
import miniLu.demo.service.AnalyticsBuffer;

@ExtendWith(MockitoExtension.class)
public class AnalyticsBufferService {

    @Mock
    private MemoryStorage memoryStorage;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AnalyticsBuffer analyticsBuffer;

    @Test
    void flushTest_WhenBufferHasEvents_ShouldDrainAndStoreThem() throws IOException, GeoIp2Exception {
        String shortCode = "123456";
        when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(httpServletRequest.getHeader("Referer")).thenReturn("https://google.com");

        analyticsBuffer.add(shortCode, httpServletRequest);
        analyticsBuffer.add(shortCode, httpServletRequest);
        analyticsBuffer.add(shortCode, httpServletRequest);

        analyticsBuffer.flush();

        ArgumentCaptor<List<Analytics>> captor = ArgumentCaptor.forClass(List.class);
        verify(memoryStorage, times(1)).putNewMetrics(captor.capture());

        List<Analytics> storedMetrics = captor.getValue();
        assertThat(storedMetrics).hasSize(3);
        assertThat(storedMetrics.get(0).getShortLink()).isEqualTo(shortCode);
        assertThat(storedMetrics.get(0).getIp()).isEqualTo("192.168.1.1");
        assertThat(storedMetrics.get(0).getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(storedMetrics.get(0).getReferer()).isEqualTo("https://google.com");
    }

    @Test
    void flushTest_WhenBufferIsEmpty_ShouldNotCallMemoryStorage() {
        analyticsBuffer.flush();
        verify(memoryStorage, never()).putNewMetrics(anyList());
    }

    @Test
    void flushRemainingTest_ShouldCallFlush() throws IOException, GeoIp2Exception {
        String shortCode = "abcdef";
        when(httpServletRequest.getRemoteAddr()).thenReturn("10.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Chrome");
        when(httpServletRequest.getHeader("Referer")).thenReturn(null);

        analyticsBuffer.add(shortCode, httpServletRequest);

        analyticsBuffer.flushRemaining();

        ArgumentCaptor<List<Analytics>> captor = ArgumentCaptor.forClass(List.class);
        verify(memoryStorage, times(1)).putNewMetrics(captor.capture());

        List<Analytics> storedMetrics = captor.getValue();
        assertThat(storedMetrics).hasSize(1);
        assertThat(storedMetrics.get(0).getShortLink()).isEqualTo(shortCode);
        assertThat(storedMetrics.get(0).getIp()).isEqualTo("10.0.0.1");
        assertThat(storedMetrics.get(0).getUserAgent()).isEqualTo("Chrome");
    }
}