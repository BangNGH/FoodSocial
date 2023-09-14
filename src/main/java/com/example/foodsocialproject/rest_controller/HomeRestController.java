package com.example.foodsocialproject.rest_controller;

import com.example.foodsocialproject.entity.*;
import com.example.foodsocialproject.services.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeRestController {
    private final PostsService postsService;
    private final UserServices userServices;
    private final LikesService likeService;
    private final CommentService commentService;
    private final UserRelaService userRelaService;
    private final ProductService productService;
    @GetMapping("/like")
    @ResponseBody
    public Boolean likePost(@RequestParam("id") String id, final HttpServletRequest request) {
        String userEmail = request.getUserPrincipal().getName();
        Users user = userServices.findbyEmail(userEmail).get();
        Posts post = postsService.findByID(UUID.fromString(id));
        if (post == null) {
            throw new RuntimeException("Could not find post");
        }
        Likes check = null;
        for (Likes like : post.getLikes()) {
            if (like.getUser().getEmail().equals(user.getEmail())) {
                check = like;
                break;
            }
        }
        if (check == null) {
            // Tăng số lượt like lên 1
            post.setLikeCount(post.getLikeCount() + 1);
            Likes postLike = new Likes();
            postLike.setPost(post);
            postLike.setUser(user);
            likeService.save(postLike);
            postsService.save(post);
            return true;
        }
//unlike
            post.getLikes().remove(check);
            likeService.delete(check.getId());
            post.setLikeCount(post.getLikeCount() - 1);
            post.getSteps().remove(check);
            postsService.save(post);
            return false;

    }

    @GetMapping("/search")
    @ResponseBody
    public List<Posts> searchPost(@RequestParam("q") String q) {
        List<Posts> posts = postsService.search(q);
        return posts;
    }

    @GetMapping("/searchUser")
    @ResponseBody
    public List<Users> searchUsers(@RequestParam("q") String q) {
        List<Users> users = userServices.search(q);
        return users;
    }

    @GetMapping("/follow")
    @ResponseBody
    public Boolean followingUser(@RequestParam("id") UUID id, final HttpServletRequest request) {
        String userEmail = request.getUserPrincipal().getName();
        Users followerAccount = userServices.findbyEmail(userEmail).get();
        Users followingAccount = userServices.findById(id);
        System.out.println("followerAccount: "+followerAccount.getFullName());
        System.out.println("followingAccount: "+followingAccount.getFullName());


        Users usreCheck = null;
        for (Users user : followerAccount.getFollowing()) {
            if (user.getEmail().equals(followingAccount.getEmail())) {
                usreCheck = user;
                break;
            }
        }

        if (usreCheck != null){

            userRelaService.unfollow(followerAccount, followingAccount);
            System.out.println("UNFOLLOW");
            return false;
        }

        userRelaService.save(followerAccount, followingAccount);
        System.out.println("FOLLOW");
        return true;
    }

    @GetMapping("/comment")
    @ResponseBody
    public Comment commentPost(@RequestParam("comment") String comment, @RequestParam("postID") UUID postID, final HttpServletRequest request) {
        Posts post = postsService.findByID(postID);
        String userEmail = request.getUserPrincipal().getName();
        Users user = userServices.findbyEmail(userEmail).get();

        return commentService.save(comment, post, user);
    }

    @GetMapping("/add-ingredient")
    public ModelAndView searchBookingBills(@RequestParam("index") Integer index) {
        List<Product> products = productService.getList();
        ModelAndView modelAndView = new ModelAndView("fragments/add-ingredient");
        modelAndView.addObject("products", products);
        modelAndView.addObject("index", index);
        return modelAndView;
    }



}
