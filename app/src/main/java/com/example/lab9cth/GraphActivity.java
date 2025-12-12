package com.example.lab9cth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        double x = getIntent().getDoubleExtra("x", 1.0);

        CthGraphView graph = findViewById(R.id.graph);
        TextView tv = findViewById(R.id.tvPoint);

        DecimalFormat fmt = new DecimalFormat("0.###");
        DecimalFormat fmtLong = new DecimalFormat("0.######");

        double y;
        double sh = Math.sinh(x);
        if (Math.abs(sh) < 1e-8) {
            tv.setText("x ≈ 0 → деление на 0 (cth не определена)");
        } else {
            y = Math.cosh(x) / sh;
            tv.setText("x = " + fmt.format(x) + "   cth(x) = " + fmtLong.format(y));
        }

        double span = Math.max(3.0, Math.abs(x) + 1.5);
        graph.plotCth(-span, span, x);
    }
}
