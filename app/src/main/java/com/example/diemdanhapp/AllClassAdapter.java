package com.example.diemdanhapp;

import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AllClassAdapter extends RecyclerView.Adapter<AllClassAdapter.ViewHolder> {

    private Context context;
    private List<Class> list;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public AllClassAdapter(Context context, List<Class> list) {
        this.context = context;
        this.list = list;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Class Class = list.get(position);
        holder.textViewClassName.setText(Class.getClassName());
        holder.textViewClassID.setText(Class.getClassID());
        holder.textViewTeacher.setText(Class.getTeacher());
        holder.textViewTime.setText(Class.getStartTime() + " - " + Class.getEndTime());
        holder.textViewDay.setText(Class.getDay());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewClassName, textViewClassID, textViewTeacher, textViewTime, textViewDay;
        Button enrollButton;

        public ViewHolder(@NonNull final View itemView, final OnItemClickListener listener) {
            super(itemView);
            textViewClassName = itemView.findViewById(R.id.textViewClassName);
            textViewClassID = itemView.findViewById(R.id.textViewClassID);
            textViewTeacher = itemView.findViewById(R.id.textViewTeacher);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewDay = itemView.findViewById(R.id.textViewDay);
            enrollButton = itemView.findViewById(R.id.expandButton);
            final CardView cardView = itemView.findViewById(R.id.cardView);
            final ConstraintLayout expandAll = itemView.findViewById(R.id.expandAll);
            expandAll.setVisibility(View.GONE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        final int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (expandAll.getVisibility() == View.GONE) {
                                TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                                expandAll.setVisibility(View.VISIBLE);
                                enrollButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        listener.onItemClick(position);
                                    }
                                });
                            } else {
                                expandAll.setVisibility(View.GONE);
                                TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                            }
                        }
                    }
                }
            });

        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recyclerview_allclass, parent, false);
        return new ViewHolder(v, mListener);
    }


}
