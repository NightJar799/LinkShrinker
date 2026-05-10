package miniLu.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_metrics", schema = "mil")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "link_id", nullable = false)
    private Long linkId;
    
    @Column(name = "ip", length = 45)
    private String ip;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "referer", length = 300)
    private String referer;
    
    @Column(name = "time_stamp", length = 50)
    private String timeStamp;
    
    @Column(name = "short_link", length = 50)
    private String shortLink;
    
    @Column(name = "country", length = 100)
    private String country;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "device", length = 100)
    private String device;
    
    @Column(name = "agent", length = 400)
    private String agent;
    
    @Column(name = "os", length = 100)
    private String os;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", insertable = false, updatable = false)
    private Link link;
}