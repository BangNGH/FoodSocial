package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.Comment;
import com.example.foodsocialproject.entity.Posts;
import com.example.foodsocialproject.entity.Users;
import com.example.foodsocialproject.repository.CommentRepository;
import com.example.foodsocialproject.repository.LikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public Comment save(String comment, Posts post, Users user) {
        Comment newComment = new Comment();
        newComment.setPost(post);
        newComment.setUser(user);
        newComment.setComment(comment);
        commentRepository.save(newComment);
        return newComment;
    }
}
