package miniLu.demo.InMemmory;

import miniLu.demo.dto.Link;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MemoryStorage {
    List<Link> list = new ArrayList<>();

    public boolean addLink(Link link) {
        return list.add(link);
    }

    public List<Link> getList() {
        return list;
    }
    public Long getLenght() {
        return (long) list.size();
    }
}
