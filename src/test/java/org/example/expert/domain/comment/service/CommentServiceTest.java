package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    public void todo_id_검색시_존재하지_않으면_예외처리한다(){
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);


        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.saveComment(authUser, todoId, request));

        assertEquals("Todo not found", exception.getMessage());
    }

    /*
    getComments
    1. 댓글 조회 성공
    2. DB 댓글 없는 경우
     */

    @Test
    void 댓글_조회시_성공(){
        // given
        long todoId = 1L;
        User user = User.fromAuthUser(new AuthUser(1L, "email", UserRole.USER));
        Comment comment1 = new Comment("First Comment", user, new Todo());
        Comment comment2 = new Comment("Second Comment", user, new Todo());
        ReflectionTestUtils.setField(comment1, "id", todoId);
        ReflectionTestUtils.setField(comment2, "id", user.getId());

        List<Comment> commentList = new ArrayList<>();
        commentList.add(comment1);
        commentList.add(comment2);

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(commentList);

        // when
        List<CommentResponse> response = commentService.getComments(todoId);

        // then
        assertNotNull(response);  // 댓글 목록 Null 체크
        assertEquals(2, response.size()); // 두개 댓글 반환되는지 확인
        assertEquals("First Comment", response.get(0).getContents(), "첫번째 댓글과 일치합니다.");
        assertEquals(1L, response.get(0).getId(),"id가 일치합니다");
    }


    @Test
    void 댓글_목록_비어있는_경우(){
        // given
        long todoId = 1L;
        List<Comment> emptyList = new ArrayList<>();

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(emptyList);

        // when
        List<CommentResponse> response = commentService.getComments(todoId);

        // then
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }


}
