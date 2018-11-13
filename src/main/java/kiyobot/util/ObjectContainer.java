package kiyobot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A container class that holds and object. Useful for determining of an instance of an object is present without
 * exposing the object itself.
 *
 * @param <T> - Can hold any object
 * @author dk
 */
public class ObjectContainer<T> {

	private final T object;
	private final boolean objectIsPresent;
	private static final Logger LOGGER = LogManager.getLogger();

	public ObjectContainer(T object) {
		if(object != null) {
			this.object = object;
			objectIsPresent = true;
		} else {
			this.object = null;
			this.objectIsPresent = false;
		}
	}

	/**
	 * Returns the object present in this container
	 * @return object
	 */
	public T getObject() {
		return this.object;
	}

	/**
	 * Returns true if the object is present. False if else.
	 * @return boolean
	 */
	public boolean objectIsPresent() {
		return this.objectIsPresent;
	}
}
