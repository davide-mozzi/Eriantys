package it.polimi.ingsw.eriantys.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BoardStatus {
	private final GameManager gm;
	private final PlayersInfo playersInfo;
	private final IslandsInfo islandsInfo;
	private final Map<String, Map<String, Integer>> cloudTiles;
	private final Map<String, String> professors;
	private final List<String> characterCards;

	public BoardStatus(GameManager gm) {
		this.gm = gm;
		this.playersInfo = new PlayersInfo();
		this.islandsInfo = new IslandsInfo();
		this.cloudTiles = gm.cloudTilesRepresentation();
		this.professors = gm.professorsRepresentation();
		this.characterCards = gm.charactersRepresentation();
	}

	private class PlayersInfo {
		private final List<String> players;
		private final Map<String, Map<String, Integer>> playersEntrances, playersDiningRooms;
		private final Map<String, Integer> playersTowers;
		private final Map<String, Integer> playersCoins;

		private PlayersInfo() {
			this.players = gm.getTurnOrder();

			this.playersEntrances = new LinkedHashMap<>();
			this.playersDiningRooms = new LinkedHashMap<>();
			this.playersTowers = new LinkedHashMap<>();
			this.playersCoins = new LinkedHashMap<>();

			for (String p : players) {
				playersEntrances.put(p, gm.entranceRepresentation(p));
				playersDiningRooms.put(p, gm.diningRoomRepresentation(p));
				playersTowers.put(p, gm.towersRepresentation(p));
				playersCoins.put(p, gm.coinsRepresentation(p));
			}
		}
	}

	private class IslandsInfo {
		private final List<String> islands;
		private final Map<String, Integer> islandsSizes;
		private final Map<String, Map<String, Integer>> islandsStudents;
		private final Map<String, String> islandsControllers;
		private final String motherNatureIsland;
		private final Map<String, Integer> islandsNoEntryTiles;

		private IslandsInfo() {
			this.islands = gm.islandsRepresentation();

			this.islandsSizes = new LinkedHashMap<>();
			this.islandsStudents = new LinkedHashMap<>();
			this.islandsControllers = new LinkedHashMap<>();

			this.motherNatureIsland = gm.motherNatureIslandRepresentation();

			this.islandsNoEntryTiles = new LinkedHashMap<>();

			for (String isle : islands) {
				this.islandsSizes.put(isle, gm.islandSizeRepresentation(isle));
				this.islandsStudents.put(isle, gm.islandStudentsRepresentation(isle));
				this.islandsControllers.put(isle, gm.islandControllerRepresentation(isle));
				this.islandsNoEntryTiles.put(isle, gm.islandNoEntryTilesRepresentation(isle));
			}
		}
	}
}