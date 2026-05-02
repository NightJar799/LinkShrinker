package miniLu.demo.service;

import miniLu.demo.InMemmory.MemoryStorage;
import miniLu.demo.dto.Link;
import org.springframework.stereotype.Service;

@Service
public class LinkShorterService {

    MemoryStorage memoryStorage;

    LinkShorterService(MemoryStorage memoryStorage){
        this.memoryStorage = memoryStorage;
    }

    public Link ShortALink(Link link) {
        Link lastLink;
        if (memoryStorage.getLenght() != 0) link.setId(memoryStorage.getList().getLast().getId()+1);
        else link.setId((long)1);
        link.setShortLink(String.valueOf(link.getId()));

        memoryStorage.addLink(link);
        return link;
    }
}
