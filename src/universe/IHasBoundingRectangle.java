package universe;

import java.awt.geom.Rectangle2D;

/**
 * Interface implemented by classes that have bounding rectangle
 *
 * @author Stanislav Kafara
 * @version 1 2022-04-11
 */
public interface IHasBoundingRectangle extends IHasPosition, IHasDimensions {

    /**
     * Calculates and returns the bounding rectangle of the object.
     *
     * @return  Bounding rectangle of the object
     */
    default Rectangle2D getBoundingRectangle() {
        return new Rectangle2D.Double(
                getX(), getY(),
                getWidth(), getHeight()
        );
    }

}
