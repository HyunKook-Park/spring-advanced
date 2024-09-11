package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Spy
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;
/*
* 1. 회원가입(이메일 중복 없이 정상 회원가입 되는지, JWT 토큰 생성 확인)
* 2. 이메일 중복
* 3. 비밀번호 정상 인코딩 되는지 검증
* */

    @Test
    void 회원가입_성공하면_JWT토큰_정상반환한다(){
        // given
        Long id = 1L;
        SignupRequest signupRequest = new SignupRequest("test.example", "pw123","user");
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("password");

        User savedUser = new User(signupRequest.getEmail(), "password", UserRole.USER);
        ReflectionTestUtils.setField(savedUser,"id", id);
        given(userRepository.save(any(User.class))).willReturn(savedUser); // 유저 저장

        given(jwtUtil.createToken(id,signupRequest.getEmail(),UserRole.USER)).willReturn("jwtToken");

        // when
        SignupResponse signupResponse = authService.signup(signupRequest);

        // then
        assertEquals("jwtToken", signupResponse.getBearerToken());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).createToken(1L, signupRequest.getEmail(), UserRole.USER);
    }

    @Test
    void 중복된_이메일로_회원가입시_예외_발생한다(){
        // given
        SignupRequest signupRequest = new SignupRequest("test.example", "pw123","user");
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(true);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> authService.signup(signupRequest));
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // 중복된 이메일일때는 repository에 저장되지 않아야함.
    }

    @Test
    void 비밀번호가_정상적으로_인코딩된다(){
        //given
        SignupRequest signupRequest = new SignupRequest("test.example", "pw123","user");
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("password");

        User savedUser = new User(signupRequest.getEmail(), "password", UserRole.USER);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        authService.signup(signupRequest);

        // then
        verify(passwordEncoder).encode(signupRequest.getPassword());
    }

    /*
    1. 로그인 (로그인 시 토큰 정상 발급하는지 확인)
    2. 가입되어있지 않은 유저이면, 예외처리
    3. 해당 유저의 비밀번호가 맞는 지 확인
    * */

    @Test
    void 정상_로그인시_토큰_발급_확인한다(){
        //given
        Long id = 1L;
        String email = "test@example.com";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        SigninRequest signinRequest = new SigninRequest(email, rawPassword);

        User user = new User(email, encodedPassword, UserRole.USER);
        ReflectionTestUtils.setField(user,"id",id);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(jwtUtil.createToken(id,user.getEmail(),user.getUserRole())).willReturn("jwtToken");

        // when
        SigninResponse response = authService.signin(signinRequest);

        // then
        assertEquals("jwtToken", response.getBearerToken());
    }


    @Test
    void 가입되어_있지_않은_이메일이면_예외처리한다(){
        // given
        String email = "test@example.com";
        String rawPassword = "password123";
        SigninRequest request = new SigninRequest(email, rawPassword);

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> authService.signin(request));
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    void 유저의_비밀번호가_틀리면_예외처리한다(){

        // given
        String email = "test@example.com";
        String rawPassword = "password123";
        String incorrectPassword = "incorrectPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        SigninRequest signinRequest = new SigninRequest(email, incorrectPassword);

        User user = new User(email, encodedPassword, UserRole.USER);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when & then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.signin(signinRequest));
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }
}
