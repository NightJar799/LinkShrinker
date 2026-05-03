package miniLu.demo.InMemmory;

import miniLu.demo.dto.Link;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class MemoryStorage {
    Map<String,Link> list = new TreeMap<>();

    public Link addLink(Link link) {
        return list.put(link.getShortLink(), link);
    }

    public Map<String,Link> getList() {
        return list;
    }
    public Long getLenght() {
        return (long) list.size();
    }
    public String getBigLink(String shortLink) {
        return list.get(shortLink).getLink();
    }
}
