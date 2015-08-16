package com.star.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;


public class CompassActivity extends AppCompatActivity {

    private float[] mAccelerometerValues = new float[3];
    private float[] mMagneticValues = new float[3];

    private CompassView mCompassView;

    private SensorManager mSensorManager;

    private int mRotation;

    private final SensorEventListener2 mSensorEventListener2 = new SensorEventListener2() {
        @Override
        public void onFlushCompleted(Sensor sensor) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mAccelerometerValues = event.values;
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mMagneticValues = event.values;
            }

            updateOrientation(calculateOrientation());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        mCompassView = (CompassView) findViewById(R.id.compassView);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        Display display = windowManager.getDefaultDisplay();

        mRotation = display.getRotation();

        updateOrientation(new float[] {0, 0, 0});
    }

    @Override
    protected void onResume() {
        super.onResume();

        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(mSensorEventListener2, accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(mSensorEventListener2, magnetic,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {

        mSensorManager.unregisterListener(mSensorEventListener2);

        super.onPause();
    }

    private void updateOrientation(float[] values) {
        if (mCompassView != null) {
            mCompassView.setBearing(values[0]);
            mCompassView.setPitch(values[1]);
            mCompassView.setRoll(-values[2]);
            mCompassView.invalidate();
        }
    }

    private float[] calculateOrientation() {
        float[] values = new float[3];
        float[] inR = new float[9];
        float[] outR = new float[9];

        SensorManager.getRotationMatrix(inR, null, mAccelerometerValues, mMagneticValues);

        int xAxis = SensorManager.AXIS_X;
        int yAxis = SensorManager.AXIS_Y;

        switch (mRotation) {
            case Surface.ROTATION_90:
                xAxis = SensorManager.AXIS_Y;
                yAxis = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                yAxis = SensorManager.AXIS_MINUS_Y;
                break;
            case Surface.ROTATION_270:
                xAxis = SensorManager.AXIS_MINUS_Y;
                yAxis = SensorManager.AXIS_X;
                break;
            default:
                break;
        }

        SensorManager.remapCoordinateSystem(inR, xAxis, yAxis, outR);

        SensorManager.getOrientation(outR, values);

        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);

        return values;
    }

}
