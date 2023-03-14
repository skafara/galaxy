package universe;

import utility.Vector2D;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * Space object representing a comet
 *
 * @author Stanislav Kafara
 * @version 1 2022-04-30
 */
public class Comet extends SpaceObject {

    /** Actual length of the side of the rectangle representing the comet */
    private final double sideLength;

    /** Painting length of the side of the rectangle representing the comet */
    private double paintingSideLength;

    /** Colour of the comet */
    private Color colour = Color.PINK;

    /**
     * Constructs a comet with given properties.
     *
     * @param name           Name of the comet
     * @param centerPosition The center of the comet
     * @param velocity       Velocity of the comet
     * @param mass           Mass of the comet
     */
    public Comet(String name, Vector2D centerPosition, Vector2D velocity, double mass) {
        super(name, centerPosition, velocity, mass);
        this.sideLength = (Math.cbrt(mass) > 0) ? Math.cbrt(mass) : 0.000000001;
        this.paintingSideLength = this.sideLength;
    }

    /**
     * Returns the width of the comet.
     *
     * @return Width of the comet
     */
    @Override
    public double getWidth() {
        return sideLength;
    }

    /**
     * Returns the height of the comet.
     *
     * @return Height of the comet
     */
    @Override
    public double getHeight() {
        return sideLength;
    }

    /**
     * Ensures that the painting has given minimal dimensions.
     *
     * @param canvasDimensions          Dimensions of the canvas
     * @param paintedAreaDimensions     Actual dimensions of the whole painted area
     * @param minimalPaintingDimensions Minimal painting dimensions which will be ensured
     */
    @Override
    public void ensureMinimalPaintingDimensions(Vector2D canvasDimensions, Vector2D paintedAreaDimensions, Vector2D minimalPaintingDimensions) {
        double currentPaintingDimension = // as if painted with actual sideLength
                canvasDimensions.getX()*getWidth()/paintedAreaDimensions.getX();
        double greaterMinimalPaintingDimension = Math.max(
                minimalPaintingDimensions.getX(), minimalPaintingDimensions.getY()
        );
        if (
                (minimalPaintingDimensions.getX()==-1 && minimalPaintingDimensions.getY()==-1) || // nulling or
                (currentPaintingDimension > greaterMinimalPaintingDimension) // as if painted with actual sideLength is enough
        ) {
            paintingSideLength = sideLength;
        } else { // set greater paintingSideLength
            double shouldScaleTimes = greaterMinimalPaintingDimension/currentPaintingDimension;
            paintingSideLength = shouldScaleTimes*sideLength;
        }
    }

    /**
     * Returns the width of the painting.
     *
     * @return Width of the painting
     */
    @Override
    public double getPaintingWidth() {
        return paintingSideLength;
    }

    /**
     * Returns the height of the painting.
     *
     * @return Height of the painting
     */
    @Override
    public double getPaintingHeight() {
        return paintingSideLength;
    }

    /**
     * Returns the x-coordinate of the comet.
     *
     * @return X-coordinate of the comet
     */
    @Override
    public double getX() {
        return getCenterPosition().getX() - 0.5*sideLength;
    }

    /**
     * Returns the y-coordinate of the comet.
     *
     * @return Y-coordinate of the comet
     */
    @Override
    public double getY() {
        return getCenterPosition().getY() - 0.5*sideLength;
    }

    /**
     * Paints itself on the given graphics context.
     *
     * @param graphics2D Graphics context
     */
    @Override
    public void paint(Graphics2D graphics2D) {
        graphics2D.setColor(colour);
        graphics2D.fill(
                getPaintingBoundingRectangle()
        );
    }

    /**
     * Paints the trajectory on the given graphics context.
     *
     * @param graphics2D Graphics context
     */
    @Override
    public void paintTrajectory(Graphics2D graphics2D) {
        Iterator<Vector2D> iterator = getTrajectory().getHistory().iterator();
        int positionsCount = getTrajectory().getLength();
        Color colour = Color.getHSBColor(0f, 0f, 0.8f);
        double sideLength = paintingSideLength/positionsCount;
        double sideLengthIncrement = sideLength;
        float alpha = 0.1f;
        float alphaIncrement = 0.4f/positionsCount;
        for (int i=0; i<positionsCount; i++) {
            Vector2D position = iterator.next();
            graphics2D.setColor(
                    new Color(
                            colour.getRed(),
                            colour.getGreen(),
                            colour.getBlue(),
                            (int) (255*alpha)
                    )
            );
            graphics2D.fill(
                    new Rectangle.Double(
                            position.getX()-0.5*sideLength, position.getY()-0.5*sideLength,
                            sideLength, sideLength
                    )
            );

            sideLength += sideLengthIncrement;
            alpha += alphaIncrement;
        }
    }

    /**
     * Returns the bounding rectangle of the painting.
     *
     * @return Bounding rectangle of the painting
     */
    @Override
    public Rectangle2D getPaintingBoundingRectangle() {
        Vector2D center = getCenterPosition();
        return new Rectangle2D.Double(
                center.getX()-0.5*paintingSideLength,
                center.getY()-0.5*paintingSideLength,
                getPaintingWidth(),
                getPaintingHeight()
        );
    }

    /**
     * Computes and returns the bounding rectangle of the trajectory.
     *
     * @return  Bounding rectangle of the trajectory
     */
    @Override
    public Rectangle2D getTrajectoryBoundingRectangle() {
        double minX, minY, maxX, maxY;
        minX = minY = Double.POSITIVE_INFINITY;
        maxX = maxY = Double.NEGATIVE_INFINITY;

        Iterator<Vector2D> iterator = getTrajectory().getHistory().iterator();
        int positionsCount = getTrajectory().getLength();
        double sideLength = paintingSideLength/positionsCount;
        double sideLengthIncrement = sideLength;
        for (int i=0; i<positionsCount; i++) {
            Vector2D position = iterator.next();
            Rectangle2D boundingRectangle = new Rectangle2D.Double(
                    position.getX()-0.5*sideLength, position.getY()-0.5*sideLength,
                    sideLength, sideLength
            );

            if (boundingRectangle.getX()<minX)      minX = boundingRectangle.getX();
            if (boundingRectangle.getY()<minY)      minY = boundingRectangle.getY();
            if (boundingRectangle.getMaxX()>maxX)   maxX = boundingRectangle.getMaxX();
            if (boundingRectangle.getMaxY()>maxY)   maxY = boundingRectangle.getMaxY();

            sideLength += sideLengthIncrement;
        }
        return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
    }

    /**
     * Realizes selection of the object:
     *  1) Sets the colour of the comet to an exceptional ("selected") colour.
     */
    @Override
    public void select() {
        colour = Color.ORANGE;
    }

    /**
     * Realizes deselection of the object:
     *  1) Sets the colour of the comet to the ordinary colour.
     */
    @Override
    public void deselect() {
        colour = Color.PINK;
    }

    /**
     * Performs hit-test.
     * Returns whether the position (point) is inside the space object.
     *
     * @param position Position (point) against which the hit-test is performed.
     * @return True, if the position (point) is inside the space object, else false.
     */
    @Override
    public boolean hitTest(Vector2D position) {
        return  getPaintingBoundingRectangle()
                    .contains(
                            position.getX(),
                            position.getY()
                    );
    }
}
