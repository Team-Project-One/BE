package project.backend.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.backend.user.dto.UserEnums;
import project.backend.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile WHERE u.id = :id")
    Optional<User> findByIdWithProfile(@Param("id") Long id);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile " +
           "WHERE u.userProfile.region = :region " +
           "AND u.userProfile.sexualOrientation = :sexualOrientation " +
           "AND u.id != :excludeUserId")
    List<User> findMatchingUsersByRegionAndOrientation(
            @Param("region") UserEnums.region region,
            @Param("sexualOrientation") UserEnums.SexualOrientation sexualOrientation,
            @Param("excludeUserId") Long excludeUserId
    );

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile " +
           "WHERE u.userProfile.region = :region " +
           "AND u.userProfile.sexualOrientation = :sexualOrientation " +
           "AND u.id != :excludeUserId " +
           "AND u.gender != :myGender")
    List<User> findMatchingUsersForStraight(
            @Param("region") UserEnums.region region,
            @Param("sexualOrientation") UserEnums.SexualOrientation sexualOrientation,
            @Param("excludeUserId") Long excludeUserId,
            @Param("myGender") UserEnums.Gender myGender
    );
}