package miniLu.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analytics {
    String ip;
    String userAgent;
    String referer;
    String timeStamp;
    String shortLink;
    String country;
    String deviceType;
    String city;
    String device;
    String agent;
    String os;
}
