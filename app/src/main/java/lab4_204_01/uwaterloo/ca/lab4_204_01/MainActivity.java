package lab4_204_01.uwaterloo.ca.lab4_204_01;

/**
 * @authors: Ning Jared, Wang Alen, Weisberg Daniel
 * Project Name: Lab3_204_01
 * Class: ECE155
 * Date Completed: June 27th, 2016
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ca.uwaterloo.sensortoy.LineGraphView;
import mapper.MapLoader;
import mapper.MapView;
import mapper.NavigationalMap;
import mapper.PathFinder;
import mapper.PositionListener;

public class MainActivity extends AppCompatActivity implements PositionListener{

    //global variables
    //holds value for screen width and screen height
    private int screenWidth = 0;
    private int screenHeight = 0;
    //Provides activity with linear layout
    private LinearLayout mLinLayout;
    //Creates linegraph
    private LineGraphView mLineGraphView;
    //Provides sensory data
    protected SensorManager mSensorManager;
    //Creates unique ID for each textview
    private int txtViewId = 0;
    //Creates new StepCounterManager object
    private StepCounterManager stepCounter;
    //Creates new MapView object
    protected static MapView mv;
    //Define variable for azimuth
    public static float azimuth;
    //Creates new userDisplacement object
    public static Vector2D userDisplacement;
    protected static PointF startP;
    protected static PointF endP;
    protected static PathFinder pFinder;
    protected static NavigationalMap map;
    public PointF compassPoint = new PointF();
    public PointF compassLine = new PointF();
    protected static boolean toastFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //north = south = east = west = 0;
        //Creates linear layout in activity
        mLinLayout = (LinearLayout) findViewById(R.id.frontActivity);
        mLinLayout.setOrientation(LinearLayout.VERTICAL);
        mLinLayout.setBackgroundColor(Color.WHITE);

        //gets the screen size
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        //initializes sensor manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        addMap(mLinLayout, "E2-3344.svg");

        //Adds lineview to activity
        addGraph(mLinLayout);

        //initializes required sensors
        addEventListeners();

        //initializes StepCounterManager
        stepCounter = new StepCounterManager();

        //initializes userDisplacement
        userDisplacement = new Vector2D();
        startP = new PointF(0.0f, 0.0f);
        endP = new PointF();


        pFinder = new PathFinder();
    }

    //initialise the step reset button
    public void onReset(View view) {
        stepCounter.mNumSteps = 0;
        userDisplacement.setxVal(0);
        userDisplacement.setyVal(0);
    }

    public void onClear(View view) {
        startP.x = 0.0f;
        startP.y = 0.0f;
        endP.x = 0.0f;
        endP.y = 0.0f;
        mv.removeAllLabeledPoints();
    }

    private void addEventListeners() {  //initializes required sensors and adds event listeners
        Sensor accelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magFSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gravSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        SensorEventListener accListener = new AccelerometerSensorEventListener((TextView) mLinLayout.findViewById(returnTextViewID(getApplicationContext())));
        SensorEventListener compassListener = new GeoAndGravitySensorEventListener((TextView) mLinLayout.findViewById(returnTextViewID(getApplicationContext())));

        mSensorManager.registerListener(accListener, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(compassListener, gravSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(compassListener, magFSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void addGraph(LinearLayout layout) { //Add line graph to activity
        mLineGraphView = new LineGraphView(getApplicationContext(), 100, Arrays.asList("minPeak", "maxPeak", "minTrough", "maxTrough", "filtered y"));
        mLineGraphView.purge();

        layout.addView(mLineGraphView);
        //to set graph width - we don't care about the height
        mLineGraphView.setGraphWidth(screenWidth);
        mLineGraphView.setVisibility(View.VISIBLE);
    }

    private void addMap(LinearLayout layout, String mapName) { //Add map to activity
        mv = new MapView(getApplicationContext(), 650, 600, 25, 25);
        map = MapLoader.loadMap(getExternalFilesDir(null), mapName);
        mv.setMap(map);
        layout.addView(mv);
        registerForContextMenu(mv);
        mv.addListener(this);
    }
    /**
     * Called when the user sets their origin/current location through the MapView.
     * @param source The MapView that caused the change.
     * @param loc The new coordinates of the location in meters.
     */
    @Override
    public void originChanged(MapView source, PointF loc) {
        startP.x = loc.x;
        startP.y = loc.y;
        mv.setUserPoint(loc.x, loc.y);
        Log.e("StartPoint", "" +  startP);
        mv.setUserPath(pFinder.findShortestPath(startP, endP, map));
    }
    /**
     * Called when the user sets their destination through the MapView.
     * @param source The MapView that caused the change.
     * @param dest The new coordinates of the destination in meters.
     */
    @Override
    public void destinationChanged(MapView source, PointF dest) {
        endP.x = dest.x;
        endP.y = dest.y;
        Log.e("EndPoint", "" +  endP);
        mv.setUserPath(pFinder.findShortestPath(startP, endP, map));
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        mv.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item) || mv.onContextItemSelected(item);
    }

    protected int returnTextViewID(Context context) { //creates dynamic textviews in activity
        Random rand = new Random();
        TextView tv1 = new TextView(context); //create a new textview
        txtViewId++;
        tv1.setId(txtViewId);
        tv1.setTextColor(Color.argb(255,
                rand.nextInt(170),
                rand.nextInt(170),
                rand.nextInt(170)));
        mLinLayout.addView(tv1); //add the textview to the activity
        return txtViewId;
    }

    class GeoAndGravitySensorEventListener implements SensorEventListener {// called when Azimuth values change

        TextView output;
        float[] mGravity;
        float[] mMagnetic;

        public GeoAndGravitySensorEventListener(TextView outputView) {
            output = outputView;
        }

        public void onAccuracyChanged(Sensor s, int i) {
        }

        public void onSensorChanged(SensorEvent se) {
            if (se.sensor.getType() == Sensor.TYPE_GRAVITY) {
                mGravity = se.values;
            }
            if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mMagnetic = se.values;
            }
            if (mGravity != null && mMagnetic != null) {
                float inR[] = new float[9];
                boolean inRsucess = SensorManager.getRotationMatrix(inR, null, mGravity, mMagnetic);

                if (inRsucess) {
                    float outR[] = new float[9];
                    //remap coordinate system to use the Z-axis to determine the bearing from magnetic north instead of the Y-axis
                    boolean outRsucess = SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
                    if (outRsucess) {
                        float mOrientation[] = new float[3];
                        SensorManager.getOrientation(outR, mOrientation);
                        azimuth = (float) (mOrientation[0] * 180 / Math.PI);
                        if (azimuth < 0) {
                            azimuth += 360;
                        }
                        compassPoint.set(startP.x,startP.y);
                        compassLine.set(startP.x + (float)Math.sin(Math.toRadians(azimuth))*2,
                                startP.y + (float)Math.cos(Math.toRadians(azimuth))*2);
                        mv.addCompassValue(compassPoint,compassLine);

                        if (toastFlag) {
                            Toast.makeText(getApplicationContext(), "Destination reached!", Toast.LENGTH_LONG).show();
                            toastFlag = false;
                        }
                    }
                }
            }
        }
    }

    class AccelerometerSensorEventListener implements SensorEventListener { //called when accelerometer sensor change

        TextView output;
        TextView coord;
        float maxY = 0.0f;
        float currInputY = 0.0f;
        float prevOutputY = 0.0f;
        float currOutputY = 0.0f;
        float gravity = 0.0f;

        float filteredY;
        float a = 0.6f;
        float b = 0.8f;

        public AccelerometerSensorEventListener(TextView outputView) {
            output = outputView;
        }

        public void onAccuracyChanged(Sensor s, int i) {
        }

        public void onSensorChanged(SensorEvent se) {

            if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                coord = (TextView) findViewById(R.id.coord);
                //east = (TextView) findViewById(R.id.East);

                //gets the Y sensor value from accelerometer
                currInputY = se.values[1];
                //saves the previous filtered Y value
                prevOutputY = currOutputY;
                //calculates the effect of gravity on the Y axis of the accelerometer with a high pass filter
                gravity = b * gravity + (1 - b) * currInputY;
                //removes effect of gravity from the Y sensor value
                filteredY = currInputY - gravity;
                //low pass filter
                currOutputY = a * filteredY + (1 - a) * prevOutputY;
                //exports values to linegraph
                float[] output1 = {0.8f, 2.5f, -0.5f, -2.5f, currOutputY};
                mLineGraphView.addPoint(output1);
                //exports Y sensor values to StepCounterManager
                stepCounter.updateDataPoint(currOutputY);

                String s = String.format("\n X: %f\n Y: %f\n Z: %f", se.values[0], se.values[1], se.values[2]);

                if (maxY < Math.abs(currOutputY)) maxY = Math.abs(currOutputY);

                s += "\n" + String.format("Max Y: %f", maxY);
                s += "\n" + "Steps: " + Integer.toString(stepCounter.getStepsTaken());
                output.setText("Accelerometer: " + s);

                coord.setText("Azimuth: " + String.format("%.0f", azimuth));
            }
        }
    }
}