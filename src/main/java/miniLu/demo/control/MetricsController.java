package miniLu.demo.control;

import lombok.extern.slf4j.Slf4j;
import miniLu.demo.Repository.LinkRepository;
import miniLu.demo.Repository.UserMetricRepository;
import miniLu.demo.entity.Link;
import miniLu.demo.entity.User;
import miniLu.demo.entity.UserMetric;

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

    private final LinkRepository linkRepository;
    private final UserMetricRepository userMetricRepository;

    public MetricsController(LinkRepository linkRepository, UserMetricRepository userMetricRepository) {
        this.linkRepository = linkRepository;
        this.userMetricRepository = userMetricRepository;
    }

    @GetMapping
    public String showUserLinks(Model model, @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/auth";
        }
        
        List<Link> userLinks = linkRepository.findByUserId(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("links", userLinks);
        
        return "metrics-list";
    }

    @PostMapping("/detail")
    public String showLinkMetrics(@RequestParam("linkId") Long linkId, 
                                  Model model, 
                                  @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/auth";
        }

        Optional<Link> linkOpt = linkRepository.findById(linkId);
        if (linkOpt.isEmpty() || !linkOpt.get().getUserId().equals(user.getId())) {
            return "redirect:/metrics";
        }

        Link link = linkOpt.get();
        List<UserMetric> metrics = userMetricRepository.findByLinkId(linkId);

        // Calculate statistics
        Map<String, Long> countryCount = calculatePercentages(metrics.stream()
                .map(m -> m.getCountry() != null ? m.getCountry() : "Unknown")
                .collect(Collectors.toList()));
        
        Map<String, Long> deviceCount = calculatePercentages(metrics.stream()
                .map(m -> m.getDevice() != null ? m.getDevice() : "Unknown")
                .collect(Collectors.toList()));
        
        Map<String, Long> osCount = calculatePercentages(metrics.stream()
                .map(m -> m.getOs() != null ? m.getOs() : "Unknown")
                .collect(Collectors.toList()));
        
        Map<String, Long> cityCount = calculatePercentages(metrics.stream()
                .map(m -> m.getCity() != null ? m.getCity() : "Unknown")
                .collect(Collectors.toList()));
        
        Map<String, Long> refererCount = calculatePercentages(metrics.stream()
                .map(m -> m.getReferer() != null && !m.getReferer().isEmpty() ? m.getReferer() : "Direct/Unknown")
                .collect(Collectors.toList()));

        int totalMetrics = metrics.size();

        // Convert to percentage maps
        model.addAttribute("link", link);
        model.addAttribute("totalMetrics", totalMetrics);
        model.addAttribute("countryStats", convertToPercentageList(countryCount, totalMetrics));
        model.addAttribute("deviceStats", convertToPercentageList(deviceCount, totalMetrics));
        model.addAttribute("osStats", convertToPercentageList(osCount, totalMetrics));
        model.addAttribute("cityStats", convertToPercentageList(cityCount, totalMetrics));
        model.addAttribute("refererStats", convertToPercentageList(refererCount, totalMetrics));
        model.addAttribute("timestamps", metrics.stream()
                .map(UserMetric::getTimeStamp)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList()));
        model.addAttribute("user", user);

        return "metrics-detail";
    }

    private Map<String, Long> calculatePercentages(List<String> items) {
        Map<String, Long> countMap = new HashMap<>();
        for (String item : items) {
            countMap.merge(item, 1L, Long::sum);
        }
        return countMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private List<Map<String, Object>> convertToPercentageList(Map<String, Long> countMap, int total) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            double percentage = total > 0 ? (entry.getValue() * 100.0) / total : 0;
            item.put("name", entry.getKey());
            item.put("count", entry.getValue());
            item.put("percentage", String.format("%.1f%%", percentage));
            result.add(item);
        }
        return result;
    }
}