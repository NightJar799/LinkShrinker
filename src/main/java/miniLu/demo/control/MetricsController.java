package miniLu.demo.control;

import lombok.extern.slf4j.Slf4j;
import miniLu.demo.dto.LinkDTO;
import miniLu.demo.dto.MetricsDTO;
import miniLu.demo.dto.UserDto;
import miniLu.demo.entity.User;
import miniLu.demo.service.LinkService;
import miniLu.demo.service.MetricsService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/metrics")
public class MetricsController {

    private final MetricsService metricsService;
    private final LinkService linkService;

    public MetricsController(MetricsService metricsService, LinkService linkService) {
        this.metricsService = metricsService;
        this.linkService = linkService;
    }

    @GetMapping
    public String showUserLinks(Model model, @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/auth";
        }
        
        List<LinkDTO> userLinks = linkService.findAllUsersLinksByEmail(user.getEmail());
        // linkRepository.findByUserId(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("links", userLinks);
        
        return "metrics-list";
    }

    @PostMapping("/detail")
    public String showLinkMetrics(@RequestParam("shortLink") String shortLink, 
                                  Model model, 
                                  @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/auth";
        }

        LinkDTO link = linkService.getLinkByID(shortLink);
        if (link == null || linkService.isLinkOwnByUser(user.getEmail(), shortLink)) {
            return "redirect:/metrics";
        }

        //List<UserMetric> metrics = userMetricRepository.findByLinkId(linkId);
        List<MetricsDTO> metrics = metricsService.getAllMetricsByLinkId(shortLink);

        Map<String, Long> countryCount = metricsService.countCountries(metrics);
        
        Map<String, Long> deviceCount = metricsService.countDevices(metrics);
        
        Map<String, Long> osCount = metricsService.countOs(metrics);
        
        Map<String, Long> cityCount = metricsService.countCities(metrics);;
        
        Map<String, Long> refererCount = metricsService.countReferers(metrics);;

        int totalMetrics = metrics.size();

        model.addAttribute("link", link);
        model.addAttribute("totalMetrics", totalMetrics);
        model.addAttribute("countryStats", metricsService.convertToPercentageList(countryCount, totalMetrics));
        model.addAttribute("deviceStats", metricsService.convertToPercentageList(deviceCount, totalMetrics));
        model.addAttribute("osStats", metricsService.convertToPercentageList(osCount, totalMetrics));
        model.addAttribute("cityStats", metricsService.convertToPercentageList(cityCount, totalMetrics));
        model.addAttribute("refererStats", metricsService.convertToPercentageList(refererCount, totalMetrics));
        model.addAttribute("timestamps", metrics.stream()
                .map(MetricsDTO::getTimeStamp)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList()));
        model.addAttribute("user", user);

        return "metrics-detail";
    }
}