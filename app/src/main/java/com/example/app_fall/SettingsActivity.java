package com.example.app_fall;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    public  BottomNavigationView bottomNavigationView;
    private static Switch sw1;
    private static Switch sw2;
    private static Switch sw3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.setting1);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.action_contact:
                    Intent intent = new Intent(SettingsActivity.this,ContactActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    return true;

                case R.id.home:
                    Intent intent2 = new Intent(SettingsActivity.this,MainActivity.class);
                    startActivity(intent2);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.about:
                    Intent intent3 = new Intent(SettingsActivity.this,AboutActivity.class);
                    startActivity(intent3);
                    overridePendingTransition(0,0);
                    return true;

            }

            return super.onOptionsItemSelected(item);

        });

        sw1 = findViewById(R.id.switch1);
        sw2 = findViewById(R.id.switch2);
        sw3 = findViewById(R.id.switch3);
        SharedPreferences sharedPreferences = getSharedPreferences("Save", MODE_PRIVATE);
        sw1.setChecked(sharedPreferences.getBoolean("value", false));
        sw1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        if (sw1.isChecked()){
                            Toast.makeText(SettingsActivity.this,"Sensor On",Toast.LENGTH_SHORT).show();

                        }
                        else{
                            Toast.makeText(SettingsActivity.this,"Sensor Off",Toast.LENGTH_SHORT).show();

                        }

                    }

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
                    }
                }

            } }) ;


        sw2.setChecked(sharedPreferences.getBoolean("value", false));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                sw2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (sw2.isChecked()){

                            Toast.makeText(SettingsActivity.this," On",Toast.LENGTH_SHORT).show();




                        }
                        else{
                            Toast.makeText(SettingsActivity.this,"Sensor Off",Toast.LENGTH_SHORT).show();

                        }

                    }



                } ) ; }
            else{
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
            }
        }


        sw3.setChecked(sharedPreferences.getBoolean("value", false));
        sw3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (sw3.isChecked()){
                    Toast.makeText(SettingsActivity.this,"Sensor On",Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(SettingsActivity.this,"Sensor Off",Toast.LENGTH_SHORT).show();
                }



            }



        } ) ;
    }

    public static Boolean sw1checked(){
        if (sw1.isChecked()){
            return true;
        }else {
            return false;
        }
    }

    public static Boolean sw2checked(){
        if (sw2.isChecked()){
            return true;
        }else {
            return false;
        }
    }

    public static Boolean sw3checked(){
        if (sw3.isChecked()){
            return true;
        }else {
            return false;
        }
    }
}