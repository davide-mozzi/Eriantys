package it.polimi.ingsw.eriantys.controller.phases;

import it.polimi.ingsw.eriantys.messages.GameMessage;

/**
 * The processing of client-side commands by the controller is operated by a message handler entity.
 * This abstract class encloses a state pattern, which will be implemented by concrete classes.
 */
public interface MessageHandler {
	public void handle(GameMessage m);
}