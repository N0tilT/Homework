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
import com.fasterxml.jackson.databind.DeserializationFeature;
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

        mSubjects = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_schedule_view);
        backBtn = (Button) findViewById(R.id.back_button);
        homeworkBtn = (Button) findViewById(R.id.homework_button);

        scheduleDayArrayList = new ArrayList<>();

        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id",-1);
        userName = intent.getStringExtra("user_name");

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        GetSchedule(userId);
        homeworkBtn.setOnClickListener((View.OnClickListener) view -> {

            Intent HomeworkIntent = new Intent(ScheduleActivity.this, HomeworkActivity.class);
            HomeworkIntent.putExtra("user_id",userId);
            HomeworkIntent.putExtra("user_name",userName);
            startActivity(HomeworkIntent);
        });
        backBtn.setOnClickListener((View.OnClickListener) view -> {
            Intent MainIntent = new Intent(ScheduleActivity.this, MainActivity.class);
            startActivity(MainIntent);
        });
    }

    private void GetSchedule(int userId) {
        new Thread(() -> {
            InetSocketAddress sa = new InetSocketAddress(LoginActivity.host, LoginActivity.port);
            try {
                Socket socket = new Socket();
                socket.connect(sa, 5000);
                socket.setReceiveBufferSize(4096);

                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());

                out.write("OBOBUS137");
                out.flush();

                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                BufferedReader buf = new BufferedReader(in);
                String response_string = buf.readLine();
                if(!response_string.equalsIgnoreCase("Welcome to the server")){
                    socket.close();
                    throw new Exception();
                }

                out = new OutputStreamWriter(socket.getOutputStream());


                out.write("/GET|schedule@"+this.userId);
                out.flush();
                in = new InputStreamReader(socket.getInputStream());
                buf = new BufferedReader(in);
                StringBuilder tmp = new StringBuilder();
                response_string = buf.readLine();
                tmp.append(response_string);
                socket.close();
                StringBuilder builder = new StringBuilder();
                int parenthesesCounter = 0;
                for (int i = 0; i < tmp.length(); i++) {
                    if(tmp.charAt(i) == '{'){
                        parenthesesCounter++;
                    }
                    if(tmp.charAt(i) == '}'){
                        parenthesesCounter--;
                    }
                    if(parenthesesCounter>1) {
                        if (tmp.charAt(i) == '\"')
                        {
                            builder.append('\\');
                        }
                    }
                    builder.append(tmp.charAt(i));
                }
                String finalResponse_string = builder.toString();
                runOnUiThread(() -> {
                    Response response;
                    try {
                        response = mapper.readValue(finalResponse_string,Response.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    if(response.getType().equals("SUCCESS")) {
                        try {
                            if(!response.getMessage().equals("Not found")){
                                if(response.getMessage().contains("[") || response.getMessage().contains("]")){
                                    this.mSubjects = mapper.readValue(response.getMessage(), new TypeReference<ArrayList<Subject>>() {});
                                }
                                else{
                                    this.mSubjects.add(mapper.readValue(response.getMessage(), new TypeReference<Subject>() {}));
                                }

                            }

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
                            scheduleDayArrayList.add(new ScheduleDay(new ArrayList<Subject>(),dayTitles.get(currentSubject.getWeekPosition()), currentSubject.getWeekPosition()));
                            flag = true;
                        }
                        scheduleDayArrayList.get(currentSubject.getWeekPosition()).addSubject(currentSubject);
                        currentDay = currentSubject.getWeekPosition();
                    }

                    mRecyclerView.setHasFixedSize(true);
                    mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                    mAdapter = new ScheduleAdapter(this, scheduleDayArrayList);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mRecyclerView.setAdapter(mAdapter);
                });
                socket.close();

            } catch (Exception ex) {
                ex.printStackTrace();

            }

        }).start();
    }
}
