package painting;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Interface implemented by classes which have paintable trajectory
 *
 * @author Stanislav Kafara
 * @version 1 2022-05-02
 */
public interface IHasPaintableTrajectory {

    /**
     * Paints the trajectory on the given graphics context.
     *
     * @param graphics2D    Graphics context
     */
    void paintTrajectory(Graphics2D graphics2D);

    /**
     * Returns the bounding rectangle of the trajectory.
     *
     * @return  Bounding rectangle of the trajectory
     */
    Rectangle2D getTrajectoryBoundingRectangle();

}
