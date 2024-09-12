package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.util.Reflection;
import org.example.expert.config.JwtFilter;
import org.example.expert.config.MockTestFilter;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.controller.TodoController;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserAdminService;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, UserAdminController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtFilter.class
                )
        }
)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserAdminService userAdminService;
    @MockBean
    private UserService userService;

    @BeforeEach
    public void setUp() {
        // mockMvc에 사용할 Filter를 MockTestFilter로 지정
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new MockTestFilter())
                .build();
    }

    @Test
    void User_단건조회() throws Exception {
        long userId = 1L;
        UserResponse response = new UserResponse(userId, "AAA@SDF.com");

        given(userService.getUser(userId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("비밀번호 변경")
    void password_변경() throws Exception {
        // given
        User user = new User("a@a.com","123123",UserRole.USER);
        ReflectionTestUtils.setField(user,"id", 1L);
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPass123", "newPass123");

        // when & then
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", user.getId())
                        .requestAttr("email","a@a.com")
                        .requestAttr("userRole", "USER"))
                .andExpect(status().isOk())
                        .andDo(print());
    }

    // userAdminController 테스트
    @Test
    @DisplayName("userRole 변경")
    void userRole_변경() throws Exception {
        // given
        long userId = 1L;
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        // when & then
        mockMvc.perform(patch("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", 1L)
                        .requestAttr("email","a@a.com")
                        .requestAttr("userRole", "USER"))
                .andExpect(status().isOk())
                .andDo(print());

    }

}