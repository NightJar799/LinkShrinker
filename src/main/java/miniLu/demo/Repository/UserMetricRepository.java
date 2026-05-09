package miniLu.demo.Repository;

import miniLu.demo.entity.UserMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserMetricRepository extends JpaRepository<UserMetric, Long> {
    List<UserMetric> findByLinkId(Long linkId);
    List<UserMetric> findByShortLink(String shortLink);
}