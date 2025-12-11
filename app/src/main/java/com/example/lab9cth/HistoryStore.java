package com.example.lab9cth;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryStore {
    private static final ArrayList<String> items = new ArrayList<>();
    private static boolean initialized = false;

    public static void init(Context ctx) {
        if (initialized) return;
        initialized = true;
        items.clear();
        items.addAll(HistoryRepository.load(ctx.getApplicationContext()));
    }

    public static void add(Context ctx, String s) {
        ensureInit(ctx);
        items.add(0, s);
        HistoryRepository.add(ctx.getApplicationContext(), s);
    }

    public static List<String> getAll(Context ctx) {
        ensureInit(ctx);
        return Collections.unmodifiableList(items);
    }

    public static void clear(Context ctx) {
        ensureInit(ctx);
        items.clear();
        HistoryRepository.clear(ctx.getApplicationContext());
    }

    private static void ensureInit(Context ctx) {
        if (!initialized) {
            init(ctx);
        }
    }
}
