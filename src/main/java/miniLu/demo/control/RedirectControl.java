package miniLu.demo.control;

import lombok.extern.slf4j.Slf4j;
import miniLu.demo.InMemmory.MemoryStorage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/link")
@Slf4j
public class RedirectControl {

    MemoryStorage memoryStorage;

    RedirectControl(MemoryStorage memoryStorage) {
        this.memoryStorage = memoryStorage;
    }

    @GetMapping("/{slink}")
    public String redirectToLink(@PathVariable("slink") String shortLink) {
        String fullLink = memoryStorage.getBigLink(shortLink);
        log.info("FillLink - " + fullLink);
        return"redirect:" + fullLink;
    }
}
