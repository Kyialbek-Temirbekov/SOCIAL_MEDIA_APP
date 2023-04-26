package org.example.util;

import org.example.fdo.ImageFDO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
@Component
public class ImageValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return ImageFDO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ImageFDO imageFDO = (ImageFDO) target;
        if(imageFDO.getImg().getSize() == 0)
            errors.rejectValue("img", "", "Image must be selected");
    }
}
