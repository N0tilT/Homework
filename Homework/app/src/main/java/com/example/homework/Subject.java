package com.example.homework;

public class Subject {
    private int subjectId;
    private String subjectTime;
    private String subjectTitle;
    private final int weekPosition;
    private final int dayPosition;

    public Subject(int subjectId, String title, String time, int weekPosition, int dayPosition){
        this.subjectId = subjectId;
        subjectTitle = title;
        subjectTime = time;
        this.weekPosition = weekPosition;
        this.dayPosition = dayPosition;
    }

    public String getSubjectTitle() {
        return subjectTitle;
    }

    public void setSubjectTitle(String subjectTitle) {
        this.subjectTitle = subjectTitle;
    }

    public String getSubjectTime() {
        return subjectTime;
    }

    public void setSubjectTime(String subjectTime) {
        this.subjectTime = subjectTime;
    }

    public int getWeekPosition() {
        return weekPosition;
    }

    public int getDayPosition() {
        return dayPosition;
    }

    public int getSubjectId() {
        return subjectId;
    }
}
