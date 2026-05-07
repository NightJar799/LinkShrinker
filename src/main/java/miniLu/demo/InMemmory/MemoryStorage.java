package miniLu.demo.InMemmory;

import miniLu.demo.dto.Analytics;
import miniLu.demo.dto.Link;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class MemoryStorage {
    Map<String,Link> list = new TreeMap<>();
    List<Analytics> analyList = new ArrayList<>();

    public Link addLink(Link link, String key) {
        return list.put(key, link);
    }

    public Map<String,Link> getList() {
        return list;
    }
    public Long getLenght() {
        return (long) list.size();
    }
    public String getBigLink(String shortLink) throws IllegalArgumentException {
        System.out.println(shortLink);
        return list.get(shortLink).getLink();
    }

    public void putNewMetrics(List<Analytics> newMetrics) {
        analyList.addAll(newMetrics);
    }

    public void printMetrics() {
        System.out.println(analyList.toString());
    }

    public Map<String,Link> getMap() {return list;}
}
