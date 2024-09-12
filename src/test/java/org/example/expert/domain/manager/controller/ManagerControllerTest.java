package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtFilter;
import org.example.expert.config.MockTestFilter;
import org.example.expert.domain.comment.controller.CommentAdminController;
import org.example.expert.domain.comment.controller.CommentController;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ManagerController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtFilter.class
                )
        }
)
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ManagerService managerService;

    @BeforeEach
    public void setUp() {
        // mockMvc에 사용할 Filter를 MockTestFilter로 지정
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new MockTestFilter())
                .build();
    }

    @Test
    void 매니저_저장() throws Exception {
        // given
        long todoId = 1L;
        UserResponse userResponse = new UserResponse(1L, "AAA@SDF.com");
        ManagerSaveRequest request = new ManagerSaveRequest(1L);
        ManagerSaveResponse response = new ManagerSaveResponse(1L,  userResponse);

        given(managerService.saveManager(any(AuthUser.class), eq(todoId), eq(request))).willReturn(response);

        // when & then
        mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("id", 1L)
                .requestAttr("email", "AAA@SDF.com")
                .requestAttr("userRole","USER"))
                .andExpect(status().isOk());
    }

    @Test
    void manager_다건조회() throws Exception {
        long todoId = 1L;
        UserResponse userResponse = new UserResponse(1L, "AAA@SDF.com");
        UserResponse userResponse2 = new UserResponse(2L, "cc12311A@SDF.com");
        List<ManagerResponse> managerResponse = List.of(
                new ManagerResponse(1L, userResponse),
                new ManagerResponse(2L, userResponse2)
        );

        given(managerService.getManagers(todoId)).willReturn(managerResponse);

        // when & then
        mockMvc.perform(get("/todos/{todoId}/managers", todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("id", 1L)
                        .requestAttr("email", "AAA@SDF.com")
                        .requestAttr("userRole","USER"))
                .andExpect(status().isOk());
    }

    @Test
    void manager_삭제() throws Exception {
        // given
        Long todoId = 1L;
        Long managerId = 2L;

        // when & then
        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId, managerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("id", 1L)
                        .requestAttr("email", "AAA@SDF.com")
                        .requestAttr("userRole","USER"))
                .andExpect(status().isOk());

        verify(managerService).deleteManager(any(AuthUser.class), eq(todoId), eq(managerId));

    }
}