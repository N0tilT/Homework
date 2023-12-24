package com.example.homework;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder>{

    private final Context mContext;
    private final ArrayList<Subject> mSubjects;
    public SubjectAdapter(Context mContext, ArrayList<Subject> mSubjects) {
        this.mContext = mContext;
        this.mSubjects = mSubjects;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_layout,parent,false);
        return  new SubjectAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject model = mSubjects.get(position);
        holder.subjectTitle.setText(model.getSubjectTitle());
        holder.subjectTime.setText(model.getSubjectTime());
    }

    @Override
    public int getItemCount() {
        return mSubjects.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView subjectTitle;
        private final TextView subjectTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTitle = itemView.findViewById(R.id.subject_title);
            subjectTime = itemView.findViewById(R.id.subject_time);
        }
    }
}
