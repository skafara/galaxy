package universe;

import utility.Vector2D;

/**
 * Interface implemented by classes that have velocity
 *
 * @author Stanislav Kafara
 * @version 2 2022-05-02
 */
public interface IHasVelocity {

    /**
     * Returns velocity.
     *
     * @return  Velocity
     */
    Vector2D getVelocity();

}
