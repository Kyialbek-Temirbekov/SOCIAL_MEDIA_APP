package org.example.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.dao.ImageDao;
import org.example.dao.UserDao;
import org.example.fdo.UserFDO;
import org.example.models.Image;
import org.example.models.User;
import org.example.util.UniqueNameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/")
public class UserController {
    private final UserDao userDao;
    private final ImageDao imageDao;
    private final UniqueNameValidator uniqueNameValidator;
    @Autowired
    public UserController(UserDao userDao, ImageDao imageDao, UniqueNameValidator validator) {
        this.userDao = userDao;
        this.imageDao = imageDao;
        this.uniqueNameValidator = validator;
    }
    @GetMapping()
    public String verify() {
        return "redirect:/login";
    }
    @GetMapping("/home")
    public String home(@SessionAttribute(value = "user", required = false) String name, Model model) {
        if(name == null)
            return "redirect:/login";
        List<Image> images = imageDao.findByDate();
        images.forEach(image -> enrich(image, name));
        model.addAttribute("imagesByDate", images);
        return "user/home";
    }
    @GetMapping("/new")
    public String newUser(@ModelAttribute("user") UserFDO userFDO) {
        return "user/join";
    }
    @PostMapping()
    public String create(@ModelAttribute("user") @Valid UserFDO userFDO,
                         BindingResult bindingResult, HttpSession session) throws IOException {
        uniqueNameValidator.validate(userFDO, bindingResult);
        if(bindingResult.hasErrors())
            return "user/join";
        userDao.save(userFDO);
        session.setAttribute("user",userFDO.getName());
        return "redirect:/home";
    }
    @GetMapping("/login")
    public String loginPage(@ModelAttribute("user") UserFDO userFDO) {
        return "user/login";
    }
    @PostMapping("/login")
    public String login(@ModelAttribute("user") @Valid UserFDO userFDO,
                        BindingResult bindingResult, HttpSession session, Model model) {
        if(userDao.identical(userFDO.getName(), userFDO.getPassword()) && !bindingResult.hasErrors()) {
            session.setAttribute("user", userFDO.getName());
            return "redirect:/home";
        }
        else {
            model.addAttribute("error", "Invalid user name or password");
            return "user/login";
        }
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    @GetMapping("/show")
    public String show(@SessionAttribute(value = "user", required = false) String name, Model model) {
        if(name == null)
            return "redirect:/login";
        model.addAttribute("user", userDao.show(name));
        model.addAttribute("imageList", imageDao.index(name));
        return "user/show";
    }
    @GetMapping("/reveal/{name}")
    public String reveal(@SessionAttribute(value = "user", required = false) String sessionName,
                         @PathVariable("name") String name, Model model) {
        if(sessionName == null)
            return "redirect:/login";
        List<Image> images = imageDao.index(name);
        images.forEach(image -> enrich(image, sessionName));
        User user = userDao.show(name);
        enrichUser(user, sessionName);
        model.addAttribute("user", user);
        model.addAttribute("imageList", images);
        model.addAttribute("currentUser", sessionName);
        return "user/reveal";
    }

    @GetMapping("/get-photo")
    public void getPhoto(@SessionAttribute(value = "user", required = false) String name,
                         HttpServletResponse response) throws IOException {
        byte[] imgData = userDao.getPhoto(name);
        response.setContentType("image/*");
        response.getOutputStream().write(imgData);
        response.getOutputStream().close();
    }
    @GetMapping("/get-photo/{name}")
    public void getPhotoByName(@PathVariable("name") String name,
                               HttpServletResponse response) throws IOException {
        byte[] imgData = userDao.getPhoto(name);
        response.setContentType("image/*");
        response.getOutputStream().write(imgData);
        response.getOutputStream().close();
    }
    @GetMapping("/edit")
    public String edit(@SessionAttribute(value = "user", required = false) String name, Model model) {
        if(name == null)
            return "redirect:/login";
        model.addAttribute("user", new UserFDO(userDao.show(name)));
        return "user/edit";
    }
    @PatchMapping("/name")
    public String updateName(@SessionAttribute(value = "user", required = false) String name,
                             @ModelAttribute("user") @Valid UserFDO userFDO,
                             BindingResult bindingResult, HttpSession session) {
        if(!userFDO.getName().equals(name))
            uniqueNameValidator.validate(userFDO, bindingResult);
        if(bindingResult.hasErrors())
            return "user/edit";
        else {
            userDao.updateName(name, userFDO);
            session.removeAttribute("user");
            session.setAttribute("user", userFDO.getName());
        }
        return "redirect:/edit";
    }
    @PatchMapping("/password")
    public String updatePassword(@SessionAttribute(value = "user", required = false) String name,
                                 @RequestParam("currentPassword")String currentPassword,
                                 @RequestParam("newPassword")String newPassword) {
        if(userDao.identical(name, currentPassword))
            userDao.updatePassword(name, newPassword);
        return "redirect:/edit";
    }
    @DeleteMapping("/account")
    public String deleteAccount(@SessionAttribute(value = "user", required = false) String name,
                                @RequestParam("password")String password, HttpSession session) {
        if(userDao.identical(name, password)) {
            userDao.deleteAccount(name);
            session.invalidate();
            return "redirect:/login";
        }
        else return "redirect:/edit";
    }
    @PatchMapping("/profile")
    public String updateProfile(@SessionAttribute(value = "user", required = false) String name,
                                @ModelAttribute("user") UserFDO userFDO) {
        userDao.updateProfile(name, userFDO);
        return "redirect:/edit";
    }
    @PatchMapping("/photo")
    public String updatePhoto(@SessionAttribute(value = "user", required = false) String name,
                              @RequestParam("photo")MultipartFile multipartFile) throws IOException {
        userDao.updatePhoto(name, multipartFile);
        return "redirect:/edit";
    }
    @DeleteMapping("/photo")
    public String deletePhoto(@SessionAttribute(value = "user", required = false) String name) throws IOException {
        userDao.updatePhoto(name, null);
        return "redirect:/edit";
    }
    @PatchMapping("/subscribe/{name}")
    public String subscribe(@SessionAttribute(value = "user", required = false) String user,
                            @PathVariable("name") String subscription) {
        userDao.subscribe(user, subscription);
        return "redirect:/reveal/" + subscription;
    }
    @GetMapping("/subscriptions")
    public String subscriptionsPage(@SessionAttribute(value = "user", required = false) String name,
                                    Model model) {
        if(name == null)
            return "redirect:/login";
        model.addAttribute("subscriptions", userDao.getSubscriptions(name));
        return "user/subscriptions";
    }

    private void enrich(Image image, String name) {
        image.setLiked(false);
        image.getLikedUsers().forEach(user -> {
            if(user.getName().equals(name)) {
                image.setLiked(true);
                return;
            }
        });
    }

    private void enrichUser(User user, String sessionName) {
        user.setSubscribed(false);
        user.getSubscribers().forEach(sub -> {
            if(sub.getName().equals(sessionName)) {
                user.setSubscribed(true);
                return;
            }
        });
    }
}
