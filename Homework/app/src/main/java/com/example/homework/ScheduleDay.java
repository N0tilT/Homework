package com.example.homework;

import java.util.ArrayList;

public class ScheduleDay {
    private String dayTitle;
    private ArrayList<Subject> subjectArrayList;

    public ScheduleDay(ArrayList<Subject> subjectArrayList, String title){

        this.subjectArrayList = subjectArrayList;
        this.dayTitle = title;
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
}
