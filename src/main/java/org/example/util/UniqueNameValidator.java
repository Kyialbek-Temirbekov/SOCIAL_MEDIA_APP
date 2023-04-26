package org.example.util;

import org.example.dao.UserDao;
import org.example.fdo.UserFDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
public class UniqueNameValidator implements Validator {
    private final UserDao userDao;
    @Autowired
    public UniqueNameValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserFDO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserFDO user = (UserFDO) target;
        if(Optional.ofNullable(userDao.show(user.getName())).isPresent())
            errors.rejectValue("name", "", "This name is already taken");
    }
}
