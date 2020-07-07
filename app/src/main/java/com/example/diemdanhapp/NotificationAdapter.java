package com.example.diemdanhapp;

import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private List<Notification> list;


    public NotificationAdapter(Context context, List<Notification> list) {
        this.context = context;
        this.list = list;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = list.get(position);
        holder.textViewClassID.setText(notification.getClassID());
        holder.textViewTime.setText(notification.getTime());
        holder.textViewWeek.setText(notification.getBuoi());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewClassID, textViewTime, textViewWeek;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            textViewClassID = itemView.findViewById(R.id.textViewClassID);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewWeek = itemView.findViewById(R.id.textViewWeek);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.recyclerview_notification, parent, false);
        return new ViewHolder(v);
    }

}
