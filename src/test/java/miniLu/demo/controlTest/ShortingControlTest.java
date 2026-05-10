package miniLu.demo.controlTest;

import miniLu.demo.dto.Link;
import miniLu.demo.entity.User;
import miniLu.demo.service.AnalyticsBuffer;
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
    private AnalyticsBuffer analyticsBuffer;

    @InjectMocks
    private ShortingController shortingController;

    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(shortingController).build();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPassword("password123");
    }

    @Test
    void getIndexPage_ShouldReturnIndexView() throws Exception {
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
        resultLink.setShortLink("abc12345");

        when(linkShorterService.ShortALink(any(Link.class), any(User.class))).thenReturn(resultLink);

        mockMvc.perform(post("/")
                        .flashAttr("link", inputLink)
                        .sessionAttr("user", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("link"));
    }

    @Test
    void redirectToLink_ShouldRedirectToFullUrl() throws Exception {
        String shortLink = "abc12345";
        String fullUrl = "https://www.example.com";

        when(analyticsBuffer.getFullLink(shortLink)).thenReturn(fullUrl);

        mockMvc.perform(get("/{slink}", shortLink))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(fullUrl));
    }

    @Test
    void redirectToLink_WhenShortLinkNotFound_ShouldRedirectToHome() throws Exception {
        String shortLink = "nonexistent";

        when(analyticsBuffer.getFullLink(shortLink)).thenReturn(null);

        mockMvc.perform(get("/{slink}", shortLink))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void redirectToLink_WhenFaviconRequested_ShouldReturnIndex() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
}