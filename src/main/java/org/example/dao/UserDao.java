package org.example.dao;

import jakarta.transaction.Transactional;
import org.example.models.User;
import org.example.fdo.UserFDO;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class UserDao  {
    private final SessionFactory sessionFactory;
    private static final byte[] imageFile;

    static {
        try {
            imageFile = new FileInputStream("C:/Users/Stranger/Projects/Lofty/src/main/webapp/DtImages/anonymous_3.png").readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    public UserDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    public void save(UserFDO userFDO) throws IOException {
        Session session = sessionFactory.getCurrentSession();
        User user = new User();
        user.setName(userFDO.getName());
        user.setPassword(Integer.toString(userFDO.getPassword().hashCode()));
        user.setBirthDay(userFDO.getBirthDay());
        user.setGender(userFDO.getGender());
        if(userFDO.getPhoto().isEmpty())
            user.setPhoto(imageFile);
        else
            user.setPhoto(userFDO.getPhotoBytes());
        session.persist(user);
    }
    public User show(String name) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.get(User.class, name);
        if(user != null) {
            Hibernate.initialize(user.getLikedImages());
            Hibernate.initialize(user.getSubscribers());
        }
        return user;
    }
    public byte[] getPhoto(String name) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.get(User.class, name);
        return user.getPhoto();
    }
    public boolean identical(String name, String password) {
        Session session = sessionFactory.getCurrentSession();
        Optional<User> user = Optional.ofNullable(session.get(User.class, name));
        return user.isPresent() && user.get().getPassword().equals(Integer.toString(password.hashCode()));
    }
    public void updateName(String name, UserFDO userFDO) {
        Session session = sessionFactory.getCurrentSession();
        session.createQuery("update User u set u.name = :newName where u.name = :name")
                .setParameter("newName", userFDO.getName()).setParameter("name",name).executeUpdate();
    }
    public void updatePassword(String name, String password) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.load(User.class, name);
        user.setPassword(Integer.toString(password.hashCode()));
    }
    public void deleteAccount(String name) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.load(User.class, name);
        user.getImages().forEach(session::remove);
        user.getComments().forEach(session::remove);
        session.remove(user);
    }
    public void updateProfile(String name, UserFDO updatedUser) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.load(User.class, name);
        user.setBirthDay(updatedUser.getBirthDay());
        user.setGender(updatedUser.getGender());
    }
    public void updatePhoto(String name, MultipartFile file) throws IOException {
        Session session = sessionFactory.getCurrentSession();
        byte[] photo = null;
        User user = session.load(User.class, name);
        if(file.isEmpty())
            return;
        photo = file.getBytes();
        user.setPhoto(photo);
    }
    public void subscribe(String userName, String subscriptionName) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.get(User.class, userName);
        User subscription = session.get(User.class, subscriptionName);
        if(subscription.getSubscribers().contains(user)) {
            subscription.getSubscribers().remove(user);
            user.getSubscriptions().remove(subscription);
        }
        else {
            subscription.subscribe(user);
            user.addSubscription(subscription);
        }
    }
    public List<User> getSubscriptions(String name) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.get(User.class, name);
        Hibernate.initialize(user.getSubscriptions());
        return user.getSubscriptions();
    }
}
