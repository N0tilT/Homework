package com.example.homework;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class AddSubjectActivity extends AppCompatActivity {

    private Button backButton;
    private Button addSubjectButton;
    private TextView dayTitle;
    private TextView subjectTitle;
    private TextView timePicked;
    private EditText editSubjectTitle;
    private NumberPicker numberPicker;
    private int weekposition = 0;
    private int dayposition = 0;
    private String subject_title;

    private static final ObjectMapper mapper = new ObjectMapper();
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);

        backButton = findViewById(R.id.back_button);
        addSubjectButton = findViewById(R.id.add_subject);
        subjectTitle = findViewById(R.id.subject_title);
        timePicked = findViewById(R.id.time_picked);
        dayTitle = findViewById(R.id.day_title);
        numberPicker = findViewById(R.id.number_picker_time);
        editSubjectTitle = findViewById(R.id.subject_title_editor);

        Intent intent = getIntent();
        dayTitle.setText(intent.getStringExtra("day_title"));
        weekposition=intent.getIntExtra("week_position",-1);
        userId = intent.getIntExtra("user_id",-1);

        timePicked.setText("8.30-10.05");

        numberPicker.setMaxValue(6);
        numberPicker.setMinValue(1);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            public void onValueChange(NumberPicker picker, int oldVal,
                                      int newVal)
            {
                dayposition=newVal-1;
                switch (newVal){
                    case 1:
                        timePicked.setText("8.30-10.05");
                        break;
                    case 2:
                        timePicked.setText("10.15-11.50");
                        break;
                    case 3:
                        timePicked.setText("12.15-13.50");
                        break;
                    case 4:
                        timePicked.setText("14.00-15.35");
                        break;
                    case 5:
                        timePicked.setText("15.45-17.20");
                        break;
                    case 6:
                        timePicked.setText("17.30-19.05");
                        break;
                    default:
                        break;
                }
            }
        });


        backButton.setOnClickListener((View.OnClickListener) view -> {
            Intent MainIntent = new Intent(AddSubjectActivity.this, ScheduleActivity.class);
            MainIntent.putExtra("user_id", userId);
            startActivity(MainIntent);
        });

        addSubjectButton.setOnClickListener(view -> AddSubject());
    }

    private void AddSubject() {
        subject_title = editSubjectTitle.getText().toString();

        if(TextUtils.isEmpty(subject_title)){
            Toast.makeText(this, "Введите название дисциплины", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Add(subject_title,weekposition,dayposition,userId);
        } catch (Exception e) {
            Intent backIntent = new Intent(this, ScheduleActivity.class);
            startActivity(backIntent);
        }

    }

    private void Add(String title,int weekposition,int dayposition, int userId) {
        new Thread(() -> {
            InetSocketAddress sa = new InetSocketAddress(LoginActivity.host, LoginActivity.port);
            try {
                Socket socket = new Socket();
                socket.connect(sa, 5000);
                socket.setReceiveBufferSize(4096);

                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                BufferedReader buf = new BufferedReader(in);
                String response_string = buf.readLine();
                if(!response_string.equalsIgnoreCase("Welcome to the server")){
                    socket.close();
                    throw new Exception();
                }

                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());

                Subject subjectToAdd = new Subject(title,dayposition,weekposition, userId);
                String serialized = mapper.writeValueAsString(subjectToAdd);

                StringBuilder tmp = new StringBuilder();
                tmp.append('{');
                for (int i = 1; i < serialized.length()-1; i++) {
                    if(serialized.charAt(i) == ':' && serialized.charAt(i) != '\"'){
                        tmp.append(serialized.charAt(i));
                        tmp.append('\"');
                    }
                    else if(serialized.charAt(i) == ',' && serialized.charAt(i-1) != '\"'){
                        tmp.append('\"');
                        tmp.append(serialized.charAt(i));
                    }
                    else{
                        tmp.append(serialized.charAt(i));
                    }
                }
                if(serialized.charAt(serialized.length()-1)!='\"'){
                    tmp.append('\"');
                }
                tmp.append('}');
                String stringify = tmp.toString();
                out.write("/POST|schedule@"+stringify);
                out.flush();

                in = new InputStreamReader(socket.getInputStream());
                buf = new BufferedReader(in);
                tmp = new StringBuilder();
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
                        Toast.makeText(this,"Предмет успешно добавлен в расписание!", Toast.LENGTH_SHORT);
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }).start();
    }
}