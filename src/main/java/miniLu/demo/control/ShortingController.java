package miniLu.demo.control;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import miniLu.demo.dto.LinkDTO;
import miniLu.demo.entity.User;
import miniLu.demo.service.AnalyticsBuffer;
import miniLu.demo.service.LinkShorterService;

import java.io.IOException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.maxmind.geoip2.exception.GeoIp2Exception;

@Slf4j
@Controller
@RequestMapping("/")
@SessionAttributes("link")
public class ShortingController {

    private final LinkShorterService linkShorterService;
    private final AnalyticsBuffer analyticsBuffer;


    ShortingController(LinkShorterService linkShorterService, AnalyticsBuffer analyticsBuffer) {
        this.linkShorterService = linkShorterService;
        this.analyticsBuffer = analyticsBuffer;
    }

    @ModelAttribute("link")
    public LinkDTO getLink() {
        return new LinkDTO();
    }

    @GetMapping()
    public String getShortPage(Model model, @AuthenticationPrincipal User user) {
        log.info("Main get Page");
        if (user != null) {
            model.addAttribute("user", user);
            log.info("Logged in user: {}", user.getEmail());
        }
        return "index";
    }

    @PostMapping
    public String useShortLink(@ModelAttribute LinkDTO link, 
                               Model model, 
                               @AuthenticationPrincipal User user) {
        log.info("Creating new short Link");
        log.info("Link - " + link.getLink());
        
        if (user == null) {
            model.addAttribute("user", user);
        }
        
        LinkDTO fullLink = linkShorterService.ShortALink(link, user.getEmail());
        model.addAttribute("link", fullLink);
        return "index";
    }

    @GetMapping("/{slink}")
    public String redirectToLink(@PathVariable("slink") String shortLink,
                                HttpServletRequest httpServletRequest,
                                @AuthenticationPrincipal User user) throws IOException, GeoIp2Exception {
        log.info("\nRedirect\n");
        if ("favicon.ico".equals(shortLink)) {
            return "index";
        }
        analyticsBuffer.add(shortLink, httpServletRequest);
        // memoryStorage.printMetrics();
        String fullLink = analyticsBuffer.getFullLink(shortLink);
        if (fullLink == null) {
            log.warn("Short link not found: {}", shortLink);
            return "redirect:/";
        }
        log.info("FullLink - " + fullLink);
        if (!fullLink.startsWith("http://") && !fullLink.startsWith("https://")) {
            fullLink = "https://" + fullLink;
        }
        return "redirect:" + fullLink;
    }
}
