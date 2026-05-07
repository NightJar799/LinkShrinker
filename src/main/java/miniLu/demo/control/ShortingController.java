package miniLu.demo.control;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import miniLu.demo.InMemmory.MemoryStorage;
import miniLu.demo.dto.Link;
import miniLu.demo.service.AnalyticsBuffer;
import miniLu.demo.service.LinkShorterService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/")
@SessionAttributes("link")
public class ShortingController {

    LinkShorterService linkShorterService;
    AnalyticsBuffer analyticsBuffer;
    MemoryStorage memoryStorage;


    ShortingController(LinkShorterService linkShorterService, MemoryStorage memoryStorage, 
                        AnalyticsBuffer analyticsBuffer) {
        this.linkShorterService = linkShorterService;
        this.analyticsBuffer = analyticsBuffer;
        this.memoryStorage = memoryStorage;
    }

    @ModelAttribute("link")
    public Link getLink() {
        return new Link();
    }

    @GetMapping()
    public String getShortPage() {
        log.info("Main get Page");
        return "index";
    }

    @PostMapping
    public String useShortLink(@ModelAttribute Link link, Model model) {
        log.info("Creating new short Link");
        log.info("Link - " + link.getLink());
        Link fullLink = linkShorterService.ShortALink(link);
        model.addAttribute("link", fullLink);
        return "index";
    }

    @GetMapping("/{slink}")
    public String redirectToLink(@PathVariable("slink") String shortLink,
                                 HttpServletRequest httpServletRequest) {
        log.info("\nRedirect\n");
        analyticsBuffer.add(shortLink, httpServletRequest);
        memoryStorage.printMetrics();
        String fullLink = memoryStorage.getBigLink(shortLink);
        log.info("FillLink - " + fullLink);
        System.out.println(memoryStorage.getLenght());
        return"redirect:" + fullLink;
    }
}
