package com.example.homework;

import java.util.ArrayList;

public class ScheduleDay {
    private String dayTitle;
    private int weekPosition;
    private ArrayList<Subject> subjectArrayList;

    public ScheduleDay(ArrayList<Subject> subjectArrayList, String title,int weekPosition){

        this.subjectArrayList = subjectArrayList;
        this.dayTitle = title;
        this.weekPosition = weekPosition;
    }

    public String getDayTitle() {
        return dayTitle;
    }

    public void setDayTitle(String dayTitle) {
        this.dayTitle = dayTitle;
    }

    public ArrayList<Subject> getSubjectArrayList() {
        return subjectArrayList;
    }

    public void setSubjectArrayList(ArrayList<Subject> subjectArrayList) {
        this.subjectArrayList = subjectArrayList;
    }

    public void addSubject(Subject currentSubject) {
        subjectArrayList.add(currentSubject);
    }

    public int getWeek_position() {
        return weekPosition;
    }

    public void setWeek_position(int week_position) {
        this.weekPosition = week_position;
    }
}
