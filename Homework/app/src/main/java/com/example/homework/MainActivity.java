package com.example.homework;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (Button) findViewById(R.id.main_login_btn);
        registerButton = (Button) findViewById(R.id.main_register_btn);


        loginButton.setOnClickListener(view -> {
            Intent loginIntent=new Intent(this,LoginActivity.class);
            startActivity(loginIntent);
        });

        registerButton.setOnClickListener(view -> {
            Intent registerIntent=new Intent(this,RegisterActivity.class);
            startActivity(registerIntent);
        });

    }
}