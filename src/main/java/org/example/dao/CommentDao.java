package org.example.dao;

import jakarta.transaction.Transactional;
import org.example.models.Comment;
import org.example.models.Image;
import org.example.models.User;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Transactional
public class CommentDao {
    private final SessionFactory sessionFactory;
    @Autowired
    public CommentDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    public void save(String owner, int imgId, Comment comment) {
        Session session = sessionFactory.getCurrentSession();
        comment.setDate(LocalDate.now().toString());
        comment.setOwner(session.createQuery("FROM User where name = :name", User.class)
                .setParameter("name", owner).getSingleResultOrNull());
        comment.setImage(session.load(Image.class, imgId));
        session.persist(comment);
    }
    public void delete(int id) {
        Session session = sessionFactory.getCurrentSession();
        Comment comment = session.load(Comment.class, id);
        comment.setOwner(null);
        comment.setImage(null);
        session.delete(comment);
    }
    public List<Comment> findByImage(int imgId) {
        Session session = sessionFactory.getCurrentSession();
        Image image = session.get(Image.class, imgId);
        Hibernate.initialize(image.getComments());
        return image.getComments();
    }
}
