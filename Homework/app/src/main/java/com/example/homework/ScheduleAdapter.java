package com.example.homework;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<ScheduleDay> mDays;

    public ScheduleAdapter(Context context, ArrayList<ScheduleDay> days){
        mContext = context;
        mDays = days;
    }


    @NonNull
    @Override
    public ScheduleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_layout,parent,false);
        return  new ScheduleAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleAdapter.ViewHolder holder, int position) {
        ScheduleDay model = mDays.get(position);
        holder.dayTitle.setText(model.getDayTitle());
        holder.mSubjects = model.getSubjectArrayList();
        holder.subjectRecyclerView.setLayoutManager(holder.mLayoutManager);
        holder.subjectRecyclerView.setAdapter(holder.mAdapter);
    }

    @Override
    public int getItemCount() {
        return mDays.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView dayTitle;
        private ArrayList<Subject> mSubjects;
        private final RecyclerView subjectRecyclerView;
        RecyclerView.LayoutManager mLayoutManager;
        RecyclerView.Adapter mAdapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTitle = itemView.findViewById(R.id.day_title);
            subjectRecyclerView = itemView.findViewById(R.id.recycler_day_view);
            mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            mAdapter = new SubjectAdapter(mContext, mSubjects);
        }
    }
}
