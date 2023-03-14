package universe;

import utility.Vector2D;

/**
 * Interface implemented by classes that have position
 *
 * @author Stanislav Kafara
 * @version 1 2022-04-11
 */
public interface IHasPosition {

    /**
     * Returns the position of the center of the object.
     *
     * @return  Position of the center of the object
     */
    Vector2D getCenterPosition();

    /**
     * Returns the x-coordinate of the object.
     *
     * @return  X-coordinate of the object
     */
    double getX();

    /**
     * Returns the y-coordinate of the object.
     *
     * @return  Y-coordinate of the object
     */
    double getY();

}
