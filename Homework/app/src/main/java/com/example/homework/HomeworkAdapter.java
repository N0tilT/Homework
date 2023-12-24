package com.example.homework;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeworkAdapter extends RecyclerView.Adapter<HomeworkAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Homework> mHomeworks;

    public HomeworkAdapter(Context context, ArrayList<Homework> homeworks){
        mContext = context;
        mHomeworks = homeworks;
    }

    @NonNull
    @Override
    public HomeworkAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.homework_layout,parent,false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeworkAdapter.ViewHolder holder, int position) {
        Homework model = mHomeworks.get(position);
        holder.homeworkTitle.setText(model.getHomework_title());
        holder.homeworkDescription.setText(model.getHomework_description());
    }

    @Override
    public int getItemCount() {
        return mHomeworks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView homeworkTitle;
        private final TextView homeworkDescription;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            homeworkTitle = itemView.findViewById(R.id.homework_title);
            homeworkDescription = itemView.findViewById(R.id.homework_description);
        }
    }
}
