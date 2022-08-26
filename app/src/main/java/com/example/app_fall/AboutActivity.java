package com.example.app_fall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;

public class AboutActivity extends AppCompatActivity {


    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.purple2)));

        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.about);


        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.action_contact:
                    Intent intent = new Intent(AboutActivity.this,ContactActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    return true;

                case R.id.home:
                    Intent intent2 = new Intent(AboutActivity.this,MainActivity.class);
                    startActivity(intent2);
                    overridePendingTransition(0,0);
                    return true;

                case R.id.setting1:
                    Intent intent4 = new Intent(AboutActivity.this,SettingsActivity.class);

                    startActivity(intent4);

                    overridePendingTransition(0,0);
                    return true;
            }

            return super.onOptionsItemSelected(item);

        });


    }
}