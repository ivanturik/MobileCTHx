package com.example.lab9cth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        rv = findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(HistoryStore.getAll());
        rv.setAdapter(adapter);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_DIST = 140;
            private static final int SWIPE_VEL = 140;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;

                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();

                // Горизонтальный
                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > SWIPE_DIST && Math.abs(velocityX) > SWIPE_VEL) {
                    if (dx > 0) {
                        finish(); // вправо -> назад
                    }
                    return true;
                }

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
        adapter.setItems(HistoryStore.getAll());
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
            HistoryStore.clear();
            adapter.setItems(HistoryStore.getAll());

            Snackbar sb = Snackbar.make(root, "История очищена", Snackbar.LENGTH_SHORT);
            sb.show();
        }
    }
}
