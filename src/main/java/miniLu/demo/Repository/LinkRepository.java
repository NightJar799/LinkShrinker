package miniLu.demo.Repository;

import miniLu.demo.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByShortLink(String shortLink);
    List<Link> findByUserId(Long userId);
}