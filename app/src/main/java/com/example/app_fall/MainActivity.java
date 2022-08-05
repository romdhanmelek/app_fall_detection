package com.example.app_fall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;


import android.graphics.Color;
import android.os.Bundle;



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
import android.os.SystemClock;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.app_fall.ml.FallModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SleepClassifyEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.database.Cursor;

public class MainActivity extends AppCompatActivity implements SensorEventListener  {

    private SensorManager sensorManager;
    public Adapter mAdapter;
    private LocationRequest locationRequest;
    private Sensor sensor;
    private TextView tv,tv1,tv2;
    private Button bt, bt1;
    private  float accX;
    private  float accY;
    private  float accZ;
    private static final String TAG = MainActivity.class.getSimpleName();
    float[] values = new float[1203];
    AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private Vibrator vibrate;
    private GraphView graph;

    private Timer timer;
    private Handler handler;
    private int count=1;
    private int i=0;
    private Viewport viewport;

    private Dbhelper Dbhelper;

    private BottomNavigationView bottomNavigationView;
    LineGraphSeries<DataPoint> seriesX = new LineGraphSeries<DataPoint>(new DataPoint[] {
            new DataPoint(0, 0),


    });
    LineGraphSeries<DataPoint> seriesY = new LineGraphSeries<DataPoint>(new DataPoint[] {
            new DataPoint(0, 0),


    });
    LineGraphSeries<DataPoint> seriesZ = new LineGraphSeries<DataPoint>(new DataPoint[] {
            new DataPoint(0, 0),


    });


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



        bt = findViewById(R.id.button);
        bt1 = findViewById(R.id.button_call);
        tv1 = findViewById(R.id.txt1);
        tv2 = findViewById(R.id.txt4);
        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);





        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.action_contact:
                    Intent intent = new Intent(MainActivity.this,ContactActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    return true;

                case R.id.about:
                    Intent intent3 = new Intent(MainActivity.this,AboutActivity.class);
                    startActivity(intent3);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.setting1:
                    Intent intent4 = new Intent(MainActivity.this,SettingsActivity.class);

                    startActivity(intent4);

                    overridePendingTransition(0,0);
                    return true;
            }

            return super.onOptionsItemSelected(item);

        });


        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                }else{
                    callEmergence();
                }


            }
        });





        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SettingsActivity.sw1checked()){
                    sensorManager.registerListener(MainActivity.this, sensor,SensorManager.SENSOR_DELAY_NORMAL);
                    Toast.makeText(MainActivity.this,"Sensor On",Toast.LENGTH_SHORT).show();

                }
                else{
                    sensorManager.unregisterListener(MainActivity.this);
                }
            }
        });



        /*SettingsActivity = new SettingsActivity(this);
        if (SettingsActivity.sw1checked()){
            sensorManager.registerListener(MainActivity.this, sensor,sensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this,"Sensor On",Toast.LENGTH_SHORT).show();

        }
        else{
            sensorManager.unregisterListener(MainActivity.this);
        }*/

        MediaPlayer player ;
        player = MediaPlayer.create(this,R.raw.ringtone);








    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

       try {
            accX = sensorEvent.values[0];
            accY = sensorEvent.values[1];
            accZ = sensorEvent.values[2];
            Thread.currentThread().sleep(20);

            count ++;
            if (count >1000){
                count = 1;
                seriesX.resetData(new DataPoint[]{new DataPoint(1,0)});
                seriesY.resetData(new DataPoint[]{new DataPoint(1,0)});
                seriesZ.resetData(new DataPoint[]{new DataPoint(1,0)});
            }
            GraphView graph = (GraphView) findViewById(R.id.graph);
            graph.setVisibility(View.VISIBLE);
            seriesX.appendData(new DataPoint(count, accX),true,count);
            seriesY.appendData(new DataPoint(count, accY),true,count);
            seriesZ.appendData(new DataPoint(count, accZ),true,count);
            seriesY.setColor(Color.GREEN);
            seriesZ.setColor(Color.RED);
            seriesX.setTitle("X axe");
            seriesY.setTitle("Y axe");
            seriesZ.setTitle("Z axe");
            viewport = graph.getViewport();
            viewport.setScrollable(true);
            viewport.setXAxisBoundsManual(true);
            viewport.setMaxX(count);

            viewport.setMinX(count-100);
            graph.addSeries(seriesX);
            graph.addSeries(seriesY);
            graph.addSeries(seriesZ);



        } catch (InterruptedException e) {
            e.printStackTrace();
        }




       if (i>1203){
           i=0;

           for(int j=0;i<1203;j++)
               values[j]=0;
       }
        values[i] = accX;
        values[i + 1] = accY;
        values[i + 2] = accZ;
        i+=3;
        RunModel(values);




               //i+=3;





            //}


            //break;




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
            if (output[0]<0.6){

                t.setText(String.valueOf(output[0])+" Fall");

                AlertSet();

                sensorManager.unregisterListener(this);
                startactivity();



            } else {
                t.setText(String.valueOf(output[0])+" ADL");






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
        if (SettingsActivity.sw2checked()==true){
            setVibrate();
        }else vibrate.cancel();
        if (SettingsActivity.sw3checked()==true){
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
                vibrate.cancel();
                timer.cancel();
                stopRingtone(player);
                dialogInterface.cancel();


                sensorManager.registerListener(MainActivity.this, sensor,SensorManager.SENSOR_DELAY_NORMAL);


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

                    sensorManager.unregisterListener(MainActivity.this);



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



    /*private void countDown() {

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

    }*/


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

            StringBuffer smsBody = new StringBuffer();
            smsBody.append(Uri.parse(message));

            mAdapter = new Adapter(this, null);

            Dbhelper = new Dbhelper(this);
            Cursor cursor= Dbhelper.getphone();
            StringBuilder phone = new StringBuilder();


            Cursor cursor1=Dbhelper.dbEmpty();
            StringBuilder table_empty = new StringBuilder();
            while (cursor1.moveToNext())
            {
                table_empty.append(cursor1.getInt(0));
            }
            int size=Integer.valueOf(String.valueOf(table_empty));
            if (size == 0){
                Toast.makeText(this,"Put CareTaker First!",Toast.LENGTH_SHORT).show();
            }
            else {
                for (int i=0;i<size+1;i++){
                    while (cursor.moveToNext())
                    {
                        phone.append(cursor.getInt(i));
                        smsManager.sendTextMessage("+216"+String.valueOf(phone).trim(),null,smsBody.toString(),null,null);
                        Toast.makeText(this,"Message is sent!",Toast.LENGTH_SHORT).show();
                        phone.delete(0,cursor.getInt(i));

                    }


                }
            }









        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"Fail sending the message!",Toast.LENGTH_SHORT).show();
        }

    }

    public void callEmergence(){

        mAdapter = new Adapter(this, null);

        Dbhelper = new Dbhelper(this);
        Cursor cursor= Dbhelper.getphone();
        StringBuilder phone = new StringBuilder();


        Cursor cursor1=Dbhelper.dbEmpty();
        StringBuilder table_empty = new StringBuilder();
        while (cursor1.moveToNext())
        {
            table_empty.append(cursor1.getInt(0));
        }
        int size=Integer.valueOf(String.valueOf(table_empty));
        if (size == 0){
            Toast.makeText(this,"Put CareTaker First!",Toast.LENGTH_SHORT).show();
        }
        else {
            while (cursor.moveToNext())
            {
                phone.append(cursor.getInt(0));
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+"+216"+ phone));
                startActivity(intent);


        }
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menulogout, menu);
        return true;

    }

   /* @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // because we want to hide delete option when we are adding a new contact
        super.onPrepareOptionsMenu(menu);

        MenuItem item = (MenuItem) menu.findItem(R.id.logout);
        item.setVisible(false);
        //item1.setVisible(false);
        //item2.setVisible(false);

        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.logout:
                Intent intent2 = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent2);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}