package miniLu.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "links", schema = "mil")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Link {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "link", nullable = false, length = 250)
    private String link;
    
    @Column(name = "short_link", nullable = false, unique = true, length = 50)
    private String shortLink;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserMetric> metrics;
}