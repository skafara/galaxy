package painting;

import utility.Vector2D;

/**
 * Interface implemented by classes which painting is ensured to have minimal dimensions
 *
 * @author Stanislav Kafara
 * @version 1 2022-04-11
 */
public interface IHasMinimalPaintingDimensions {

    /**
     * Ensures that the painting has given minimal dimensions.
     *
     * @param canvasDimensions          Dimensions of the canvas
     * @param paintedAreaDimensions     Actual dimensions of the whole painted area
     * @param minimalPaintingDimensions Minimal painting dimensions which will be ensured
     */
    void ensureMinimalPaintingDimensions(
            Vector2D canvasDimensions, Vector2D paintedAreaDimensions,
            Vector2D minimalPaintingDimensions
    );

}
