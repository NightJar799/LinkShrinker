package miniLu.demo.control;

import lombok.extern.slf4j.Slf4j;
import miniLu.demo.InMemmory.MemoryStorage;
import miniLu.demo.dto.Link;
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

    ShortingController(LinkShorterService linkShorterService) {
        this.linkShorterService = linkShorterService;
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
        log.info("Link - " + link.getLink());
        Link fullLink = linkShorterService.ShortALink(link);
        model.addAttribute("link", fullLink);
        return "index";
    }
}
