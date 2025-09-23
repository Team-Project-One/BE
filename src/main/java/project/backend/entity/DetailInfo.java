package project.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detail_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailInfo {
	@Id // 기본키 필드
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "basic_info_id", nullable = false)
	private BasicInfo basicInfo;
	
	@Column(name = "profile_image_path") // 상세 정보 - 프로필 사진
	private String profileImagePath;
	
	@Column(name = "drinking_frequency") // 상세 정보 - 음주빈도
	private String drinkingFrequency; // 안 마심, 가끔 마심, 적당히 마심, 자주 마심
	
	@Column(name = "smoking_status") // 상세 정보 - 흡연여부
	private String smokingStatus; // 비흡연, 가끔 흡연, 흡연
	
	@Column(name = "height") // 상세 정보 - 키
	private Integer height;
	
	@Column(name = "pet") // 상세 정보 - 반려동물
	private String pet; // 없음, 강아지, 고양이, 기타
	
	@Column(name = "religion") // 상세 정보 - 종교
	private String religion; // 무교, 불교, 기독교, 천주교, 기타
	
	@Column(name = "child_plan") // 상세 정보 - 자녀계획
	private String childPlan; // 원함, 상관없음, 원하지 않음
	
	@Column(name = "mbti", length = 4) // 상세 정보 - MBTI
	private String mbti;
}