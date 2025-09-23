package project.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import project.backend.entity.BasicInfo;

@Repository
public interface BasicInfoRepository extends JpaRepository<BasicInfo, Long> {
	Optional<BasicInfo> findByName(String name);
}