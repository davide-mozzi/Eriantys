package it.polimi.ingsw.eriantys.model.characters;

import it.polimi.ingsw.eriantys.model.Color;
import it.polimi.ingsw.eriantys.model.GameManager;
import it.polimi.ingsw.eriantys.model.IslandGroup;
import it.polimi.ingsw.eriantys.model.exceptions.IllegalInfluenceStateException;
import it.polimi.ingsw.eriantys.model.exceptions.ItemNotAvailableException;
import it.polimi.ingsw.eriantys.model.exceptions.NoMovementException;

import java.util.List;

/**
 * Represents the "herald" character card and the corresponding effect.
 *
 * @see CharacterCard
 */
public class Herald extends BaseCharacterCard {
    /**
     * Reference to the {@link GameManager} object for the current game.
     */
    private final GameManager gameManager;
    
    /**
     * Initial cost to activate the {@link Herald} effect.
     */
    private static final int INITIAL_COST = 3;

    /**
     * Constructs a new {@link Herald} character card.
     *
     * @param gameManager Reference to the {@link GameManager} for the current game.
     */
    public Herald(GameManager gameManager) {
        super(INITIAL_COST);
        this.gameManager = gameManager;
    }

    @Override
    public void applyEffect(List<Color> sourceColors,
                            List<Color> destinationColors,
                            Color targetColor,
                            IslandGroup targetIsland)
            throws NoMovementException, ItemNotAvailableException, IllegalInfluenceStateException {
        super.applyEffect(sourceColors, destinationColors, targetColor, targetIsland);
        gameManager.resolve(targetIsland); // TODO: 30/03/2022 Check for problems calling resolve at any time
    }
}
