package miniLu.demo.serviceTest;

import miniLu.demo.InMemmory.MemoryStorage;
import miniLu.demo.dto.Link;
import miniLu.demo.service.LinkShorterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkShorterServiceTest {

    @Mock
    private MemoryStorage memoryStorage;

    @InjectMocks
    private LinkShorterService linkShorterService;

    @Test
    void ShortALinkStorageEmpty() {
        when(memoryStorage.getLenght()).thenReturn(0L);
        when(memoryStorage.addLink(any(Link.class), any(String.class))).thenReturn(null);

        Link link = new Link();
        link.setLink("https://www.google.com");

        Link result = linkShorterService.ShortALink(link);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getShortLink()).startsWith("link/");
        assertThat(result.getLink()).isEqualTo("https://www.google.com");
    }

    @Test
    void ShortALinkStorageNonEmpty() {

        HashMap<String, Link> existingLinks = new HashMap<>();
        Link existingLink = new Link();
        existingLink.setLink("https://www.github.com/NightJar799");
        existingLink = linkShorterService.ShortALink(existingLink);
        existingLinks.put(existingLink.getShortLink(), existingLink);

        when(memoryStorage.getList()).thenReturn(existingLinks);
        when(memoryStorage.getLenght()).thenReturn(2L);
        when(memoryStorage.addLink(any(Link.class), any(String.class))).thenReturn(null);

        Link link = new Link();
        link.setLink("https://www.github.com");

        Link result = linkShorterService.ShortALink(link);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getShortLink()).startsWith("link/");
    }

    @Test
    void ShortALinkEncodingCheck() {
        when(memoryStorage.getLenght()).thenReturn(0L);
        when(memoryStorage.addLink(any(Link.class), any(String.class))).thenReturn(null);

        Link link = new Link();
        link.setLink("https://www.stackoverflow.com");

        Link result = linkShorterService.ShortALink(link);

        String shortLink = result.getShortLink();
        assertThat(shortLink).matches("link/[0-9A-Za-z]+");
        assertThat(shortLink.length()).isGreaterThan(5);
    }
}