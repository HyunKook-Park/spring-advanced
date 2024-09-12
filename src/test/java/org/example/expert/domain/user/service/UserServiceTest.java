package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void 유저_단건조회_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long userId = 1L;
        User user = User.fromAuthUser(authUser);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when & then
        UserResponse result = userService.getUser(userId);

        assertNotNull(result);
    }

    @Test
    void 비밀번호_변경_테스트() {
        // given
        long userId = 1L;
        User user = mock(User.class);
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPass123", "newPass123");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(request.getNewPassword())).willReturn("encodeNewPassword");

        // when & then
        userService.changePassword(userId, request);

        verify(user).changePassword("encodeNewPassword");
    }

    @Test
    void 새비밀번호와_기존비밀번호_같으면_예외처리(){
        long userId = 1L;
        User user = mock(User.class);
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPass123", "newPass123");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(true);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 잘못된_비밀번호_입력시_예외처리(){
        long userId = 1L;
        User user = mock(User.class);
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPass123", "newPass123");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(false);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());

    }

    @Test
    void 새비밀번호가_유효성검사_실패시_예외발생() {
        long userId = 1L;
        User user = mock(User.class);
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPass123", "hiyo");

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));
        assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());


    }
}