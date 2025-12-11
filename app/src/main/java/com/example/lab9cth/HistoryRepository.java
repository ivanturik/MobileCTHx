package com.example.lab9cth;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public final class HistoryRepository {

    private static final String PREFS = "lab9cth_prefs";
    private static final String KEY_HISTORY = "history_lines";

    private HistoryRepository() {}

    public static ArrayList<String> load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String joined = sp.getString(KEY_HISTORY, "");
        ArrayList<String> list = new ArrayList<>();
        if (joined == null || joined.trim().isEmpty()) return list;

        String[] lines = joined.split("\n");
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                list.add(line.trim());
            }
        }
        return list;
    }

    public static void add(Context ctx, String item) {
        ArrayList<String> list = load(ctx);
        list.add(0, item); // новое сверху
        save(ctx, list);
    }

    public static void clear(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_HISTORY).apply();
    }

    private static void save(Context ctx, List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (s == null) continue;
            // на всякий случай убираем переносы
            s = s.replace("\n", " ").replace("\r", " ");
            sb.append(s).append("\n");
        }
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_HISTORY, sb.toString()).apply();
    }
}
