package miniLu.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import miniLu.demo.entity.UserMetric;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkDTO {
    
    private String link;
    
    private String shortLink;
    
    // private User user;
    
    // private List<UserMetric> metrics;
}
