package miniLu.demo.util;

import org.springframework.stereotype.Component;

import miniLu.demo.dto.LinkDTO;
import miniLu.demo.dto.MetricsDTO;
import miniLu.demo.dto.RegisterDTO;
import miniLu.demo.dto.UserDto;
import miniLu.demo.entity.Link;
import miniLu.demo.entity.User;
import miniLu.demo.entity.UserMetric;

@Component
public class Mapper {
    
    public LinkDTO map(Link link, Class<LinkDTO> type) {
        return new LinkDTO().builder().link(link.getLink()).
        shortLink(link.getShortLink()).build();
    }

    public MetricsDTO map(UserMetric userMetric, Class<MetricsDTO> type) {
        return new MetricsDTO().builder().agent(userMetric.getAgent()).
        ip(userMetric.getIp()).city(userMetric.getCity()).country(userMetric.getCountry())
        .device(userMetric.getDevice()).os(userMetric.getOs()).referer(userMetric.getReferer())
        .userAgent(userMetric.getUserAgent()).timeStamp(userMetric.getTimeStamp())
        .shortLink(userMetric.getShortLink()).build();
    }

    public UserDto map(User user, Class<UserDto> type) {
        return new UserDto().builder().email(user.getEmail()).name(user.getName())
        .password(user.getPassword()).links(user.getLinks()).build();
    }

    public User map(RegisterDTO dto, Class<User> type) {
        User user = new User(dto.getEmail(), dto.getPassword(), dto.getPassword());
        return user;
    }

}
