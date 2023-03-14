package history;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Represents a collection of historical values.
 *
 * @author  Stanislav Kafara
 * @version 1 2022-04-30
 */
public abstract class AHistory<V> {

    /** Implicit values limit */
    private static final int IMPLICIT_VALUES_LIMIT = 10;

    /** History. */
    private final Deque<V> history;

    /** Amount of saved values */
    private int length;

    /** History is limited to this many values. */
    private int limit;

    /**
     * Constructs an empty history with implicit values limit.
     */
    public AHistory() {
        this(IMPLICIT_VALUES_LIMIT);
    }

    /**
     * Constructs an empty history with given properties.
     *
     * @param limit    Limit of the amount of saved values
     */
    public AHistory(int limit) {
        this.history = new ConcurrentLinkedDeque<>();
        this.limit = limit;
    }

    /**
     * Returns current value of the source of the historical values.
     *
     * @return  Current value
     */
    public abstract V getCurrentValue();

    /**
     * Returns the history.
     *
     * @return  History
     */
    public Deque<V> getHistory() {
        return history;
    }

    /**
     * Returns the length of the history.
     *
     * @return  Amount of saved values
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the limit of the amount of saved values.
     *
     * @return  Limit of the amount of saved values
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets the limit of the amount of saved values.
     *
     * @param limit Limit of the amount of saved values
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Adds current value to the end of the history.
     */
    public void addCurrentValue() {
        if (length>=limit) {
            for (int i=0; i<=length-limit; i++) {
                history.removeFirst();
                length--;
            }
        }
        history.addLast(getCurrentValue());
        length++;
    }

}
