package it.polimi.ingsw.eriantys.model;

import com.google.gson.JsonObject;
import it.polimi.ingsw.eriantys.model.exceptions.InvalidArgumentException;
import it.polimi.ingsw.eriantys.model.exceptions.IslandNotFoundException;
import it.polimi.ingsw.eriantys.model.exceptions.NoMovementException;
import it.polimi.ingsw.eriantys.model.exceptions.NotEnoughMovementsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {
	Player Alice = new Player("Alice", 9, 6);
	Player Bob = new Player("Bob", 9, 6);
	Player Eve = new Player("Eve", 9, 6);

	List<String> players = new ArrayList<>();

	@BeforeEach
	void init() {
		players.add(Alice.getNickname());
		players.add(Bob.getNickname());
		players.add(Eve.getNickname());
	}

	@Test
	void createGameManager_SinglePlayer_ThrowsGsonException() {
		assertThrows(Throwable.class, () -> new GameManager(List.of("foo"), false));
	}

	@Test
	void setupEntrances_NormalPreConditions_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		for (String p : gm.getTurnOrder()) {
			Map<String, Integer> entrance = gm.entranceRepresentation(p);
			for (String k : entrance.keySet()) {
				assertEquals(0, entrance.get(k));
			}
		}

		assertDoesNotThrow(gm::setupEntrances);

		for (String p : gm.getTurnOrder()) {
			Map<String, Integer> entrance = gm.entranceRepresentation(p);
			int numStudents = entrance.keySet().stream().mapToInt(entrance::get).reduce(0, Integer::sum);
			assertEquals(9, numStudents);
		}
	}

	@Test
	void setupPlayer_PassInvalidTowerColor_ThrowException() {
		GameManager gm = new GameManager(players, false);

		assertThrowsExactly(InvalidArgumentException.class, () -> gm.setupPlayer(Eve.getNickname(), "RED", "DESERT"));
	}

	@Test
	void setupPlayer_PassInvalidWizard_ThrowException() {
		GameManager gm = new GameManager(players, false);

		assertThrowsExactly(InvalidArgumentException.class, () -> gm.setupPlayer(Eve.getNickname(), "BLACK", "SPACE"));
	}

	@Test
	void setupPlayer_ValidParameters_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		assertDoesNotThrow(() -> gm.setupPlayer(Eve.getNickname(), "BLACK", "DESERT"));
	}

	@Test
	void setupRound_NormalPreConditions_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		Map<String, Map<String, Integer>> clouds = gm.cloudTilesRepresentation();
		for (Map<String, Integer> cloud : clouds.values()) {
			int numStudents = cloud.keySet().stream().mapToInt(cloud::get).reduce(0, Integer::sum);
			assertEquals(0, numStudents);
		}

		assertDoesNotThrow(gm::setupRound);

		clouds = gm.cloudTilesRepresentation();
		for (Map<String, Integer> cloud : clouds.values()) {
			int numStudents = cloud.keySet().stream().mapToInt(cloud::get).reduce(0, Integer::sum);
			assertEquals(4, numStudents);
		}
	}

	@Test
	void getAvailableAssistantCards_NoCardsPlayed_AllCardsAvailable() {
		GameManager gm = new GameManager(players, false);
		Map<String, List<String>> res = gm.getAvailableAssistantCards();
		for (List<String> cards : res.values()) {
			List<String> cardLiterals = Arrays.stream(AssistantCard.values()).map(AssistantCard::toString).toList();
			for (String card : cards) {
				assertTrue(cardLiterals.contains(card));
			}
			for (String card : cardLiterals) {
				assertTrue(cards.contains(card));
			}
		}
	}

	@Test
	void getAvailableAssistantCards_DogCardPlayed_AllButDogAvailable() {
		GameManager gm = new GameManager(players, false);

		Map<String, String> played = new HashMap<>();
		for (String p : gm.getTurnOrder())
			played.put(p, "DOG");
		gm.handleAssistantCards(played);

		Map<String, List<String>> res = gm.getAvailableAssistantCards();
		for (String player : res.keySet()) {
			List<String> cards = res.get(player);
			List<String> cardLiterals = Arrays.stream(AssistantCard.values()).map(AssistantCard::toString).toList();
			for (String card : cards) {
				assertEquals(!card.equals("DOG"), cardLiterals.contains(card));
			}
			for (String card : cardLiterals) {
				assertEquals(!card.equals("DOG"), cards.contains(card));
			}
		}
	}

	@Test
	void handleMovedStudent_NonexistentNickname_ThrowsInvalidArgumentException() {
		GameManager gm = new GameManager(players, false);
		assertThrowsExactly(InvalidArgumentException.class, () -> gm.handleMovedStudent("foo", "RED", "01"));
	}

	@Test
	void handleMovedStudent_InvalidColor_ThrowsInvalidArgumentException() {
		GameManager gm = new GameManager(players, false);
		assertThrowsExactly(InvalidArgumentException.class, () -> gm.handleMovedStudent("Bob", "bar", "01"));
	}

	@Test
	void handleMovedStudent_NonexistentIsland_ThrowsIslandNotFoundException() {
		GameManager gm = new GameManager(players, false);
		assertThrowsExactly(IslandNotFoundException.class, () -> gm.handleMovedStudent("Bob", "RED", "99"));
	}

	@Test
	void handleMovedStudent_UnavailableColor_ThrowsNoMovementException() {
		GameManager gm = new GameManager(players, false);
		assertDoesNotThrow(gm::setupEntrances);
		Map<String, Integer> entrance = gm.entranceRepresentation("Bob");
		String color = "RED";
		int numStudents = entrance.get(color);

		while(numStudents > 0) {
			assertDoesNotThrow(() -> gm.handleMovedStudent("Bob", color, "01"));
			numStudents--;
		}

		assertThrowsExactly(NoMovementException.class, () -> gm.handleMovedStudent("Bob", color, "01"));
	}

	@Test
	void handleMovedStudent_LegalParameters_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);
		assertDoesNotThrow(gm::setupEntrances);
		Map<String, Integer> entrance = gm.entranceRepresentation("Bob");
		List<String> availableColors = entrance.keySet().stream().filter(k -> entrance.get(k) > 0).toList();
		String color = availableColors.get(0);
		assertDoesNotThrow(() -> gm.handleMovedStudent("Bob", color, "01"));
	}

	@Test
	void handleMotherNatureMovement_NonexistentIsland_ThrowsIslandNotFoundException() {
		GameManager gm = new GameManager(players, false);
		assertThrowsExactly(IslandNotFoundException.class, () -> gm.handleMotherNatureMovement("939"));
	}

	@Test
	void handleMotherNatureMovement_NotEnoughMovements_ThrowsNotEnoughMovementsException() {
		GameManager gm = new GameManager(players, false);
		assertDoesNotThrow(gm::setupBoard);

		Map<String, String> played = new HashMap<>();
		for (String p : gm.getTurnOrder())
			played.put(p, "CHEETAH");
		gm.handleAssistantCards(played);
		assertDoesNotThrow(() -> gm.setCurrentPlayer("Bob"));

		int index = Integer.parseInt(gm.motherNatureIslandRepresentation());
		String dest = String.format("%02d", (index < 10 ? index + 3 : index - 9));
		assertThrowsExactly(NotEnoughMovementsException.class, () -> gm.handleMotherNatureMovement(dest));
	}

	@Test
	void handleMotherNatureMovement_LegalParameters_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);
		assertDoesNotThrow(gm::setupBoard);

		Map<String, String> played = new HashMap<>();
		for (String p : gm.getTurnOrder())
			played.put(p, "CHEETAH");
		gm.handleAssistantCards(played);

		for (String p : gm.getTurnOrder()) {
			int index = Integer.parseInt(gm.motherNatureIslandRepresentation());
			String dest = String.format("%02d", (index == 12 ? 1 : index + 1));
			assertDoesNotThrow(() -> gm.setCurrentPlayer(p));
			assertDoesNotThrow(() -> gm.handleMotherNatureMovement(dest));
			int updatedIndex = Integer.parseInt(gm.motherNatureIslandRepresentation());
			assertEquals((index == 12 ? 1 : index + 1), updatedIndex);
		}
	}

	@Test
	void handleSelectedCloud_NonexistentPlayer_ThrowsInvalidArgumentException() {
		GameManager gm = new GameManager(players, false);
		assertThrowsExactly(InvalidArgumentException.class, () -> gm.handleSelectedCloud("foo", 0));
	}

	@Test
	void handleSelectedCloud_CloudIndexOutOfBounds_ThrowsInvalidArgumentException() {
		GameManager gm = new GameManager(players, false);
		assertThrowsExactly(InvalidArgumentException.class, () -> gm.handleSelectedCloud("Bob", 3));
	}

	@Test
	void handleSelectedCloud_EmptyCloud_ThrowNoMovementException() {
		GameManager gm = new GameManager(players, false);
		assertDoesNotThrow(gm::setupBoard);
		assertDoesNotThrow(gm::setupRound);

		assertDoesNotThrow(() -> gm.handleSelectedCloud("Alice", 0));
		assertThrowsExactly(NoMovementException.class, () -> gm.handleSelectedCloud("Bob", 0));
	}

	@Test
	void handleSelectedCloud_LegalParameters_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);
		assertDoesNotThrow(gm::setupBoard);
		assertDoesNotThrow(gm::setupRound);

		Map<String, Integer> entrance = gm.entranceRepresentation("Alice");
		assertEquals(0, entrance.keySet().stream().mapToInt(entrance::get).reduce(0, Integer::sum));
		assertDoesNotThrow(() -> gm.handleSelectedCloud("Alice", 0));

		entrance = gm.entranceRepresentation("Alice");
		assertEquals(4, entrance.keySet().stream().mapToInt(entrance::get).reduce(0, Integer::sum));
	}

	@Test
	void HandleAssistantCardsAndGetTurnOrder() {
		GameManager gm = new GameManager(players, false);

		Map<String, String> playedCards = new HashMap<>();

		playedCards.put(Alice.getNickname(), AssistantCard.FOX.toString());
		playedCards.put(Bob.getNickname(), AssistantCard.TURTLE.toString());
		playedCards.put(Eve.getNickname(), AssistantCard.CHEETAH.toString());

		gm.handleAssistantCards(playedCards);	// ignore return statement

		List<String> turnOrder = gm.getTurnOrder();

		assertEquals(Eve.getNickname(), turnOrder.get(0));
		assertEquals(Alice.getNickname(), turnOrder.get(1));
		assertEquals(Bob.getNickname(), turnOrder.get(2));
	}

	@Test
	void changeInfluenceState_PassNull_ThrowException() {
		GameManager gm = new GameManager(players, false);

		assertThrowsExactly(InvalidArgumentException.class, () -> gm.changeInfluenceState(null));
	}

	@Test
	void handleCharacterCard_IndexOutOfBounds_ThrowException() {
		GameManager gm = new GameManager(players, true);

		assertThrowsExactly(InvalidArgumentException.class, () -> gm.handleCharacterCard(-1, new JsonObject()));
		assertThrowsExactly(InvalidArgumentException.class, () -> gm.handleCharacterCard(4, new JsonObject()));
	}

	@Test
	void getWinner_MoreTowers_ReturnCorrectPlayer() {
		GameManager gm = new GameManager(players, false);
		gm.getTurnOrder();
	}

	@Test
	void getWinner_NotLastRound_ReturnNull() {
		GameManager gm = new GameManager(players, false);
		assertNull(gm.getWinner());
	}

	@Test
	void entranceRepresentation_InvalidUsername_ThrowException() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertThrowsExactly(NullPointerException.class, () -> gm.entranceRepresentation("admin"));
	}

	@Test
	void entranceRepresentation_ValidUsername_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		assertNotNull(gm.entranceRepresentation(Eve.getNickname()));
	}

	@Test
	void diningRoomRepresentation_InvalidUsername_ThrowException() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertThrowsExactly(NullPointerException.class, () -> gm.diningRoomRepresentation("admin"));
	}

	@Test
	void diningRoomRepresentation_ValidUsername_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		assertNotNull(gm.diningRoomRepresentation(Eve.getNickname()));
	}

	@Test
	void towerColorRepresentation_BeforeSetup_ThrowException() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertThrowsExactly(NullPointerException.class, () -> gm.towerColorRepresentation("admin"));
		assertThrowsExactly(NullPointerException.class, () -> gm.towerColorRepresentation(Eve.getNickname()));
	}

	@Test
	void towerColorRepresentation_AfterSetupAndInvalidUsername_ThrowException() throws InvalidArgumentException {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		gm.setupPlayer(Alice.getNickname(), TowerColor.WHITE.toString(), Wizard.SNOW.toString());
		gm.setupPlayer(Bob.getNickname(), TowerColor.BLACK.toString(), Wizard.DESERT.toString());
		gm.setupPlayer(Eve.getNickname(), TowerColor.GREY.toString(), Wizard.SKY.toString());

		assertThrowsExactly(NullPointerException.class, () -> gm.towerColorRepresentation("admin"));
	}

	@Test
	void towerColorRepresentation_AfterSetupAndValidUsername_NormalPostConditions() throws InvalidArgumentException {
		GameManager gm = new GameManager(players, false);

		gm.setupPlayer(Alice.getNickname(), TowerColor.WHITE.toString(), Wizard.SNOW.toString());
		gm.setupPlayer(Bob.getNickname(), TowerColor.BLACK.toString(), Wizard.DESERT.toString());
		gm.setupPlayer(Eve.getNickname(), TowerColor.GREY.toString(), Wizard.SKY.toString());

		assertSame("WHITE", gm.towerColorRepresentation(Alice.getNickname()));
		assertSame("BLACK", gm.towerColorRepresentation(Bob.getNickname()));
		assertSame("GREY", gm.towerColorRepresentation(Eve.getNickname()));
	}

	@Test
	void towersRepresentation_UsernameNotFound_ThrowException() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertThrowsExactly(NullPointerException.class, () -> gm.towersRepresentation("admin"));
	}

	@Test
	void towersRepresentation_NoActions_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		for (String p : players)
			assertEquals(6, gm.towersRepresentation(p));
	}

	@Test
	void coinsRepresentation_SimplifiedModeAndInvalidUsername_ReturnNull() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertDoesNotThrow(() -> gm.coinsRepresentation("admin"));
		assertNull(gm.coinsRepresentation("admin"));
	}

	@Test
	void coinsRepresentation_SimplifiedModeAndValidUsername_ReturnNull() {
		GameManager gm = new GameManager(players, false);

		assertDoesNotThrow(() -> gm.coinsRepresentation(Eve.getNickname()));
		assertNull(gm.coinsRepresentation(Eve.getNickname()));
	}

	@Test
	void coinsRepresentation_ExpertModeAndInvalidUsername_ThrowException() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, true);

		assertThrowsExactly(NullPointerException.class, () -> gm.coinsRepresentation("admin"));
	}

	@Test
	void coinsRepresentation_ExpertModeAndValidUsernameAndNoAction_NormalPostConditions() {
		GameManager gm = new GameManager(players, true);

		assertEquals(1, gm.coinsRepresentation(Eve.getNickname()));
	}

	@Test
	void islandSizeRepresentation_PassInvalidIsle_ReturnNull() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertDoesNotThrow(() -> gm.islandSizeRepresentation("13"));	// The throwing does not happen because of the catch clause
		assertNull(gm.islandSizeRepresentation("13"));
	}

	@Test
	void islandSizeRepresentation_AfterSetup_ReturnSingleIslandSize() {
		GameManager gm = new GameManager(players, false);

		assertEquals(1, gm.islandSizeRepresentation("03"));
	}

	@Test
	void islandStudentsRepresentation_PassInvalidIsle_ReturnNull() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertDoesNotThrow(() -> gm.islandStudentsRepresentation("13"));	// The throwing does not happen because of the catch clause
		assertNull(gm.islandStudentsRepresentation("13"));
	}

	@Test
	void islandStudentsRepresentation_AfterSetup_ReturnNotNull() {
		GameManager gm = new GameManager(players, false);

		assertNotNull(gm.islandStudentsRepresentation("03"));
	}

	@Test
	void islandControllerRepresentation_PassInvalidIsle_ReturnNull() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertDoesNotThrow(() -> gm.islandControllerRepresentation("13"));	// The throwing does not happen because of the catch clause
		assertNull(gm.islandControllerRepresentation("13"));
	}

	@Test
	void islandControllerRepresentation_NoController_ReturnNull() {
		GameManager gm = new GameManager(players, false);

		assertNull(gm.islandControllerRepresentation("03"));
	}

	@Test
	void motherNatureIslandRepresentation_BeforeSetup_ThrowException() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertThrowsExactly(NullPointerException.class, gm::motherNatureIslandRepresentation);
	}

	@Test
	void motherNatureIslandRepresentation_AfterSetup_ReturnNotNull() throws InvalidArgumentException, NoMovementException {
		GameManager gm = new GameManager(players, false);

		gm.setupBoard();

		assertNotNull(gm.motherNatureIslandRepresentation());
	}

	@Test
	void islandNoEntryTilesRepresentation_SimplifiedModeAndInvalidIsle_ReturnNull() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertNull(gm.islandNoEntryTilesRepresentation("13"));
	}

	@Test
	void islandNoEntryTilesRepresentation_SimplifiedModeAndValidIsle_ReturnNull() {
		GameManager gm = new GameManager(players, false);

		assertNull(gm.islandNoEntryTilesRepresentation("03"));
	}

	@Test
	void islandNoEntryTilesRepresentation_ExpertModeAndInvalidIsle_ReturnNull() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, true);

		assertDoesNotThrow(() -> gm.islandNoEntryTilesRepresentation("13"));	// The throwing does not happen because of the catch clause
		assertNull(gm.islandNoEntryTilesRepresentation("13"));
	}

	@Test
	void islandNoEntryTilesRepresentation_ExpertModeAndValidIsle_ReturnZero() {
		GameManager gm = new GameManager(players, true);

		assertEquals(0, gm.islandNoEntryTilesRepresentation("03"));
	}

	@Test
	void cloudTilesRepresentation_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		assertEquals(3, gm.cloudTilesRepresentation().keySet().size());
	}

	@Test
	void professorsRepresentation_AfterSetup_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		Map<String, String> rep = gm.professorsRepresentation();

		assertEquals(5, rep.keySet().size());
		for (String c : rep.keySet())
			assertNull(rep.get(c));
	}

	@Test
	void charactersRepresentation_SimplifiedMode_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		assertNull(gm.charactersRepresentation());
	}

	@Test
	void charactersRepresentation_ExpertMode_NormalPostConditions() {
		GameManager gm = new GameManager(players, true);

		List<String> rep = gm.charactersRepresentation();

		assertEquals(3, rep.size());
		for (String c : rep)
			assertNotNull(c);
	}

	@Test
	void characterCostRepresentation_SimplifiedModeAndNonexistentCard_NormalPostConditions() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertNull(gm.characterCostRepresentation("Mage"));
	}

	@Test
	void characterCostRepresentation_SimplifiedModeAndExistentCard_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		assertNull(gm.characterCostRepresentation("Minstrel"));
	}

	@Test
	void characterCostRepresentation_ExpertModeAndNonexistentCard_NormalPostConditions() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, true);

		assertNull(gm.characterCostRepresentation("Mage"));
	}

	@Test
	void characterCostRepresentation_ExpertModeAndExistentCard_NormalPostConditions() {
		GameManager gm = new GameManager(players, true);

		Integer rep = gm.characterCostRepresentation("Minstrel");

		assertTrue(rep == null || (rep >= 1 && rep <= 4));
	}

	@Test
	void characterStudentsRepresentation_SimplifiedModeAndNonexistentCard_NormalPostConditions() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertNull(gm.characterStudentsRepresentation("Mage"));
	}

	@Test
	void characterStudentsRepresentation_SimplifiedModeAndExistentCard_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		assertNull(gm.characterStudentsRepresentation("Minstrel"));
	}

	@Test
	void characterStudentsRepresentation_ExpertModeAndNonexistentCard_NormalPostConditions() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, true);

		assertNull(gm.characterStudentsRepresentation("Mage"));
	}

	@Test
	void characterStudentsRepresentation_NotInstanceOfContainerCard_NormalPostConditions() {
		GameManager gm = new GameManager(players, true);

		assertNull(gm.characterStudentsRepresentation("Minstrel"));	// Minstrel is not a subclass of ContainerCharacterCard
	}

	@Test
	void characterNoEntryTilesRepresentation_SimplifiedModeAndNonexistentCard_NormalPostConditions() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, false);

		assertNull(gm.characterNoEntryTilesRepresentation("Mage"));
	}

	@Test
	void characterNoEntryTilesRepresentation_SimplifiedModeAndExistentCard_NormalPostConditions() {
		GameManager gm = new GameManager(players, false);

		assertNull(gm.characterNoEntryTilesRepresentation("Minstrel"));
	}

	@Test
	void characterNoEntryTilesRepresentation_ExpertModeAndNonexistentCard_NormalPostConditions() {
		// Note: the test case will never happen
		GameManager gm = new GameManager(players, true);

		assertNull(gm.characterNoEntryTilesRepresentation("Mage"));
	}
}