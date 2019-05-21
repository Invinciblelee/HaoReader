package com.monke.monkeybook.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ListUtils {

    public static <T> void filter(List<T> list, ListLook<T> hook) {
        if (list == null) return;
        final ArrayList<T> r = new ArrayList<>();
        for (T t : list) {
            if (hook.test(t)) {
                r.add(t);
            }
        }
        r.trimToSize();
        list.clear();
        list.addAll(r);
    }

    public static <T> void removeDuplicate(List<T> list, Comparator<T> comparator) {
        if (list == null) return;
        final Set<T> set = new TreeSet<>(comparator);
        set.addAll(list);
        list.clear();
        list.addAll(set);
    }

    public static <T> void removeDuplicate(List<T> list) {
        if (list == null) return;
        LinkedHashSet<T> lh = new LinkedHashSet<>(list);
        list.clear();
        list.addAll(lh);
    }

    public static <T> List<Object> toObjectList(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(list);
    }

    public static List<String> toStringList(Object object) {
        if (object == null) {
            return new ArrayList<>();
        }
        final List<String> stringList = new ArrayList<>();
        if (object instanceof List) {
            for (Object obj : (List) object) {
                stringList.add(StringUtils.valueOf(obj));
            }
        } else {
            stringList.add(StringUtils.valueOf(object));
        }

        return stringList;
    }

    public static List<Object> toObjectList(Object object) {
        if (object == null) {
            return new ArrayList<>();
        }
        final List<Object> objectList = new ArrayList<>();
        if (object instanceof List) {
            objectList.addAll((List) object);
        } else {
            objectList.add(object);
        }
        return objectList;
    }

    @SafeVarargs
    public static <T> List<T> mutableList(T... a) {
        if (a == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(a));
    }

    public interface ListLook<T> {
        boolean test(T t);
    }


}
