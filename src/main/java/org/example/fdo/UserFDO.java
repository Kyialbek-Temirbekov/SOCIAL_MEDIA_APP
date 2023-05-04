package org.example.fdo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.models.Gender;
import org.example.models.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class UserFDO {
    @NotEmpty(message = "Name should not be empty")
    @Size(max = 30, message = "Name should be between 1 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\-._~:/?#\\[\\]@!$&'()*+,;=%]+$",
            message = "Name should not contain spaces, commas, quotation marks, backslashes etc")
    private String name;
    @NotEmpty(message = "Password should not be empty")
    @Size(max = 20, message = "Password should be between 1 and 20 characters")
    private String password;
    private String birthDay;
    private Gender gender;
    private MultipartFile photo;
    public UserFDO() {}

    public UserFDO(User user) {
        this.name = user.getName();
        this.password = user.getPassword();
        this.birthDay = user.getBirthDay();
        this.gender = user.getGender();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public MultipartFile getPhoto() {
        return photo;
    }

    public byte[] getPhotoBytes() throws IOException {
        return photo.getBytes();
    }

    public void setPhoto(MultipartFile photo) {
        this.photo = photo;
    }
}

