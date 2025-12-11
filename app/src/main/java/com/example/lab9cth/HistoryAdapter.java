package com.example.lab9cth;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    private final ArrayList<String> data = new ArrayList<>();

    public HistoryAdapter(List<String> items) {
        setItems(items);
    }

    public void setItems(List<String> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.label.setText("Запись " + (position + 1));
        holder.value.setText(data.get(position));

        View.OnClickListener openGraph = v -> {
            double x = parseX(data.get(position));
            if (!Double.isFinite(x)) {
                Toast.makeText(v.getContext(), "Не могу определить x для графика", Toast.LENGTH_SHORT).show();
                return;
            }
            Context ctx = v.getContext();
            Intent intent = new Intent(ctx, GraphActivity.class);
            intent.putExtra("x", x);
            ctx.startActivity(intent);
        };

        holder.graphButton.setOnClickListener(openGraph);
        holder.itemView.setOnClickListener(openGraph);

        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(24f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(240)
                .setStartDelay(Math.min(position * 18L, 120))
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private double parseX(String line) {
        if (line == null) return Double.NaN;
        int start = line.indexOf("cth(");
        int end = line.indexOf(')', start + 4);
        if (start < 0 || end < 0 || end <= start + 4) return Double.NaN;
        try {
            String raw = line.substring(start + 4, end).trim().replace(',', '.');
            return Double.parseDouble(raw);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView label;
        final TextView value;
        final MaterialButton graphButton;
        VH(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.tvHistoryLabel);
            value = itemView.findViewById(R.id.tvHistoryValue);
            graphButton = itemView.findViewById(R.id.btnHistoryGraph);
        }
    }
}
