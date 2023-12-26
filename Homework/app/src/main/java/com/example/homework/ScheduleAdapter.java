package com.example.homework;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        holder.week_position = model.getWeek_position();
        holder.mAdapter = new SubjectAdapter(mContext, holder.mSubjects);
        holder.subjectRecyclerView.setLayoutManager(holder.mLayoutManager);
        holder.subjectRecyclerView.setAdapter(holder.mAdapter);

        holder.addSubjectButton.setOnClickListener((View.OnClickListener) view -> {
            Intent addSubjectntent = new Intent(mContext, AddSubjectActivity.class);
            addSubjectntent.putExtra("week_position",holder.week_position);
            addSubjectntent.putExtra("day_title",holder.dayTitle.getText());
            addSubjectntent.putExtra("user_id",model.getSubjectArrayList().get(0).getUserId());
            mContext.startActivity(addSubjectntent);
        });
    }

    @Override
    public int getItemCount() {
        return mDays.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView dayTitle;
        private int week_position = 0;
        private final Button addSubjectButton;
        private ArrayList<Subject> mSubjects = new ArrayList<>();
        private final RecyclerView subjectRecyclerView;
        RecyclerView.LayoutManager mLayoutManager;
        RecyclerView.Adapter mAdapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTitle = itemView.findViewById(R.id.day_title);
            addSubjectButton = itemView.findViewById(R.id.ad_subject_button);
            subjectRecyclerView = itemView.findViewById(R.id.recycler_day_view);
            mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        }
    }
}
