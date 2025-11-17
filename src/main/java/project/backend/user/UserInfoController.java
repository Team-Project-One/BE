package project.backend.user;

import java.io.IOException;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import project.backend.user.dto.SignUpRequestDTO;
import project.backend.user.dto.UserResponseDTO;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "회원가입", description = "json 유저 정보 + 사진파일 필요")
public class UserInfoController {

	private final UserService userService;

	// 회원가입 (사진파일 + json 상세 정보)
	@PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserResponseDTO> signUp(
			@ModelAttribute SignUpRequestDTO requestDTO,
			@RequestPart(value = "profileImage", required = false) MultipartFile profileImage) throws IOException {
		UserResponseDTO response = userService.registerNewUser(requestDTO, profileImage);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}