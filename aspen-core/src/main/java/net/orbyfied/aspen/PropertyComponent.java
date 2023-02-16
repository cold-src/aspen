package net.orbyfied.aspen;

/**
 * A component which can be added to a property
 * to add extra functionality.
 */
public interface PropertyComponent<T, P> {

    default T checkLoadedValue(T val) {
        return val;
    }

}
