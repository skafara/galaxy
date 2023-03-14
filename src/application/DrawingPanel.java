package application;

import painting.IHasMinimalPaintingDimensions;
import universe.SpaceObject;
import universe.Universe;
import utility.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a drawing panel for a simulation.
 *
 * @author Stanislav Kafara
 * @version 3 2022-05-09
 */
public class DrawingPanel extends JPanel {

    /**
     * Minimal space object dimension
     *
     * (8, 8) is enough for the space object to be recognized.
     */
    private static final Vector2D MINIMAL_OBJECT_DIMENSIONS = new Vector2D(8, 8);

    /** Simulation which is drawn on the panel */
    private final Simulation simulation;

    /** Universe of the simulation */
    private final Universe universe;

    /** Selected space object */
    private SpaceObject selectedObject;

    /** Transform provided by the graphics context - device dependent */
    private AffineTransform providedTransform;

    /**
     * Transform of the drawing canvas
     *
     * Canvas is the rectangle of the panel, which encloses the simulated universe. It is:
     *  1) centered on the panel,
     *  2) scaled to fit in the whole simulated universe
     */
    private AffineTransform canvasTransform;

    /**
     * Constructs a drawing panel for the given simulation.
     *
     * @param simulation    Simulation to be drawn on the panel
     */
    public DrawingPanel(Simulation simulation) {
        this.simulation = simulation;
        this.universe = simulation.getUniverse();
    }

    /**
     * Paints the whole simulation.
     *  1) the simulated universe
     *  2) the simulation time
     *  3) information about the selected space object
     *
     * @param g Graphics context
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D graphics2D = (Graphics2D) g;
        AffineTransform defaultTransform = graphics2D.getTransform();
        if (this.providedTransform==null) {
            this.providedTransform = (AffineTransform) defaultTransform.clone();
        }
        // painting universe
        paintUniverse(graphics2D);
        // drawing simulation time and information about selected space object
        graphics2D.setTransform(defaultTransform);
        paintSimulationInformation(graphics2D);
    }

    /**
     * Paints the universe on the provided graphics context.
     *
     * @param graphics2D Graphics context
     */
    public void paintUniverse(Graphics2D graphics2D) {
        Rectangle2D objectsBoundingRectangle = universe.getObjectsBoundingRectangle();
        Vector2D objectsBoundingRectangleDimensions = new Vector2D(
                objectsBoundingRectangle.getWidth(),
                objectsBoundingRectangle.getHeight()
        );

        Vector2D canvasDimensions = getCanvasDimensions(
                objectsBoundingRectangleDimensions,
                getCanvasScale(getContentScale(objectsBoundingRectangleDimensions))
        );

        for (IHasMinimalPaintingDimensions spaceObject : universe.getSpaceObjects()) {
            spaceObject.ensureMinimalPaintingDimensions(
                    canvasDimensions, objectsBoundingRectangleDimensions,
                    MINIMAL_OBJECT_DIMENSIONS
            );
        }

        Rectangle2D objectsPaintingBoundingRectangle = universe.getPaintingBoundingRectangle();
        this.canvasTransform = getCanvasTransform(
                objectsPaintingBoundingRectangle
        );
        // drawing the simulated universe
        AffineTransform transform = graphics2D.getTransform();
        transform.concatenate(canvasTransform);
        graphics2D.setTransform(transform);
        universe.paint(graphics2D);
    }

    /**
     * Paints the simulation information on the provided graphics context.
     *
     * @param graphics2D Graphics context
     */
    public void paintSimulationInformation(Graphics2D graphics2D) {
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.setColor(Color.BLACK);
        int fontHeight = fontMetrics.getHeight();
        String simulationTimeString = getSimulationTimeString();
        graphics2D.drawString(
                "Simulation time",
                getWidth() - fontMetrics.stringWidth("Simulation time") - 4, fontHeight
        );
        graphics2D.drawString(
                simulationTimeString,
                getWidth() - fontMetrics.stringWidth(simulationTimeString) - 4, 2 * fontHeight
        );

        // drawing info about the selected space object
        if (selectedObject != null) {
            graphics2D.drawString(
                    selectedObject.getName(),
                    4, getHeight() - 4 - 6 * fontHeight
            );
            graphics2D.drawString(
                    "Position:",
                    4, getHeight() - 4 - 5 * fontHeight
            );
            graphics2D.drawString(
                    String.format(
                            "[x] %.2f",
                            selectedObject.getCenterPosition().getX()
                    ),
                    4, getHeight() - 4 - 4 * fontHeight
            );
            graphics2D.drawString(
                    String.format(
                            "[y] %.2f",
                            selectedObject.getCenterPosition().getY()
                    ),
                    4, getHeight() - 4 - 3 * fontHeight
            );
            graphics2D.drawString(
                    "Velocity:",
                    4, getHeight() - 4 - 2 * fontHeight
            );
            graphics2D.drawString(
                    String.format(
                            "[x / ms^-1] %.2f",
                            selectedObject.getVelocity().getX()
                    ),
                    4, getHeight() - 4 - fontHeight
            );
            graphics2D.drawString(
                    String.format(
                            "[y / ms^-1] %.2f",
                            selectedObject.getVelocity().getY()
                    ),
                    4, getHeight() - 4
            );
        }
    }

    /**
     * Returns the latest used canvas transform
     * to fit in the whole universe with ensured minimal space object's dimensions.
     * This transform is for point (screen) to point (universe) conversion.
     *
     * @return  Latest canvas transform
     */
    public AffineTransform getCanvasTransform() {
        return canvasTransform;
    }

    /**
     * Calculates and returns the final canvas transform.
     *
     * @param boundingRectangle Bounding rectangle of the content to be drawn on the canvas.
     * @return                  Transform of the canvas
     */
    public AffineTransform getCanvasTransform(Rectangle2D boundingRectangle) {
        AffineTransform canvasTransform = new AffineTransform();

        Vector2D contentDimensions = getContentDimensions(boundingRectangle);
        Vector2D contentScale = getContentScale(contentDimensions);
        Vector2D contentTranslated = getContentTranslate(boundingRectangle);
        double canvasScale = getCanvasScale(contentScale);
        Vector2D canvasDimensions = getCanvasDimensions(contentDimensions, canvasScale);

        canvasTransform.translate(
                (getWidth()-canvasDimensions.getX()) / 2,
                (getHeight()-canvasDimensions.getY()) / 2
        );
        canvasTransform.scale(
                canvasScale, canvasScale
        );
        canvasTransform.translate(
                -contentTranslated.getX(), -contentTranslated.getY()
        );

        return canvasTransform;
    }

    /**
     * Calculates and returns the dimensions of the content.
     *
     * @param contentBoundingRectangle  Bounding rectangle of the content
     * @return                          Dimensions of the content
     */
    private Vector2D getContentDimensions(Rectangle2D contentBoundingRectangle) {
        return new Vector2D(
                contentBoundingRectangle.getWidth(),
                contentBoundingRectangle.getHeight()
        );
    }

    /**
     * Calculates and returns the scale of the content against the drawing panel.
     *
     * @param contentDimensions Dimensions of the content
     * @return                  Scale of the content against the drawing panel
     */
    private Vector2D getContentScale(Vector2D contentDimensions) {
        return new Vector2D( // how many times the content is larger than the canvas dimensions
                contentDimensions.getX() / getWidth(),
                contentDimensions.getY() / getHeight()
        );
    }

    /**
     * Calculates and returns the translation of the content against (0, 0).
     *
     * @param contentBoundingRectangle  Bounding rectangle of the content
     * @return                          Translation vector of the content
     */
    private Vector2D getContentTranslate(Rectangle2D contentBoundingRectangle) {
        return new Vector2D( // content displacement
                contentBoundingRectangle.getX(),
                contentBoundingRectangle.getY()
        );
    }

    /**
     * Calculates and returns the scale of the canvas.
     *
     * @param contentScale  Scale of the content
     * @return              Scale of the canvas
     */
    private double getCanvasScale(Vector2D contentScale) {
        return (contentScale.getX() > contentScale.getY())
                // this many times should be the canvas axes multiplied by
                ? 1/contentScale.getX() : 1/contentScale.getY();
    }

    /**
     * Calculates and returns the canvas dimensions.
     *
     * @param contentDimensions Dimensions of the canvas
     * @param canvasScale       Scale of the canvas
     * @return                  Dimensions of the canvas
     */
    private Vector2D getCanvasDimensions(Vector2D contentDimensions, double canvasScale) {
        return new Vector2D( // dimensions of the content drawn on canvas (= canvas dimensions)
                canvasScale * contentDimensions.getX(),
                canvasScale * contentDimensions.getY()
        );
    }

    /**
     * Returns a String-representation of total elapsed simulation time.
     *
     * @return  String-representation of total elapsed simulation time
     */
    private String getSimulationTimeString() {
        double simulationTime = simulation.getTime();
        int y = (int) (simulationTime / 31536000);
        int d = (int) (simulationTime / 86400 - 365 * y);
        int h = (int) (simulationTime / 3600 - 24 * d - 8760 * y);
        int m = (int) (simulationTime / 60 - 60 * h - 1440 * d - 525600 * y);
        double s = simulationTime % 60;
        if (m==0 && h==0 && d==0 && y==0) {
            return String.format(
                    "%5.2fs", s
            );
        } else if (h==0 && d==0 && y==0) {
            return String.format(
                    "%2dm %5.2fs", m, s
            );
        } else if (d==0 && y==0) {
            return String.format(
                    "%dh %2dm %5.2fs", h, m, s
            );
        } else if (y==0) {
            return String.format(
                    "%dd %dh %2dm %5.2fs", d, h, m, s
            );
        } else {
            return String.format(
                    "%dy %dd %dh %dmin %2.2fs",
                    y, d, h, m, s
            );
        }
    }

    /**
     * Calculates the actual point (position) in the universe and returns whether the point belongs to the space object.
     *
     * @param canvasTransform   Transform of the drawing canvas
     * @param spaceObject       Space object to be tested
     * @param position          Point (position) to be tested
     * @return                  True, if the space object contains the point (position), else false.
     */
    public boolean hitTest(AffineTransform canvasTransform, SpaceObject spaceObject, Vector2D position) {
        Point2D absolutePosition = new Point2D.Double(position.getX(), position.getY());
        Point2D inverseTransformed = new Point2D.Double();
        try {
            canvasTransform.inverseTransform(
                    absolutePosition, inverseTransformed
            );
        } catch (NoninvertibleTransformException e) {
            // cannot occur
        }
        return spaceObject.hitTest(
                new Vector2D(
                        inverseTransformed.getX(),
                        inverseTransformed.getY()
                )
        );
    }

    /**
     * Selects the space object.
     *  1) Deselects previously selected object (if any)
     *  2) and selects the given space object.
     *
     * @param spaceObject   Space object to be selected
     */
    public void selectObject(SpaceObject spaceObject) {
        if (selectedObject!=null) {
            selectedObject.deselect();
        }
        selectedObject = spaceObject;
        if (spaceObject==null) {
            repaint();
            return;
        }
        selectedObject.select();
        repaint();
    }

    /**
     * Ensures that in case of collision of space objects the selection updates.
     *
     * @param resolvedCollisions    Resolved collisions
     */
    public void correctObjectSelection(Map<Collection<SpaceObject>, SpaceObject> resolvedCollisions) {
        if (selectedObject==null) {
            return;
        }
        for (Map.Entry<Collection<SpaceObject>, SpaceObject> resolvedCollision : resolvedCollisions.entrySet()) {
            for (SpaceObject collidedObject : resolvedCollision.getKey()) {
                if (collidedObject==selectedObject) {
                    selectObject(resolvedCollision.getValue());
                    return;
                }
            }
        }
    }

}
