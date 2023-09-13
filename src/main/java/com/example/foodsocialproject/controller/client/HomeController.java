package com.example.foodsocialproject.controller.client;

import com.example.foodsocialproject.daos.FileUploadUtil;
import com.example.foodsocialproject.entity.*;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.services.PostsService;
import com.example.foodsocialproject.services.ProductService;
import com.example.foodsocialproject.services.UserInfoService;
import com.example.foodsocialproject.services.UserServices;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = {""})
public class HomeController {
    private final UserServices userService;
    private final PostsService postsService;
    private final UserInfoService userInfoService;
    private final ProductService productService;

    @GetMapping("")
    public String index(Model model,Principal p){
        List<Posts> listPosts = postsService.getList().stream().sorted(Comparator.comparing(Posts::getCreatedAt,Comparator.reverseOrder())).collect(Collectors.toList());
        model.addAttribute("listPosts",listPosts);
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        model.addAttribute("user",foundUser);
        List<Posts> topPosts = postsService.get5PostsTopLike();
        List<Users> topUser = postsService.getTop5User();
        model.addAttribute("topPosts",topPosts);
        model.addAttribute("topUser",topUser);
        model.addAttribute("product1", productService.getRandomProduct());
        model.addAttribute("product2", productService.getRandomProduct());
        model.addAttribute("product3", productService.getRandomProduct());
        return "client/home/index";
    }
    @GetMapping("/post-edit/{id}")
    public String editPost(@PathVariable("id") UUID id, Model model,Principal p){
        Posts foundPosts = postsService.findByID(id);
        model.addAttribute("recipe", foundPosts);
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        model.addAttribute("user",foundUser);
        return "client/home/createPost";
    }
    @GetMapping("/login")
    public String login(Model model){
        return "client/auth-login";
    }
    @GetMapping("/profile")
    public String profile(Model model, Principal p){
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();

        String myEmail = p.getName();
        Users myAccount = userService.findbyEmail(myEmail).get();
        return getString(model, foundUser, myAccount);
    }

    @GetMapping("/profile/{id}")
    public String seeProfile(@PathVariable("id") UUID id, Model model, Principal p){
        Users user = userService.findById(id);

        String myEmail = p.getName();
        Users followerUser = userService.findbyEmail(myEmail).get();
        return getString(model, user,followerUser);
    }

    private String getString(Model model, Users user, Users followerUser) {
        List<Posts> listPosts = user.getPosts().stream().sorted(Comparator.comparing(Posts::getCreatedAt,Comparator.reverseOrder())).collect(Collectors.toList());
        model.addAttribute("user",user);
        model.addAttribute("userInfo",user.getUserInfo());
        model.addAttribute("listPosts",listPosts);
        if (listPosts.isEmpty()){
            model.addAttribute("listPostsEmpty",true);
        }
        if (followerUser.getId().equals(user.getId())){
            model.addAttribute("myAccount",true);
        }else {
            model.addAttribute("myAccount",false);
            model.addAttribute("followerUser",followerUser);
        }
       if (followerUser.getFollowing().contains(user)){
           model.addAttribute("follwed",true);
       }else model.addAttribute("follwed",false);
        return "client/home/profile";
    }

    @PostMapping("/updateInfo")
    public String updateInfo(@Valid @ModelAttribute("userInfo") UserInfo userInfo, BindingResult bindingResult, @RequestParam("imageFile") MultipartFile multipartFile, Model model, Principal p) {
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        if (bindingResult.hasErrors()) {
            model.addAttribute("user",foundUser);
            model.addAttribute("userInfo",foundUser.getUserInfo());
            model.addAttribute("error", "Xảy ra lỗi.");
            model.addAttribute("listPosts",foundUser.getPosts());
            if (foundUser.getPosts().isEmpty()){
                model.addAttribute("listPostsEmpty",true);
            }
            model.addAttribute("myAccount",true);
            return "client/home/profile";
        }
        try {
            String mainFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            userInfo.getUser().setAvatarUrl(mainFileName);
            userInfoService.save(userInfo, foundUser);
            String uploadDir = "./avatar-images/" + foundUser.getId();
            FileUploadUtil.saveFile(uploadDir,multipartFile, mainFileName);
            model.addAttribute("success", "Cập nhật thành công.");
        } catch (ResourceNotFoundException e) {
            System.out.println("Has Error "+e.getMessage());
            model.addAttribute("error", "Xảy ra lỗi.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("user",foundUser);
        model.addAttribute("userInfo",foundUser.getUserInfo());
        model.addAttribute("listPosts",foundUser.getPosts());

        return "client/home/profile";
    }

    @GetMapping("/post")
    public String post(Model model,Principal p){
        Posts recipe = new Posts();
        model.addAttribute("recipe",recipe);
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        model.addAttribute("user",foundUser);
        return "client/home/createPost";
    }

    @PostMapping("/createRecipe")
    public String save(@Valid @ModelAttribute("recipe") Posts recipe, BindingResult bindingResult,Principal p, Model model, @RequestParam("numberOfImages") int numberOfImages, RedirectAttributes ra, @RequestParam("imageFile") MultipartFile multipartFile, @RequestParam("extraImage") MultipartFile[] extraMultipartFile ) throws IOException {
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        if (bindingResult.hasErrors()) {
            return "client/home/createPost";
        }
       String mainFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
       recipe.setImage(mainFileName);

       int count=0;
        List<Steps> stepsList = new ArrayList<Steps>();
       for (MultipartFile extraMultiPart : extraMultipartFile){
           String extraImageName = StringUtils.cleanPath(extraMultiPart.getOriginalFilename());
           if (count<=numberOfImages){
               Steps step = new Steps();
               step.setStepImg(extraImageName);
               step.setStepNumber(count+1);
               step.setRecipe(recipe);
                step.setStepDescription(recipe.getSteps().get(count).getStepDescription());
               stepsList.add(step);
           }
          count++;
       }

       recipe.setSteps(stepsList);
        recipe.setUser(foundUser);
        for (Ingredients ingredient:recipe.getIngredients()
        ) {
            ingredient.setRecipe(recipe);
        }
        Posts savedRecipe = postsService.save(recipe);

        String postUploadDir = "./post-images/" + savedRecipe.getId();
        String stepUploadDir = "./steps-images/" + savedRecipe.getId();
        FileUploadUtil.saveFile(postUploadDir,multipartFile, mainFileName);

       for (MultipartFile extraMultiFile : extraMultipartFile){
           String fileName = StringUtils.cleanPath(extraMultiFile.getOriginalFilename());

           FileUploadUtil.saveFile(stepUploadDir,extraMultiFile, fileName);
       }


        ra.addFlashAttribute("raMessage", "Đăng bài thành công");
        model.addAttribute("user",foundUser);
        model.addAttribute("userInfo",foundUser.getUserInfo());
        model.addAttribute("listPosts",foundUser.getPosts());
        return "client/home/profile";
    }

    @GetMapping("/post-details/{id}")
    public String getPostDetails(@PathVariable("id") UUID id, Model model, Principal p, HttpSession session){
        Posts post = postsService.findByID(id);
        session.setAttribute("id", id);
        List<Steps> stepsList = post.getSteps().stream().sorted(Comparator.comparing(Steps::getStepNumber)).toList();
        post.setSteps(stepsList);
        model.addAttribute("post",post);
        model.addAttribute("comment",new Comment());
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        model.addAttribute("user",foundUser);
        model.addAttribute("products", productService.getList());
        Long productID1 = post.getProduct_id1();
        Long productID2 = post.getProduct_id2();
        Long productID3 = post.getProduct_id3();
        if(productID1 == null)
            model.addAttribute("product1", productService.getRandomProduct());
        else
            model.addAttribute("product1", productService.getProductById(productID1));

        if(productID2 == null)
            model.addAttribute("product2", productService.getRandomProduct());
        else
            model.addAttribute("product2", productService.getProductById(productID2));

        if(productID3 == null)
            model.addAttribute("product3", productService.getRandomProduct());
        else
            model.addAttribute("product3", productService.getProductById(productID3));

        return "client/home/post_details";
    }
    @PostMapping("/changeProduct1")
    public String changeProduct1(@RequestParam(name = "productID") Long productID, HttpSession session,
                                 RedirectAttributes redirectAttributes){
        UUID idP = (UUID)session.getAttribute("id");
        Posts post = postsService.findByID(idP);
        post.setProduct_id1(productID);
        postsService.save(post);
        redirectAttributes.addAttribute("id", idP);
        return "redirect:/post-details/{id}";
    }
    @PostMapping("/changeProduct2")
    public String changeProduct2(@RequestParam(name = "productID") Long productID, HttpSession session,
                                 RedirectAttributes redirectAttributes){
        UUID idP = (UUID)session.getAttribute("id");
        Posts post = postsService.findByID(idP);
        post.setProduct_id2(productID);
        postsService.save(post);
        redirectAttributes.addAttribute("id", idP);
        return "redirect:/post-details/{id}";
    }
    @PostMapping("/changeProduct3")
    public String changeProduct3(@RequestParam(name = "productID") Long productID, HttpSession session,
                                 RedirectAttributes redirectAttributes){
        UUID idP = (UUID)session.getAttribute("id");
        Posts post = postsService.findByID(idP);
        post.setProduct_id3(productID);
        postsService.save(post);
        redirectAttributes.addAttribute("id", idP);
        return "redirect:/post-details/{id}";
    }
}
