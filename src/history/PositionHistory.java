package history;

import universe.IHasPosition;
import utility.Vector2D;

/**
 * Represents a position history (trajectory)
 *
 * @author Stanislav Kafara
 * @version 1 2022-05-02
 */
public class PositionHistory extends AHistory<Vector2D> {

    /** Source of the values */
    private final IHasPosition source;

    /**
     * Constructs an empty position history.
     *
     * @param source    Source of the values
     */
    public PositionHistory(IHasPosition source) {
        super();
        this.source = source;
    }

    /**
     * Constructs an empty limited position history.
     *
     * @param source    Source of the values
     * @param limit     Limit of the amount of saved values
     */
    public PositionHistory(IHasPosition source, int limit) {
        super(limit);
        this.source = source;
    }

    /**
     * Returns current value of the source of the historical values.
     *
     * @return  Current value
     */
    @Override
    public Vector2D getCurrentValue() {
        return source.getCenterPosition();
    }
}
