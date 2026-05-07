package miniLu.demo.service;

import lombok.extern.slf4j.Slf4j;
import miniLu.demo.InMemmory.MemoryStorage;
import miniLu.demo.dto.Link;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkShorterService {

    MemoryStorage memoryStorage;

    LinkShorterService(MemoryStorage memoryStorage){
        this.memoryStorage = memoryStorage;
    }

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    public Link ShortALink(Link link) {
        if (memoryStorage.getLenght() != 0) link.setId((long) (memoryStorage.getList().size()+1));
        else link.setId((long)1);

        String link62 = into62BitLink(link.getId());

        link.setFullShortLink("link/" + link62);

        memoryStorage.addLink(link, link62);
        log.info("shortLink - " + link.getShortLink());
        log.info("FullLink - " + link.getLink());
        log.info("Id - " + link.getId());
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
