package com.example.lab9cth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        double x = getIntent().getDoubleExtra("x", 1.0);

        CthGraphView graph = findViewById(R.id.graph);
        TextView tv = findViewById(R.id.tvPoint);

        double y;
        double sh = Math.sinh(x);
        if (Math.abs(sh) < 1e-8) {
            tv.setText("x ≈ 0 → деление на 0 (cth не определена)");
        } else {
            y = Math.cosh(x) / sh;
            tv.setText("x = " + x + "   cth(x) = " + y);
        }

        graph.plotCth(-3, 3, x); // диапазон можно поменять
    }
}
