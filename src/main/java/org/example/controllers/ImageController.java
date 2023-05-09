package org.example.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.dao.CommentDao;
import org.example.dao.ImageDao;
import org.example.dao.TagDao;
import org.example.dao.UserDao;
import org.example.fdo.ImageFDO;
import org.example.models.Comment;
import org.example.models.Image;
import org.example.models.Tag;
import org.example.models.User;
import org.example.util.ImageValidator;
import org.example.util.PaginationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/image")
public class ImageController {
    private final ImageDao imageDao;
    private final TagDao tagDao;
    private final CommentDao commentDao;
    private final UserDao userDao;
    private final ImageValidator imageValidator;
    @Autowired
    public ImageController(ImageDao imageDao, TagDao tagDao, CommentDao commentDao, UserDao userDao, ImageValidator imageValidator) {
        this.imageDao = imageDao;
        this.tagDao = tagDao;
        this.commentDao = commentDao;
        this.userDao = userDao;
        this.imageValidator = imageValidator;
    }

    @GetMapping("/new")
    public String newImage(@ModelAttribute("image") ImageFDO imageFDO) {
        return "image/new";
    }
    @PostMapping()
    public String save(@SessionAttribute(value = "user", required = false) String user,
                       @ModelAttribute("image") @Valid ImageFDO imageFDO,
                       BindingResult bindingResult) throws IOException {
        imageValidator.validate(imageFDO,bindingResult);
        if(bindingResult.hasErrors())
            return "image/new";
        int id = imageDao.save(user, imageFDO);
        tagDao.save(id, imageFDO.getTags());
        return "redirect:/image/" + id;
    }
    @GetMapping("/{id}")
    public String show(@SessionAttribute(value = "user", required = false) String name,
                       @PathVariable("id") int id, Model model, @ModelAttribute("comment") Comment comment) {
        Image image = imageDao.show(id);
        enrich(image, name);
        model.addAttribute("image", image);
        List<Tag> tagList = tagDao.findAllByImgId(id);
        List<Tag> tagForm = new ArrayList<>();
        model.addAttribute("tags",tagList);
        model.addAttribute("comments", commentDao.findByImage(id));
        return "image/show";
    }
    @GetMapping("/get-image/{id}")
    public void getImage(@PathVariable("id") int id, HttpServletResponse response) throws IOException {
        byte[] imgData = imageDao.getImg(id);
        response.setContentType("image/*");
        response.getOutputStream().write(imgData);
        response.getOutputStream().close();
    }
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id, Model model) {
        model.addAttribute("image", new ImageFDO(imageDao.show(id)));
        return "image/edit";
    }
    @PatchMapping("/{id}")
    public String update(@ModelAttribute("image") ImageFDO imageFDO,
                         @PathVariable("id") int id) {
        imageDao.update(id, imageFDO);
        return "redirect:/image/" + id;
    }
    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") int id) {
        imageDao.delete(id);
        return "redirect:/show";
    }
    @PostMapping("/tag/{id}")
    public String addTag(@RequestParam("tags") String tags, @PathVariable("id") int id) {
        tagDao.save(id, tags);
        return "redirect:/image/" + id;
    }
    @DeleteMapping("/tag/{id}")
    public String deleteTag(@PathVariable("id") int id, @RequestParam("tagId") int tagId) {
        tagDao.delete(tagId);
        return "redirect:/image/" + id;
    }
    @GetMapping("/reveal/{id}")
    public String reveal(@SessionAttribute(value = "user", required = false) String name,
                         @PathVariable("id") int id, Model model,
                         @ModelAttribute("comment") Comment comment) {
        Image image = imageDao.show(id);
        List<Image> relatedImages = imageDao.findRelated(id);

        enrich(image, name);
        relatedImages.forEach(img -> enrich(img, name));

        model.addAttribute("image", image);
        model.addAttribute("relatedImages", relatedImages);
        model.addAttribute("comments", commentDao.findByImage(id));
        model.addAttribute("currentUser", userDao.show(name));
        return "image/reveal";
    }
    @GetMapping("/search/{page}")
    public String search(@SessionAttribute(value = "user", required = false) String name,
                         @PathVariable("page") int page, @RequestParam("search")
                        String query, Model model) {
        int pageSize = 12;
        PaginationResult<Image> paginationResult = imageDao.findBySearchQuery(page,pageSize,query);
        paginationResult.getRecords().forEach(image -> enrich(image, name));
        model.addAttribute("paginationResult", paginationResult);
        model.addAttribute("searchQuery", query);
        return "image/search";
    }
    @PostMapping("/comment/{imgId}")
    public String saveComment(@SessionAttribute(value = "user", required = false) String name,
                              @PathVariable("imgId") int id,
                              @ModelAttribute("comment") Comment comment) {
        commentDao.save(name, id, comment);
        return "redirect:/image/reveal/" + id;
    }
    @DeleteMapping("/comment/{id}")
    public String deleteComment(@PathVariable("id") int id, @RequestParam("imgId") int imgId) {
        commentDao.delete(id);
        return "redirect:/image/reveal/" + imgId;
    }
    @PatchMapping("/like/{id}")
    public String like(@SessionAttribute(value = "user", required = false) String name,
                       @PathVariable("id") int id) {
        imageDao.like(id, name);
        return "redirect:/image/reveal/" + id;
    }
    @GetMapping("/favorites")
    public String favorites(@SessionAttribute(value = "user", required = false) String name, Model model) {
        List<Image> favorites = imageDao.favorites(name);
        favorites.forEach(image -> enrich(image, name));
        model.addAttribute("favoriteImages", favorites);
        return "image/favorites";
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
}
