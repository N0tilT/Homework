package com.example.homework;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Subject {
    @JsonIgnore
    private String subjectTime;
    private String subjectTitle;
    private final int weekPosition;
    private final int dayPosition;
    private final int userId;

    @JsonCreator
    public Subject(@JsonProperty("subject_title") String title, @JsonProperty("subject_day_position") int dayPosition, @JsonProperty("subject_week_position") int weekPosition,@JsonProperty("user_id") int userId){
        this.subjectTitle = title;
        this.weekPosition = weekPosition;
        this.dayPosition = dayPosition;
        this.userId = userId;

        switch (dayPosition){

            case 0:
                subjectTime = "8.30";
                break;
            case 1:
                subjectTime = "10.15";
                break;
            case 2:
                subjectTime = "12.15";
                break;
            case 3:
                subjectTime = "14.00";
                break;
            case 4:
                subjectTime = "15.45";
                break;
            case 5:
                subjectTime = "17.30";
                break;
            default:
                subjectTime = "00-00";
                break;
        }
    }

    @JsonGetter("subject_title")
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
    @JsonGetter("subject_week_position")

    public int getWeekPosition() {
        return weekPosition;
    }

    @JsonGetter("subject_day_position")
    public int getDayPosition() {
        return dayPosition;
    }

    public int getUserId() {
        return userId;
    }
}
