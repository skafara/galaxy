package application;

import history.IHasTrajectory;
import history.IHasVelocityHistory;
import org.jfree.svg.SVGGraphics2D;
import universe.SpaceObject;
import utility.Vector2D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.*;

/**
 * Main class of the application
 *
 * @author	Stanislav Kafara
 * @version	3 2022-05-09
 */
public class Galaxy_SP2022 extends JFrame {

	/** Simulation */
	private final Simulation simulation;

	/** Drawing panel for the simulation */
	private final DrawingPanel drawingPanel;

	/**
	 * Space objects are limited to save this many velocities.
	 * 30 seconds of history + the most recent velocity (current)
	 */
	private final int VELOCITIES_LIMIT = 30*Simulation.VELOCITIES_FREQUENTION + 1;

	/** Mapping of a velocity history to a chart frame */
	private final Map<IHasVelocityHistory, Collection<VelocityChartFrame>> velocityChartFrames;

	/** Timer for periodic application updates */
	private final Timer timer;

	/** Unix timestamp of the last update of the application */
	private long lastUpdate;

	/** TimerTask for the periodic update of the application */
	private final TimerTask periodicUpdate;

	/** Simulation frames per second */
	private final int FPS = 60;

	/** Simulation speed-up */
	private double speedUp = 1;

	/**
	 * Runs the application.
	 *
	 * @param args	Program arguments; 0: data-file path
	 */
	public static void main(String[] args) {
		try {
			if (args.length==0) {
				throw new IllegalArgumentException("You must provide the data-file.");
			}
			Simulation simulation = Simulation.fromFile(Paths.get(args[0]));
			Galaxy_SP2022 application = new Galaxy_SP2022(simulation);
			application.launch();
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			System.out.format("Could not load data from file: %s.%n", args[0]);
			System.exit(1);
		}
	}

	/**
	 * Constructs the main application.
	 *
	 * @param simulation	The simulation to be visualized.
	 */
	public Galaxy_SP2022(Simulation simulation) {
		this.simulation = simulation;
		this.drawingPanel = new DrawingPanel(simulation);
		this.velocityChartFrames = new HashMap<>();

		this.timer = new Timer();
		this.periodicUpdate = new TimerTask() {

			int updateCounter = 0;

			@Override
			public void run() {
				long currentTimeMillis = System.currentTimeMillis();
				if (simulation.isRunning()==false) {
					lastUpdate = currentTimeMillis;
					return;
				}
				for (IHasTrajectory spaceObject : simulation.getUniverse().getSpaceObjects()) {
					if (updateCounter%(1000/spaceObject.getTrajectory().getLimit()) == 0) {
						spaceObject.getTrajectory().addCurrentValue();
					}
				}
				if (updateCounter%(1000/Simulation.VELOCITIES_FREQUENTION) == 0) {
					for (IHasVelocityHistory spaceObject : simulation.getUniverse().getSpaceObjects()) {
						spaceObject.getVelocityHistory().addCurrentValue();
					}
				}
				if (updateCounter%(1000/FPS) == 0) {
					double elapsedSec = (currentTimeMillis-lastUpdate)/1000.0; // time elapsed since last application update
					Simulation.Step step = simulation.step(speedUp*elapsedSec);
					for (IHasVelocityHistory spaceObject : step.getResolvedCollisions().values()) {
						spaceObject.getVelocityHistory().setLimit(VELOCITIES_LIMIT);
					}
					correctChartFrames(step.getResolvedCollisions());
					drawingPanel.correctObjectSelection(step.getResolvedCollisions());
					drawingPanel.repaint();
					lastUpdate = currentTimeMillis;
				}
				updateCounter = (updateCounter+1) % 1000;
			}
		};

		drawingPanel.setPreferredSize(new Dimension(800, 600));
		add(drawingPanel, BorderLayout.CENTER);
		add(getControlPanel(), BorderLayout.SOUTH);
		pack();
		setMinimumSize(new Dimension(400, 300));
		setLocationRelativeTo(null);
		setTitle("Stanislav Kafara, A21B0160P; Galaxy_SP2022");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * Launches the visualization.
	 * 	1) Sets action listeners.
	 * 	2) Sets periodic application update timer.
	 * 	3) Begins visualizing the simulation.
	 */
	private void launch() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new KeyEventDispatcher() {
					@Override
					public boolean dispatchKeyEvent(KeyEvent e) {
						if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_SPACE) {
							if (simulation.isRunning()==false) {
								simulation.resume();
							} else {
								simulation.pause();
							}
						}
						return false;
					}
				});
		drawingPanel.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						AffineTransform canvasTransform = drawingPanel.getCanvasTransform();
						Vector2D position = new Vector2D(
								e.getX(), e.getY()
						);
						boolean clickedOnSpaceObject = false;
						for (SpaceObject spaceObject : simulation.getUniverse().getSpaceObjects()) {
							if (drawingPanel.hitTest(canvasTransform, spaceObject, position)) {
								drawingPanel.selectObject(spaceObject);
								clickedOnSpaceObject = true;
								VelocityChartFrame velocityChartFrame = new VelocityChartFrame(
										spaceObject.getVelocityHistory()
								);
								velocityChartFrame.setChartTitle(spaceObject.getName());
								if (velocityChartFrames.containsKey(spaceObject)==false) {
									velocityChartFrames.put(spaceObject, new ArrayList<>());
								}
								velocityChartFrames.get(spaceObject).add(velocityChartFrame);
								break;
							}
						}
						if (clickedOnSpaceObject==false) {
							drawingPanel.selectObject(null);
						}
					}
				}
		);
		for (IHasVelocityHistory spaceObject : simulation.getUniverse().getSpaceObjects()) {
			spaceObject.getVelocityHistory().setLimit(VELOCITIES_LIMIT);
		}
		lastUpdate = System.currentTimeMillis();
		timer.schedule(periodicUpdate, 0, 1);
		setVisible(true);
		simulation.launch();
	}

	/**
	 * In case of a collision adapts the affected chart frames
	 * to the object that came from the collision as a resolution.
	 *
	 * @param resolvedCollisions	Resolved collisions
	 */
	private void correctChartFrames(Map<Collection<SpaceObject>, SpaceObject> resolvedCollisions) {
		for (Map.Entry<Collection<SpaceObject>, SpaceObject> resolvedCollision : resolvedCollisions.entrySet()) {
			Collection<VelocityChartFrame> mergedVelocityChartFrames = null;
			for (SpaceObject collidedObject : resolvedCollision.getKey()) {
				if (velocityChartFrames.containsKey(collidedObject)) {
					if (mergedVelocityChartFrames==null) {
						mergedVelocityChartFrames = new ArrayList<>();
					}
					for (VelocityChartFrame velocityChartFrame : velocityChartFrames.get(collidedObject)) {
						mergedVelocityChartFrames.add(velocityChartFrame);
					}
					velocityChartFrames.remove(collidedObject);
				}
			}
			if (mergedVelocityChartFrames!=null) {
				SpaceObject spaceObject = resolvedCollision.getValue();
				for (VelocityChartFrame velocityChartFrame : mergedVelocityChartFrames) {
					velocityChartFrame.setChartTitle(spaceObject.getName());
					velocityChartFrame.setChartData(spaceObject.getVelocityHistory());
				}
				velocityChartFrames.put(spaceObject, mergedVelocityChartFrames);
			}
		}
	}

	/**
	 * Creates and returns control panel.
	 *
	 * @return	Control panel
	 */
	private JPanel getControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.add(getExportControlPanel(), BorderLayout.WEST);
		controlPanel.add(getSpeedUpControlPanel(), BorderLayout.EAST);
		return controlPanel;
	}

	/**
	 * Creates and returns export control panel.
	 *
	 * @return	Export control panel
	 */
	private JPanel getExportControlPanel() {
		JPanel exportPanel = new JPanel();
		JButton pngButton = new JButton("Export To PNG");
		JButton svgButton = new JButton("Export To SVG");
		pngButton.addActionListener(e -> exportToPNG(Paths.get("./export/galaxy.png")));
		svgButton.addActionListener(e -> exportToSVG(Paths.get("./export/galaxy.svg")));
		pngButton.setFocusable(false);
		svgButton.setFocusable(false);
		exportPanel.add(pngButton);
		exportPanel.add(svgButton);
		return exportPanel;
	}

	/**
	 * Creates and returns speed-up control panel.
	 *
	 * @return	Speed-up control panel
	 */
	private JPanel getSpeedUpControlPanel() {
		JPanel speedUpPanel = new JPanel();
		JButton slowerButton = new JButton("2x Slower");
		JButton resetButton = new JButton("Reset");
		JButton fasterButton = new JButton("2x Faster");
		slowerButton.addActionListener(e -> speedUp *= 0.5);
		resetButton.addActionListener(e -> speedUp = 1);
		fasterButton.addActionListener(e -> speedUp *= 2);
		slowerButton.setFocusable(false);
		resetButton.setFocusable(false);
		fasterButton.setFocusable(false);
		speedUpPanel.add(fasterButton);
		speedUpPanel.add(resetButton);
		speedUpPanel.add(slowerButton);
		return speedUpPanel;
	}

	/**
	 * Exports the drawing panel to an image in PNG format.
	 *
	 * @param path	Path to the image
	 */
	private void exportToPNG(Path path) {
		try {
			BufferedImage image = new BufferedImage(drawingPanel.getWidth(), drawingPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics2D = image.createGraphics();
			drawingPanel.paint(graphics2D);
			assureExportDirectoryExists(path.getParent());
			ImageIO.write(image, "png", path.toFile());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Exports the drawing panel to an image in SVG format.
	 *
	 * @param path	Path to the image
	 */
	private void exportToSVG(Path path) {
		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
			SVGGraphics2D graphics2D = new SVGGraphics2D(drawingPanel.getWidth(), drawingPanel.getHeight());
			drawingPanel.paint(graphics2D);
			assureExportDirectoryExists(path.getParent());
			bufferedWriter.write(graphics2D.getSVGElement());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Assures the export directory exists, otherwise creates not existing directories.
	 *
	 * @param path Path to directory to be assured to exist.
	 */
	private void assureExportDirectoryExists(Path path) throws IOException {
		if (Files.exists(path)) {
			return;
		}
		Files.createDirectories(path);
	}

}
