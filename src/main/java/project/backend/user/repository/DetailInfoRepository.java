package project.backend.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import project.backend.user.entity.DetailInfo;

@Repository
public interface DetailInfoRepository extends JpaRepository<DetailInfo, Long> {
	Optional<DetailInfo> findByBasicInfoId(Long basicInfoId);
	
	// 같은 거주 지역(place)인 사용자들 목록
	List<DetailInfo> findByPlace(String place);
}