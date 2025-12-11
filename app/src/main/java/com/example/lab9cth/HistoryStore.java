package com.example.lab9cth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryStore {
    private static final ArrayList<String> items = new ArrayList<>();

    public static void add(String s) { items.add(0, s); }
    public static List<String> getAll() { return Collections.unmodifiableList(items); }
    public static void clear() { items.clear(); }
}
