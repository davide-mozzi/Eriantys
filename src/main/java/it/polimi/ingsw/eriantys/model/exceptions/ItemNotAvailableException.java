package it.polimi.ingsw.eriantys.model.exceptions;

/**
 * This exception is thrown when trying to remove a certain item that is no longer there.
 */
public class ItemNotAvailableException extends Exception {

	/**
	 * Constructs a new exception with a custom message.
	 * @param message the detail message
	 */
	public ItemNotAvailableException(String message) {
		super(message);
	}
}