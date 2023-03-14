package history;

import universe.IHasVelocity;
import utility.Vector2D;

/**
 * Represents a velocity history
 *
 * @author Stanislav Kafara
 * @version 1 2022-05-02
 */
public class VelocityHistory extends AHistory<Vector2D> {

    /** Source of the values */
    private final IHasVelocity source;

    /**
     * Constructs an empty velocity history.
     *
     * @param source    Source of the values
     */
    public VelocityHistory(IHasVelocity source) {
        super();
        this.source = source;
    }

    /**
     * Constructs an empty limited velocity history.
     *
     * @param source    Source of the values
     * @param limit     Limit of the amount of saved values
     */
    public VelocityHistory(IHasVelocity source, int limit) {
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
        return source.getVelocity();
    }
}
