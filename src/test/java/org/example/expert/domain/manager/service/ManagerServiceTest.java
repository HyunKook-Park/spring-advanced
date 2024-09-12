package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;


    @Test
    public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    void 등록하는_담당자가_존재하지_않을_경우_예외처리한다(){
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        long managerUserId = 2L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerSaveRequest.getManagerUserId())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.saveManager(authUser, todoId, managerSaveRequest));
        assertEquals("등록하려고 하는 담당자 유저가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 일정_작성자_본인을_담당자로_등록시_예외처리한다(){
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        long managerUserId = 2L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerSaveRequest.getManagerUserId())).willReturn(Optional.of(user));

        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, managerSaveRequest));
        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());

    }

    // delete manager 테스트
    /*
    * 1. delete 정상 작동 테스트
    * 2. user not found 예외처리
    * 3. todo not found 예외처리
    * 4. 일정 만든 유저 예외처리
    * 5. manager not found 예외처리
    * 6. 일정 등록된 담당자 예외처리
    * */
    @Test
    void 담당자_정상_삭제(){
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long userId = 1L;
        long todoId = 1L;
        long managerUserId = 2L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        Manager managerUser = new Manager(user,todo);  // 매니저로 등록할 유저

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));

        // when & then
        managerService.deleteManager(authUser, todoId, managerUserId);

        verify(managerRepository, times(1)).delete(managerUser);
    }

    @Test
    void 유저_못찾아서_예외처리(){
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long userId = 1L;
        long managerUserId = 2L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        Manager managerUser = new Manager(user,todo);  // 매니저로 등록할 유저

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(authUser, userId, managerUserId));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void _일정_만든_유저없다면_예외처리(){
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long userId = 1L;
        long todoId = 1L;
        long managerUserId = 2L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", null);

        Manager managerUser = new Manager(user,todo);  // 매니저로 등록할 유저

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(authUser, userId, managerUserId));

        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다. 1", exception.getMessage());

    }

    @Test
    void 유저아이디와_일정의_유저아이디가_다르다면_예외처리(){
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long userId = 1L;
        long todoId = 1L;
        long managerUserId = 2L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        AuthUser authUser2 = new AuthUser(2L, "a@a.com", UserRole.USER);
        User user2 = User.fromAuthUser(authUser2);
        Manager managerUser = new Manager(user,todo);  // 매니저로 등록할 유저

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user2));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(authUser, userId, managerUserId));

        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다. 2", exception.getMessage());

    }

    @Test
    void 담당자가_일정에_등록되어_있지_않다면_예외처리(){
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long userId = 1L;
        long todoId = 1L;
        long todoId2 = 2L;
        long managerUserId = 2L;

        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        Todo todo2 = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo2, "id", todoId2);
        Manager managerUser = new Manager(user,todo2);  // 매니저로 등록할 유저

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerRepository.findById(anyLong())).willReturn(Optional.of(managerUser));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(authUser, userId, managerUserId));
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }


}
