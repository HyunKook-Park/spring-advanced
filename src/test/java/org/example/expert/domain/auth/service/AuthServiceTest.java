package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    @Mock
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
        SignupRequest signupRequest = new SignupRequest("test.example", "pw123","user");
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("password");

        User savedUser = new User(signupRequest.getEmail(), "password", UserRole.USER);
        ReflectionTestUtils.setField(savedUser,"id", 1L);
        given(userRepository.save(any(User.class))).willReturn(savedUser); // 유저 저장

        given(jwtUtil.createToken(1L,signupRequest.getEmail(),UserRole.USER)).willReturn("jwtToken");

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
}
