package org.example.dao;

import jakarta.transaction.Transactional;
import org.example.models.Image;
import org.example.models.Tag;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringTokenizer;

@Component
@Transactional
public class TagDao {
    private final SessionFactory sessionFactory;
    @Autowired
    public TagDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    public void save(int imgId, String tags) {
        Session session = sessionFactory.getCurrentSession();
        StringTokenizer tokenizer = new StringTokenizer(tags, " #");
        while(tokenizer.hasMoreTokens()) {
            Tag tag = new Tag(tokenizer.nextToken());
            tag.setImage(session.load(Image.class, imgId));
            session.persist(tag);
        }
    }
    public List<Tag> findAllByImgId(int imgId) {
        Session session = sessionFactory.getCurrentSession();
        Image image = session.get(Image.class, imgId);
        Hibernate.initialize(image.getTags());
        return image.getTags();
    }
    public void delete(int id) {
        Session session = sessionFactory.getCurrentSession();
        Tag tag = session.load(Tag.class, id);
        tag.getImage().getTags().remove(tag);
        session.remove(tag);
    }
}
