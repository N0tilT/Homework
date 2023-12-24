package com.example.homework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class ScheduleActivity  extends AppCompatActivity {

    ArrayList<ScheduleDay> scheduleDayArrayList;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    ArrayList<Subject> mSubjects;

    private static final ObjectMapper mapper = new ObjectMapper();
    Button backBtn;
    Button homeworkBtn;

    private int userId;
    private String userName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_schedule_view);
        backBtn = (Button) findViewById(R.id.back_button);
        homeworkBtn = (Button) findViewById(R.id.homework_button);

        scheduleDayArrayList = new ArrayList<>();

        Intent intent = getIntent();
        userId = Integer.parseInt(intent.getStringExtra("user_id"));
        userName = intent.getStringExtra("user_name");

        GetSchedule(userId);
        homeworkBtn.setOnClickListener((View.OnClickListener) view -> {
            String tmp = "";
            for (int i = 0; i < scheduleDayArrayList.size(); i++) {
                for (int j = 0; j < scheduleDayArrayList.get(i).getSubjectArrayList().size(); j++) {
                    String[] tmpsplitted = tmp.split("\t");
                    int subject = scheduleDayArrayList.get(i).getSubjectArrayList().get(j).getSubjectId();
                    if(!Arrays.asList(tmpsplitted).contains(subject)){
                        tmp += subject + "\t";
                    }
                }
            }
            Intent HomeworkIntent = new Intent(ScheduleActivity.this, HomeworkActivity.class);
            HomeworkIntent.putExtra("user_id",userId);
            HomeworkIntent.putExtra("user_name",userName);
            HomeworkIntent.putExtra("schedule",tmp);
            startActivity(HomeworkIntent);
        });
        backBtn.setOnClickListener((View.OnClickListener) view -> {
            Intent MainIntent = new Intent(ScheduleActivity.this, MainActivity.class);
            startActivity(MainIntent);
        });

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAdapter = new ScheduleAdapter(this, scheduleDayArrayList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void GetSchedule(int userId) {
        new Thread(() -> {
            InetSocketAddress sa = new InetSocketAddress(LoginActivity.host, LoginActivity.port);
            try {
                Socket socket = new Socket();
                socket.connect(sa, 5000);
                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());


                out.write("/GET|schedule@"+userId);
                out.flush();
                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                BufferedReader buf = new BufferedReader(in);
                String response_string = buf.readLine();
                runOnUiThread(() -> {
                    Response response;
                    try {
                        response = mapper.readValue(response_string,Response.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    if(response.getType() == "SUCCESS") {
                        try {
                            this.mSubjects = mapper.readValue(response.getMessage(), new TypeReference<ArrayList<Subject>>() {});
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    ArrayList<String> dayTitles = new ArrayList<String>();

                    dayTitles.add("Понедельник");
                    dayTitles.add("Вторник");
                    dayTitles.add("Среда");
                    dayTitles.add("Четверг");
                    dayTitles.add("Пятница");
                    dayTitles.add("Суббота");
                    dayTitles.add("Воскресенье");

                    boolean flag = false;
                    int currentDay = 0;
                    for (int i = 0; i < mSubjects.size(); i++) {
                        Subject currentSubject = mSubjects.get(i);
                        if(currentDay != currentSubject.getWeekPosition()){
                            flag = false;
                        }
                        if(!flag){
                            scheduleDayArrayList.add(new ScheduleDay(new ArrayList<Subject>(),dayTitles.get(currentSubject.getWeekPosition())));
                            flag = true;
                        }
                        scheduleDayArrayList.get(currentSubject.getWeekPosition()).addSubject(currentSubject);
                        currentDay = currentSubject.getWeekPosition();
                    }

                });
                socket.close();

            } catch (Exception ex) {
                ex.printStackTrace();

            }

        }).start();
    }
}
