package lab4_204_01.uwaterloo.ca.lab4_204_01;

/**
 * Simple vector class in with two co-ordinates
 * Vector can be represented in polar and cartesian co ordinates
 */
public class Vector2D {
    //cartesian co-ordinates
    private double xVal;
    private double yVal;

    //polar co-ordinates
    private double angle;
    private double vecMag;

    public Vector2D(){
        xVal = yVal = angle = vecMag = 0.0d;
    }

    public double getyVal() {
        return yVal;
    }

    public void setyVal(int yVal) {
        this.yVal = yVal;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public double getVecMag() {
        return vecMag;
    }

    public void setVecMag(int vecMag) {
        this.vecMag = vecMag;
    }

    public double getxVal() {
        return xVal;
    }

    public void setxVal(int xVal) {
        this.xVal = xVal;
    }


    public void addPolarVec(double deg, double mag){
        double xEquiv = mag * Math.sin(Math.toRadians(deg));
        double yEquiv = mag * Math.cos(Math.toRadians(deg));

        xVal += xEquiv;
        yVal += yEquiv;
        updatePolarCoOrd();
    }

    private void updatePolarCoOrd(){
        angle = Math.toDegrees(Math.atan(xVal/yVal));
        vecMag = Math.sqrt(xVal*xVal + yVal*yVal);
    }

    private void updateCartCoOrd(){
        xVal = vecMag * Math.sin(angle);
        yVal = vecMag * Math.cos(angle);
    }

    public void addCartesianVec (double x, double y){
        xVal += x;
        yVal += y;
        updatePolarCoOrd();
    }

    public void addVector(Vector2D vec){
        xVal += vec.xVal;
        yVal += vec.yVal;
        updatePolarCoOrd();
    }
}
