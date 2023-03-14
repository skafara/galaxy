package universe;

import history.IHasTrajectory;
import history.IHasVelocityHistory;
import history.PositionHistory;
import history.VelocityHistory;
import painting.IHasMinimalPaintingDimensions;
import painting.IHasPaintableTrajectory;
import painting.IHasPaintingDimensions;
import painting.IPaintable;
import utility.Vector2D;

import java.awt.geom.Ellipse2D;

/**
 * Abstract ancestor of all space objects
 *
 * @author Stanislav Kafara
 * @version 2 2022-05-07
 */
public abstract class SpaceObject implements
        IHasPosition, IHasDimensions, IHasBoundingRectangle, IHasTrajectory,
        IPaintable, IHasPaintingDimensions, IHasMinimalPaintingDimensions,
        IHasPaintableTrajectory, ISelectable, IHasVelocity, IHasVelocityHistory
{

    /** Name of the space object */
    private final String name;

    /** Mass of the space object */
    private final double mass;

    /** The universe to which the space object belongs. */
    private Universe universe;

    /** The center of the space object */
    private Vector2D centerPosition;

    /** Trajectory of the space object */
    private final PositionHistory trajectory;

    /** Velocity of the space object */
    private Vector2D velocity;

    /** Velocity history */
    private final VelocityHistory velocityHistory;

    /**
     * Constructs a space object with provided properties.
     *
     * @param name              Name of the space object
     * @param centerPosition    The center of the space object
     * @param velocity          Velocity of the space object
     * @param mass              Mass of the space object
     */
    public SpaceObject
            (
                    String name,
                    Vector2D centerPosition, Vector2D velocity,
                    double mass
            )
    {
        this.name = name;
        this.centerPosition = centerPosition;
        this.trajectory = new PositionHistory(this);
        this.velocity = velocity;
        this.velocityHistory = new VelocityHistory(this);
        this.mass = mass;
    }

    /**
     * Performs hit-test.
     * Returns whether the position (point) is inside the space object.
     *
     * @param position  Position (point) against which the hit-test is performed.
     * @return          True, if the position (point) is inside the space object, else false.
     */
    public abstract boolean hitTest(Vector2D position);

    /**
     * Returns the name of the space object.
     *
     * @return  Name of the space object
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the mass of the space object.
     *
     * @return  Mass of the space object
     */
    public double getMass() {
        return mass;
    }

    /**
     * Make the space object to remember to which universe it belongs.
     * This <em>must be done before asking for acceleration</em> of the space object.
     *
     * @param universe                  The universe to which the space object belongs.
     * @throws IllegalStateException    In case that the universe has already been set.
     */
    public void setUniverse(Universe universe) throws IllegalStateException {
        if (this.universe==null) {
            this.universe = universe;
        } else {
            throw new IllegalStateException("The universe has already been set.");
        }
    }

    /**
     * Computes and returns the space object's acceleration.
     * This <em>can be called only after setting the universe</em> of the space object.
     *
     * @return  Acceleration of the space object
     */
    public Vector2D getAcceleration() {
        Vector2D acceleration = new Vector2D();
        for (SpaceObject otherSpaceObject : universe.getSpaceObjects()) {
            if (this==otherSpaceObject) {
                continue;
            }
            Vector2D r = otherSpaceObject.getCenterPosition()
                                .subtract(getCenterPosition());
            double rMagnitude = r.magnitude();
            acceleration = acceleration.add(
                    r.mul(
                            otherSpaceObject.getMass()/(rMagnitude*rMagnitude*rMagnitude)
                    )
            );
        }
        return acceleration.mul(universe.getGravitationalConstant());
    }

    /**
     * Computes and returns the space object's momentum.
     *
     * @return  Momentum of the space object
     */
    public Vector2D getMomentum() {
        return velocity.mul(mass);
    }

    /**
     * Returns the velocity of the space object.
     *
     * @return  Velocity of the space object
     */
    @Override
    public Vector2D getVelocity() {
        return velocity;
    }

    /**
     * Returns the velocity history.
     *
     * @return  Velocity history
     */
    @Override
    public VelocityHistory getVelocityHistory() {
        return velocityHistory;
    }

    /**
     * Returns the position of the center of the space object.
     *
     * @return  Position of the center of the space object
     */
    @Override
    public Vector2D getCenterPosition() {
        return centerPosition;
    }

    /**
     * Returns the trajectory of the space object.
     *
     * @return  Trajectory of the space object
     */
    @Override
    public PositionHistory getTrajectory() {
        return trajectory;
    }

    /**
     * Performs collision test.
     * Returns whether this and other space object collide.
     *
     * @param other Other space object against which the collision test is performed.
     * @return      True, if this and other space object collide, else false.
     */
    public boolean collides(SpaceObject other) {
        if (this instanceof Planet) {
            Planet thisPlanet = (Planet) this;
            if (other instanceof Planet) {
                Planet otherPlanet = (Planet) other;
                if  (
                        otherPlanet.getCenterPosition()
                                .subtract(thisPlanet.getCenterPosition())
                                .magnitude() <= thisPlanet.getRadius() + otherPlanet.getRadius()
                )
                {
                    return true;
                }
            } else if (other instanceof Comet) {
                if (
                        new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight()) // planet
                                .intersects(other.getBoundingRectangle()) // intersects with comet
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Changes the velocity of the object by given velocityChange.
     *
     * @param velocityChange    Velocity change to be added to the velocity of the space object
     */
    public void changeVelocityBy(Vector2D velocityChange) {
        this.velocity = this.velocity.add(velocityChange);
    }

    /**
     * Changes the position of the space object by given positionChange.
     *
     * @param positionChange    Position change to be added to the position of the space object
     */
    public void changePositionBy(Vector2D positionChange) {
        this.centerPosition = this.centerPosition.add(positionChange);
    }

    /**
     * Tests whether this and other object are equal.
     *
     * This and other object are equal if they meet following requirements:
     *  1) both of them are space objects,
     *  2) their names are equal.
     *
     * @param o Other object
     * @return  True, if this and other object are equal, else false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpaceObject other = (SpaceObject) o;
        return name.equals(other.name);
    }

    /**
     * Computes hashCode of the space object.
     *
     * HashCode depends on following:
     *  1) the name of the space object.
     *
     * @return  HashCode of the space obejct
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
