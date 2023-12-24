package com.example.homework;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class HomeworkActivity extends AppCompatActivity {

    ArrayList<Homework> homeworkArrayList;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;

    private static final ObjectMapper mapper = new ObjectMapper();
    private int userId;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_homework_view);

        homeworkArrayList = new ArrayList<>();
        Intent intent = getIntent();
        String[] tmp = intent.getStringExtra("schedule").split("\t");
        for (int i = 0; i < tmp.length; i++) {
            GetHomework(Integer.parseInt(tmp[i]));
        }

        userId = Integer.parseInt(intent.getStringExtra("user_id"));
        userName = intent.getStringExtra("user_name");

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAdapter = new HomeworkAdapter(this, homeworkArrayList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void GetHomework(int subjectId) {
        new Thread(() -> {
            InetSocketAddress sa = new InetSocketAddress(LoginActivity.host, LoginActivity.port);
            try {
                Socket socket = new Socket();
                socket.connect(sa, 5000);
                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());


                out.write("/GET|homework@"+subjectId);
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
                            this.homeworkArrayList = mapper.readValue(response.getMessage(), new TypeReference<ArrayList<Homework>>() {});
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                });
                socket.close();

            } catch (Exception ex) {
                ex.printStackTrace();

            }

        }).start();
    }
}
