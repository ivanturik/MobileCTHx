package com.example.lab9cth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CthGraphView extends View {

    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<Double> xs = new ArrayList<>();
    private final List<Double> ys = new ArrayList<>();

    private double highlightX = Double.NaN;

    public CthGraphView(Context c) { super(c); init(); }
    public CthGraphView(Context c, @Nullable AttributeSet a) { super(c, a); init(); }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        axisPaint.setStrokeWidth(3f);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setColor(0xDDFFFFFF);

        gridPaint.setStrokeWidth(1.2f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(0x66FFFFFF);

        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(0xFF4BD7EC);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setShadowLayer(14f, 0, 0, 0x8824C0E8);

        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(0xFFFFFFFF);
        pointPaint.setShadowLayer(18f, 0, 4f, 0xAA4BD7EC);
    }

    public void plotCth(double minX, double maxX, double highlightX) {
        this.highlightX = highlightX;

        xs.clear();
        ys.clear();

        int n = 600;
        double step = (maxX - minX) / n;

        for (int i = 0; i <= n; i++) {
            double x = minX + i * step;

            double sh = Math.sinh(x);
            if (Math.abs(sh) < 1e-4) continue; // разрыв возле 0

            double y = Math.cosh(x) / sh;

            if (!Double.isFinite(y)) continue;
            if (Math.abs(y) > 10) continue; // чтоб не улетало

            xs.add(x);
            ys.add(y);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth(), h = getHeight();
        int pad = (int) (Math.min(w, h) * 0.12f);

        for (int i = 1; i <= 4; i++) {
            float y = pad + (h - 2f * pad) * i / 5f;
            canvas.drawLine(pad, y, w - pad, y, gridPaint);
            float x = pad + (w - 2f * pad) * i / 5f;
            canvas.drawLine(x, pad, x, h - pad, gridPaint);
        }

        if (xs.isEmpty()) return;

        double minX = xs.get(0), maxX = xs.get(0);
        double minY = ys.get(0), maxY = ys.get(0);
        for (int i = 1; i < xs.size(); i++) {
            minX = Math.min(minX, xs.get(i));
            maxX = Math.max(maxX, xs.get(i));
            minY = Math.min(minY, ys.get(i));
            maxY = Math.max(maxY, ys.get(i));
        }

        double yPad = (maxY - minY) * 0.1;
        if (yPad == 0) yPad = 1;
        minY -= yPad; maxY += yPad;

        if (minY <= 0 && 0 <= maxY) {
            float y0 = mapY(0, h, pad, minY, maxY);
            canvas.drawLine(pad, y0, w - pad, y0, axisPaint);
        }
        if (minX <= 0 && 0 <= maxX) {
            float x0 = mapX(0, w, pad, minX, maxX);
            canvas.drawLine(x0, pad, x0, h - pad, axisPaint);
        }

        Path path = new Path();
        boolean started = false;
        for (int i = 0; i < xs.size(); i++) {
            float x = mapX(xs.get(i), w, pad, minX, maxX);
            float y = mapY(ys.get(i), h, pad, minY, maxY);
            if (!started) { path.moveTo(x, y); started = true; }
            else path.lineTo(x, y);
        }
        canvas.drawPath(path, linePaint);

        // точка для highlightX
        if (Double.isFinite(highlightX)) {
            double sh = Math.sinh(highlightX);
            if (Math.abs(sh) >= 1e-4) {
                double y = Math.cosh(highlightX) / sh;
                if (Double.isFinite(y) && Math.abs(y) <= 10) {
                    float px = mapX(highlightX, w, pad, minX, maxX);
                    float py = mapY(y, h, pad, minY, maxY);
                    canvas.drawCircle(px, py, 10f, pointPaint);
                }
            }
        }
    }

    private float mapX(double x, int w, int pad, double minX, double maxX) {
        return (float) (pad + (x - minX) * (w - 2f * pad) / (maxX - minX));
    }

    private float mapY(double y, int h, int pad, double minY, double maxY) {
        return (float) (h - pad - (y - minY) * (h - 2f * pad) / (maxY - minY));
    }
}
