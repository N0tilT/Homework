package com.example.homework;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Homework {
    private String homework_title;
    private String homework_description;
    private int userId;

    @JsonCreator
    public Homework(@JsonProperty("user_id") int user_id,@JsonProperty("homework_title") String homework_title,@JsonProperty("homework_description") String homework_description){
        this.userId = user_id;
        this.homework_title = homework_title;
        this.homework_description = homework_description;
    }

    public String getHomework_title() {
        return homework_title;
    }

    public void setHomework_title(String homeworkTitlte) {
        this.homework_title = homeworkTitlte;
    }

    public String getHomework_description() {
        return homework_description;
    }

    public void setHomework_description(String homeworkDescription) {
        this.homework_description = homeworkDescription;
    }
}
