package org.example.dao;

import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.example.models.Image;
import org.example.fdo.ImageFDO;
import org.example.util.PaginationResult;
import org.example.models.User;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.StringJoiner;

@Component
@Transactional
public class ImageDao {
    private final SessionFactory sessionFactory;

    @Autowired
    public ImageDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public int save(String user, ImageFDO imageFDO) throws IOException {
        Session session = sessionFactory.getCurrentSession();
        Image image = new Image();
        image.setTitle(imageFDO.getTitle());
        image.setCaption(imageFDO.getCaption());
        image.setImg(imageFDO.getImgBytes());
        image.setStatus(imageFDO.getStatus());
        image.setDate(LocalDate.now().toString());
        image.setOwner(session.load(User.class, user));

        session.save(image);
        return image.getId();
    }

    public List<Image> index(String name) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.get(User.class, name);
        List<Image> images = user.getImages();
        images.forEach(image -> Hibernate.initialize(image.getLikedUsers()));
        return images;
    }

    public Image show(int id) {
        Session session = sessionFactory.getCurrentSession();
        Image image =  session.get(Image.class, id);
        Hibernate.initialize(image.getLikedUsers());
        return image;
    }

    public List<Image> findByDate() {
        Session session = sessionFactory.getCurrentSession();
        String query =  "select i from Image i where i.status = 'Public' order by i.id desc limit 24";
        List<Image> images = session.createQuery(query).getResultList();
        images.forEach(image -> Hibernate.initialize(image.getLikedUsers()));
        return images;
    }

    public byte[] getImg(int id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Image.class, id).getImg();
    }

    public void update(int id, ImageFDO imageFDO) {
        Session session = sessionFactory.getCurrentSession();
        Image image = session.load(Image.class, id);
        image.setTitle(imageFDO.getTitle());
        image.setCaption(imageFDO.getCaption());
        image.setStatus(imageFDO.getStatus());
    }

    public void delete(int id) {
        Session session = sessionFactory.getCurrentSession();
        Image image = session.load(Image.class, id);
        image.getOwner().getImages().remove(image);
        image.getTags().forEach(session::remove);
        image.getComments().forEach(session::remove);
        session.remove(image);
    }
    public List<Image> findRelated(int id) {
        Session session = sessionFactory.getCurrentSession();
        StringJoiner regex = new StringJoiner("|");
        Image image = session.get(Image.class, id);
        image.getTags().forEach(tag -> regex.add(tag.getTag()));
        String query = "SELECT * FROM image WHERE image.id IN(SELECT DISTINCT tag.img_id FROM tag WHERE " +
                "tag.img_tag REGEXP :regex) AND image.id <> :imgId AND image.status = 'Public' LIMIT 24";
        List<Image> images = session.createNativeQuery(query, Image.class)
                .setParameter("regex",regex.toString()).setParameter("imgId",id).getResultList();
        images.forEach(img -> Hibernate.initialize(img.getLikedUsers()));
        return images;
    }
    public PaginationResult<Image> findBySearchQuery(int pageNumber, int pageSize, String searchQuery) {
        long totalRecords;
        int lastPageNumber;
        List<Image> records;
        Session session = sessionFactory.getCurrentSession();
        String countQuery = "SELECT COUNT(image.id) FROM image WHERE image.id IN(SELECT DISTINCT tag.img_id " +
                "FROM tag WHERE tag.img_tag REGEXP :regex) AND image.status = 'Public'";
        totalRecords = session.createNativeQuery(countQuery, Long.class)
                .setParameter("regex", searchQuery).getSingleResult();
        if(totalRecords % pageSize == 0)
            lastPageNumber = (int)(totalRecords / pageSize);
        else
            lastPageNumber = (int)(totalRecords / pageSize) + 1;

        String query = "SELECT * FROM image WHERE image.id IN(SELECT DISTINCT tag.img_id " +
                "FROM tag WHERE tag.img_tag REGEXP :searchQuery) AND image.status = 'Public'";
        TypedQuery<Image> typedQuery = session.createNativeQuery(query, Image.class)
                .setParameter("searchQuery", searchQuery);
        typedQuery.setFirstResult((pageNumber - 1) * pageSize);
        typedQuery.setMaxResults(pageSize);
        records = typedQuery.getResultList();
        records.forEach(image -> Hibernate.initialize(image.getLikedUsers()));
        PaginationResult<Image> paginationResult = new PaginationResult<>();
        paginationResult.setPageSize(pageSize);
        paginationResult.setCurrentPageNumber(pageNumber);
        paginationResult.setLastPageNumber(lastPageNumber);
        paginationResult.setTotalRecords(totalRecords);
        paginationResult.setRecords(records);
        return paginationResult;
    }
    public void like(int imgId, String userName) {
        Session session = sessionFactory.getCurrentSession();
        Image image = session.get(Image.class, imgId);
        User user = session.get(User.class, userName);
        if(image.getLikedUsers().contains(user)) {
            image.getLikedUsers().remove(user);
            user.getLikedImages().remove(image);
        }
        else {
            image.addLikedUsers(user);
            user.addLikedImages(image);
        }
    }
    public List<Image> favorites(String name) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.get(User.class, name);
        List<Image> images = user.getLikedImages();
        images.forEach(image -> Hibernate.initialize(image.getLikedUsers()));
        return images;
    }
}

//    String query = "SELECT DISTINCT * FROM image JOIN tag ON image.id = tag.img_id " +
//            "WHERE tag.img_tag REGEXP :regex AND image.id <> :imgId";