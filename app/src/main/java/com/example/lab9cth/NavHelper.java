package com.example.lab9cth;

import android.content.Intent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class NavHelper {
    public static final int TAB_CALC = 0;
    public static final int TAB_HISTORY = 1;
    public static final int TAB_INFO = 2;

    private static final int SWIPE_THRESHOLD = 80;
    private static final int SWIPE_VELOCITY_THRESHOLD = 80;

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
        View target = touchArea;
        if (target == null) {
            View content = a.findViewById(android.R.id.content);
            if (content instanceof ViewGroup) {
                target = ((ViewGroup) content).getChildAt(0);
            } else {
                target = content;
            }
        }

        View touchSurface = a.getWindow() == null ? null : a.getWindow().getDecorView();

        if (target != null && touchSurface != null) {
            touchSurface.setClickable(true);
            new SwipeController(a, touchSurface, target, currentTab);
        }
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

        int enterAnim = toTab > fromTab ? R.anim.slide_in_right : R.anim.slide_in_left;
        int exitAnim = toTab > fromTab ? R.anim.slide_out_left : R.anim.slide_out_right;

        Intent intent = new Intent(a, cls);
        a.startActivity(intent);
        a.overridePendingTransition(enterAnim, exitAnim);
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

    private static final class SwipeController implements View.OnTouchListener {
        private final AppCompatActivity activity;
        private final View touchSurface;
        private final View target;
        private final int currentTab;

        private final int touchSlop;
        private final int minFlingVelocity;

        private float downX;
        private float downY;
        private float lastDx;
        private boolean dragging;
        private VelocityTracker tracker;

        SwipeController(AppCompatActivity activity, View touchSurface, View target, int currentTab) {
            this.activity = activity;
            this.touchSurface = touchSurface;
            this.target = target;
            this.currentTab = currentTab;

            ViewConfiguration cfg = ViewConfiguration.get(activity);
            touchSlop = cfg.getScaledTouchSlop();
            minFlingVelocity = cfg.getScaledMinimumFlingVelocity();

            touchSurface.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getActionMasked();

            if (action == MotionEvent.ACTION_DOWN) {
                downX = event.getX();
                downY = event.getY();
                lastDx = 0f;
                dragging = false;
                tracker = VelocityTracker.obtain();
                tracker.addMovement(event);
                return true;
            }

            if (tracker != null) tracker.addMovement(event);

            if (action == MotionEvent.ACTION_MOVE) {
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;

                if (!dragging) {
                    if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > touchSlop) {
                        if (canMove(dx)) {
                            dragging = true;
                            ViewParent parent = target.getParent();
                            if (parent != null) {
                                parent.requestDisallowInterceptTouchEvent(true);
                            }
                        }
                    }
                }

                if (dragging) {
                    dx = clampDirection(dx);
                    lastDx = dx;
                    float translated = dx * 0.9f;
                    float progress = Math.min(1f, Math.abs(translated) / Math.max(1, target.getWidth()));

                    target.setTranslationX(translated);
                    target.setScaleY(1f - 0.05f * progress);
                    target.setAlpha(1f - 0.15f * progress);
                    return true;
                }
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                float velocityX = 0f;
                if (tracker != null) {
                    tracker.computeCurrentVelocity(1000);
                    velocityX = tracker.getXVelocity();
                    tracker.recycle();
                    tracker = null;
                }

                if (dragging) {
                    boolean fast = Math.abs(velocityX) > minFlingVelocity * 1.5f;
                    boolean farEnough = Math.abs(lastDx) > target.getWidth() * 0.22f;

                    if (farEnough || fast) {
                        int toTab = currentTab + (lastDx < 0 ? 1 : -1);
                        animateAwayAndOpen(toTab, lastDx < 0);
                    } else {
                        resetPosition();
                    }
                    dragging = false;
                    return true;
                }
            }

            return false;
        }

        private boolean canMove(float dx) {
            if (dx < 0 && currentTab == TAB_INFO) return false;
            if (dx > 0 && currentTab == TAB_CALC) return false;
            return true;
        }

        private float clampDirection(float dx) {
            if (currentTab == TAB_CALC) return Math.min(0, dx);
            if (currentTab == TAB_INFO) return Math.max(0, dx);
            return dx;
        }

        private void animateAwayAndOpen(int toTab, boolean toRight) {
            float end = (toRight ? -1 : 1) * target.getWidth();
            target.animate()
                    .translationX(end)
                    .alpha(0.5f)
                    .setDuration(160)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> {
                        target.setAlpha(1f);
                        target.setTranslationX(0f);
                        target.setScaleY(1f);
                        openTab(activity, currentTab, toTab);
                    })
                    .start();
        }

        private void resetPosition() {
            target.animate()
                    .translationX(0f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(180)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }
}
