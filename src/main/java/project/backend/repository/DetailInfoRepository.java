package project.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import project.backend.entity.DetailInfo;

@Repository
public interface DetailInfoRepository extends JpaRepository<DetailInfo, Long> {
	Optional<DetailInfo> findByBasicInfoId(Long basicInfoId);
}