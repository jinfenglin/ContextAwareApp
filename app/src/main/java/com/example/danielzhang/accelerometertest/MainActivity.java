package com.example.danielzhang.accelerometertest;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends Activity implements SensorEventListener,
        OnClickListener, ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {
    private SensorManager sensorManager;
    private Button btnStart, btnStop, btnSave;
    private boolean started = false;
    private ArrayList<AccelData> ACSensorData;
    private ArrayList<GPSData> LOCSensorData;
    LocationManager locationManager;
    private LinearLayout layout;
    private View mChart;
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    private AudioRecorderManager audioRecorderManager;
    private long lastTimestamp=0;
    private double last_x;
    private double last_y;
    private double last_z;
    private double SHAKE_THRESHOLD=2500;

    private int shakeCount=0;
    private int timeUpperBound = 3000;
    private long lastShakeTime = 3000;
    private TextView t;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t = (TextView)findViewById(R.id.editText);

        layout = (LinearLayout) findViewById(R.id.chart_container);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ACSensorData = new ArrayList();
        LOCSensorData = new ArrayList();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
        audioRecorderManager = new AudioRecorderManager();

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        if (ACSensorData == null || ACSensorData.size() == 0 || audioRecorderManager.getBuffer().size() == 0) {
            btnSave.setEnabled(false);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (started == true) {
            sensorManager.unregisterListener(this);
        }

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {

            Sensor sensor = event.sensor;
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //TODO: get values
                long timestamp = System.currentTimeMillis();
                if (timestamp - 100 > lastTimestamp || timestamp == 0) {
                    long diffTime = (timestamp - lastTimestamp);
                    lastTimestamp = timestamp;
                    double x = event.values[0];
                    double y = event.values[1];
                    double z = event.values[2];
                    double speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                    if (timestamp - lastShakeTime>100&& speed > SHAKE_THRESHOLD) {
                        Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                        if (timestamp - lastShakeTime<1000)
                            shakeCount++;
                        lastShakeTime = System.currentTimeMillis();

                    }

                    if (timestamp-lastShakeTime>5000)
                        t.setText("Normal");
                    last_x = x;
                    last_y = y;
                    last_z = z;
                    if (shakeCount>1)
                    {
                        t.setText("Exercising");
                        Toast.makeText(this, "exercise",Toast.LENGTH_SHORT).show();
                        shakeCount=0;
                        //TaskManager.killService("blueTooth"); Test blueTooth
                        TaskManager.killProcessByName("", this);
                    }
//
//                    if (timestamp >timeUpperBound)
//                    {
//                        timeUpperBound+=3000;
//                        if (shakeCount>1)
//                        {
//                            shakeCount=0;
//                            Toast.makeText(this, "exercising", Toast.LENGTH_SHORT).show();
//
//                        }
//                        else{
//                            shakeCount=0;
//                            Toast.makeText(this, "normal", Toast.LENGTH_SHORT).show();
//                        }
//                    }
                }
                //else #add other sensors
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnSave.setEnabled(false);
                ACSensorData = new ArrayList();
                // save prev data if available
                started = true;
                Sensor accel = sensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, accel,
                        SensorManager.SENSOR_DELAY_FASTEST);
                audioRecorderManager.startRecording();
                break;
            case R.id.btnStop:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnSave.setEnabled(true);
                started = false;
                sensorManager.unregisterListener(this);
                audioRecorderManager.stopRecording();
                layout.removeAllViews();
                openChart();

                // show data in chart
                break;
            case R.id.btnSave:
                long t = ACSensorData.get(0).getTimestamp();
                File file = new File(path + "/data.txt");
                FileOutputStream outputStream;
                String line = "";

                for (AccelData data : ACSensorData) {
                    line = line + (data.getTimestamp() - t) + ";" + data.getX() + ";" + data.getY() + ";" + data.getZ() + "\n";
                }

                try {
                    outputStream = new FileOutputStream(file);
                    outputStream.write(line.getBytes());
                    outputStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                String line2 = "";
                File file2 = new File(path + "/data2.txt");
                FileOutputStream outputStream2;
                for (GPSData data : LOCSensorData) {
                    line2 = line2 + (data.getTimestamp() - t) + ";" + data.getLongitude() + ";" + data.getLatitude() + ";" + "\n";
                }

                try {
                    outputStream2 = new FileOutputStream(file2);
                    outputStream2.write(line2.getBytes());
                    outputStream2.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                audioRecorderManager.writeDataToPath(path + "/audio.txt", t);
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Data Saved.");
                builder1.setCancelable(true);


                builder1.setNegativeButton(
                        "Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
                break;
            default:
                break;
        }

    }

    private void openChart() {
        if (ACSensorData != null || ACSensorData.size() > 0) {
            long t = ACSensorData.get(0).getTimestamp();
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            XYSeries zSeries = new XYSeries("Z");

            for (AccelData data : ACSensorData) {
                xSeries.add(data.getTimestamp() - t, data.getX());
                ySeries.add(data.getTimestamp() - t, data.getY());
                zSeries.add(data.getTimestamp() - t, data.getZ());
            }

            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);

            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);

            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setYTitle("Values of Acceleration");
            multiRenderer.setZoomButtonsVisible(true);
            for (int i = 0; i < ACSensorData.size(); i++) {

                multiRenderer.addXTextLabel(i + 1, ""
                        + (ACSensorData.get(i).getTimestamp() - t));
            }
            for (int i = 0; i < 12; i++) {
                multiRenderer.addYTextLabel(i + 1, "" + i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);

            // Getting a reference to LinearLayout of the MainActivity Layout

            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
//            layout.addView(mChart);

        }
    }

    @Override
    public void onConnected(Bundle bundle) {
//         Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (location == null) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//
//        } else {
//            //If everything went fine lets get latitude and longitude
//            currentLatitude = location.getLatitude();
//            currentLongitude = location.getLongitude();
//
//            Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
//        }
//
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
 /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }
}