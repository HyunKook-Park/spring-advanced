package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    /* 일정 저장 관련 테스트
     1. 일정 저장, 날씨 호출 테스트
     */

    @Test
    void 일정_저장(){
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저
        String weather = "sunny";
        TodoSaveRequest request = new TodoSaveRequest("Test Title", "Test Contents");
        Todo todo = new Todo(request.getTitle(), request.getContents(), weather, user);

        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        // when & then
        TodoSaveResponse result = todoService.saveTodo(authUser, request);

        assertNotNull(result);
    }

    /* 일정 페이징 조회 테스트
    1. 일정 페이징 정상 조회 테스트
    2. 일정 조회 간 요청한 페이지가 데이터가 없는 경우
    3. 페이지 요청이 유효하지 않은 경우
     */
    @Test
    void 일정_페이징_정상_조회(){
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("TITLE1", "contents 1", "sunny", user);
        Todo todo2 = new Todo("TITLE2", "contents 2", "sunny", user);
        int page = 1;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size);
        List<Todo> todoList = List.of(todo, todo2);
        Page<Todo> todos = new PageImpl<>(todoList , pageable,2);

        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(todos);

        // when & then
        Page<TodoResponse> result = todoService.getTodos(page,size);
        assertNotNull(result);
    }

    /*일정 단건조회 테스트
    * 1. 일정 정산 조회
    * 2. 일정 없을 경우 예외처리
    * */

    @Test
    void 일정_단건_정상_조회(){
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        Todo todo = new Todo("TITLE1", "contents 1", "sunny", user);
        ReflectionTestUtils.setField(todo,"id", todoId);
        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when
        TodoResponse result = todoService.getTodo(todoId);

        // then
        assertNotNull(result);
        assertEquals(todoId, result.getId());
        assertEquals("TITLE1", result.getTitle());
        assertEquals("contents 1", result.getContents());
        assertEquals("sunny", result.getWeather());
        assertEquals(user.getId(), result.getUser().getId());
        assertEquals(user.getEmail(), result.getUser().getEmail());
        assertNotNull(result.getUser());
    }

    @Test
    void 일정_없을시_예외처리(){
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        Todo todo = new Todo("TITLE1", "contents 1", "sunny", user);
        ReflectionTestUtils.setField(todo,"id", todoId);
        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> todoService.getTodo(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }
}
