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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class RegisterActivity extends AppCompatActivity {

    public static String host = "82.179.140.18";
    public static int port = 45125;
    public static String password;
    public static String hashpassword;
    public static String login;
    public static String username;

    private static final ObjectMapper mapper = new ObjectMapper();
    private Button loginButton;
    private EditText loginInput;
    private Button registerBtn;
    private Button backBtn;

    private EditText usernameInput, passwordInput;
    private User user;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerBtn =(Button) findViewById(R.id.register_btn);
        loginInput =(EditText) findViewById(R.id.register_login_input);

        passwordInput =(EditText) findViewById(R.id.register_password_input);
        backBtn = (Button) findViewById(R.id.back_btn);

        registerBtn.setOnClickListener(view -> CreateAccount());
        backBtn.setOnClickListener((View.OnClickListener) view -> {
            Intent MainIntent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(MainIntent);
        });

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void CreateAccount() {
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
            register();
            Intent ScheduleIntent = new Intent(RegisterActivity.this, ScheduleActivity.class);
            ScheduleIntent.putExtra("user_id", user.getUserId());
            ScheduleIntent.putExtra("user_name", user.getUserLogin());
            startActivity(ScheduleIntent);
        } catch (Exception e) {
            Intent backIntent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(backIntent);
        }

    }

    private void register() {
        new Thread(() -> {
            InetSocketAddress sa = new InetSocketAddress(host, port);
            try {
                Socket socket = new Socket();
                socket.connect(sa, 5000);
                socket.setReceiveBufferSize(512);

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

                //send login and password in correct format
                out.write("/POST|register@"+login+"@"+password);
                out.flush();

                in = new InputStreamReader(socket.getInputStream());
                buf = new BufferedReader(in);
                StringBuilder tmp = new StringBuilder();

                while(!response_string.equals("}")){

                    response_string = buf.readLine();
                    tmp.append(response_string);
                }
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
                    if(parenthesesCounter>1){
                        if (tmp.charAt(i) == '\"')
                        {
                            builder.append('\\');
                        }
                    }
                    builder.append(tmp.charAt(i));

                }
                String finalResponse_string = tmp.toString();
                runOnUiThread(() -> {
                    Response response;
                    try {
                        response = mapper.readValue(finalResponse_string,Response.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    if(response.getType().equals("SUCCESS")) {
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
