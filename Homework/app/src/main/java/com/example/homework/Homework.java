package com.example.homework;

public class Homework {
    private String homework_title;
    private String homework_description;
    private int subject_id;

    public Homework(String homework_titlte, String homework_description, int subject_id){
        this.homework_title = homework_titlte;
        this.homework_description = homework_description;
        this.subject_id = subject_id;
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

    public int getSubject_id() {
        return subject_id;
    }

    public void setSubject_id(int subject_id) {
        this.subject_id = subject_id;
    }
}
