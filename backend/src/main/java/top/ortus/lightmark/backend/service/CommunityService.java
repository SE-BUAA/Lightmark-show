package top.ortus.lightmark.backend.service;

import top.ortus.lightmark.backend.common.PageResponse;
import top.ortus.lightmark.backend.dto.module.CommentDTO;
import top.ortus.lightmark.backend.dto.module.PostDTO;
import top.ortus.lightmark.backend.dto.module.QuestionDTO;

import java.util.Map;

public interface CommunityService {

    PageResponse<PostDTO> listPosts(Long userId, Map<String, String> params);

    PostDTO getPost(Long userId, Long id);

    PostDTO createPost(Long userId, PostDTO payload);

    PostDTO updatePost(Long userId, Long id, PostDTO payload);

    boolean deletePost(Long userId, boolean admin, Long id);

    Map<String, Object> togglePostLike(Long userId, Long id);

    PageResponse<CommentDTO> listPostComments(Long postId, Map<String, String> params);

    CommentDTO createPostComment(Long userId, Long postId, CommentDTO payload, String ip);

    boolean deletePostComment(Long userId, boolean admin, Long postId, Long commentId);

    PageResponse<QuestionDTO> listQuestions(Map<String, String> params);

    QuestionDTO createQuestion(Long userId, QuestionDTO payload);

    QuestionDTO getQuestion(Long id);

    QuestionDTO answerQuestion(Long userId, boolean admin, Long id, Map<String, Object> payload);

    boolean deleteQuestion(Long userId, boolean admin, Long id);

    boolean deleteAnswer(Long userId, boolean admin, Long id);
}
