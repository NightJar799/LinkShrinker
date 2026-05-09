package miniLu.demo.control;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import miniLu.demo.InMemmory.MemoryStorage;
import miniLu.demo.dto.Link;
import miniLu.demo.service.AnalyticsBuffer;
import miniLu.demo.service.LinkShorterService;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.maxmind.geoip2.exception.GeoIp2Exception;

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
                             HttpServletRequest httpServletRequest) throws IOException, GeoIp2Exception {
    log.info("\nRedirect\n");
    if ("favicon.ico".equals(shortLink)) {
        return "index";
    }
    analyticsBuffer.add(shortLink, httpServletRequest);
    memoryStorage.printMetrics();
    if (memoryStorage.getList().get(shortLink) == null) {
        log.warn("Short link not found: {}", shortLink);
        return "redirect:/";
    }
    String fullLink = memoryStorage.getBigLink(shortLink);
    log.info("FullLink - " + fullLink);
    System.out.println(memoryStorage.getLenght());
    if (!fullLink.startsWith("http://") && !fullLink.startsWith("https://")) {
        fullLink = "https://" + fullLink;
    }
    return "redirect:" + fullLink;
}
}
