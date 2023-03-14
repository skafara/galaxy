package application;

import history.VelocityHistory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import utility.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Represents a frame which contains a chart of velocity history
 *
 * @author  Stanislav Kafara
 * @version	2 2022-05-02
 */
public class VelocityChartFrame extends JFrame {

    /** Implicit time limit */
    private static final int IMPLICIT_TIME_LIMIT = 30;

    /** Chart dataset */
    private final XYSeriesCollection dataset;

    /** Chart */
    private final JFreeChart chart;

    /** Velocity history to be plotted */
    private VelocityHistory velocityHistory;

    /** Time limit (X-axis length) */
    private int timeLimit;

    /** TimerTask for periodic chart update */
    private final TimerTask periodicUpdate;

    /**
     * Constructs a velocity chart frame with provided velocity history.
     *
     * @param velocityHistory   Velocity history to be plotted
     */
    public VelocityChartFrame(VelocityHistory velocityHistory) {
        this(velocityHistory, IMPLICIT_TIME_LIMIT);
    }

    /**
     * Constructs a velocity chart frame with provided velocity history and time axis limit.
     *
     * @param velocityHistory   Velocity history to be plotted
     * @param timeLimit         Time axis limit
     */
    public VelocityChartFrame(VelocityHistory velocityHistory, int timeLimit) {
        this.velocityHistory = velocityHistory;
        this.timeLimit = timeLimit;

        this.dataset = new XYSeriesCollection();
        this.chart = ChartFactory.createXYLineChart(
                null,"Time [s]", "Velocity [km/h]",
                this.dataset
        );
        styleChart();
        setChartData(velocityHistory);

        this.periodicUpdate = new TimerTask() {
            @Override
            public void run() {
                updateChart();
            }
        };
        new Timer().schedule(
                this.periodicUpdate,
                1000/Simulation.VELOCITIES_FREQUENTION,
                1000/Simulation.VELOCITIES_FREQUENTION
        );

        ChartPanel chartPanel = new ChartPanel(this.chart);
        chartPanel.setPreferredSize(new Dimension(640, 480));
        add(chartPanel);
        pack();
        setMinimumSize(new Dimension(400, 300));
        setTitle("Stanislav Kafara, A21B0160P; Galaxy_SP2022 : Velocity Chart");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Sets the chart title.
     *
     * @param title Chart title
     */
    public void setChartTitle(String title) {
        chart.setTitle(title);
    }

    /**
     * Sets the velocity history to be plotted and updates the chart.
     *
     * @param velocityHistory   Velocity history to be plotted
     */
    public void setChartData(VelocityHistory velocityHistory) {
        this.velocityHistory = velocityHistory;
        updateChart();
    }

    /**
     * Sets chart style.
     */
    private void styleChart() {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(234, 234, 234));
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.getDomainAxis().setRange(-timeLimit, 0);
    }

    /**
     * Updates the chart dataset.
     */
    private void updateChart() {
        int limit = velocityHistory.getLength();
        int chartRange = timeLimit* Simulation.VELOCITIES_FREQUENTION+1;
        if (limit>chartRange) {
            limit = chartRange;
        }
        Iterator<Vector2D> iterator = velocityHistory.getHistory().descendingIterator();
        XYSeries xySeries = new XYSeries("Velocity");
        for (int t=0; t>-limit; t--) {
            Vector2D velocity = iterator.next();
            double kmh = 3.6*velocity.magnitude();
            xySeries.add(1.0/ Simulation.VELOCITIES_FREQUENTION*t, kmh);
        }
        dataset.removeAllSeries();
        dataset.addSeries(xySeries);
    }

}
