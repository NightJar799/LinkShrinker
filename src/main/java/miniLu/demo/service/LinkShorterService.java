package miniLu.demo.service;

import lombok.extern.slf4j.Slf4j;
import miniLu.demo.Repository.LinkRepository;
import miniLu.demo.Repository.UserRepository;
import miniLu.demo.dto.LinkDTO;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkShorterService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    LinkShorterService(LinkRepository linkRepository, UserRepository userRepository){
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
    }

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    public LinkDTO ShortALink(LinkDTO link, String userEmail) {
        String link62 = into62BitLink(linkRepository.count());
        miniLu.demo.entity.Link newLink = new miniLu.demo.entity.Link();
        Long userId = userRepository.findByEmail(userEmail).getId();
        newLink.setLink(link.getLink());
        newLink.setShortLink(link62);
        newLink.setUserId(userId);
        linkRepository.save(newLink);

        log.info("shortLink - " + newLink.getShortLink());
        log.info("FullLink - " + newLink.getLink());
        log.info("Id - " + newLink.getId());

        link.setShortLink(link62);
        return link;
    }

    private String into62BitLink(Long id) {
        if (id < 10) return String.valueOf(id);

        StringBuilder sb = new StringBuilder();
        while (id != 0) {
            sb.append(ALPHABET.charAt((int) (id % BASE)));
            id /= BASE;
        }

        return String.valueOf(sb);
    }
}
