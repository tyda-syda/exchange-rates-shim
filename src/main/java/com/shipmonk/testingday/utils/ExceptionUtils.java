package com.shipmonk.testingday.utils;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.springframework.dao.DataIntegrityViolationException;

public final class ExceptionUtils {

    public static boolean isUniqueViolation(DataIntegrityViolationException e) {
        return e.getCause() instanceof ConstraintViolationException cv && ConstraintKind.UNIQUE == cv.getKind();
    }

}
