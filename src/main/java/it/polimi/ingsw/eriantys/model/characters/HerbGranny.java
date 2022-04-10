package it.polimi.ingsw.eriantys.model.characters;

import it.polimi.ingsw.eriantys.model.Board;
import it.polimi.ingsw.eriantys.model.Color;
import it.polimi.ingsw.eriantys.model.IslandGroup;
import it.polimi.ingsw.eriantys.model.exceptions.DuplicateNoEntryTileException;
import it.polimi.ingsw.eriantys.model.exceptions.InvalidArgumentException;
import it.polimi.ingsw.eriantys.model.exceptions.ItemNotAvailableException;

import java.util.List;
import java.util.Stack;

/**
 * Represents the "herb granny" character card and the corresponding effect.
 *
 * @see CharacterCard
 */
public class HerbGranny extends BaseCharacterCard {
    /**
     * No Entry tiles currently available on this card.
     */
    private final Stack<Integer> tiles;

    /**
     * Reference to the {@link Board} object for the current game.
     */
    private final Board board;

    /**
     * Initial cost to activate the {@link HerbGranny} effect.
     */
    private static final int INITIAL_COST = 2;

    /**
     * Total number of No Entry tiles available in the game.
     */
    private static final int MAX_ENTRY_TILES = 4;

    /**
     * Constructs a new {@link HerbGranny} character card.
     *
     * @param board Reference to the {@link Board} object for the current game.
     */
    public HerbGranny(Board board) {
        super(INITIAL_COST);
        this.tiles = new Stack<>();
        this.board = board;
    }

    /**
     * Method called when a No Entry tile is removed from an island to put it back on this card.
     *
     * @param id ID of the tile to put back.
     *
     * @see Board
     * @see IslandGroup
     */
    public void returnTile(int id) {
        if (!tiles.contains(id)) {
            tiles.push(id);
        }
    }

    @Override
    public void setupEffect() {
        super.setupEffect();
        for (int i = 0; i < MAX_ENTRY_TILES; i++) {
            tiles.push(MAX_ENTRY_TILES - i);
        }
        board.setReturnNoEntryTile(this::returnTile);
    }

    @Override
    public void applyEffect(List<Color> sourceColors,
                            List<Color> destinationColors,
                            Color targetColor,
                            IslandGroup targetIsland) throws ItemNotAvailableException, DuplicateNoEntryTileException, InvalidArgumentException {
        if (targetIsland == null) {
            throw new InvalidArgumentException("targetIsland argument is null.");
        }
        if (tiles.empty()) {
            throw new ItemNotAvailableException("There are no NoEntry tiles on the HerbGranny character card.");
        }
        targetIsland.putNoEntryTile(tiles.pop());
        increaseCost();
    }
}
