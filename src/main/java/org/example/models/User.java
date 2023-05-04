package org.example.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "password")
    private String password;
    @Column(name = "birth_day")
    private String birthDay;
    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(name = "photo")
    private byte[] photo;
    @Transient
    private boolean subscribed;
    @OneToMany(mappedBy = "owner")
    private List<Image> images;
    @OneToMany(mappedBy = "owner")
    private List<Comment> comments;
    @ManyToMany(mappedBy = "likedUsers")
    private List<Image> likedImages;
    @ManyToMany
    @JoinTable(
            name = "subscriptions",
            joinColumns = @JoinColumn(name = "user"),
            inverseJoinColumns = @JoinColumn(name = "subscriber"))
    private List<User> subscriptions;
    @ManyToMany(mappedBy = "subscriptions")
    private List<User> subscribers;

    public User() {}

    public User(String name, String password, String birthDay, Gender gender, byte[] photo) {
        this.name = name;
        this.password = password;
        this.birthDay = birthDay;
        this.gender = gender;
        this.photo = photo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Image> getLikedImages() {
        if(this.likedImages == null)
            this.likedImages = new ArrayList<>();
        return likedImages;
    }

    public void setLikedImages(List<Image> likedImages) {
        this.likedImages = likedImages;
    }

    public void addLikedImages(Image image) {
        if(this.likedImages == null)
            this.likedImages = new ArrayList<>();
        this.likedImages.add(image);
    }

    public List<User> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<User> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<User> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<User> subscribers) {
        this.subscribers = subscribers;
    }

    public void subscribe(User user) {
        if(this.subscribers == null)
            this.subscribers = new ArrayList<>();
        this.subscribers.add(user);
    }

    public void addSubscription(User user) {
        if(this.subscriptions == null)
            this.subscriptions = new ArrayList<>();
        this.subscriptions.add(user);
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
}
