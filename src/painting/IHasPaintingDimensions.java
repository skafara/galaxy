package painting;

/**
 * Interface implemented by classes which painting has dimensions
 *
 * @author Stanislav Kafara
 * @version 1 2022-04-11
 */
public interface IHasPaintingDimensions {

    /**
     * Returns the width of the painting.
     *
     * @return Width of the painting
     */
    double getPaintingWidth();

    /**
     * Returns the height of the painting.
     *
     * @return Height of the painting
     */
    double getPaintingHeight();

}
