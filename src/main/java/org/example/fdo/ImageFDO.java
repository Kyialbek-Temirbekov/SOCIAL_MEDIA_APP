package org.example.fdo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.models.Image;
import org.example.models.Status;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ImageFDO {
    @NotEmpty(message = "Title should not be empty")
    @Size(max = 30, message = "Title should be between 1 and 30 characters")
    private String title;
    @Size(max = 300, message = "Caption should not contain more than 300 characters")
    private String caption;
    private MultipartFile img;
    @NotNull(message = "Status must be selected")
    private Status status;
    @NotEmpty(message = "Recommended")
    private String tags;

    public ImageFDO() {}

    public ImageFDO(Image image) {
        this.title = image.getTitle();
        this.caption = image.getCaption();
        this.status = image.getStatus();
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public MultipartFile getImg() {
        return img;
    }
    public byte[] getImgBytes() throws IOException {
        return img.getBytes();
    }

    public void setImg(MultipartFile img) {
        this.img = img;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
