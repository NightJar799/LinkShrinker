package miniLu.demo.serviceTest;

import miniLu.demo.Repository.LinkRepository;
import miniLu.demo.dto.LinkDTO;
import miniLu.demo.dto.UserDto;
import miniLu.demo.entity.User;
import miniLu.demo.service.LinkShorterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkShorterServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private LinkShorterService linkShorterService;

    @Test
    void shortALink_ShouldCreateShortLink() {
        miniLu.demo.entity.Link entityLink = new miniLu.demo.entity.Link();
        entityLink.setId(1L);
        entityLink.setLink("https://www.google.com");
        entityLink.setShortLink("test123");
        entityLink.setUserId(1L);

        when(linkRepository.save(any(miniLu.demo.entity.Link.class))).thenReturn(entityLink);

        LinkDTO link = new LinkDTO();
        link.setLink("https://www.google.com");

        UserDto user = new UserDto();
//        user.setId(1L);
        user.setEmail("test@example.com");

        LinkDTO result = linkShorterService.ShortALink(link, user.getEmail());

        assertThat(result).isNotNull();
        assertThat(result.getShortLink()).isNotNull();
        assertThat(result.getShortLink()).matches("[0-9A-Za-z]+");
        assertThat(result.getLink()).isEqualTo("https://www.google.com");
    }

    @Test
    void shortALink_ShouldGenerateUniqueShortLinks() {
        miniLu.demo.entity.Link entityLink1 = new miniLu.demo.entity.Link();
        entityLink1.setId(1L);
        entityLink1.setLink("https://www.github.com");
        entityLink1.setShortLink("abc1234");
        entityLink1.setUserId(1L);

        miniLu.demo.entity.Link entityLink2 = new miniLu.demo.entity.Link();
        entityLink2.setId(2L);
        entityLink2.setLink("https://www.stackoverflow.com");
        entityLink2.setShortLink("def5678");
        entityLink2.setUserId(1L);

        when(linkRepository.save(any(miniLu.demo.entity.Link.class)))
                .thenReturn(entityLink1)
                .thenReturn(entityLink2);

        UserDto user = new UserDto();
//        user.setId(1L);

        LinkDTO link1 = new LinkDTO();
        link1.setLink("https://www.github.com");

        LinkDTO link2 = new LinkDTO();
        link2.setLink("https://www.stackoverflow.com");

        LinkDTO result1 = linkShorterService.ShortALink(link1, user.getEmail());
        LinkDTO result2 = linkShorterService.ShortALink(link2, user.getEmail());

        assertThat(result1.getShortLink()).isNotNull();
        assertThat(result2.getShortLink()).isNotNull();
        assertThat(result1.getShortLink()).matches("[0-9A-Za-z]+");
        assertThat(result2.getShortLink()).matches("[0-9A-Za-z]+");
    }

    @Test
    void shortALink_ShouldSetCorrectUserId() {
        miniLu.demo.entity.Link entityLink = new miniLu.demo.entity.Link();
        entityLink.setId(1L);
        entityLink.setLink("https://www.example.com");
        entityLink.setShortLink("0");
        entityLink.setUserId(2L);

        when(linkRepository.save(any(miniLu.demo.entity.Link.class))).thenReturn(entityLink);

        LinkDTO link = new LinkDTO();
        link.setLink("https://www.example.com");

        UserDto user = new UserDto();
//        user.setId(2L);
        user.setEmail("user2@example.com");

        LinkDTO result = linkShorterService.ShortALink(link, user.getEmail());

        assertThat(result).isNotNull();
        assertThat(result.getShortLink()).isEqualTo("0");
    }
}