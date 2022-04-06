package it.polimi.ingsw.eriantys.model;

import it.polimi.ingsw.eriantys.model.exceptions.IslandNotFoundException;
import it.polimi.ingsw.eriantys.model.exceptions.NoMovementException;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

	@Test
	void getIsland_PassNull_ThrowException() {
		assertThrowsExactly(IslandNotFoundException.class, () -> new Board(2, 3).getIsland(null));
	}

	@Test
	void getIsland_PassEmptyString_ThrowException() {
		assertThrowsExactly(IslandNotFoundException.class, () -> new Board(2, 3).getIsland(""));
	}

	@Test
	void getIsland_PassValidId_ReturnIsland() throws IslandNotFoundException {
		Board board = new Board(2, 3);
		IslandGroup res = board.getIsland("02");
		assertEquals("02", res.getId());
	}

	@Test
	void getIsland_PassInvalidIdLow_ThrowException() {
		assertThrowsExactly(IslandNotFoundException.class, () -> new Board(2, 3).getIsland("00"));
	}

	@Test
	void getIsland_PassInvalidIdHigh_ThrowException() {
		assertThrowsExactly(IslandNotFoundException.class, () -> new Board(2, 3).getIsland("13"));
	}

	@Test
	void getMotherNatureIsland_BeforeSetup_ThrowException() {
		assertNull(new Board(2, 3).getMotherNatureIsland());
	}

	@Test
	void getMotherNatureIsland_AfterSetup_ReturnValidIsland() {
		Board board = new Board(2, 3);
		board.setup();
		IslandGroup res = assertDoesNotThrow(board::getMotherNatureIsland);
		assertDoesNotThrow(() -> board.getIsland(res.getId()));
	}

	@Test
	void getMotherNatureIsland_NotDeployed_ReturnNull() {
		assertNull(new Board(2, 3).getMotherNatureIsland());
	}

	@Test
	void getMotherNatureIsland_DeployedIsland01_ReturnIsland01() throws IslandNotFoundException {
		Board board = new Board(2, 3);
		IslandGroup dest = board.getIsland("01");
		board.moveMotherNature(dest);
		assertEquals(dest, board.getMotherNatureIsland());
	}

	@Test
	void setup_NormalPostConditions() throws IslandNotFoundException {
		Board board = new Board(2, 3);
		board.setup();

		Map<Color, Integer> colors = new HashMap<>();
		List<Integer> emptyIslands = new ArrayList<>();

		for (int i = 0; i < 12; i++) {
			IslandGroup temp = board.getIsland(String.format("%02d", i+1));
			boolean foundColor = false;

			for (Color color : Color.values())
				if (temp.getQuantity(color) > 0) {
					// check that each island contains at most a student of each color
					assertEquals(1, temp.getQuantity(color));

					// check that on each island there is only one color such that there are students of that color on the island
					assertFalse(foundColor);
					foundColor = true;

					colors.put(color, 1 + (colors.get(color) == null ? 0 : colors.get(color)));
				}

			if (!foundColor)
				emptyIslands.add(i);
		}

		// check that there are exactly 2 students of each color deployed on the islands
		for (Color color : Color.values())
			assertEquals(2, colors.get(color));

		// check that there are exactly 2 islands with no students on it
		assertEquals(2, emptyIslands.size());

		// check that one of the 2 empty islands has Mother Nature on it
		assertTrue(
					board.getMotherNatureIsland().equals(board.getIsland(String.format("%02d", 1 + emptyIslands.get(0))))
					|| board.getMotherNatureIsland().equals(board.getIsland(String.format("%02d", 1 + emptyIslands.get(1)))));

		// check that the 2 empty islands are exactly one opposite the other
		assertEquals((int) emptyIslands.get(0), (emptyIslands.get(1) + 6) % 12);
	}

	@Test
	void drawStudents_NullPlayer_ThrowException() {
		assertThrowsExactly(NoMovementException.class,
					() -> new Board(2, 3).drawStudents(0, null));
	}

	@Test
	void drawStudents_IndexOutOfBounds_ThrowException() {
		Player p = new Player("p");
		assertThrowsExactly(NoMovementException.class,
					() -> new Board(2, 3).drawStudents(-1, p));
		assertThrowsExactly(NoMovementException.class,
					() -> new Board(2, 3).drawStudents(2, p));
	}

	@Test
	void drawStudents_EmptyCloud_ThrowException() {
		Board board = new Board(2, 3);
		Player p = new Player("p");
		assertThrowsExactly(NoMovementException.class, () -> board.drawStudents(0, p));
	}

	@Test
	void drawStudents_IndexAndPlayerOk_MoveStudents() throws NoMovementException {
		Board board = new Board(2, 3);
		Player p = new Player("p");
		board.refillClouds();
		board.drawStudents(0, p);
		assertEquals(3,
					Arrays.stream(Color.values()).mapToInt(p.getEntrance()::getQuantity).reduce(0, Integer::sum));
	}

	@Test
	void moveMotherNature_NonexistentIsland_ReturnFalse() {
		Board board = new Board(2, 3);
		assertFalse(board.moveMotherNature(new IslandGroup("99")));
	}

	@Test
	void moveMotherNature_ExistingIsland_ReturnTrueAndMoveMotherNature() throws IslandNotFoundException {
		Board board = new Board(2, 3);
		IslandGroup dest = board.getIsland("02");
		assertTrue(board.moveMotherNature(dest));
		assertEquals(dest, board.getMotherNatureIsland());
	}

	@Test
	void refillClouds_EmptyClouds_FillClouds() {
		Board board = new Board(2, 3);
		Player p = new Player("p");
		board.refillClouds();
		assertDoesNotThrow(() -> board.drawStudents(0, p));
		assertDoesNotThrow(() -> board.drawStudents(1, p));
	}

	@Test
	void unifyIslands_NullTarget_ThrowException() {
		assertThrowsExactly(IslandNotFoundException.class,
					() -> new Board(2, 3).unifyIslands(null));
	}

	@Test
	void unifyIslands_NonexistentTarget_ThrowException() {
		assertThrowsExactly(IslandNotFoundException.class,
					() -> new Board(2, 3).unifyIslands(new IslandGroup("89")));
	}

	@Test
	void unifyIslands_DifferentControllers_NoEffect() throws IslandNotFoundException {
		Board board = new Board(2, 3);
		Player p1 = new Player("p1");
		Player p2 = new Player("p2");

		board.getIsland("01").setController(p1);
		board.getIsland("02").setController(p2);
		board.getIsland("03").setController(p1);

		board.unifyIslands(board.getIsland("02"));

		assertDoesNotThrow(() -> board.getIsland("01"));
		assertDoesNotThrow(() -> board.getIsland("02"));
		assertDoesNotThrow(() -> board.getIsland("03"));
	}

	@Test
	void unifyIslands_TargetAndPrevSameController_MergeTargetAndPrev() throws IslandNotFoundException {
		Board board = new Board(2, 3);
		Player p1 = new Player("p1");
		Player p2 = new Player("p2");

		board.getIsland("01").setController(p1);
		board.getIsland("02").setController(p1);
		board.getIsland("03").setController(p2);

		board.unifyIslands(board.getIsland("02"));

		assertDoesNotThrow(() -> board.getIsland("03"));
		assertThrowsExactly(IslandNotFoundException.class, () -> board.getIsland("01"));
		assertThrowsExactly(IslandNotFoundException.class, () -> board.getIsland("02"));

		assertDoesNotThrow(() -> board.getIsland("01-02"));
	}

	@Test
	void unifyIslands_TargetAndNextSameController_MergeTargetAndNext() throws IslandNotFoundException {
		Board board = new Board(2, 3);
		Player p1 = new Player("p1");
		Player p2 = new Player("p2");

		board.getIsland("01").setController(p2);
		board.getIsland("02").setController(p1);
		board.getIsland("03").setController(p1);

		board.unifyIslands(board.getIsland("02"));

		assertDoesNotThrow(() -> board.getIsland("01"));
		assertThrowsExactly(IslandNotFoundException.class, () -> board.getIsland("02"));
		assertThrowsExactly(IslandNotFoundException.class, () -> board.getIsland("03"));

		assertDoesNotThrow(() -> board.getIsland("02-03"));
	}

	@Test
	void unifyIslands_TargetPrevAndNextSameController_MergeTargetPrevAndNext() throws IslandNotFoundException {
		Board board = new Board(2, 3);
		Player p1 = new Player("p1");

		board.getIsland("01").setController(p1);
		board.getIsland("02").setController(p1);
		board.getIsland("03").setController(p1);

		board.unifyIslands(board.getIsland("02"));

		assertThrowsExactly(IslandNotFoundException.class, () -> board.getIsland("01"));
		assertThrowsExactly(IslandNotFoundException.class, () -> board.getIsland("02"));
		assertThrowsExactly(IslandNotFoundException.class, () -> board.getIsland("03"));

		assertDoesNotThrow(() -> board.getIsland("01-02-03"));
	}
}