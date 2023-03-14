package application;

import history.IHasTrajectory;
import universe.Comet;
import universe.Planet;
import universe.SpaceObject;
import universe.Universe;
import utility.Vector2D;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a simulation.
 *
 * @author Stanislav Kafara
 * @version 1 2022-05-02
 */
public class Simulation {

    /** This many velocities may be recorded in one second for a space object. */
    public static final int VELOCITIES_FREQUENTION = 5;

    /** Simulated universe */
    private final Universe universe;

    /** Status of the simulation */
    private boolean running;

    /** Simulation time of the simulated universe */
    private double time;

    /**
     * Speed of the simulation
     *
     * ... how many simulation seconds are in one actual second.
     */
    private double speed;

    /**
     * Returns a newly constructed simulation read from the data-file.
     *
     * @param path          Path to the data-file
     * @return              Newly constructed simulation
     * @throws IOException  In case of any issue while reading from the data-file
     */
    public static Simulation fromFile(Path path) throws IOException {
        try(BufferedReader bfr = Files.newBufferedReader(path)) {
            Collection<SpaceObject> spaceObjects = ConcurrentHashMap.newKeySet();

            String line = bfr.readLine(); // 0/double:gravitationalConstant, 1/double:simulationSpeed
            String[] values = line.split(",");
            double gravitationalConstant = Double.parseDouble(values[0]);
            double simulationSpeed = Double.parseDouble(values[1]);
            while ((line = bfr.readLine()) != null) {
                // 0/String:name, 1/String:type, 2/double:posX, 3/double:posY, 4/double:velX, 5/double:velY, 6/double:mass
                SpaceObject spaceObject = null;
                values = line.split(",");
                if (values[1].equals("universe.Planet")) {
                    spaceObject = new Planet(
                            values[0],
                            new Vector2D(
                                    Double.parseDouble(values[2]),
                                    Double.parseDouble(values[3])
                            ),
                            new Vector2D(
                                    Double.parseDouble(values[4]),
                                    Double.parseDouble(values[5])
                            ),
                            Double.parseDouble(values[6])
                    );
                } else if (values[1].equals("universe.Comet")) {
                    spaceObject = new Comet(
                            values[0],
                            new Vector2D(
                                    Double.parseDouble(values[2]),
                                    Double.parseDouble(values[3])
                            ),
                            new Vector2D(
                                    Double.parseDouble(values[4]),
                                    Double.parseDouble(values[5])
                            ),
                            Double.parseDouble(values[6])
                    );
                }
                // other space objects not being loaded yet
                if (spaceObject != null) {
                    spaceObjects.add(spaceObject);
                }
            }
            Universe universe = new Universe(gravitationalConstant, spaceObjects);
            for (SpaceObject spaceObject : universe.getSpaceObjects()) { // assign the created universe to the space objects
                spaceObject.setUniverse(universe);
            }
            return new Simulation(universe, simulationSpeed);
        }
    }

    /**
     * Constructs a simulation with given properties.
     *
     * @param universe  Universe
     * @param speed     Simulation speed
     */
    private Simulation(Universe universe, double speed) {
        this.universe = universe;
        this.speed = speed;
    }

    /**
     * Returns the simulated universe.
     *
     * @return  Simulated universe
     */
    public Universe getUniverse() {
        return universe;
    }

    /**
     * Returns the simulation time.
     *
     * @return  Simulation time
     */
    public double getTime() {
        return time;
    }

    /**
     * Returns the status of the simulation.
     *
     * @return  True, if the simulation is running, else false.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Launches the simulation.
     */
    public void launch() {
        if (time!=0) {
            throw new IllegalStateException("application.Simulation has already been launched.");
        }
        resume();
    }

    /**
     * Resumes the simulation.
     *  1) Sets the status of the simulation to running.
     */
    public void resume() {
        running = true;
    }

    /**
     * Pauses the simulation.
     *  1) Sets the status of the simulation to not running.
     */
    public void pause() {
        running = false;
    }

    /**
     * Performs a step of the simulation.
     *  1) Updates the system.
     *  2) Detects and resolves collisions.
     *
     * @param t Seconds elapsed (actual) since last simulation step
     * @return  Summary of the step of the simulation
     */
    public Step step(double t) {
        double elapsedSimulationTime = speed*t;
        time += elapsedSimulationTime;
        updateSystem(elapsedSimulationTime);
        Collection<Collection<SpaceObject>> detectedCollisions = universe.detectCollisions();
        Map<Collection<SpaceObject>, SpaceObject> resolvedCollisions = universe.resolveCollisions(detectedCollisions);
        return new Step(resolvedCollisions);
    }

    /**
     * Performs the simulation of {@code t} simulation seconds.
     * For each space object:
     *  1) determine its acceleration against other space objects
     *  2) and change its velocity and position accordingly.
     *
     * @param t Elapsed simulation time
     */
    private void updateSystem(double t) {
        double dt_min = speed;
        int trajectoriesPositionsLimit;
        if (universe.getSpaceObjectsCount()<=10) {
            dt_min /= 1000;
            trajectoriesPositionsLimit = 100;
        } else if (universe.getSpaceObjectsCount()<=100) {
            dt_min /= 100;
            trajectoriesPositionsLimit = 10;
        } else {
            dt_min /= 10;
            trajectoriesPositionsLimit = 5;
        }
        for (IHasTrajectory spaceObject : universe.getSpaceObjects()) {
            spaceObject.getTrajectory()
                    .setLimit(trajectoriesPositionsLimit);
        }

        while (t>0) {
            double dt = (t<dt_min) ? t : dt_min;

            Map<SpaceObject, Vector2D> accelerations = new HashMap<>();
            for (SpaceObject spaceObject : universe.getSpaceObjects()) {
                accelerations.put(
                        spaceObject,
                        spaceObject.getAcceleration()
                );
            }
            for (SpaceObject spaceObject : universe.getSpaceObjects()) {
                Vector2D acceleration = accelerations.get(spaceObject);
                Vector2D halfDA = acceleration.mul(0.5*dt);

                spaceObject.changeVelocityBy(halfDA);
                spaceObject.changePositionBy(
                        spaceObject.getVelocity().mul(dt)
                );
                spaceObject.changeVelocityBy(halfDA);
            }

            t -= dt;
        }
    }

    /**
     * Represents a summary of the simulation step.
     *
     * @author Stanislav Kafara
     * @version 1 2022-04-12
     */
    public class Step {

        /** Resolved collisions */
        private final Map<Collection<SpaceObject>, SpaceObject> resolvedCollisions;

        /**
         * Constructs a summary of the simulation step.
         *
         * @param resolvedCollisions    Resolved collisions
         */
        private Step(Map<Collection<SpaceObject>, SpaceObject> resolvedCollisions) {
            this.resolvedCollisions = resolvedCollisions;
        }

        /**
         * Returns resolved collisions.
         *
         * @return  Resolved collisions
         */
        public Map<Collection<SpaceObject>, SpaceObject> getResolvedCollisions() {
            return resolvedCollisions;
        }
    }

}
