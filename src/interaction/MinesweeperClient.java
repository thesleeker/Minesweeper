package interaction;

import lib.Client;
import lib.List;
import logic.MinesweeperGame.Coordinates;
import logic.MinesweeperGame.Difficulty;

public class MinesweeperClient extends Client {
	public static class Command extends lib.Command.Client {
		public static final Command REGISTER = new Command(new String[] { "R: " }, true, new Action() {
			@Override
			protected void run(lib.Client client, String[] args) {
				MinesweeperClient msClient = (MinesweeperClient) client;
				if (!args[0].equals(msClient.nick)) {
					msClient.gui.addPlayer(args[0]);
				}
			}
		}), PLAYERS = new Command(new String[] { "P: " }, true, new Action() {
			@Override
			protected void run(lib.Client client, String[] args) {
				String[] players = args[0].split(", ");
				for (String player : players) {
					((MinesweeperClient) client).gui.addPlayer(player);
				}
			}
		}), FIELD = new Command(new String[] { "F: ", "; " }, true, new Action() {
			@Override
			protected void run(lib.Client client, String[] args) {
				MinesweeperClient msClient = (MinesweeperClient) client;
				
				String[] rows = args[1].substring(1, args[1].length() - 1).split("\\], \\["), fields = rows[0].split(", ");
				msClient.gui.newField(fields.length, rows.length, Integer.parseInt(args[0]));
				byte status;
				for (int y = 0; y < rows.length; y++) {
					fields = rows[y].split(", ");
					for (int x = 0; x < fields.length; x++) {
						status = Byte.parseByte(fields[x]);
						if (status >= 0) {
							msClient.gui.open(x, y, status);
						} else {
							msClient.gui.mark(x, y, status);
						}
					}
				}
			}
		}), DEREGISTER = new Command(new String[] { "D: " }, true, new Action() {
			@Override
			protected void run(lib.Client client, String[] args) {
				((MinesweeperClient) client).gui.removePlayer(args[0]);
			}
		}), NEW_GAME = new Command(new String[] { "N: ", ", ", ", " }, true, new Action() {
			@Override
			protected void run(lib.Client client, String[] args) {
				((MinesweeperClient) client).gui.newField(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
						Integer.parseInt(args[2]));
			}
		}), OPEN = new Command(new String[] { "O: ", ", ", ", " }, true, new Action() {
			@Override
			protected void run(lib.Client client, String[] args) {
				((MinesweeperClient) client).gui.open(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Byte.parseByte(args[2]));
			}
		}), LOST = new Command(new String[] { "L: ", "; " }, true, new Action() {
			@Override
			protected void run(lib.Client client, String[] args) {
				MinesweeperClient msClient = (MinesweeperClient) client;
				String[] coordinatesString = args[0].substring(1, args[0].length() -1).split(", ");
				Coordinates trigger = new Coordinates(Integer.parseInt(coordinatesString[0]), Integer.parseInt(coordinatesString[1]));
				msClient.gui.lost(trigger, parseCellList(args[1]));
			}
		}), WON = new Command(new String[] { "W" }, false, new Action() {
			@Override
			protected void run(lib.Client client, String[] args) {
				((MinesweeperClient) client).gui.won();
			}
		}), MARK = new Command(new String[] { "M: ", ", ", ", " }, true, new Action() {
			@Override
			protected void run(lib.Client client, String[] args) {
				((MinesweeperClient) client).gui.mark(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
						Byte.parseByte(args[2]));
			}
		}), ERROR = new Command(new String[] { "ERROR" }, false, new Action() {
			@Override
			protected void run(lib.Client client, String[] args) {
				((MinesweeperClient) client).gui.serverError();
			}
		});

		public static final Command[] SET = { OPEN, MARK, REGISTER, DEREGISTER, WON, LOST, NEW_GAME, FIELD, PLAYERS, ERROR };

		private Command(String[] blocks, boolean hasTrailingArg, lib.Command.Client.Action action) {
			super(blocks, hasTrailingArg, action);
		}

		private static List parseCellList(String list) {
			String[] stringCells = list.substring(1, list.length() - 1).split("\\], \\[");
			List cells = new List();
			String[] cellCoordinates;
			for (String cell : stringCells) {
				cellCoordinates = cell.split(", ");
				cells.append(
						new Coordinates(Integer.parseInt(cellCoordinates[0]), Integer.parseInt(cellCoordinates[1])));
			}
			return cells;
		}
	}

	private MinesweeperGUI gui;
	private String nick;

	public MinesweeperClient(String ip, int port, String nick, MinesweeperGUI gui) {
		super(ip, port);
		this.nick = nick;
		this.gui = gui;
		send(MinesweeperServer.Command.REGISTER.generateCommand(new String[] { nick }));
	}

	@Override
	public void processMessage(String message) {
		for (Command command : Command.SET) {
			try {
				command.run(this, message);
				return;
			} catch (IllegalArgumentException e) {
			}
		}
	}

	public void open(int x, int y) {
		send(MinesweeperServer.Command.OPEN.generateCommand(new String[] { "" + x, "" + y }));
	}

	public void mark(int x, int y, int mark) {
		send(MinesweeperServer.Command.MARK.generateCommand(new String[] { "" + x, "" + y, "" + mark }));
	}
	
	public void newGame(int width, int height, int mineCount) {
		send(MinesweeperServer.Command.NEW_GAME_CUSTOM.generateCommand(new Integer[] { width, height, mineCount }));
	}
	
	public void newGame(Difficulty difficulty) {
		send(MinesweeperServer.Command.NEW_GAME_PRESET.generateCommand(new Integer[] { difficulty.getNumber() }));
	}
}
