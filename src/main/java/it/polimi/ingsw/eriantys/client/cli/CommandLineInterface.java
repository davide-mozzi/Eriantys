package it.polimi.ingsw.eriantys.client.cli;

import com.google.gson.JsonObject;
import it.polimi.ingsw.eriantys.client.UserInterface;
import it.polimi.ingsw.eriantys.controller.GameInfo;
import it.polimi.ingsw.eriantys.messages.server.*;
import it.polimi.ingsw.eriantys.model.AssistantCard;
import it.polimi.ingsw.eriantys.model.BoardStatus;
import it.polimi.ingsw.eriantys.model.Color;
import it.polimi.ingsw.eriantys.model.GameConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This class represents the command line user interface (CLI).
 * It handles user inputs and messages received from the server.
 */
public class CommandLineInterface extends UserInterface {
	private final Scanner scanner;

	private boolean running;

	/**
	 * Constructs a {@link CommandLineInterface} object.
	 *
	 * @throws IOException if the {@link Scanner} to read user inputs couldn't be created
	 */
	public CommandLineInterface() throws IOException {
		super();
		this.scanner = new Scanner(System.in);
	}

	@Override
	public synchronized void showInfo(String details) {
		System.out.println(details + "\n");
	}

	@Override
	public synchronized void showError(String details) {
		System.out.println(ConsoleColors.ANSI_RED + details + ConsoleColors.ANSI_RESET + "\n");
	}

	private boolean wrongArgNumber(String[] tokens, int expected) {
		int argsNumber = tokens.length - 1;
		if (argsNumber != expected) {
			showError(String.format("Expected %d argument(s), received %d", expected, argsNumber));
			return true;
		}
		return false;
	}

	private boolean wrongArgNumber(String[] tokens, int min, int max) {
		int argsNumber = tokens.length - 1;
		if (argsNumber < min || argsNumber > max) {
			showError(String.format("Expected between %d and %d arguments, received %d", min, max, argsNumber));
			return true;
		}
		return false;
	}

	/**
	 * Shows the application title.
	 */
	private void showTitle() {
		String title = null;

		try (InputStream in = CommandLineInterface.class.getClassLoader().getResourceAsStream("graphics/title.txt")) {
			if (in == null)
				throw new RuntimeException();	// TODO: Is this necessary?
			title = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("This is a Throwable#printStackTrace() method call.");
			e.printStackTrace();
		}

		System.out.println(title);
		System.out.println("\n");
	}

	/**
	 * Starts the loop reading user inputs and handling commands.
	 * Notifies the client that the user interface is ready.
	 */
	@Override
	public void run() {
		synchronized (client) {
			client.notifyAll();
		}
		this.running = true;
		showTitle();
		while (running) {
			String line = scanner.nextLine();
			String[] tokens = line.split("( )+");
			try {
				switch (tokens[0].toLowerCase()) {
					case "/help", "/h" -> {
						if (wrongArgNumber(tokens, 0)) break;
						client.askHelp();
					}
					case "/user", "/u" -> {
						if (wrongArgNumber(tokens, 1)) break;
						client.sendHandshake(tokens[1]);
					}
					case "/lobbies", "/l" -> {
						if (wrongArgNumber(tokens, 0)) break;
						client.askLobbies();
					}
					case "/join", "/j" -> {
						if (wrongArgNumber(tokens, 1)) break;
						client.joinLobby(tokens[1]);
					}
					case "/create", "/cr" -> {
						if (wrongArgNumber(tokens, 1, 2)) break;
						client.createLobby(tokens[1], tokens.length == 3 ? tokens[2] : "true");
					}
					case "/leave", "/e" -> {
						if (wrongArgNumber(tokens, 0)) break;
						client.leaveLobby();
					}
					case "/wizard", "/w" -> {
						if (wrongArgNumber(tokens, 1)) break;
						client.setWizard(tokens[1].toUpperCase());
						showInfo(String.format("Wizard set to: %s", tokens[1]));
					}
					case "/tower", "/t" -> {
						if (wrongArgNumber(tokens, 1)) break;
						client.setTowerColor(tokens[1].toUpperCase());
						showInfo(String.format("Tower color set to: %s", tokens[1]));
					}
					case "/assistant", "/a" -> {
						if (wrongArgNumber(tokens, 1)) break;
						client.playAssistantCard(tokens[1].toUpperCase());
					}
					case "/dining", "/sd" -> {
						if (wrongArgNumber(tokens, 1)) break;
						client.moveStudent(tokens[1].toUpperCase(), GameConstants.DINING_ROOM);
					}
					case "/island", "/si" -> {
						if (wrongArgNumber(tokens, 2)) break;
						client.moveStudent(tokens[2].toUpperCase(), tokens[1]);
					}
					case "/mother", "/m" -> {
						if (wrongArgNumber(tokens, 1)) break;
						client.moveMotherNature(tokens[1]);
					}
					case "/cloud", "/cl" -> {
						if (wrongArgNumber(tokens, 1)) break;
						client.chooseCloud(Integer.parseInt(tokens[1]));
					}
					case "/character", "/ch" -> {
						if (wrongArgNumber(tokens, 1)) break;
						int id = Integer.parseInt(tokens[1]);
						client.setCharacterCard(id);
						showCharacterCardArgs(id);
					}
					case "/ccarguments", "/ccargs" -> parseCharacterCardArgs(Arrays.copyOfRange(tokens, 1, tokens.length));
					case "/bstat", "/bs" -> {
						if (wrongArgNumber(tokens, 0)) break;
						showBoard();
					}
					case "/sbstat", "/sbs" -> {
						if (wrongArgNumber(tokens, 0)) break;
						showSchoolBoard();
					}
					case "/acards", "/acl" -> {
						if (wrongArgNumber(tokens, 0)) break;
						showAssistantCards();
					}
					case "/ccards", "/ccl" -> {
						if (wrongArgNumber(tokens, 0)) break;
						showCharacterCards();
					}
					case "/cdesc", "/cc" -> {
						if (wrongArgNumber(tokens, 1)) break;
						showCharacterDescription(tokens[1]);
					}
					case "/ccstat", "/ccs" -> {
						if (wrongArgNumber(tokens, 1)) break;
						showCharacterStatus(tokens[1]);
					}
					case "/pstat", "/ps" -> {
						if (wrongArgNumber(tokens, 1)) break;
						showSchoolBoard(tokens[1]);
					}
					case "/reconnect", "/r" -> {
						if (wrongArgNumber(tokens, 0)) break;
						client.sendReconnect();
					}
					case "" -> {}
					default -> showError("Invalid command");
				}
			} catch (IndexOutOfBoundsException e) {
				showError("Missing argument");
			} catch (NumberFormatException e) {
				showError("Invalid number format");
			}
		}
	}

	private void parseCharacterCardArgs(String[] args) {
		if (client.getCharacterCard() == null) {
			showError("Select a character card first");
			return;
		}
		List<String> sourceColors = new ArrayList<>();
		List<String> destinationColors = new ArrayList<>();
		String targetColor = null;
		String targetIsland = null;
		boolean source = true;
		for (String arg : args) {
			if (Color.stringLiterals().contains(arg.toUpperCase())) {
				if (sourceColors.isEmpty()) {
					if (targetColor == null) {
						targetColor = arg.toUpperCase();
					} else {
						sourceColors.add(targetColor);
						targetColor = null;
						destinationColors.add(arg.toUpperCase());
					}
				} else if (source) {
					sourceColors.add(arg.toUpperCase());
					source = false;
				} else {
					destinationColors.add(arg.toUpperCase());
					source = true;
				}
			} else if (targetIsland == null) {
				targetIsland = arg;
			} else {
				showError("Invalid arguments: more than one argument that is not a valid color");
				return;
			}
		}
		if (sourceColors.size() != destinationColors.size()) {
			showError("Invalid arguments: must provide the same number of source and destination colors");
			return;
		}
		client.playCharacterCard(sourceColors.toArray(new String[0]),
				destinationColors.toArray(new String[0]),
				targetColor,
				targetIsland);
	}

	private void showCharacterCards() {
		BoardStatus boardStatus = client.getBoardStatus();
		if (boardStatus == null) return;
		List<String> cards = boardStatus.getCharacterCards();
		if (cards == null) return;
		StringBuilder output = new StringBuilder("Available character cards for this game:");
		for (int i = 0; i < cards.size(); i++) {
			String card = cards.get(i);
			output.append("\n[").append(i).append("] ").append(card);
		}
		showInfo(output.toString());
	}

	private void showCharacterCardArgs(int id) {
		BoardStatus boardStatus = client.getBoardStatus();
		if (boardStatus == null) return;
		String card = boardStatus.getCharacterCards().get(id);
		if (!characterCardInfo.getAsJsonObject(card).has("cmd")) {
			client.playCharacterCard(null, null, null, null);
			return;
		}
		String cmd = characterCardInfo.getAsJsonObject(card).get("cmd").getAsString();
		showInfo("Set the arguments for the " + card + " character card using:\n " + cmd);
	}

	private void showCharacterDescription(String card) {
		if (!characterCardInfo.has(card)) {
			showError("Invalid character card name");
			return;
		}
		JsonObject cardInfo = characterCardInfo.getAsJsonObject(card);
		String setup = Optional.ofNullable(cardInfo.get("setup")).map(j -> "Setup: " + j.getAsString() + "\n").orElse("");
		String effect = "Effect: " + cardInfo.get("effect").getAsString();
		showInfo(setup + effect);
	}

	private void showCharacterStatus(String card) {
		if (!characterCardInfo.has(card)) {
			showError("Invalid character card name");
			return;
		}
		BoardStatus boardStatus = client.getBoardStatus();
		if (boardStatus == null) return;
		StringBuilder output = new StringBuilder();
		Map<String, Integer> cardsCost = boardStatus.getCharacterCardsCost();
		if (cardsCost != null) {
			output.append("Cost: ");
			Integer cost = cardsCost.get(card);
			if (cost != null) {
				output.append(cost);
			}
		}
		Map<String, Integer> cardsNoEntryTiles = boardStatus.getCharacterCardsNoEntryTiles();
		if (cardsNoEntryTiles != null) {
			Integer noEntryTiles = cardsNoEntryTiles.get(card);
			if (noEntryTiles != null) {
				output.append("\nNo-entry tiles: ").append(noEntryTiles);
			}
		}
		Map<String, Map<String, Integer>> cardsStudents = boardStatus.getCharacterCardsStudents();
		if (cardsStudents != null) {
			Map<String, Integer> students = cardsStudents.get(card);
			if (students != null) {
				output.append("\nStudents:");
				for (String color : students.keySet()) {
					output.append("\n  ").append(students.get(color)).append(" ").append(color);
				}
			}
		}
		showInfo(output.toString());
	}

	private void showBoard() {
		BoardStatus boardStatus = client.getBoardStatus();
		if (boardStatus == null) return;
		String output;
		GridBuilder islandGridBuilder = new GridBuilder(3);
		final List<String> islands = boardStatus.getIslands();
		final Map<String, Integer> islandsSizes = boardStatus.getIslandSizes();
		final Map<String, Map<String, Integer>> islandsStudents = boardStatus.getIslandStudents();
		final Map<String, String> islandsControllers = boardStatus.getIslandControllers();
		final String motherNatureIsland = boardStatus.getMotherNatureIsland();
		final Map<String, Integer> islandsNoEntryTiles = boardStatus.getIslandNoEntryTiles();
		for (String island : islands) {
			StringBuilder islandStringBuilder = new StringBuilder();
			String controller = Optional.ofNullable(islandsControllers.get(island)).orElse("none");
			islandStringBuilder.append("[").append(island).append("]")
					.append("\ngroup of ").append(islandsSizes.get(island)).append(" islands")
					.append("\ncontroller: ").append(controller)
					.append("\nstudents:");
			Map<String, Integer> students = islandsStudents.get(island);
			for (String color : students.keySet()) {
				islandStringBuilder.append("\n  ").append(students.get(color)).append(" ").append(color);
			}
			if (island.equals(motherNatureIsland)) {
				islandStringBuilder.append("\nhas Mother Nature");
			}
			Integer noEntryTiles = islandsNoEntryTiles.get(island);
			if (noEntryTiles != null && noEntryTiles > 0) {
				islandStringBuilder.append("\nhas ").append(noEntryTiles).append(" no-entry tiles");
			}
			islandGridBuilder.add(islandStringBuilder.toString());
		}
		output = "Islands:\n" + islandGridBuilder;
		Map<String, Map<String, Integer>> cloudTiles = boardStatus.getCloudTiles();
		GridBuilder cloudGridBuilder = new GridBuilder(3);
		for (String cloud : cloudTiles.keySet()) {
			StringBuilder cloudStringBuilder = new StringBuilder();
			cloudStringBuilder.append("[").append(cloud).append("]");
			for (String color : cloudTiles.get(cloud).keySet()) {
				cloudStringBuilder.append("\n  ").append(cloudTiles.get(cloud).get(color)).append(" ").append(color);
			}
			cloudGridBuilder.add(cloudStringBuilder.toString());
		}
		output += "\n\nClouds:\n" + cloudGridBuilder;
		showInfo(output);
	}

	/**
	 * Prints the current status of the player's schoolboard.
	 */
	public void showSchoolBoard() {
		showSchoolBoard(client.getUsername());
	}

	/**
	 * Prints the current status of a player's schoolboard.
	 *
	 * @param player the player whose schoolboard should be printed
	 */
	public void showSchoolBoard(String player) {
		BoardStatus boardStatus = client.getBoardStatus();
		if (boardStatus == null) return;
		StringBuilder output = new StringBuilder(player).append("'s school board:");
		final List<String> players = boardStatus.getPlayers();
		if (!players.contains(player)) {
			showError(player + " not in player list");
			return;
		}
		final Map<String, Integer> playerEntrance = boardStatus.getPlayerEntrances().get(player);
		final Map<String, Integer> playerDiningRoom = boardStatus.getPlayerDiningRooms().get(player);
		final Integer playerTowers = boardStatus.getPlayerTowers().get(player);
		if (boardStatus.getPlayerCoins() != null) {
			final Integer playerCoins = boardStatus.getPlayerCoins().get(player);
			output.append("\ncoins: ").append(playerCoins);
		}
		final Map<String, String> professors = boardStatus.getProfessors();
		output.append("\ntowers: ").append(playerTowers);
		output.append("\nentrance:");
		for (String color : playerEntrance.keySet()) {
			output.append("\n  ").append(playerEntrance.get(color)).append(" ").append(color);
		}
		output.append("\ndining room:");
		for (String color : playerDiningRoom.keySet()) {
			output.append("\n  ").append(playerDiningRoom.get(color)).append(" ").append(color);
			if (Objects.equals(professors.get(color), player)) {
				output.append(" - PROFESSOR");
			}
		}
		showInfo(output.toString());
	}

	private void showAssistantCards() {
		List<String> cards = client.getAvailableCards();
		if (cards == null) return;
		StringBuilder output = new StringBuilder("Available assistant cards:");
		for (String cardName : cards) {
			AssistantCard card = AssistantCard.valueOf(cardName);
			output.append("\n- ")
					.append(cardName)
					.append(" (value = ")
					.append(card.value())
					.append(", movement = ")
					.append(card.movement())
					.append(")");
		}
		showInfo(output.toString());
	}

	/**
	 * {@inheritDoc}
	 * Prints a message letting the player know that the action has been accepted.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(Accepted message) {
		showInfo("Ok");
	}

	/**
	 * {@inheritDoc}
	 * Saves the selected username and checks if a reconnection is available.
	 * Prints a message letting the player know the username was accepted and how to reconnect.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(AcceptedUsername message) {
		showInfo("Ok");
		client.setUsername(message.getUsername());
		if (client.hasReconnectSettings()) {
			showInfo("Reconnection available, type /r or /reconnect to join");
		}
	}

	/**
	 * {@inheritDoc}
	 * Saves the game id and the reconnection settings.
	 * Prints a message letting the player know the lobby was joined correctly.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(AcceptedJoinLobby message) {
		client.setGameId(message.getGameId());
		client.putReconnectSettings(message);
		showInfo("Lobby joined");
	}

	/**
	 * {@inheritDoc}
	 * Removes reconnection settings.
	 * Prints a message letting the player know the lobby was left correctly.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(AcceptedLeaveLobby message) {
		client.removeReconnectSettings();
		showInfo("Lobby left");
	}

	/**
	 * {@inheritDoc}
	 * Prints the correct help message received from the server.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(HelpResponse message) {
		showInfo(message.getContent());
	}

	/**
	 * {@inheritDoc}
	 * Prints the list of available lobbies.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(AvailableLobbies message) {
		List<GameInfo> lobbies = message.getLobbies();
		if (lobbies.isEmpty()) {
			showInfo("No available lobbies");
		} else {
			StringBuilder output = new StringBuilder("Available lobbies:");
			for (GameInfo lobby : message.getLobbies()) {
				output.append("\n").append(lobby);
			}
			showInfo(output.toString());
		}
	}


	/**
	 * {@inheritDoc}
	 * Prints the list of players in the joined lobby
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(LobbyUpdate message) {
		StringBuilder output = new StringBuilder("Players in the lobby:");
		for (String player : message.getPlayers()) {
			output.append("\n- ").append(player);
		}
		showInfo(output.toString());
	}

	/**
	 * {@inheritDoc}
	 * Prints the assistant cards played by the other players and then, if the player is the next one to play,
	 * prints the available assistant cards.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(AssistantCardUpdate message) {
		client.setAvailableCards(message.getAvailableCards().get(client.getUsername()));
		if (!message.getPlayedCards().isEmpty()) {
			StringBuilder output = new StringBuilder("Played assistant cards:");
			Map<String, String> playedCards = message.getPlayedCards();
			for (String player : playedCards.keySet()) {
				String cardName = playedCards.get(player);
				AssistantCard card = AssistantCard.valueOf(cardName);
				output.append("\n- ")
						.append(player)
						.append(" \u2192 ")
						.append(cardName)
						.append(" (value = ")
						.append(card.value())
						.append(", movement = ")
						.append(card.movement())
						.append(")");
			}
			showInfo(output.toString());
		}
		super.handleMessage(message);
		if (Objects.equals(client.getUsername(), message.getNextPlayer())) {
			showAssistantCards();
		}
	}

	/**
	 * {@inheritDoc}
	 * Saves the board status.
	 * If the player is the next one to play, prints a message.
	 * @param message the received message
	 */
	@Override
	public void handleMessage(BoardUpdate message) {
		client.setBoardStatus(message.getStatus());
		super.handleMessage(message);
	}

	/**
	 * {@inheritDoc}
	 * Prints the tower colors and wizards selected by other players and, if the player is the next to choose,
	 * prints the remaining tower colors and wizards.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(UserSelectionUpdate message) {
		if (!message.getTowerColors().isEmpty() && !message.getWizards().isEmpty()) {
			StringBuilder output = new StringBuilder("User selections:");
			Map<String, String> towerColors = message.getTowerColors();
			Map<String, String> wizards = message.getWizards();
			for (String player : towerColors.keySet()) {
				String towerColor = towerColors.get(player);
				String wizard = wizards.get(player);
				output.append("\n- ")
						.append(player)
						.append(" \u2192 ")
						.append(towerColor)
						.append(" towers, ")
						.append(wizard)
						.append(" wizard");
			}
			showInfo(output.toString());
		}
		super.handleMessage(message);
		if (!Objects.equals(client.getUsername(), message.getNextPlayer())) return;
		StringBuilder output;
		if (client.getTowerColor() == null) {
			output = new StringBuilder();
			output.append("Choose a tower color between:");
			for (String color : message.getAvailableTowerColors()) {
				output.append("\n- ").append(color);
			}
			showInfo(output.toString());
		}
		if (client.getWizard() == null) {
			output = new StringBuilder();
			output.append("Choose a wizard between:");
			for (String wizard : message.getAvailableWizards()) {
				output.append("\n- ").append(wizard);
			}
			showInfo(output.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 * Removes reconnection settings.
	 * Prints the winner.
	 * Stops the application.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(GameOverUpdate message) {
		client.removeReconnectSettings();

		if (Objects.equals(message.getWinner(), GameConstants.TIE))
			showInfo("The game ends in a tie!");
		else
			showInfo(message.getWinner() + " is the winner of the game!");

		client.setRunning(false);
		running = false;

		showInfo("Press enter to exit");
	}

	/**
	 * {@inheritDoc}
	 * Saves the initial board status.
	 * Prints a message letting the player know that the game has started.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(InitialBoardStatus message) {
		showInfo("The game has begun!");
		client.setBoardStatus(message.getStatus());
	}

	/**
	 * {@inheritDoc}
	 * Prints a message with disconnection details.
	 *
	 * @param message the received message
	 */
	@Override
	public void handleMessage(DisconnectionUpdate message) {
		String subject = message.getSubject();
		int numPlayers = message.getNumPlayers();
		boolean gameIdle = message.isGameIdle();
		showInfo(subject + " has disconnected, "
				+ numPlayers + (numPlayers > 1 ? " players" : " player") + " currently connected"
				+ (gameIdle ? "\n\nGame idle" : ""));
	}

	@Override
	public synchronized void quit() {
		running = false;
		showError("An error has occurred, you have been disconnected");
		showInfo("Press enter to exit");
	}
}
