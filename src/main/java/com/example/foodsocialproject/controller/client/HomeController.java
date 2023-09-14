package com.example.foodsocialproject.controller.client;

import com.example.foodsocialproject.daos.FileUploadUtil;
import com.example.foodsocialproject.entity.*;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.repository.EventRepository;
import com.example.foodsocialproject.services.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = {""})
public class HomeController {
    private final UserServices userService;
    private final PostsService postsService;
    private final UserInfoService userInfoService;
    private final ProductService productService;
    private final EventService eventService;
    private final EventParticipantService event_participantService;
    private final event_ingredientService event_ingredientService;

    @GetMapping("/create-post-4event/{eventID}")
    public String createPostEvent(@PathVariable("eventID") UUID eventID, Model model, Principal p) {
        Events event = eventService.getEvent(eventID);
        Posts recipe = new Posts();
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        EventParticipants newParticip = event_participantService.findEventParticipants(eventID, foundUser);
        if (newParticip.getPost() != null) {
            return "redirect:/post-details/" + newParticip.getPost().getId();
        }
        model.addAttribute("user", foundUser);
        model.addAttribute("recipe", recipe);
        model.addAttribute("event", event);
        return "client/home/createPost";
    }

    @GetMapping("/create-event")
    public String createEvent(Model model, Principal p) {
        List<Product> products = productService.getList();
        Events event = new Events();
        model.addAttribute("event", event);
        String userEmail = p.getName();
        Users currentUser = userService.findbyEmail(userEmail).get();
        model.addAttribute("user", currentUser);
        model.addAttribute("products", products);

        return "client/home/create_event";
    }

    @GetMapping("/event-details/{id}")
    public String eventDetails(@PathVariable("id") UUID id, Model model, Principal p, HttpSession session) {
        Events event = eventService.findById(String.valueOf(id));
        model.addAttribute("event", event);
        model.addAttribute("comment", new Comment());
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        model.addAttribute("user", foundUser);
        model.addAttribute("products", productService.getList());

        model.addAttribute("product1", productService.getRandomProduct());
        model.addAttribute("product2", productService.getRandomProduct());
        model.addAttribute("product3", productService.getRandomProduct());

        return "client/home/event_details";
    }

    @GetMapping("/join-event/{userid}/{eventid}")
    public String joinEvent(@PathVariable("userid") UUID userid, @PathVariable("eventid") UUID eventid, RedirectAttributes ra) {
        Users joinUser = userService.findById(userid);
        Events event = eventService.getEvent(eventid);
        event_participantService.saveRela(event, joinUser);
        ra.addFlashAttribute("raMessage", "Bạn đã tham gia thành công");
        return "redirect:/profile";
    }

    @PostMapping("/create-event")
    public String createEvent(@Valid @ModelAttribute("event") Events event, BindingResult bindingResult, Principal p, @RequestParam("productID") long productID, Model model, RedirectAttributes ra, @RequestParam("imageFile") MultipartFile multipartFile, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") @RequestParam("formattedDateTime") Date endTime, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") @RequestParam("formattedSDateTime") Date startTime) throws IOException {
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        if (bindingResult.hasErrors()) {
            System.out.println("ERROR " + bindingResult.getAllErrors());
            return "client/home/create_event";
        }

        String mainFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        event.setEvent_image(mainFileName);
        event.setEnd_date(endTime);
        event.setStart_date(startTime);
        event.setOrganizer(foundUser);
        Events savedEvents = eventService.saveEvent(event);
        event_ingredient newRela = new event_ingredient();
        newRela.setEvent(event);
        newRela.setProductid(productService.getProductById(productID));
        event_ingredientService.save(newRela);
        String eventUploadDir = "./event-images/" + savedEvents.getId();
        FileUploadUtil.saveFile(eventUploadDir, multipartFile, mainFileName);

        ra.addFlashAttribute("raMessage", "Đăng bài thành công");
        return "redirect:/";
    }

    @GetMapping("")
    public String index(Model model, Principal p, HttpSession session) {
        List<Posts> listPosts = postsService.getList().stream().sorted(Comparator.comparing(Posts::getCreatedAt, Comparator.reverseOrder())).collect(Collectors.toList());
        model.addAttribute("listPosts", listPosts);
        List<Events> events = eventService.getList().stream().filter(event -> event.getActived_event() == true).toList();
        model.addAttribute("events", events);

        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        model.addAttribute("user", foundUser);
        List<Posts> topPosts = postsService.get5PostsTopLike();
        List<Users> topUser = postsService.getTop5User();
        model.addAttribute("topPosts", topPosts);
        model.addAttribute("topUser", topUser);


        String key = (String) session.getAttribute("ads1");
        List<Product> products = productService.searchProductByName(key);
        for (Product product : products) {
            System.out.println("product list: " + product.getName());
        }
        if (products.isEmpty()) {
            model.addAttribute("product1", productService.getRandomProduct());
            model.addAttribute("product2", productService.getRandomProduct());
            model.addAttribute("product3", productService.getRandomProduct());
        } else {
            if (products.size() >= 3) {
                Collections.shuffle(products, new Random()); // Trộn danh sách
                List<Product> randomThreeProducts = products.subList(0, 3);
                model.addAttribute("product1", randomThreeProducts.get(0));
                model.addAttribute("product2", randomThreeProducts.get(1));
                model.addAttribute("product3", randomThreeProducts.get(2));
            }
           else if (products.size() == 2) {
                model.addAttribute("product1", products.get(0));
                model.addAttribute("product2", products.get(1));
                model.addAttribute("product3", productService.getRandomProduct());
            }
           else if (products.size() == 1) {
                model.addAttribute("product1", products.get(0));
                model.addAttribute("product2", productService.getRandomProduct());
                model.addAttribute("product3", productService.getRandomProduct());
            }
            else {
                model.addAttribute("product1",productService.getRandomProduct());
                model.addAttribute("product2", productService.getRandomProduct());
                model.addAttribute("product3", productService.getRandomProduct());
            }
        }


        /* model.addAttribute("product3", productService.getRandomProduct());*/
        return "client/home/index";
    }

    @GetMapping("/post-edit/{id}")
    public String editPost(@PathVariable("id") UUID id, Model model, Principal p) {
        Posts foundPosts = postsService.findByID(id);
        model.addAttribute("recipe", foundPosts);
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        model.addAttribute("user", foundUser);
        model.addAttribute("event", false);
        return "client/home/createPost";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "client/auth-login";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal p) {
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();

        String myEmail = p.getName();
        Users myAccount = userService.findbyEmail(myEmail).get();

        return getString(model, foundUser, myAccount);
    }

    @GetMapping("/profile/{id}")
    public String seeProfile(@PathVariable("id") UUID id, Model model, Principal p) {
        Users user = userService.findById(id);

        String myEmail = p.getName();
        Users followerUser = userService.findbyEmail(myEmail).get();
        return getString(model, user, followerUser);
    }

    private String getString(Model model, Users user, Users followerUser) {
        List<Posts> listPosts = user.getPosts().stream().sorted(Comparator.comparing(Posts::getCreatedAt, Comparator.reverseOrder())).collect(Collectors.toList());
        model.addAttribute("user", user);
        model.addAttribute("userInfo", user.getUserInfo());
        model.addAttribute("listPosts", listPosts);
        if (listPosts.isEmpty()) {
            model.addAttribute("listPostsEmpty", true);
        }
        if (followerUser.getId().equals(user.getId())) {
            model.addAttribute("myAccount", true);
        } else {
            model.addAttribute("myAccount", false);
            model.addAttribute("followerUser", followerUser);
        }
        if (followerUser.getFollowing().contains(user)) {
            model.addAttribute("follwed", true);
        } else model.addAttribute("follwed", false);
        List<Events> myEvents = eventService.getList().stream().filter(e -> e.getParticipants().contains(user)).collect(Collectors.toList());
        List<Events> events = eventService.getList();

        model.addAttribute("myEvents", myEvents);
        model.addAttribute("events", events);
        return "client/home/profile";
    }

    @PostMapping("/updateInfo")
    public String updateInfo(@Valid @ModelAttribute("userInfo") UserInfo userInfo, BindingResult bindingResult, @RequestParam("imageFile") MultipartFile multipartFile, Model model, Principal p) {
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", foundUser);
            model.addAttribute("userInfo", foundUser.getUserInfo());
            model.addAttribute("error", "Xảy ra lỗi.");
            model.addAttribute("listPosts", foundUser.getPosts());
            if (foundUser.getPosts().isEmpty()) {
                model.addAttribute("listPostsEmpty", true);
            }
            model.addAttribute("myAccount", true);
            return "client/home/profile";
        }
        try {
            String mainFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            userInfo.getUser().setAvatarUrl(mainFileName);
            userInfoService.save(userInfo, foundUser);
            String uploadDir = "./avatar-images/" + foundUser.getId();
            FileUploadUtil.saveFile(uploadDir, multipartFile, mainFileName);
            model.addAttribute("success", "Cập nhật thành công.");
        } catch (ResourceNotFoundException e) {
            System.out.println("Has Error " + e.getMessage());
            model.addAttribute("error", "Xảy ra lỗi.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Events> myEvents = eventService.getList().stream().filter(e -> e.getParticipants().contains(foundUser)).collect(Collectors.toList());
        ;
        model.addAttribute("myEvents", myEvents);
        model.addAttribute("user", foundUser);
        model.addAttribute("userInfo", foundUser.getUserInfo());
        model.addAttribute("listPosts", foundUser.getPosts());

        return "client/home/profile";
    }

    @GetMapping("/post")
    public String post(Model model, Principal p) {
        Posts recipe = new Posts();
        model.addAttribute("recipe", recipe);
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        model.addAttribute("user", foundUser);
        model.addAttribute("event", false);
        return "client/home/createPost";
    }

    @PostMapping("/createRecipe")
    public String save(@Valid @ModelAttribute("recipe") Posts recipe, BindingResult bindingResult, Principal p, Model model, @RequestParam("numberOfImages") int numberOfImages, RedirectAttributes ra, @RequestParam("imageFile") MultipartFile multipartFile, @RequestParam("extraImage") MultipartFile[] extraMultipartFile, @RequestParam(value = "eventID", defaultValue = "false") String eventID) throws IOException {
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        if (bindingResult.hasErrors()) {
            return "client/home/createPost";
        }
        String mainFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        recipe.setImage(mainFileName);

        int count = 0;
        List<Steps> stepsList = new ArrayList<Steps>();
        for (MultipartFile extraMultiPart : extraMultipartFile) {
            String extraImageName = StringUtils.cleanPath(extraMultiPart.getOriginalFilename());
            if (count <= numberOfImages) {
                Steps step = new Steps();
                step.setStepImg(extraImageName);
                step.setStepNumber(count + 1);
                step.setRecipe(recipe);
                step.setStepDescription(recipe.getSteps().get(count).getStepDescription());
                stepsList.add(step);
            }
            count++;
        }

        recipe.setSteps(stepsList);
        recipe.setUser(foundUser);
        for (Ingredients ingredient : recipe.getIngredients()
        ) {
            ingredient.setRecipe(recipe);
        }
        Posts savedRecipe = postsService.save(recipe);

        String postUploadDir = "./post-images/" + savedRecipe.getId();
        String stepUploadDir = "./steps-images/" + savedRecipe.getId();
        FileUploadUtil.saveFile(postUploadDir, multipartFile, mainFileName);

        for (MultipartFile extraMultiFile : extraMultipartFile) {
            String fileName = StringUtils.cleanPath(extraMultiFile.getOriginalFilename());

            FileUploadUtil.saveFile(stepUploadDir, extraMultiFile, fileName);
        }

        if (!eventID.equals("false")) {
            System.out.println("EVENT ID: " + eventID);
            event_participantService.findEventParticipants(eventID, savedRecipe);
            ra.addFlashAttribute("raMessage", "Đăng bài dự thi thành công");
        } else

            ra.addFlashAttribute("raMessage", "Đăng bài thành công");
        model.addAttribute("user", foundUser);
        model.addAttribute("userInfo", foundUser.getUserInfo());
        model.addAttribute("listPosts", foundUser.getPosts());
        return "redirect:/profile";
    }

    @GetMapping("/post-details/{id}")
    public String getPostDetails(@PathVariable("id") UUID id, Model model, Principal p, HttpSession session) {
        Posts post = postsService.findByID(id);
        session.setAttribute("id", id);
        List<Steps> stepsList = post.getSteps().stream().sorted(Comparator.comparing(Steps::getStepNumber)).toList();
        post.setSteps(stepsList);
        model.addAttribute("post", post);
        model.addAttribute("comment", new Comment());
        String userEmail = p.getName();
        Users foundUser = userService.findbyEmail(userEmail).get();
        model.addAttribute("user", foundUser);
        model.addAttribute("products", productService.getList());
        Long productID1 = post.getProduct_id1();
        Long productID2 = post.getProduct_id2();
        Long productID3 = post.getProduct_id3();
        if (productID1 == null)
            model.addAttribute("product1", productService.getRandomProduct());
        else
            model.addAttribute("product1", productService.getProductById(productID1));

        if (productID2 == null)
            model.addAttribute("product2", productService.getRandomProduct());
        else
            model.addAttribute("product2", productService.getProductById(productID2));

        if (productID3 == null)
            model.addAttribute("product3", productService.getRandomProduct());
        else
            model.addAttribute("product3", productService.getProductById(productID3));

        return "client/home/post_details";
    }

    @PostMapping("/changeProduct1")
    public String changeProduct1(@RequestParam(name = "productID") Long productID, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        UUID idP = (UUID) session.getAttribute("id");
        Posts post = postsService.findByID(idP);
        post.setProduct_id1(productID);
        postsService.save(post);
        redirectAttributes.addAttribute("id", idP);
        return "redirect:/post-details/{id}";
    }

    @PostMapping("/changeProduct2")
    public String changeProduct2(@RequestParam(name = "productID") Long productID, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        UUID idP = (UUID) session.getAttribute("id");
        Posts post = postsService.findByID(idP);
        post.setProduct_id2(productID);
        postsService.save(post);
        redirectAttributes.addAttribute("id", idP);
        return "redirect:/post-details/{id}";
    }

    @PostMapping("/changeProduct3")
    public String changeProduct3(@RequestParam(name = "productID") Long productID, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        UUID idP = (UUID) session.getAttribute("id");
        Posts post = postsService.findByID(idP);
        post.setProduct_id3(productID);
        postsService.save(post);
        redirectAttributes.addAttribute("id", idP);
        return "redirect:/post-details/{id}";
    }
}
