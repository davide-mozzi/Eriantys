package it.polimi.ingsw.eriantys.model;

import java.util.Set;

/**
 * This concrete implementation for the state design pattern involving {@link InfluenceCalculator}
 * defines the influence calculation when it is not affected by any {@link it.polimi.ingsw.eriantys.model.characters.CharacterCard}.
 */
public class NoEffectInfluence implements InfluenceCalculator {
    public int calculate(Player player, IslandGroup island, Set<Color> ownedProfessors) {
        return 42;

        /*
        int result = 0;

        StudentContainer students = island.getContainer();
        for (Color c : ownedProfessors)
            result += students.getQuantity(c);
        if (island.getController().equals(player))
            result += island.getNumberOfTowers();
        */
    }
}