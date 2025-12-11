package com.example.lab9cth;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;

    private EditText etX;
    private TextView tvResult;
    private MaterialButton btnCalc, btnInfo, btnGraph;
    private MaterialCheckBox cbAbs;

    private double lastX = Double.NaN; // для графика

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // IDs должны совпадать с твоим activity_main.xml
        etX = findViewById(R.id.etX);
        tvResult = findViewById(R.id.tvResult);
        btnCalc = findViewById(R.id.btnCalc);
        btnInfo = findViewById(R.id.btnInfo);
        btnGraph = findViewById(R.id.btnGraph);
        cbAbs = findViewById(R.id.cbAbs);

        btnGraph.setVisibility(View.GONE);

        btnCalc.setOnClickListener(v -> calculate());
        btnInfo.setOnClickListener(v -> startActivity(new Intent(this, InfoActivity.class)));

        btnGraph.setOnClickListener(v -> {
            if (!Double.isFinite(lastX)) return;
            Intent i = new Intent(this, GraphActivity.class);
            i.putExtra("x", lastX);
            startActivity(i);
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_DIST = 140;
            private static final int SWIPE_VEL = 140;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;

                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();

                // Только горизонтальный свайп
                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > SWIPE_DIST && Math.abs(velocityX) > SWIPE_VEL) {
                    if (dx < 0) {
                        // влево -> история
                        startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void calculate() {
        String s = etX.getText() == null ? "" : etX.getText().toString().trim();
        if (s.isEmpty()) {
            tvResult.setText("Введите x");
            btnGraph.setVisibility(View.GONE);
            return;
        }

        double x;
        try {
            x = Double.parseDouble(s.replace(',', '.'));
        } catch (Exception e) {
            tvResult.setText("Некорректный ввод");
            btnGraph.setVisibility(View.GONE);
            return;
        }

        boolean useAbs = cbAbs != null && cbAbs.isChecked();
        double xForCalc = useAbs ? Math.abs(x) : x;

        // Условие из задания: 0 и отрицательные — недопустимы (если не предусмотрено)
        if (!useAbs && xForCalc <= 0) {
            tvResult.setText("Недопустимый ввод данных: x должен быть > 0");
            btnGraph.setVisibility(View.GONE);
            return;
        }

        // cth(x)=cosh(x)/sinh(x), деление на 0 при sinh(x)=0 (вблизи 0)
        double sh = Math.sinh(xForCalc);
        if (Math.abs(sh) < 1e-8) {
            tvResult.setText("Недопустимая операция: деление на 0");
            btnGraph.setVisibility(View.GONE);
            return;
        }

        double y = Math.cosh(xForCalc) / sh;

        lastX = xForCalc;
        btnGraph.setVisibility(View.VISIBLE);

        String res = String.format(Locale.US, "cth(%.6f) = %.6f", xForCalc, y);
        tvResult.setText(res);

        HistoryStore.add(res);
    }
}
