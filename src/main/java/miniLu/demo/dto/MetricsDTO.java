package miniLu.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricsDTO {
    
    private Long linkId;
    
    private String ip;
    
    private String userAgent;
    
    private String referer;
    
    private String timeStamp;
    
    private String shortLink;
    
    private String country;
    
    private String city;
    
    private String device;
    
    private String agent;
    
    private String os;
}
