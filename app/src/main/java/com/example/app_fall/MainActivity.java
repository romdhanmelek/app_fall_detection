package com.example.app_fall;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_fall.ml.FallDetectionModel;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.internal.location.zzz;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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
    private Switch sw;
    private Timer timer;
    private Handler handler;
    private Activity context;


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


        /* locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000); */

        //FusedLocationProviderClient fusedLocationProviderClient ;
        //fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        sw = findViewById(R.id.switch1);
        bt = findViewById(R.id.button);
        tv1 = findViewById(R.id.txt1);
        SharedPreferences sharedPreferences = getSharedPreferences("Save", MODE_PRIVATE);
        sw.setChecked(sharedPreferences.getBoolean("value", false));
        sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        if (sw.isChecked()){
                            sensorManager.registerListener(MainActivity.this, sensor,sensorManager.SENSOR_DELAY_NORMAL);

                        }
                        else{
                            sensorManager.unregisterListener(MainActivity.this);
                        }

                    }

                } else {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
                }

            } }) ;



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
            values[i] = (float) (accX*26.12);
            values[i+1] = (float) (accY*26.12);
            values[2 + i] = (float) (accZ*26.12);}

            RunModel(values);
            break;

        }while(!run);
    }

    private void RunModel(float[] values) {
        try {
            FallDetectionModel model = FallDetectionModel.newInstance(MainActivity.this);
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
            FallDetectionModel.Outputs outputs = model.process(inputFeature0);
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
                sensorManager.unregisterListener(this);





            }


        } catch ( IOException e) {
            // TODO Handle the exception
        }
    }


    @SuppressLint("HandlerLeak")
    private void AlertSet() {


        builder = new AlertDialog.Builder(this);
        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Timer timer = new Timer();


        long[] pattern ={100,500,100,500};
        vibrate.vibrate(pattern,2);
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
                sensorManager.registerListener(MainActivity.this, sensor,sensorManager.SENSOR_DELAY_NORMAL);


            }
        });
        final AlertDialog alertDialog=builder.create();



        TimerTask timerTask = new TimerTask() {
            int countTime = 20;
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

                    //sensorManager.unregisterListener(MainActivity.this);



                }



            }

        };
        //timerTask.cancel();
        timer.schedule(timerTask, 100, 1000);





        //countDown();

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
            int countTime = 30;
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


    private void startActivity(){
        Intent intent = new Intent(MainActivity.this,MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);


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


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}