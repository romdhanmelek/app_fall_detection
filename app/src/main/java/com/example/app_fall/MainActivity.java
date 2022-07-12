package com.example.app_fall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationRequest;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.app_fall.ml.FallModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener  {

    private SensorManager sensorManager;

    private LocationRequest locationRequest;
    private Sensor sensor;
    private TextView tv,tv1;
    private Button bt;
    private  float accX;
    private  float accY;
    private  float accZ;
    private static final String TAG = MainActivity.class.getSimpleName();
    float[] values = new float[1203];
    AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private Vibrator vibrate;
    private Switch sw1,sw2,sw3;
    private Timer timer;
    private Handler handler;
    private Activity context;
    private static boolean fall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(this.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this,null);
        }
        else{
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }



        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        sw1 = findViewById(R.id.switch1);
        sw2 = findViewById(R.id.switch2);
        sw3 = findViewById(R.id.switch3);
        bt = findViewById(R.id.button);
        tv1 = findViewById(R.id.txt1);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ContactActivity.class);
                startActivity(intent);
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("Save", MODE_PRIVATE);
        sw1.setChecked(sharedPreferences.getBoolean("value", false));
        sw1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        if (sw1.isChecked()){
                            sensorManager.registerListener(MainActivity.this, sensor,sensorManager.SENSOR_DELAY_NORMAL);

                        }
                        else{
                            sensorManager.unregisterListener(MainActivity.this);
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
                           tv1.setText("vibratorOn");





                        }
                        else{
                            tv1.setText("vibratorOff");

                        }

                    }



                } ) ; }
            else{
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
            }
        }

        MediaPlayer player ;
        player = MediaPlayer.create(this,R.raw.ringtone);
        sw3.setChecked(sharedPreferences.getBoolean("value", false));
        sw3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (sw3.isChecked()){
                    tv1.setText("RingtoneOn");

                }
                else{
                    tv1.setText("RingtoneOff");
                }



            }



        } ) ;



    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        boolean run = true;

        do{
            accX = sensorEvent.values[0];
            accY = sensorEvent.values[1];
            accZ = sensorEvent.values[2];
            Log.d(TAG, accX + " " + accY + " " + accZ);
            for(int i = 0; i<1201; i+=3){
            values[i] =  accX;
            values[i+1] = accY;
            values[i+2] = accZ;}

            RunModel(values);
            break;

        }while(!run);
    }

    private void RunModel(float[] values) {
        try {
            FallModel model = FallModel.newInstance(MainActivity.this);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 401 * 3);

            byteBuffer.order(ByteOrder.nativeOrder());
            for (int j=0; j<1200;j+=3){
                //an error here to fix related to java.nio.BufferOverflowException
                byteBuffer.putFloat(values[j]);
                byteBuffer.putFloat(values[1+j]);
                byteBuffer.putFloat(values[2+j]);
            }
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 401, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            FallModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            float[] output = outputFeature0.getFloatArray();
            model.close();
            TextView t = findViewById(R.id.txt);
            //TextView tx = findViewById(R.id.text);
            if (output[0]>0.3){
                t.setText(String.valueOf(output[0])+" ADL");




            } else {


                t.setText(String.valueOf(output[0])+" Fall");

                AlertSet();
                isAlertset();
                sensorManager.unregisterListener(this);
                startactivity();






            }


        } catch ( IOException e) {
            // TODO Handle the exception
        }
    }




    @SuppressLint("HandlerLeak")
    private void AlertSet() {
        MediaPlayer player ;
        player = MediaPlayer.create(this,R.raw.ringtone);
        //isAlertset();
        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Timer timer = new Timer();
        if (sw2.isChecked()){
            setVibrate();
        }else //vibrate.cancel();
        if (sw3.isChecked()){
            addRingtone(player);
        }else stopRingtone(player);


        builder = new AlertDialog.Builder(this);
        builder.setTitle("FALL DETECTED!");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Call the contact
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED){

                        vibrate.cancel();
                        timer.cancel();
                        stopRingtone(player);
                        getLocation();
                    }
                }


            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                vibrate.cancel();
                timer.cancel();
                stopRingtone(player);

                sensorManager.registerListener(MainActivity.this, sensor,sensorManager.SENSOR_DELAY_NORMAL);


            }
        });
        final AlertDialog alertDialog=builder.create();
        TimerTask timerTask = new TimerTask() {
            int countTime = 45;
            @Override
            public void run() {
                if (countTime > 0){

                    alertDialog.setMessage("Deny or Confirm Fall detection: \n" +
                            "SMS will be sent automatically when timer runs out!!\n"+ countTime );
                    countTime --;


                }else{

                    getLocation();
                    timer.cancel();
                    alertDialog.cancel();
                    vibrate.cancel();
                    stopRingtone(player);

                    //sensorManager.unregisterListener(MainActivity.this);



                }



            }

        };
        //timerTask.cancel();
        timer.schedule(timerTask, 100, 1000);
        alertDialog.show();





    }






    private void getLocation(){

        FusedLocationProviderClient fusedLocationProviderClient ;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){



            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location=task.getResult();
                    if (location!=null){
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                            float latitude= (float) addresses.get(0).getLatitude();
                            float longitude= (float) addresses.get(0).getLongitude();

                            //tv1.setText(String.valueOf(addresses.get(0).getLatitude())+"\n"+String.valueOf(addresses.get(0).getLongitude()));
                            SendSMS(latitude,longitude);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }



    private void countDown() {

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int countTime = 45;
            @Override
            public void run() {
                if (countTime > 0){

                    countTime --;
                }else{
                    //SendSMS();
                }


            }
        };
        timer.schedule(timerTask, 100, 1000);

    }


    private void startactivity(){
        Intent intent = new Intent(MainActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(intent);


    }



    private void SendSMS(float latitude,float longitude) {
        try {
            SmsManager smsManager= SmsManager.getDefault();

            String message ="Fall Detected Location\n"+"http://google.com/maps/?q="+String.valueOf(latitude)+","+String.valueOf(longitude);
            //SmsManager smsManager = SmsManager.getDefault();
            StringBuffer smsBody = new StringBuffer();
            smsBody.append(Uri.parse(message));
            smsManager.sendTextMessage("+21650771430".trim(),null,smsBody.toString(),null,null);
            Toast.makeText(this,"Message is sent!",Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"Fail sending the message!",Toast.LENGTH_SHORT).show();
        }

    }



    public void addRingtone(MediaPlayer player){

            if (player == null){
                player = MediaPlayer.create(this,R.raw.ringtone);
            }
            player.start();




    }

    public void stopRingtone(MediaPlayer player){

            if (player != null){
                player.release();}


            player=null;


    }

    public void setVibrate(){

        long[] pattern = {100, 500, 100, 500};
        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrate.vibrate(pattern, 2);




    }

    public boolean isAlertset(){

        fall=true;
        return fall;


    }






    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}