package miniLu.demo.serviceTest;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import jakarta.servlet.http.HttpServletRequest;
import miniLu.demo.Repository.LinkRepository;
import miniLu.demo.Repository.UserMetricRepository;
import miniLu.demo.entity.Link;
import miniLu.demo.entity.UserMetric;
import miniLu.demo.service.AnalyticsBuffer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Разрешаем неиспользуемые стабы
public class AnalyticsBufferServiceTest {

    @Mock
    private UserMetricRepository userMetricRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AnalyticsBuffer analyticsBuffer;

    private Link testLink;

    @BeforeEach
    void setUp() {
        testLink = new Link();
        testLink.setId(1L);
        testLink.setLink("https://www.example.com");
        testLink.setShortLink("abc12345");
        testLink.setUserId(1L);
    }

    @Test
    void getFullLink_ShouldReturnFullLink() {
        when(linkRepository.findByShortLink("abc12345")).thenReturn(Optional.of(testLink));

        String fullLink = analyticsBuffer.getFullLink("abc12345");
        assertThat(fullLink).isEqualTo("https://www.example.com");
    }

    @Test
    void getFullLink_WhenLinkNotFound_ShouldReturnNull() {
        when(linkRepository.findByShortLink("nonexistent")).thenReturn(Optional.empty());

        String fullLink = analyticsBuffer.getFullLink("nonexistent");
        assertThat(fullLink).isNull();
    }

    @Test
    void add_ShouldAddMetricToBuffer() {
        when(linkRepository.findByShortLink("abc12345")).thenReturn(Optional.of(testLink));
        when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(httpServletRequest.getHeader("Referer")).thenReturn("https://google.com");

        analyticsBuffer.add("abc12345", httpServletRequest);
        analyticsBuffer.flush();

        ArgumentCaptor<List<UserMetric>> captor = ArgumentCaptor.forClass(List.class);
        verify(userMetricRepository, times(1)).saveAll(captor.capture());

        List<UserMetric> storedMetrics = captor.getValue();
        assertThat(storedMetrics).hasSize(1);
        assertThat(storedMetrics.get(0).getShortLink()).isEqualTo("abc12345");
        assertThat(storedMetrics.get(0).getIp()).isEqualTo("192.168.1.1");
        assertThat(storedMetrics.get(0).getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(storedMetrics.get(0).getReferer()).isEqualTo("https://google.com");
    }

    @Test
    void flush_WhenBufferIsEmpty_ShouldNotSave() {
        analyticsBuffer.flush();
        verify(userMetricRepository, never()).saveAll(anyList());
    }

    @Test
    void flush_WhenBufferHasMultipleEvents_ShouldSaveAll() {
        when(linkRepository.findByShortLink("abc12345")).thenReturn(Optional.of(testLink));
        when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(httpServletRequest.getHeader("Referer")).thenReturn("https://google.com");

        analyticsBuffer.add("abc12345", httpServletRequest);
        analyticsBuffer.add("abc12345", httpServletRequest);
        analyticsBuffer.add("abc12345", httpServletRequest);

        analyticsBuffer.flush();

        ArgumentCaptor<List<UserMetric>> captor = ArgumentCaptor.forClass(List.class);
        verify(userMetricRepository, times(1)).saveAll(captor.capture());

        List<UserMetric> storedMetrics = captor.getValue();
        assertThat(storedMetrics).hasSize(3);
        storedMetrics.forEach(metric -> {
            assertThat(metric.getShortLink()).isEqualTo("abc12345");
            assertThat(metric.getIp()).isEqualTo("192.168.1.1");
        });
    }

    @Test
    void flushRemaining_ShouldSaveRemainingMetrics() {
        when(linkRepository.findByShortLink("abc12345")).thenReturn(Optional.of(testLink));
        when(httpServletRequest.getRemoteAddr()).thenReturn("10.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Chrome");
        when(httpServletRequest.getHeader("Referer")).thenReturn("https://example.com");

        analyticsBuffer.add("abc12345", httpServletRequest);
        analyticsBuffer.flushRemaining();

        ArgumentCaptor<List<UserMetric>> captor = ArgumentCaptor.forClass(List.class);
        verify(userMetricRepository, times(1)).saveAll(captor.capture());

        List<UserMetric> storedMetrics = captor.getValue();
        assertThat(storedMetrics).hasSize(1);
        assertThat(storedMetrics.get(0).getShortLink()).isEqualTo("abc12345");
        assertThat(storedMetrics.get(0).getIp()).isEqualTo("10.0.0.1");
        assertThat(storedMetrics.get(0).getUserAgent()).isEqualTo("Chrome");
        assertThat(storedMetrics.get(0).getReferer()).isEqualTo("https://example.com");
    }

    @Test
    void add_WhenLinkNotFound_ShouldNotAddToBuffer() {
        when(linkRepository.findByShortLink("nonexistent")).thenReturn(Optional.empty());

        analyticsBuffer.add("nonexistent", httpServletRequest);
        analyticsBuffer.flush();

        verify(userMetricRepository, never()).saveAll(anyList());
    }
}