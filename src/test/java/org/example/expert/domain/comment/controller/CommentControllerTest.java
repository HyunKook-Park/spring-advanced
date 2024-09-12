package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtFilter;
import org.example.expert.config.MockTestFilter;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CommentController.class},
        excludeFilters = {
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
        }
)

class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;
    @MockBean
    UserService userService;

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // mockMvc에 사용할 Filter를 MockTestFilter로 지정
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new MockTestFilter())
                .build();
    }

    @Test
    @DisplayName("댓글 저장")
    void 댓글_저장() throws Exception {
        // given
        long todoId = 1L;
        UserResponse userResponse = new UserResponse(2L, "AAA@SDF.com");
        CommentSaveRequest request = new CommentSaveRequest("댓글");
        CommentSaveResponse response = new CommentSaveResponse(1L, "댓글", userResponse);

        given(commentService.saveComment(any(AuthUser.class), eq(todoId), eq(request))).willReturn(response);

        // when & then
        mockMvc.perform(post("/todos/{todoId}/comments", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("id", 1L)
                .requestAttr("email", "AAA@SDF.com")
                .requestAttr("userRole", "USER"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 조회")
    void getComments() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        List<CommentResponse> commentResponseList = List.of(
                new CommentResponse(1L, "댓글1", new UserResponse(2L, "user1@asde.com")),
                new CommentResponse(2L, "댓글2", new UserResponse(3L, "user2@fre.com"))
        );

        given(commentService.getComments(eq(todoId))).willReturn(commentResponseList);

        // when & then
        mockMvc.perform(get("/todos/{todoId}/comments", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("id", 1L)
                .requestAttr("email", "AAA@SDF.com")
                .requestAttr("userRole", "USER"))
                .andExpect(status().isOk());
    }
}