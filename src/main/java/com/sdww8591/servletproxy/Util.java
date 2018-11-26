package com.sdww8591.servletproxy;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class Util {

    public static <T> List<T> enumerationToList(Enumeration<T> enumeration) {
        if (enumeration == null) {
            throw new NullPointerException("enumeration can not be null!");
        }

        List<T> result = new LinkedList<>();
        while (enumeration.hasMoreElements()) {
            result.add(enumeration.nextElement());
        }
        return result;
    }
}
