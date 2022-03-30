package it.polimi.ingsw.eriantys.model.influence;

import it.polimi.ingsw.eriantys.model.characters.CharacterCard;
import it.polimi.ingsw.eriantys.model.characters.MushroomGuy;

import it.polimi.ingsw.eriantys.model.Player;
import it.polimi.ingsw.eriantys.model.IslandGroup;
import it.polimi.ingsw.eriantys.model.Color;

import java.util.Set;

/**
 * This concrete implementation for the state design pattern involving {@link InfluenceCalculator}
 * defines the {@link MushroomGuy}'s {@link CharacterCard} effect.
 */
public class ExcludeColorInfluence extends CommonInfluence implements InfluenceCalculator {
    private final Color excluded;

    ExcludeColorInfluence(Color excluded) {
        this.excluded = excluded;
    }

    public int calculate(Player player, IslandGroup island, Set<Color> ownedProfessors) {
        int result = 0;

        for (Color c : ownedProfessors)
            if (c != excluded)
                result += evaluateColor(island, c);
        result += evaluateTowers(island, player);

        return result;
    }
}