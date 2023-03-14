package universe;

/**
 * Interface implemented by classes which can be selected
 *
 * @author Stanislav Kafara
 * @version 1 2022-04-11
 */
public interface ISelectable {

    /**
     * Realizes selection of the object.
     */
    void select();

    /**
     * Realizes deselection of the object.
     */
    void deselect();

}
