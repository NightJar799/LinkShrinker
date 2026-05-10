package miniLu.demo.service;

import lombok.extern.slf4j.Slf4j;
import miniLu.demo.Repository.LinkRepository;
import miniLu.demo.dto.Link;
import miniLu.demo.entity.User;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkShorterService {

    private final LinkRepository linkRepository;

    LinkShorterService(LinkRepository linkRepository){
        this.linkRepository = linkRepository;
    }

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    public Link ShortALink(Link link, User user) {
        String link62 = into62BitLink(linkRepository.count());
        miniLu.demo.entity.Link newLink = new miniLu.demo.entity.Link();
        newLink.setLink(link.getLink());
        newLink.setShortLink(link62);
        newLink.setUserId(user.getId());
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
