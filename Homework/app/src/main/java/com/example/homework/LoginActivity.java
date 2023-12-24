package com.example.homework;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class LoginActivity extends AppCompatActivity {
    public static String host = "82.179.140.18";
    public static int port = 45146;
    public static String password;
    public static String login;

    private static final ObjectMapper mapper = new ObjectMapper();

    private EditText loginInput;
    private Button loginBtn;
    private Button backBtn;
    private String response_string;

    private EditText usernameInput, passwordInput;

    private User user;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtn = (Button) findViewById(R.id.login_btn);
        loginInput = (EditText) findViewById(R.id.login_input);
        passwordInput = (EditText) findViewById(R.id.login_password_input);
        backBtn = (Button) findViewById(R.id.back_btn);

        loginBtn.setOnClickListener(view -> LoginAccount());
        backBtn.setOnClickListener((View.OnClickListener) view -> {
            Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(MainIntent);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void LoginAccount() {
        login = loginInput.getText().toString();
        password = passwordInput.getText().toString();

        if(TextUtils.isEmpty(login)){
            Toast.makeText(this, "Введите логин", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] hash = md.digest(password.getBytes());

        password = Base64.getEncoder().encodeToString(hash);
        try {
            login();
        } catch (Exception e) {
            Intent backIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(backIntent);
        }

        Intent ScheduleIntent = new Intent(LoginActivity.this, ScheduleActivity.class);
        ScheduleIntent.putExtra("user_id", user.getUserId());
        ScheduleIntent.putExtra("user_name", user.getUserLogin());
        startActivity(ScheduleIntent);
    }

    private void login() {
        new Thread(() -> {
            InetSocketAddress sa = new InetSocketAddress(host, port);
            try {
                Socket socket = new Socket();
                socket.connect(sa, 5000);
                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());

                out.write("/POST|login@"+login+"@"+password);
                out.flush();

                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                BufferedReader buf = new BufferedReader(in);
                response_string = buf.readLine();
                socket.close();
                runOnUiThread(() -> {
                    Response response;
                    try {
                        response = mapper.readValue(response_string,Response.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    if(response.getType() == "SUCCESS") {
                        try {
                            this.user = mapper.readValue(response.getMessage(), User.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }).start();
    }
}
