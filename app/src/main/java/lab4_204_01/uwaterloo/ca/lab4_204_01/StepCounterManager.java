package lab4_204_01.uwaterloo.ca.lab4_204_01;

import android.graphics.PointF;
import android.util.Log;

import java.util.Random;

/*
* StepCounterManager class
*
* Uses a stream of float values via updateDataPoint and processes them to determine the number of steps
* taken from accelerometer data
*
* Implements a simple finite state machine design
*
* Instantiate in onCreate using:
* stepCounter = new StepCounterManager();
*
* In event listeners use stepCounter.updateDataPoint(se.value[1]); // bc we are using the y values
*/
public class StepCounterManager {
    public final String CLASS_NAME = getClass().getSimpleName();

    private static float mStepThreshold = 0.25f; // determine with empircal numbers - hardcode this
    private static float mMaxPeakAllowed = 2.5f;
    private static float mMinPeakAllowed = 0.6f;
    private static float mMinTroughAllowed = -0.5f;
    private static float mMaxTroughAllowed = -2.5f;

    protected float[] azimuthValues = new float[6];
    Random rand = new Random();

    protected int mNumSteps = 0;
    private float mBaseline = 0; // determine with initial readings

    private float currentDP = 0;
    private float previousDP = 0;

    protected static final float STEP_DISTANCE = 0.75f;

    private StepState mCurrentState;

    private FSM mFsm;

    public float getPeakRequired(){
        return mMinPeakAllowed;
    }
    public void setPeakRequired(float peak){
        mMinPeakAllowed = peak;
    }

    public float getTroughRequired(){
        return mMinTroughAllowed;
    }
    public void setTroughRequired(float trough){
        mMinTroughAllowed = trough;
    }

    public void reset(){
        mNumSteps = 0;
        mCurrentState = StepState.BASELINE;

    }

    enum StepState
    {
        BASELINE,
        INITIAL,
        RISING,
        MAXPEAK,
        FALLING,
        TROUGH,
        STEPCOMPLETE,
        REJECTED
    }

    StepCounterManager()
    {
        mFsm = new FSM();
    }

    public int getStepsTaken()
    {
        return mNumSteps;
    }

    public void updateDataPoint (float newDataPoint)
    {
        if(mCurrentState == StepState.BASELINE){
            mBaseline = 0;
            mCurrentState = StepState.INITIAL;
        }
        previousDP = currentDP;
        currentDP = newDataPoint;

        //Log.e(mCurrentState.toString(), Float.toString(currentDP));

        if(mFsm != null) {
            switch (mCurrentState) {
                case REJECTED:
               //     Log.e(CLASS_NAME, "Rejected" + Float.toString(currentDP));
                    mCurrentState = StepState.INITIAL;
                    break;
                case INITIAL:
                    mFsm.states[0].onState(currentDP, previousDP);
                    break;
                case RISING:
                    mFsm.states[1].onState(currentDP, previousDP);
                    break;
                case MAXPEAK:
                    mFsm.states[2].onState(currentDP, previousDP);
                    break;
                case FALLING:
                    mFsm.states[3].onState(currentDP, previousDP);
                    break;
                case TROUGH:
                    mFsm.states[4].onState(currentDP, previousDP);
                    break;
                case STEPCOMPLETE:
                    mFsm.states[5].onState(currentDP, previousDP);
                    break;
                default:
                    //Log.e(CLASS_NAME, "Not valid step state");
            }
        }
    }

    class FSM //Finite State Machine
    {
        FSM()
        {
            mCurrentState = StepState.BASELINE;
            mNumSteps = 0;
        }
        State[] states = {new InitialState(), new RisingState(), new MaxPeakState(), new FallingState(),
                new TroughState(),  new StepCompleteState()};
    }

    abstract class State
    {
        public abstract void onState(float curDP, float prevDP);
        public abstract void nextState();
    }

    class InitialState extends State
    {
        public void onState(float curDP, float prevDP)
        {
            azimuthValues[0] = MainActivity.azimuth;
            this.nextState();

        }

        public void nextState() { mCurrentState = StepState.RISING; }
    }

    class RisingState extends State
    {
        public void onState(float curDP, float prevDP)
        {
            azimuthValues[1] = MainActivity.azimuth;
            if(curDP > mStepThreshold) this.nextState();
            //this is the case if the graph stops dropping before some threshold is reached
            else if(curDP < prevDP){
                mCurrentState = StepState.REJECTED;
            }
        }
        public void nextState() { mCurrentState = StepState.MAXPEAK; }
    }

    class MaxPeakState extends State
    {
        public void onState(float curDP, float prevDP)
        {
            azimuthValues[2] = MainActivity.azimuth;
            if(curDP < prevDP){
                float localPeak = prevDP;
                if( localPeak > mMaxPeakAllowed || localPeak < mMinPeakAllowed ){
                    mCurrentState = StepState.REJECTED;
                }
                else{
                    this.nextState();
                }
            }
        }
        public void nextState() { mCurrentState = StepState.FALLING;}
    }

    class FallingState extends State
    {
        public void onState(float curDP, float prevDP)
        {
            azimuthValues[3] = MainActivity.azimuth;
            if(curDP <= mBaseline){
                this.nextState();
            }
            else if(curDP > prevDP){
                mCurrentState = StepState.REJECTED;
            }
        }
        public void nextState() { mCurrentState = StepState.TROUGH; }
    }

    class TroughState extends State
    {
        public void onState(float curDP, float prevDP)
        {
            azimuthValues[4] = MainActivity.azimuth;
            if(curDP > prevDP){
                float localTrough = prevDP;
                if(localTrough > mMinTroughAllowed || localTrough < mMaxTroughAllowed){
                    mCurrentState = StepState.REJECTED;
                }
                else{
                    this.nextState();
                }
            }
        }
        public void nextState() { mCurrentState = StepState.STEPCOMPLETE; }
    }

    class StepCompleteState extends State
    {
        public void onState(float curDP, float prevDP)
        {
            azimuthValues[5] = MainActivity.azimuth;
            Log.e("StepState","" + azimuthValues[0]+ " " + azimuthValues[1]+ " " + azimuthValues[2]+ " " + azimuthValues[3]+  " " + azimuthValues[4] +  " " + azimuthValues[5]);

            azimuthValues[5] = MainActivity.azimuth;
            Log.e("StepState","" + azimuthValues[0]+ " " + azimuthValues[1]+ " " + azimuthValues[2]+ " " + azimuthValues[3]+  " " + azimuthValues[4] +  " " + azimuthValues[5]);

            float randomAzimuth = azimuthValues[rand.nextInt(5)];

            PointF nextStep = new PointF();

            float nextY = (float)Math.cos(randomAzimuth)*STEP_DISTANCE;
            Log.e("nextY",""+nextY);
            nextStep.set(MainActivity.startP.x + (float)Math.sin(Math.toRadians(randomAzimuth))*STEP_DISTANCE,
                    MainActivity.startP.y + (float)Math.cos(Math.toRadians(randomAzimuth))*STEP_DISTANCE);


            if (MainActivity.map.calculateIntersections(MainActivity.startP, nextStep).size() == 0) {
                mNumSteps++;
                MainActivity.userDisplacement.addPolarVec(randomAzimuth, STEP_DISTANCE);
                MainActivity.startP.x = nextStep.x;
                MainActivity.startP.y = nextStep.y;
                MainActivity.mv.setUserPoint(MainActivity.startP.x, MainActivity.startP.y);
                MainActivity.mv.setUserPath(MainActivity.pFinder.findShortestPath(MainActivity.startP, MainActivity.endP, MainActivity.map));

                if (Math.abs(MainActivity.startP.x - MainActivity.mv.getDestinationPoint().x) < 1f &&
                        Math.abs(MainActivity.startP.y - MainActivity.mv.getDestinationPoint().y) < 1f) {
                    MainActivity.toastFlag = true;
                }
            }
            this.nextState();
        }
        public void nextState(){ mCurrentState = StepState.INITIAL; }
    }
}
