package miniLu.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import miniLu.demo.Repository.LinkRepository;
import miniLu.demo.Repository.UserMetricRepository;
import miniLu.demo.dto.MetricsDTO;
import miniLu.demo.util.Mapper;

@Service
public class MetricsService {

    private final UserMetricRepository userMetricRepository;
    private final LinkRepository linkRepository;
    private final Mapper mapper;

    MetricsService(UserMetricRepository userMetricRepository, LinkRepository linkRepository, Mapper mapper) {
        this.userMetricRepository = userMetricRepository;
        this.linkRepository = linkRepository;
        this.mapper = mapper;
    }

    public List<MetricsDTO> getAllMetricsByLinkId(String shortLink) {
        Long linkId = linkRepository.findByShortLink(shortLink).get().getId();
        List<MetricsDTO> metrics = userMetricRepository.findByLinkId(linkId).stream().map(metric ->
            mapper.map(metric, MetricsDTO.class)).collect(Collectors.toList());

        return metrics;
    }
    
    public Map<String, Long> calculatePercentages(List<String> items) {
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

    public List<Map<String, Object>> convertToPercentageList(Map<String, Long> countMap, int total) {
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

    public Map<String, Long> countCountries(List<MetricsDTO> metrics) {
        return calculatePercentages(metrics.stream()
                .map(m -> m.getCountry() != null ? m.getCountry() : "Unknown")
                .collect(Collectors.toList()));
    }

    public Map<String, Long> countDevices(List<MetricsDTO> metrics) {
        return calculatePercentages(metrics.stream()
                .map(m -> m.getDevice() != null ? m.getDevice() : "Unknown")
                .collect(Collectors.toList()));
    }

    public Map<String, Long> countOs(List<MetricsDTO> metrics) {
        return calculatePercentages(metrics.stream()
                .map(m -> m.getOs() != null ? m.getOs() : "Unknown")
                .collect(Collectors.toList()));
    }

    public Map<String, Long> countCities(List<MetricsDTO> metrics) {
        return calculatePercentages(metrics.stream()
                .map(m -> m.getCity() != null ? m.getCity() : "Unknown")
                .collect(Collectors.toList()));
    }

    public Map<String, Long> countReferers(List<MetricsDTO> metrics) {
        return calculatePercentages(metrics.stream()
                .map(m -> m.getReferer() != null && !m.getReferer().isEmpty() ? m.getReferer() : "Direct/Unknown")
                .collect(Collectors.toList()));
    }
}
