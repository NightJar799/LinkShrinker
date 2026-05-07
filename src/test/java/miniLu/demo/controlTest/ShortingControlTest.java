package miniLu.demo.controlTest;

import miniLu.demo.InMemmory.MemoryStorage;
import miniLu.demo.dto.Link;
import miniLu.demo.service.LinkShorterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import miniLu.demo.control.ShortingController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ShortingControlTest {

    private MockMvc mockMvc;

    @Mock
    private LinkShorterService linkShorterService;

    @Mock
    private MemoryStorage memoryStorage;

    @InjectMocks
    private ShortingController shortingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(shortingController).build();
    }

    @Test
    void getIndexPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void useShortLink_ShouldCreateShortLinkAndReturnIndex() throws Exception {
        Link inputLink = new Link();
        inputLink.setLink("https://www.example.com/very/long/url");

        Link resultLink = new Link();
        resultLink.setLink("https://www.example.com/very/long/url");
        resultLink.setShortLink("link/abc123");
        resultLink.setId(1000000L);

        when(linkShorterService.ShortALink(any(Link.class))).thenReturn(resultLink);

        mockMvc.perform(post("/")
                        .param("link", "https://www.example.com/very/long/url"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("link"));
    }

    @Test
    void redirectToLink() throws Exception {
        String shortLink = "abc123";
        String fullUrl = "https://www.example.com";

        when(memoryStorage.getBigLink(shortLink)).thenReturn(fullUrl);

        mockMvc.perform(get("/{slink}", shortLink))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(fullUrl));
    }
}