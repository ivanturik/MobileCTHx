package com.example.lab9cth;

import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class NavHelper {
    public static final int TAB_CALC = 0;
    public static final int TAB_HISTORY = 1;
    public static final int TAB_INFO = 2;

    private static final int SWIPE_THRESHOLD = 120;
    private static final int SWIPE_VELOCITY_THRESHOLD = 120;

    private NavHelper() {}

    public static void setupBottomNav(AppCompatActivity a, BottomNavigationView nav, int currentTab) {
        int selectedId = tabToMenuId(currentTab);
        nav.setSelectedItemId(selectedId);

        nav.setOnItemSelectedListener(item -> {
            int targetTab = menuIdToTab(item.getItemId());
            if (targetTab == currentTab) return true;
            openTab(a, currentTab, targetTab);
            return true;
        });
    }

    public static void attachSwipeTabs(AppCompatActivity a, View touchArea, int currentTab) {
        GestureDetector gd = new GestureDetector(a, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return tryHandleHorizontalSwipe(a, currentTab, e1, e2, velocityX, velocityY);
            }
        });

        touchArea.setOnTouchListener((v, event) -> gd.onTouchEvent(event));
    }

    public static boolean tryHandleHorizontalSwipe(AppCompatActivity a,
                                                   int currentTab,
                                                   MotionEvent e1,
                                                   MotionEvent e2,
                                                   float velocityX,
                                                   float velocityY) {
        if (e1 == null || e2 == null) return false;

        float dx = e2.getX() - e1.getX();
        float dy = e2.getY() - e1.getY();

        if (Math.abs(dx) > Math.abs(dy)
                && Math.abs(dx) > SWIPE_THRESHOLD
                && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

            int targetTab = currentTab + (dx < 0 ? 1 : -1);
            if (targetTab < TAB_CALC || targetTab > TAB_INFO) return false;

            openTab(a, currentTab, targetTab);
            return true;
        }
        return false;
    }

    private static void openTab(AppCompatActivity a, int fromTab, int toTab) {
        Class<?> cls;
        if (toTab == TAB_CALC) cls = MainActivity.class;
        else if (toTab == TAB_HISTORY) cls = HistoryActivity.class;
        else cls = InfoActivity.class;

        Intent intent = new Intent(a, cls);
        a.startActivity(intent);

        if (toTab > fromTab) a.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        else a.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        a.finish(); // чтобы не плодить стек из 100 экранов
    }

    private static int tabToMenuId(int tab) {
        if (tab == TAB_CALC) return R.id.nav_calc;
        if (tab == TAB_HISTORY) return R.id.nav_history;
        return R.id.nav_info;
    }

    private static int menuIdToTab(int id) {
        if (id == R.id.nav_calc) return TAB_CALC;
        if (id == R.id.nav_history) return TAB_HISTORY;
        return TAB_INFO;
    }
}
