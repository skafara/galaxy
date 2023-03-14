package utility;

/**
 * Represents a vector in 2D.
 *
 * @author Stanislav Kafara
 * @version 1 2022-04-11
 */
public class Vector2D {

    /**
     * Components of the vector
     *
     * 0: x-coordinate
     * 1: y-coordinate
     */
    private double[] components;

    /**
     * Magnitude of the vector
     *
     * -1 if not asked for the magnitude yet, else the calculated magnitude
     */
    private double magnitude = -1;

    /**
     * Constructs neutral vector.
     */
    public Vector2D() {
        this(0, 0);
    }

    /**
     * Constructs vector with given coordinates.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    public Vector2D(double x, double y) {
        this.components = new double[2];
        this.components[0] = x;
        this.components[1] = y;
    }

    /**
     * Returns x-coordinate of the vector.
     *
     * @return  X-coordinate of the vector
     */
    public double getX() {
        return components[0];
    }

    /**
     * Returns y-coordinate of the vector.
     *
     * @return  Y-coordinate of the vector
     */
    public double getY() {
        return components[1];
    }

    /**
     * Adds the given vector to the vector and returns the sum.
     *
     * @param v Vector to be added
     * @return  Sum of the vector and the given vector
     */
    public Vector2D add(Vector2D v) {
        return new Vector2D(
                this.components[0] + v.components[0],
                this.components[1] + v.components[1]
        );
    }

    /**
     * Subtracts the given vector from the vector and returns the difference.
     *
     * @param v Vector to be subtracted
     * @return  Difference of the vector and the given vector
     */
    public Vector2D subtract(Vector2D v) {
        return new Vector2D(
                this.components[0] - v.components[0],
                this.components[1] - v.components[1]
        );
    }

    /**
     * Multiplies the vector by the given constant.
     *
     * @param d Constant the vector to be multiplied by
     * @return  Vector multiplied by the constant
     */
    public Vector2D mul(double d) {
        return new Vector2D(
                d * components[0],
                d * components[1]
        );
    }

    /**
     * If not calculated yet, calculates the magnitude of the vector (difficult operation)
     * and returns the magnitude, otherwise only returns the pre-calculated magnitude.
     *
     * @return  Magnitude of the vector
     */
    public double magnitude() {
        if (magnitude==-1) {
            magnitude = Math.sqrt(components[0]*components[0] + components[1]*components[1]);
        }
        return magnitude;
    }

}
