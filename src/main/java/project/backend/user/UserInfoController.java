package project.backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import project.backend.user.dto.SignUpRequestDTO;
import project.backend.user.dto.UserResponseDTO;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserInfoController {

	private final UserService userService;

	// 기본 정보 저장
	@PostMapping("/signup")
	public ResponseEntity<UserResponseDTO> signUp(@RequestBody SignUpRequestDTO requestDTO) {
		UserResponseDTO response = userService.registerNewUser(requestDTO);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}