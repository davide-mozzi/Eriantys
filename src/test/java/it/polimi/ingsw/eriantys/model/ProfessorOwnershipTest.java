package it.polimi.ingsw.eriantys.model;

import it.polimi.ingsw.eriantys.model.exceptions.InvalidArgumentException;
import it.polimi.ingsw.eriantys.model.exceptions.NoMovementException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ProfessorOwnershipTest {
	Player currentPlayer;

	@Test
	void getProfessors_PassNull_ReturnEmptySet() {
		assertEquals(0, new ProfessorOwnership(this::supplyPlayer).getProfessors(null).size());
	}

	@Test
	void getProfessors_PlayerWithNoProfessors_ReturnEmptySet() {
		assertEquals(0, new ProfessorOwnership(this::supplyPlayer).getProfessors(new Player("p", 9, 6)).size());
	}

	@Test
	void getProfessors_PlayerWithSomeProfessors_ReturnNonEmptySet() throws InvalidArgumentException, NoMovementException {
		Player p = new Player("p", 9, 6);
		this.currentPlayer = p;
		ProfessorOwnership ownership = new ProfessorOwnership(this::supplyPlayer);
		Bag bag = new Bag();

		bag.moveTo(p.getDiningRoom(), Color.PINK);
		ownership.update(new HashSet<>(List.of(Color.PINK)));
		assertTrue(ownership.getProfessors(p).contains(Color.PINK));
	}

	@Test
	void update_TieEffectActive_ChangeProfessorsWithWinOrTie() throws InvalidArgumentException, NoMovementException {
		Player p1 = new Player("p1", 7, 8);
		Player p2 = new Player("p2", 7, 8);
		Bag bag = new Bag();
		ProfessorOwnership ownership = new ProfessorOwnership(this::supplyPlayer);

		ownership.activateEffect();

		this.currentPlayer = p1;
		bag.moveTo(p1.getDiningRoom(), Color.BLUE);
		ownership.update(new HashSet<>(List.of(Color.BLUE)));
		assertTrue(ownership.getProfessors(p1).contains(Color.BLUE));

		this.currentPlayer = p2;
		bag.moveTo(p2.getDiningRoom(), Color.BLUE);
		ownership.update(new HashSet<>(List.of(Color.BLUE)));
		assertTrue(ownership.getProfessors(p2).contains(Color.BLUE));
		assertFalse(ownership.getProfessors(p1).contains(Color.BLUE));
	}

	@Test
	void update_TieEffectInactive_NoChangeProfessorsWithTie() throws InvalidArgumentException, NoMovementException {
		Player p1 = new Player("p1", 7, 8);
		Player p2 = new Player("p2", 7, 8);
		Bag bag = new Bag();
		ProfessorOwnership ownership = new ProfessorOwnership(this::supplyPlayer);

		ownership.activateEffect();
		ownership.deactivateEffect();

		this.currentPlayer = p1;
		bag.moveTo(p1.getDiningRoom(), Color.RED);
		ownership.update(new HashSet<>(List.of(Color.RED)));
		assertTrue(ownership.getProfessors(p1).contains(Color.RED));

		this.currentPlayer = p2;
		bag.moveTo(p2.getDiningRoom(), Color.RED);
		ownership.update(new HashSet<>(List.of(Color.RED)));
		assertTrue(ownership.getProfessors(p1).contains(Color.RED));
		assertFalse(ownership.getProfessors(p2).contains(Color.RED));
	}

	@Test
	void update_TieEffectActive_ChangeProfessorsWithWin() throws InvalidArgumentException, NoMovementException {
		Player p1 = new Player("p1", 7, 8);
		Player p2 = new Player("p2", 7, 8);
		Bag bag = new Bag();
		ProfessorOwnership ownership = new ProfessorOwnership(this::supplyPlayer);

		ownership.activateEffect();

		this.currentPlayer = p1;
		bag.moveTo(p1.getDiningRoom(), Color.YELLOW);
		ownership.update(new HashSet<>(List.of(Color.YELLOW)));
		assertTrue(ownership.getProfessors(p1).contains(Color.YELLOW));

		this.currentPlayer = p2;
		bag.moveTo(p2.getDiningRoom(), Color.YELLOW);
		bag.moveTo(p2.getDiningRoom(), Color.YELLOW);
		ownership.update(new HashSet<>(List.of(Color.YELLOW)));
		assertTrue(ownership.getProfessors(p2).contains(Color.YELLOW));
		assertFalse(ownership.getProfessors(p1).contains(Color.YELLOW));
	}

	@Test
	void update_TieEffectInactive_ChangeProfessorsWithWin() throws InvalidArgumentException, NoMovementException {
		Player p1 = new Player("p1", 7, 8);
		Player p2 = new Player("p2", 7, 8);
		Bag bag = new Bag();
		ProfessorOwnership ownership = new ProfessorOwnership(this::supplyPlayer);

		this.currentPlayer = p1;
		bag.moveTo(p1.getDiningRoom(), Color.YELLOW);
		ownership.update(new HashSet<>(List.of(Color.YELLOW)));
		assertTrue(ownership.getProfessors(p1).contains(Color.YELLOW));

		this.currentPlayer = p2;
		bag.moveTo(p2.getDiningRoom(), Color.YELLOW);
		bag.moveTo(p2.getDiningRoom(), Color.YELLOW);
		ownership.update(new HashSet<>(List.of(Color.YELLOW)));
		assertTrue(ownership.getProfessors(p2).contains(Color.YELLOW));
		assertFalse(ownership.getProfessors(p1).contains(Color.YELLOW));
	}

	@Test
	void update_TieEffectActive_NoChangeProfessorsWithTieAt0() {
		Player p = new Player("p", 9, 6);
		ProfessorOwnership ownership = new ProfessorOwnership(this::supplyPlayer);

		this.currentPlayer = p;

		assertEquals(0, ownership.getProfessors(p).size());
		ownership.activateEffect();
		ownership.update(Arrays.stream(Color.values()).collect(Collectors.toSet()));
		assertEquals(0, ownership.getProfessors(p).size());
	}

	@Test
	void update_TieEffectInactive_NoChangeProfessorsWithTieAt0() {
		Player p = new Player("p", 9, 6);
		ProfessorOwnership ownership = new ProfessorOwnership(this::supplyPlayer);

		this.currentPlayer = p;

		assertEquals(0, ownership.getProfessors(p).size());
		ownership.update(Arrays.stream(Color.values()).collect(Collectors.toSet()));
		assertEquals(0, ownership.getProfessors(p).size());
	}

	Player supplyPlayer() {
		return currentPlayer;
	}
}