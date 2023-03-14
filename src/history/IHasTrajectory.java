package history;

/**
 * Interface implemented by classes which have trajectory
 *
 * @author Stanislav Kafara
 * @version 1 2022-04-29
 */
public interface IHasTrajectory {

    /**
     * Returns the trajectory.
     *
     * @return  Trajectory
     */
    PositionHistory getTrajectory();

}
