package it.polimi.ingsw.eriantys.controller.phases;

import it.polimi.ingsw.eriantys.controller.Game;
import it.polimi.ingsw.eriantys.messages.GameMessage;
import it.polimi.ingsw.eriantys.messages.client.MoveStudent;
import it.polimi.ingsw.eriantys.messages.server.AssistantCardUpdate;
import it.polimi.ingsw.eriantys.model.exceptions.InvalidArgumentException;
import it.polimi.ingsw.eriantys.model.exceptions.IslandNotFoundException;
import it.polimi.ingsw.eriantys.model.exceptions.NoMovementException;
import it.polimi.ingsw.eriantys.server.exceptions.NoConnectionException;

import java.util.LinkedHashMap;

/**
 * This concrete implementation for the state design pattern involving {@link MessageHandler}
 * defines how the action phase message {@link MoveStudent} should be processed.
 */
public class MoveStudentHandler extends PlayCharacterCardHandler {
	private int movementCount;

	/**
	 * Constructs a new {@link MoveStudentHandler} for the specified game.
	 * @param game the {@link Game} this message handler refers to.
	 */
	public MoveStudentHandler(Game game) {
		super(game);
		this.movementCount = 0;
	}

	@Override
	public void handle(GameMessage m) throws NoConnectionException {
		if (m instanceof MoveStudent moveStudent)
			process(moveStudent);
		else super.handle(m);
	}

	@Override
	public void handleDisconnectedUser(String username) {
		game.receiveMotherNatureMovement();
	}

	@Override
	public void sendReconnectUpdate(String username) {
		game.sendBoardUpdate();
		game.sendUpdate(new AssistantCardUpdate(new LinkedHashMap<>(), game.getAssistantCards()), false);
	}

	private void process(MoveStudent message) throws NoConnectionException {
		String sender = message.getSender();
		String color = message.getColor();
		String destination = message.getDestination();

		try {
			game.moveStudent(sender, color, destination);
		} catch (NoMovementException e) {
			game.refuseRequest(message, "Invalid movement");
			return;
		} catch (IslandNotFoundException e) {
			game.refuseRequest(message, "Island not found: " + destination);
			return;
		} catch (InvalidArgumentException e) {
			game.refuseRequest(message, "Invalid color: " + color);
			return;
		}

		game.acceptRequest(message);
		movementCount++;
		checkStateTransition();
	}

	private void checkStateTransition() {
		if (movementCount == game.getCloudSize()) game.receiveMotherNatureMovement();
		else game.sendBoardUpdate();
	}
}