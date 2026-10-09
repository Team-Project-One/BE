package project.backend.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.backend.matching.entity.MatchSajuInfo;
import project.backend.user.entity.User;
import java.util.Optional;

public interface MatchSajuInfoRepository extends JpaRepository<MatchSajuInfo, Long> {

    @Query("SELECT m FROM MatchSajuInfo m WHERE " +
            "(m.user = :user AND m.matchedUser = :partner) OR " +
            "(m.user = :partner AND m.matchedUser = :user)")
    Optional<MatchSajuInfo> findByUsers(@Param("user") User user, @Param("partner") User partner);
}