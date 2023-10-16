package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.errors.exceptions.NotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentAdminServiceImpl implements CommentAdminService {

    private final CommentRepository commentRepository;

    @Override
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Comment with id = " + commentId + " doesn't exist", "object not found"));

        commentRepository.deleteById(commentId);
    }
}
