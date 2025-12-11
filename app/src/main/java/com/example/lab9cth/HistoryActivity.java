package com.example.lab9cth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class HistoryActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private RecyclerView rv;
    private HistoryAdapter adapter;

    private boolean clearArmed = false; // после первого свайпа вниз
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        HistoryStore.init(this);

        rv = findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(HistoryStore.getAll(this));
        rv.setAdapter(adapter);
        rv.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavHistory);
        NavHelper.setupBottomNav(this, bottomNav, NavHelper.TAB_HISTORY);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavHistory);
        NavHelper.setupBottomNav(this, bottomNav, NavHelper.TAB_HISTORY);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_DIST = 140;
            private static final int SWIPE_VEL = 140;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (NavHelper.tryHandleHorizontalSwipe(HistoryActivity.this,
                        NavHelper.TAB_HISTORY, e1, e2, velocityX, velocityY)) {
                    return true;
                }

                if (e1 == null || e2 == null) return false;

                float dy = e2.getY() - e1.getY();
                float dx = e2.getX() - e1.getX();

                // Вертикальный вниз (двойной)
                if (Math.abs(dy) > Math.abs(dx) && dy > SWIPE_DIST && Math.abs(velocityY) > SWIPE_VEL) {
                    handleDownSwipe();
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // обновить историю, если вернулись с Main
        adapter.setItems(HistoryStore.getAll(this));
        rv.scheduleLayoutAnimation();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void handleDownSwipe() {
        View root = findViewById(android.R.id.content);

        if (!clearArmed) {
            clearArmed = true;

            Snackbar sb = Snackbar.make(root, "Очистить историю? Свайп вниз ещё раз", Snackbar.LENGTH_SHORT);
            sb.setBackgroundTint(0xFFD32F2F); // красный
            sb.show();

            // если второй свайп не сделан быстро — сбрасываем
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> clearArmed = false, 1800);
        } else {
            clearArmed = false;
            HistoryStore.clear(this);
            adapter.setItems(HistoryStore.getAll(this));
            rv.scheduleLayoutAnimation();

            Snackbar sb = Snackbar.make(root, "История очищена", Snackbar.LENGTH_SHORT);
            sb.show();
        }
    }
}
