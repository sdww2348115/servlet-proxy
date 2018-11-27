package com.sdww8591.servletproxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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

    public static boolean predicateHeader(Map.Entry<String, String> entry) {
        if (entry.getKey().trim().equalsIgnoreCase("Content-Length")) {
            return false;
        }
        return true;
    }

    public static boolean isInputStreamReadable(InputStream inputStream) {
        try {
            inputStream.available();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
