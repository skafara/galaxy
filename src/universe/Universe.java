package universe;

import painting.IHasPaintableTrajectory;
import painting.IPaintable;
import utility.Vector2D;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

/**
 * Represents a universe.
 *
 * @author Stanislav Kafara
 * @version 2 2022-05-07
 */
public class Universe implements IPaintable {

    /** Gravitational constant of the universe */
    private final double gravitationalConstant;

    /** Space objects in the universe */
    private final Collection<SpaceObject> spaceObjects;

    /** Space objects count in the universe */
    private int spaceObjectsCount;

    /**
     * Constructs a universe with given properties.
     *
     * @param gravitationalConstant Gravitational constant of the universe
     * @param spaceObjects          Space objects in the universe
     */
    public Universe(double gravitationalConstant, Collection<SpaceObject> spaceObjects) {
        this.gravitationalConstant = gravitationalConstant;
        this.spaceObjects = spaceObjects;
        this.spaceObjectsCount = spaceObjects.size();
    }

    /**
     * Returns the space objects in the universe.
     *
     * @return  Space objects in the universe
     */
    public Collection<SpaceObject> getSpaceObjects() {
        return spaceObjects;
    }

    /**
     * Returns the count of the space objects in the universe.
     *
     * @return  Count of the space objects in the universe
     */
    public int getSpaceObjectsCount() {
        return spaceObjectsCount;
    }

    /**
     * Returns the gravitational constant of the universe.
     *
     * @return  Gravitational constant of the universe
     */
    public double getGravitationalConstant() {
        return gravitationalConstant;
    }

    /**
     * Calculates and returns the bounding rectangle of the given space objects.
     *
     * @param spaceObjects  Space objects that will be enclosed in the bounding rectangle
     * @return              Bounding rectangle enclosing the given space objects
     */
    public Rectangle2D getObjectsBoundingRectangle(Collection<SpaceObject> spaceObjects) {
        double minX, minY, maxX, maxY;
        minX = minY = Double.POSITIVE_INFINITY;
        maxX = maxY = Double.NEGATIVE_INFINITY;
        for (IHasBoundingRectangle spaceObject : spaceObjects) {
            Vector2D upperLeft = new Vector2D(
                    spaceObject.getX(),
                    spaceObject.getY()
            );
            Vector2D lowerRight = new Vector2D(
                    spaceObject.getX() + spaceObject.getWidth(),
                    spaceObject.getY() + spaceObject.getHeight()
            );
            if (upperLeft.getX()<minX) minX = upperLeft.getX();
            if (upperLeft.getY()<minY) minY = upperLeft.getY();
            if (lowerRight.getX()>maxX) maxX = lowerRight.getX();
            if (lowerRight.getY()>maxY) maxY = lowerRight.getY();
        }
        return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
    }

    /**
     * Calculates and returns the bounding rectangle of the space objects in the universe.
     *
     * @return  Bounding rectangle enclosing the space objects in the universe
     */
    public Rectangle2D getObjectsBoundingRectangle() {
        return getObjectsBoundingRectangle(spaceObjects);
    }

    /**
     * Returns the bounding rectangle of the painting of the whole universe.
     *
     * @return  Bounding rectangle of the painting of the whole universe
     */
    @Override
    public Rectangle2D getPaintingBoundingRectangle() {
        double minX, minY, maxX, maxY;
        minX = minY = Double.POSITIVE_INFINITY;
        maxX = maxY = Double.NEGATIVE_INFINITY;
        for (SpaceObject spaceObject : spaceObjects) {
            Rectangle2D paintingBoundingRectangle =         spaceObject.getPaintingBoundingRectangle();
            Rectangle2D trajectoryBoundingRectangle =       spaceObject.getTrajectoryBoundingRectangle();

            if (paintingBoundingRectangle.getX()<minX)      minX = paintingBoundingRectangle.getX();
            if (paintingBoundingRectangle.getY()<minY)      minY = paintingBoundingRectangle.getY();
            if (paintingBoundingRectangle.getMaxX()>maxX)   maxX = paintingBoundingRectangle.getMaxX();
            if (paintingBoundingRectangle.getMaxY()>maxY)   maxY = paintingBoundingRectangle.getMaxY();
            if (trajectoryBoundingRectangle.getX()<minX)    minX = trajectoryBoundingRectangle.getX();
            if (trajectoryBoundingRectangle.getY()<minY)    minY = trajectoryBoundingRectangle.getY();
            if (trajectoryBoundingRectangle.getMaxX()>maxX) maxX = trajectoryBoundingRectangle.getMaxX();
            if (trajectoryBoundingRectangle.getMaxY()>maxY) maxY = trajectoryBoundingRectangle.getMaxY();
        }
        return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
    }

    /**
     * Detects and returns collisions of space objects.
     *
     * @return  Collection of collections of together colliding objects
     */
    public Collection<Collection<SpaceObject>> detectCollisions() {
        List<SpaceObject> spaceObjects = new ArrayList<>(getSpaceObjects()); // for a reasonable iteration
        Map<SpaceObject, Collection<SpaceObject>> allCollisions = new HashMap<>();
        Collection<Collection<SpaceObject>> detectedCollisions = new HashSet<>();
        for (int i=0; i<getSpaceObjectsCount(); i++) {
            Collection<SpaceObject> spaceObjectCollisions = null;
            SpaceObject spaceObject = spaceObjects.get(i);
            for (int j=i+1; j<getSpaceObjectsCount(); j++) {
                SpaceObject otherSpaceObject = spaceObjects.get(j);
                if (spaceObject.collides(otherSpaceObject)) {
                    if (spaceObjectCollisions==null) {
                        spaceObjectCollisions = new HashSet<>();
                    }
                    spaceObjectCollisions.add(otherSpaceObject);
                }
            }
            if (spaceObjectCollisions!=null) {
                allCollisions.put(spaceObject, spaceObjectCollisions);
            }
        }
        for (Map.Entry<SpaceObject, Collection<SpaceObject>> entry : allCollisions.entrySet()) {
            Collection<SpaceObject> collision = new HashSet<>();
            collision.add(entry.getKey()); // object that detected collision
            collision.addAll(entry.getValue()); // objects that collide with it
            for (SpaceObject spaceObject : entry.getValue()) {
                if (allCollisions.containsKey(spaceObject)==true) { // if colliding object collides with others, add them
                    collision.addAll(allCollisions.get(spaceObject));
                }
            }
            for (Map.Entry<SpaceObject, Collection<SpaceObject>> otherEntry : allCollisions.entrySet()) {
                if (entry!=otherEntry && otherEntry.getValue().contains(entry.getKey())==true) {
                    collision.add(otherEntry.getKey());
                }
            }
            detectedCollisions.add(collision);
        }
        return detectedCollisions; // {{1,2,4}, {8,12}}
    }

    /**
     * Resolves detected collisions and returns a mapping of collection of colliding objects and its resolution.
     *
     * @param detectedCollisions    Detected collisions
     * @return                      Resolved collisions
     */
    public Map<Collection<SpaceObject>, SpaceObject> resolveCollisions(Collection<Collection<SpaceObject>> detectedCollisions) {
        Map<Collection<SpaceObject>, SpaceObject> resolvedCollisions = new HashMap<>();
        for (Collection<SpaceObject> detectedCollision : detectedCollisions) {
            Rectangle2D collisionBoundingRectangle = getObjectsBoundingRectangle(detectedCollision);
            double collisionMass = 0;
            Vector2D collisionMomentum = new Vector2D();
            SpaceObject dominantObject = null;
            for (SpaceObject spaceObject : detectedCollision) {
                Vector2D momentum = spaceObject.getMomentum();
                collisionMass += spaceObject.getMass();
                collisionMomentum = collisionMomentum.add(
                        momentum
                );
                if (dominantObject==null) {
                    dominantObject = spaceObject;
                } else if (momentum.magnitude() > dominantObject.getMomentum().magnitude()) {
                    dominantObject = spaceObject;
                }
            }
            Vector2D vel = collisionMomentum.mul(1/collisionMass);
            SpaceObject resolvedSpaceObject;
            if (dominantObject instanceof Planet) {
                resolvedSpaceObject = new Planet(
                        dominantObject.getName(),
                        new Vector2D(
                                collisionBoundingRectangle.getCenterX(),
                                collisionBoundingRectangle.getCenterY()
                        ),
                        vel,
                        collisionMass
                );
            } else {
                resolvedSpaceObject = new Comet(
                        dominantObject.getName(),
                        new Vector2D(
                                collisionBoundingRectangle.getCenterX(),
                                collisionBoundingRectangle.getCenterY()
                        ),
                        vel,
                        collisionMass
                );
            }
            resolvedSpaceObject.setUniverse(this);
            for (SpaceObject collidedObject : detectedCollision) {
                spaceObjects.remove(collidedObject);
                spaceObjectsCount--;
            }
            spaceObjects.add(resolvedSpaceObject);
            spaceObjectsCount++;
            resolvedCollisions.put(detectedCollision, resolvedSpaceObject);
        }
        return resolvedCollisions;
    }

    /**
     * Paints the whole universe (all the space objects in the universe) on the graphics context.
     *
     * @param graphics2D    Graphics context
     */
    @Override
    public void paint(Graphics2D graphics2D) {
        for (IHasPaintableTrajectory spaceObject : spaceObjects) {
            spaceObject.paintTrajectory(graphics2D);
        }
        for (IPaintable spaceObject : spaceObjects) {
            spaceObject.paint(graphics2D);
        }
    }

}
