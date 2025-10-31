package project.backend.entity;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import project.backend.kakaoLogin.User;

@Entity
@Table(name = "basic_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicInfo {
	@Id // 기본키 필드
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false) // 기본 정보 - 이름
	private String name;
	
	@Column(nullable = false) // 기본 정보 - 성별
	private String gender; // 남자 또는 여자
	
	@Column(name = "birth_date", nullable = false)
	private LocalDate birthDate;
	
	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToOne(mappedBy = "basicInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private DetailInfo detailInfo;
}