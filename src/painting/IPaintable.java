package painting;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Interface implemented by classes that can be painted on graphics context
 *
 * @author Stanislav Kafara
 * @version 1 2022-04-11
 */
public interface IPaintable {

    /**
     * Paints itself on the given graphics context.
     *
     * @param graphics2D    Graphics context
     */
    void paint(Graphics2D graphics2D);

    /**
     * Returns the bounding rectangle of the painting.
     *
     * @return  Bounding rectangle of the painting
     */
    Rectangle2D getPaintingBoundingRectangle();

}
