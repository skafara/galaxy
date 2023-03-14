package universe;

import utility.Vector2D;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * Space object representing a planet
 *
 * @author Stanislav Kafara
 * @version 2 2022-04-30
 */
public class Planet extends SpaceObject {

    /**
     * Actual (but not real) radius of the planet.
     *
     * Radius of the planet is determined as if:
     *  1) the planet was a perfect sphere
     *  2) with space object's mass
     *  3) and density of 1 kg*m^-3.
     */
    private final double radius;

    /**
     * Radius to be used for painting the planet.
     *
     * It is implicitly the actual radius, but may change in time
     * as it depends on the result of the {@code ensureMinimumPaintingDimensions} method, if called.
     */
    private double paintingRadius;

    /** Colour of the planet */
    private Color colour;

    /**
     * Constructs a planet with given properties.
     *
     * @param name              Name of the planet
     * @param centerPosition    The center of the planet
     * @param velocity          Velocity of the planet
     * @param mass              Mass of the planet
     */
    public Planet
            (
                    String name,
                    Vector2D centerPosition, Vector2D velocity,
                    double mass
            )
    {
        super(name, centerPosition, velocity, mass);
        this.radius = Math.cbrt(.75*mass/Math.PI);
        this.paintingRadius = this.radius;
        this.colour = Color.BLUE;
    }

    /**
     * Returns the actual radius of the planet.
     *
     * @return  Radius (actual) of the planet
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Returns the x-coordinate of the upper-left corner of the planet's bounding rectangle.
     *
     * @return  X-coordinate of the upper-left corner of the planet's bounding rectangle
     */
    @Override
    public double getX() {
        return getCenterPosition().getX()-radius;
    }

    /**
     * Returns the y-coordinate of the upper-left corner of the planet's bounding rectangle.
     *
     * @return  Y-coordinate of the upper-left corner of the planet's bounding rectangle
     */
    @Override
    public double getY() {
        return getCenterPosition().getY()-radius;
    }

    /**
     * Calculates and returns the width of the planet.
     *
     * @return Width of the planet
     */
    @Override
    public double getWidth() {
        return 2*radius;
    }

    /**
     * Calculates and returns the height of the planet.
     *
     * @return Height of the planet
     */
    @Override
    public double getHeight() {
        return 2*radius;
    }

    /**
     * Computes and returns the bounding rectangle of the painting.
     *
     * @return  Bounding rectangle of the painting
     */
    @Override
    public Rectangle2D getPaintingBoundingRectangle() {
        Vector2D center = getCenterPosition();
        return new Rectangle2D.Double(
                center.getX()-paintingRadius,
                center.getY()-paintingRadius,
                getPaintingWidth(),
                getPaintingHeight()
        );
    }

    /**
     * Calculates and returns the width of the painting.
     *
     * @return Width of the painting
     */
    @Override
    public double getPaintingWidth() {
        return 2*paintingRadius;
    }

    /**
     * Calculates and returns the height of the painting.
     *
     * @return Height of the painting
     */
    @Override
    public double getPaintingHeight() {
        return 2*paintingRadius;
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
        double radius = paintingRadius/positionsCount;
        double radiusIncrement = radius;
        for (int i=0; i<positionsCount; i++) {
            Vector2D position = iterator.next();
            Rectangle2D boundingRectangle = new Rectangle2D.Double(
                    position.getX()-radius, position.getY()-radius,
                    2*radius, 2*radius
            );

            if (boundingRectangle.getX()<minX)      minX = boundingRectangle.getX();
            if (boundingRectangle.getY()<minY)      minY = boundingRectangle.getY();
            if (boundingRectangle.getMaxX()>maxX)   maxX = boundingRectangle.getMaxX();
            if (boundingRectangle.getMaxY()>maxY)   maxY = boundingRectangle.getMaxY();

            radius += radiusIncrement;
        }
        return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
    }

    /**
     * Performs hit-test.
     * Returns whether the position (point) is inside the space object.
     *
     * @param position  Position (point) against which the hit-test is performed.
     * @return          True, if the position (point) is inside the space object, else false.
     */
    @Override
    public boolean hitTest(Vector2D position) {
        double x = position.getX();
        double y = position.getY();
        return
                (x-getCenterPosition().getX())*(x-getCenterPosition().getX()) +
                (y-getCenterPosition().getY())*(y-getCenterPosition().getY())
                <= paintingRadius*paintingRadius;
    }

    /**
     * Paints itself on the given graphics context.
     *
     * @param graphics2D    Graphics context
     */
    @Override
    public void paint(Graphics2D graphics2D) {
        graphics2D.setColor(colour);
        graphics2D.fill(
                new Ellipse2D.Double(
                        getCenterPosition().getX()-paintingRadius,
                        getCenterPosition().getY()-paintingRadius,
                        getPaintingWidth(),
                        getPaintingHeight()
                )
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
        double radius = paintingRadius/positionsCount;
        double radiusIncrement = radius;
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
                    new Ellipse2D.Double(
                            position.getX()-radius, position.getY()-radius,
                            2*radius, 2*radius
                    )
            );

            radius += radiusIncrement;
            alpha += alphaIncrement;
        }
    }

    /**
     * Ensures that the painting has given minimal dimensions.
     *
     * @param canvasDimensions          Dimensions of the canvas
     * @param paintedAreaDimensions     Actual dimensions of the universe
     * @param minimalPaintingDimensions Minimal painting dimensions which will be ensured
     */
    @Override
    public void ensureMinimalPaintingDimensions(
            Vector2D canvasDimensions, Vector2D paintedAreaDimensions,
            Vector2D minimalPaintingDimensions
    ) {
        double currentPaintingDimension = // as if painted with actual radius
                canvasDimensions.getX()*getWidth()/paintedAreaDimensions.getX();
        double greaterMinimalPaintingDimension = Math.max(
                minimalPaintingDimensions.getX(), minimalPaintingDimensions.getY()
        );
        if (
                (minimalPaintingDimensions.getX()==-1 && minimalPaintingDimensions.getY()==-1) || // nulling or
                (currentPaintingDimension > greaterMinimalPaintingDimension) // as if painted with actual radius is enough
        ) {
            paintingRadius = radius;
        } else { // set greater paintingRadius
            double shouldScaleTimes = greaterMinimalPaintingDimension/currentPaintingDimension;
            paintingRadius = shouldScaleTimes*radius;
        }
    }

    /**
     * Realizes selection of the object:
     *  1) Sets the colour of the planet to an exceptional ("selected") colour.
     */
    @Override
    public void select() {
        colour = Color.RED;
    }

    /**
     * Realizes deselection of the object:
     *  1) Sets the colour of the planet to the ordinary colour.
     */
    @Override
    public void deselect() {
        colour = Color.BLUE;
    }

}
