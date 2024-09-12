package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtFilter;
import org.example.expert.config.MockTestFilter;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.controller.ManagerController;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TodoController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtFilter.class
                )
        }
)
class TodoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TodoService todoService;

    @BeforeEach
    public void setUp() {
        // mockMvc에 사용할 Filter를 MockTestFilter로 지정
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new MockTestFilter())
                .build();
    }

    @Test
    @DisplayName("todo 저장")
    void Todo_저장() throws Exception {
        UserResponse userResponse = new UserResponse(1L, "AAA@SDF.com");
        TodoSaveRequest request = new TodoSaveRequest("제목", "내용");
        TodoSaveResponse response = new TodoSaveResponse(1L, request.getTitle(), request.getContents(), "sunny", userResponse);

        given(todoService.saveTodo(any(AuthUser.class),eq(request))).willReturn(response);

        // when & then
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("id", 1L)
                        .requestAttr("email", "AAA@SDF.com")
                        .requestAttr("userRole","USER"))
                .andExpect(status().isOk());
    }

    @Test
    void Todos_페이징_다건조회() throws Exception {
        UserResponse userResponse = new UserResponse(1L, "AAA@SDF.com");
        UserResponse userResponse2 = new UserResponse(2L, "AAddd1A@SDF.com");
        int page = 1;
        int size = 2;
        List<TodoResponse> responses = List.of(
                new TodoResponse(1L, "제목1", "내용1", "sunny", userResponse, LocalDateTime.now(), LocalDateTime.now()),
                new TodoResponse(1L, "제목2", "내용2", "sunny", userResponse2, LocalDateTime.now(), LocalDateTime.now())
        );
        Page<TodoResponse> todoPage = new PageImpl<>(responses);
        given(todoService.getTodos(page,size)).willReturn(todoPage);

        // when & then
        mockMvc.perform(get("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("id", 1L)
                        .requestAttr("email", "AAA@SDF.com")
                        .requestAttr("userRole","USER"))
                .andExpect(status().isOk());
    }

    @Test
    void Todo_단건조회() throws Exception {
        long todoId = 1L;
        UserResponse userResponse = new UserResponse(1L, "AAA@SDF.com");
        TodoResponse response = new TodoResponse(1L, "제목1", "내용1", "sunny", userResponse, LocalDateTime.now(), LocalDateTime.now());

        given(todoService.getTodo(todoId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/todos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}