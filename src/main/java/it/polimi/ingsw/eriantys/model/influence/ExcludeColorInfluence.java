package it.polimi.ingsw.eriantys.model.influence;

import it.polimi.ingsw.eriantys.model.Color;
import it.polimi.ingsw.eriantys.model.IslandGroup;
import it.polimi.ingsw.eriantys.model.Player;
import it.polimi.ingsw.eriantys.model.characters.CharacterCard;
import it.polimi.ingsw.eriantys.model.characters.MushroomGuy;
import it.polimi.ingsw.eriantys.model.exceptions.InvalidArgumentException;

import java.util.Set;

/**
 * This concrete implementation for the state design pattern involving {@link InfluenceCalculator}
 * defines the {@link MushroomGuy}'s {@link CharacterCard} effect.
 */
public class ExcludeColorInfluence extends CommonInfluence implements InfluenceCalculator {
	private final Color excluded;

	public ExcludeColorInfluence(Color excluded) {
		this.excluded = excluded;
	}

	@Override
	public int calculate(Player player, IslandGroup island, Set<Color> ownedProfessors) throws InvalidArgumentException {
		if (excluded == null)
			throw new InvalidArgumentException("Cannot proceed with null internal attributes parameter(s).");
		if (player == null || island == null || ownedProfessors == null)
			throw new InvalidArgumentException("Cannot proceed with null parameter(s).");

		int result = 0;

		for (Color c : ownedProfessors)
			if (c != excluded)
				result += evaluateColor(island, c);
		result += evaluateTowers(island, player);

		return result;
	}
}