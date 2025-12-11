package com.example.lab9cth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    static class VH extends RecyclerView.ViewHolder {
        final TextView label;
        final TextView value;
        VH(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.tvHistoryLabel);
            value = itemView.findViewById(R.id.tvHistoryValue);
        }
    }
}
