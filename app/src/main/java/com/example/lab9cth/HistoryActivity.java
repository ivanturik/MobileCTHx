package com.example.lab9cth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class HistoryActivity extends AppCompatActivity {
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

        SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            handleDownSwipe();
            swipeRefresh.setRefreshing(false);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavHistory);
        NavHelper.setupBottomNav(this, bottomNav, NavHelper.TAB_HISTORY);

        NavHelper.attachSwipeTabs(this, findViewById(R.id.historyRoot), NavHelper.TAB_HISTORY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // обновить историю, если вернулись с Main
        adapter.setItems(HistoryStore.getAll(this));
        rv.scheduleLayoutAnimation();
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
