package me.bullterrier292;

import java.util.ArrayList;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;




public class WorldRestoreCommands implements Listener, CommandExecutor {
	int task1;
	int task2;
	int task3;
	int task4;
	int taskGoThoughWholeMap;
	int goingChunks;
	int goingNotfixed;
	int fixThis;

	int autoChecker1;
	int autoChecker2;

	int counter1;
	int counter2;
	int counter3;
	int counter4;
	int counterChunk;
	// -288 86 112
	// + +

	int chunkNumber = 0;
	int pluschunks = 0;
	private String worldname = "world";

	public String cmd1 = "wr";
	public String cmd2 = "wrdc";
	public String cmd3 = "wrdsc";
	public String cmd4 = "wrautoc";
	public String cmd5 = "wrautosc";
	public String cmd6 = "wrthis";
	public String cmd7 = "wrstop";
	public String cmd8 = "wrset";
	public String cmd9 = "wrhelp";
	public String cmd10 = "wrthat";

	// =======================================================================================
	// =======================================================================================
	// =======================================================================================

	private ArrayList<String> wholeChunkExpired = new ArrayList();
	private ArrayList<String> surfaceChunksExpired = new ArrayList();

	// =======================================================================================
	// =======================================================================================
	// =======================================================================================
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Plugin p = WorldRestore.getPlugin(WorldRestore.class);

		if (sender.isOp() || sender instanceof ConsoleCommandSender) {

			if (cmd.getName().equalsIgnoreCase(cmd1)) {

				String section = "ChunkPoints.";
				boolean nameisFound = false;
				if (args.length == 1) {
					if (p.getConfig().contains(section)) {

						for (String key : p.getConfig().getConfigurationSection(section).getKeys(false)) {
							ConfigurationSection info = p.getConfig().getConfigurationSection(section);

							if (key.equalsIgnoreCase(args[0])) {

								String valuesInfo = info.getString(key);

								String[] valueParts = valuesInfo.split(":");

								String[] chunkCordinateParts = valueParts[0].split(",");
								int x = Integer.parseInt(chunkCordinateParts[0]);
								int z = Integer.parseInt(chunkCordinateParts[1]);
								World world = Bukkit.getServer().getWorld(worldname);
								Chunk c = world.getChunkAt(x, z);
								int y = Integer.parseInt(valueParts[1]);
								String DirecionStr = valueParts[2];
								int howManyChunksOnce = Integer.parseInt(valueParts[3]);
								int howManySecondDelay = Integer.parseInt(valueParts[4]);
								int howManyChunks = Integer.parseInt(valueParts[5]);

								goingToNextChunk(c, y, DirecionStr, howManyChunksOnce, howManySecondDelay,
										howManyChunks, true, true);
								nameisFound = true;
								break;
							}
						}
						if (nameisFound) {

							sender.sendMessage(ChatColor.GREEN + "World restore has been started!");
							Bukkit.broadcastMessage(ChatColor.GREEN
									+ "World restore has been started, server might lag little, lasts approximately 5h ");

						} else {

							sender.sendMessage(ChatColor.RED + "Name not found!");

						}

					}
				} else {
					sender.sendMessage(ChatColor.RED + "/wr name");
				}
				/*
				 * Chunk c = ((Player) sender).getLocation().getChunk(); int howManyChunksOnce =
				 * 2; int howManySecondDelay = 5; int howManyChunks = 3;
				 * 
				 * goingToNextChunk(c, 0, "ES", howManyChunksOnce, howManySecondDelay,
				 * howManyChunks);
				 */
				return true;
			} else if (cmd.getName().equalsIgnoreCase(cmd2)) {
				counter2 = 0;
				int howManyChunksOnce = 2;
				int howManySecondDelay = 4;
				int y = 0;
				Bukkit.getScheduler().cancelTask(task1);

				String sectionName1 = "Main.wholeChunkIds";

				if (p.getConfig().contains(sectionName1)) {

					for (String key : p.getConfig().getConfigurationSection(sectionName1).getKeys(false)) {
						counter2++;
					}

					counter2 = counter2 / howManyChunksOnce + 1;

					sender.sendMessage("starting");
					task1 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(p, new Runnable() {
						@Override
						public void run() {

							if (counter2 <= 0) {
								Bukkit.getScheduler().cancelTask(task1);
							}
							fixDetectedChunksToDefault(y, howManyChunksOnce);
							counter2--;

						}

					}, 0, 20 * howManySecondDelay);

				}

				return true;
			} else if (cmd.getName().equalsIgnoreCase(cmd3)) {
				counter3 = 0;
				int howManyChunksOnce = 3;
				int howManySecondDelay = 4;
				int y = 54;
				Bukkit.getScheduler().cancelTask(task2);
				if (p.getConfig().contains("Main.surfaceChunkIds")) {
					for (String key : p.getConfig().getConfigurationSection("Main.surfaceChunkIds").getKeys(false)) {
						counter3++;
					}

					counter3 = counter3 / howManyChunksOnce + 1;

					sender.sendMessage("starting");
					task2 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(p, new Runnable() {
						@Override
						public void run() {

							if (counter3 <= 0) {
								Bukkit.getScheduler().cancelTask(task2);
							}
							fixDetectedChunksToDefault(y, howManyChunksOnce);
							counter3--;
							// System.out.println(counter3);

						}

					}, 0, 20 * howManySecondDelay);

				}

				return true;
			} else if (cmd.getName().equalsIgnoreCase(cmd4)) {

				if (args.length > 0) {

					// System.out.println("starting");
					if (isInteger(args[0])) {
						sender.sendMessage(ChatColor.GREEN + "Automatic whole Chunks has been enabled");
						autochecker(0, Integer.parseInt(args[0]));
						autoUpdater(0);
					} else {
						sender.sendMessage(ChatColor.GREEN + "whole chunk updates have been canceled");
						Bukkit.getScheduler().cancelTask(task3);
						Bukkit.getScheduler().cancelTask(autoChecker1);
					}

				} else {
					sender.sendMessage(ChatColor.RED + "Give more arguments");
				}
				return true;
			} else if (cmd.getName().equalsIgnoreCase(cmd5)) {

				if (args.length > 0) {

					// System.out.println("starting");
					if (isInteger(args[0])) {
						sender.sendMessage(ChatColor.GREEN + "Automatic surface Chunks has been enabled");
						autochecker(1, Integer.parseInt(args[0]));
						autoUpdater(1);
					} else {
						sender.sendMessage(ChatColor.RED + "Surface chunk updates have been canceled");
						Bukkit.getScheduler().cancelTask(task4);
						Bukkit.getScheduler().cancelTask(autoChecker2);
					}

				} else {
					sender.sendMessage(ChatColor.RED + "Give more arguments");
				}
				return true;
			} else if (cmd.getName().equalsIgnoreCase(cmd6)) {

				if (sender instanceof Player) {
					Player player = (Player) sender;
					Chunk c = player.getLocation().getChunk();
					int x = c.getBlock(0, 0, 0).getX();
					int z = c.getBlock(0, 0, 0).getZ();

					if (args.length > 0) {
						if (isInteger(args[0])) {
							int chunks = Integer.parseInt(args[0]);

							Chunk corChunk = Bukkit.getServer().getWorld(worldname).getChunkAt(c.getX() - chunks,
									c.getZ() - chunks);

							chunks = chunks * 2;
							chunks += 1;
							fixThis(corChunk, 0, "ES", 3, 5, chunks, false, false);

						}

						sender.sendMessage(ChatColor.GREEN + "Chunk has been retored");
					} else {
						goingThroughChunks(worldname, x, 0, z, 256, 1, false, false);
						sender.sendMessage(ChatColor.GREEN + "Chunk has been retored");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Player can only use this command");
				}

				return true;
			} else if (cmd.getName().equalsIgnoreCase(cmd7)) {
				Bukkit.getScheduler().cancelTask(counterChunk);
				sender.sendMessage(ChatColor.RED + "Restore whole world has been canceled!");
				return true;

			} else if (cmd.getName().equalsIgnoreCase(cmd8)) {
				if (args.length == 5) {
					if (sender instanceof Player) {
						Chunk c = ((Player) sender).getLocation().getChunk();
						int chunkIdPart1 = c.getX();
						int chunkIdPart2 = c.getZ();
						String chunkid = String.valueOf(chunkIdPart1) + ',' + String.valueOf(chunkIdPart2);
						int y = 0;

						p.getConfig().set("ChunkPoints." + args[0],
								chunkid + ":" + y + ":" + args[1] + ":" + args[2] + ":" + args[3] + ":" + args[4]);
						p.saveConfig();
						sender.sendMessage(ChatColor.GREEN
								+ "ChunkPoint has been added to list. Now you can restore that point by /rw name");
					} else {
						sender.sendMessage(ChatColor.RED
								+ "/wrset Name Direction(for example ES) HowManyChunksOnce HowManySecondsDelayBetweenUpdates HowManyChunks(MultiliedItself)");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "This is command is only for players");
				}
				return true;
			} else if (cmd.getName().equalsIgnoreCase(cmd9)) {
				if (sender instanceof Player) {
					Player pla = (Player) sender;
					pla.performCommand("help WorldRestore");
				}
			} else if (cmd.getName().equalsIgnoreCase(cmd10)) {
				// String worldname, int x, int y, int z, int chunkHeight, int chunksTogo,
				// boolean checkPlayers, boolean checkRegions)
				// Location chunkloc = new Location(Bukkit.getServer().getWorld(worldname), x,
				// y, z);
				// Chunk c = Bukkit.getServer().getWorld(worldname).getChunkAt(chunkloc);

				int chunkX = 0;
				int chunkZ = 0;
				int y = 0;
				int chunkHeight = 256;

				if (args.length >= 4) {

					if (isInteger(args[0]) && isInteger(args[1]) && isInteger(args[2]) && isInteger(args[3])) {
						chunkX = Integer.parseInt(args[0]);
						chunkZ = Integer.parseInt(args[1]);
						y = Integer.parseInt(args[2]);
						chunkHeight = Integer.parseInt(args[3]);

					} else {

						return false;
					}

					Player player = (Player) sender;
					System.out.println(player.getWorld().getChunkAt(player.getLocation()));
					Chunk c = Bukkit.getServer().getWorld(worldname).getChunkAt(chunkX, chunkZ);
					int x = c.getBlock(0, 0, 0).getX();
					int z = c.getBlock(0, 0, 0).getZ();

					goingThroughChunks(worldname, x, y, z, chunkHeight, 1, false, false);
					sender.sendMessage(ChatColor.GREEN + "Chunk has been fixed");

					return true;
				} else {
					return false;
				}

			}
		} else {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that");
		}

		return true;

	}

	public static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	public void autoUpdater(int sectionValue) {
		Plugin p = WorldRestore.getPlugin(WorldRestore.class);

		int howManySecondDelay;

		if (sectionValue == 0) {

			howManySecondDelay = 10;

			task3 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(p, new Runnable() {
				@Override
				public void run() {
					if (wholeChunkExpired.size() > 0) {
						String chunkKey = wholeChunkExpired.get(0);
						fixSpecificChunk(chunkKey, sectionValue);
						wholeChunkExpired.remove(0);
					}
				}

			}, 0, 20 * howManySecondDelay);

		} else if (sectionValue == 1) {

			howManySecondDelay = 10;

			task4 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(p, new Runnable() {
				@Override
				public void run() {
					if (surfaceChunksExpired.size() > 0) {
						String chunkKey = surfaceChunksExpired.get(0);
						fixSpecificChunk(chunkKey, sectionValue);
						surfaceChunksExpired.remove(0);
					}
				}

			}, 0, 20 * howManySecondDelay);
		}

	}

	public void autochecker(int sectionValue, int timeInMs) {
		Plugin p = WorldRestore.getPlugin(WorldRestore.class);
		int howManySecondDelay = 5;
		if (sectionValue == 0) {
			autoChecker1 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(p, new Runnable() {
				@Override
				public void run() {
					wholeChunkExpired = checkChunkTimes(sectionValue, timeInMs);

				}

			}, 0, 20 * howManySecondDelay);
		} else if (sectionValue == 1) {
			autoChecker2 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(p, new Runnable() {
				@Override
				public void run() {
					surfaceChunksExpired = checkChunkTimes(sectionValue, timeInMs);
				}

			}, 0, 20 * howManySecondDelay);
		}
	}

	public void fixDetectedChunksToDefault(int y, int howManyChunksOnce) {
		Plugin p = WorldRestore.getPlugin(WorldRestore.class);
		int chunkHeight = 256;
		int chunksTogo = 1;
		int count = 0;
		String section = "Main.wholeChunkIds";

		if (y != 0) {
			section = "Main.surfaceChunkIds";
			chunkHeight = 131;
		}
		if (p.getConfig().contains(section)) {

			for (String key : p.getConfig().getConfigurationSection(section).getKeys(false)) {
				ConfigurationSection chunkInfo = p.getConfig().getConfigurationSection(section);

				String valuesInfo = chunkInfo.getString(key);

				String[] valueParts = valuesInfo.split(":");

				String[] chunkCordinateParts = valueParts[0].split(",");
				int x = Integer.parseInt(chunkCordinateParts[0]);
				int z = Integer.parseInt(chunkCordinateParts[1]);

				goingThroughChunks("world", x, y, z, chunkHeight, chunksTogo, true, true);

				count++;

				if (count >= howManyChunksOnce) {
					break;
				}
			}

		}

	}

	public void fixSpecificChunk(String chunkKey, int sectionValue) {
		Plugin p = WorldRestore.getPlugin(WorldRestore.class);
		String section = "Main.wholeChunkIds";
		int chunkHeight = 256;
		int y = 0;

		if (sectionValue == 0) {
			// who chunk
			section = "Main.wholeChunkIds";
			chunkHeight = 256;
			y = 0;
		} else if (sectionValue == 1) {
			// surface 55-130
			section = "Main.surfaceChunkIds";
			chunkHeight = 130;
			y = 55;
		}
		if (p.getConfig().contains(section + "." + chunkKey)) {
			ConfigurationSection chunkInfo = p.getConfig().getConfigurationSection(section);

			String valuesInfo = chunkInfo.getString(chunkKey);

			String[] valueParts = valuesInfo.split(":");

			String[] chunkCordinateParts = valueParts[0].split(",");
			int x = Integer.parseInt(chunkCordinateParts[0]);
			int z = Integer.parseInt(chunkCordinateParts[1]);

			goingThroughChunks("world", x, y, z, chunkHeight, 1, true, true);

		}
	}

	public void fixNotfixedChunks(int delayIns, int howManyChunksOnce) {
		Plugin p = WorldRestore.getPlugin(WorldRestore.class);
		String section = "Main.notFixed";

		Bukkit.getScheduler().cancelTask(goingNotfixed);

		goingNotfixed = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(p, new Runnable() {
			@Override
			public void run() {
				int chunkstogo = howManyChunksOnce;
				if (p.getConfig().contains(section)) {
					for (String key : p.getConfig().getConfigurationSection(section).getKeys(false)) {
						ConfigurationSection chunkInfo = p.getConfig().getConfigurationSection(section);

						String valuesInfo = chunkInfo.getString(key);

						String[] valueParts = valuesInfo.split(",");

						int x = Integer.parseInt(valueParts[0]);
						int z = Integer.parseInt(valueParts[1]);

						String section2 = section + "." + key;

						p.getConfig().set(section2, null);
						p.saveConfig();
						goingThroughChunks("world", x, 0, z, 256, 1, true, true);
						chunkstogo--;
						if (chunkstogo <= 0) {
							break;
						}
					}
				}
			}

		}, 0, 20 * delayIns);
	}

	public static ArrayList checkChunkTimes(int sectionValue, double timeWhenExpireMs) {
		Plugin p = WorldRestore.getPlugin(WorldRestore.class);
		String section = "Main.wholeChunkIds";
		ArrayList<String> chunkIds = new ArrayList();
		if (sectionValue == 0) {
			// who chunk
			section = "Main.wholeChunkIds";

		} else if (sectionValue == 1) {
			// surface 55-130
			section = "Main.surfaceChunkIds";

		}
		if (p.getConfig().contains(section)) {
			for (String key : p.getConfig().getConfigurationSection(section).getKeys(false)) {
				ConfigurationSection chunkInfo = p.getConfig().getConfigurationSection(section);

				String valuesInfo = chunkInfo.getString(key);

				String[] valueParts = valuesInfo.split(":");

				Calendar cal = Calendar.getInstance();
				Long timeNow = cal.getTimeInMillis();

				double placedTime = Double.parseDouble(valueParts[1]);

				double timeDif = timeNow - placedTime;
				// System.out.println(timeDif);
				if (timeDif >= timeWhenExpireMs) {

					chunkIds.add(key);

				}

			}
		}
		return chunkIds;
	}

	public void testingWG() {
		// System.out.println("test");
	}

	int chunkX;
	int chunkZ;

	int chunkSwitcher1;
	int chunkSwitcher2;
	int lastChunkX;
	int lastChunkZ;

	public boolean firstISlowerThan(int x, int y) {
		if (x <= y) {
			return true;
		}
		return false;
	}

	public void goingToNextChunk(Chunk c, int y, String DirecionStr, int howManyChunksOnce, int howManySecondDelay,
			int howManyChunks, boolean checkPlayers, boolean checkRegions) {
		Plugin plugin = WorldRestore.getPlugin(WorldRestore.class);

		DirecionStr = "ES";
		String worldname = "world";

		chunkX = c.getX();
		chunkZ = c.getZ();
		counterChunk = 1 + (howManyChunks * howManyChunks) / howManyChunksOnce;

		Bukkit.getScheduler().cancelTask(goingChunks);

		int defaultChunkX = chunkX;
		int defaultChunkZ = chunkZ;

		// effected by directionES
		int dirMultiplierX = 1;
		int dirMultiplierZ = 1;

		lastChunkX = chunkX;
		lastChunkX += (howManyChunks * dirMultiplierX);
		lastChunkZ = chunkZ;
		lastChunkZ += +(howManyChunks * dirMultiplierZ);

		boolean firstIslowerX = firstISlowerThan(chunkX, lastChunkX);
		boolean firstIslowerZ = firstISlowerThan(chunkZ, lastChunkZ);

		goingChunks = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {

				for (int i = 0; i < howManyChunksOnce; i++) {

					Chunk chunk = Bukkit.getServer().getWorld(worldname).getChunkAt(chunkX, chunkZ);

					int x = chunk.getBlock(0, 0, 0).getX();
					int z = chunk.getBlock(0, 0, 0).getZ();

					goingThroughChunks(worldname, x, y, z, 256, 1, checkPlayers, checkRegions);

					if (firstIslowerX) {
						if (chunkX == lastChunkX - 1) {
							// System.out.println(counterChunk);
							// System.out.println("Chunkid" + chunkX + "," + chunkZ);
							chunkZ += (1 * dirMultiplierZ);
							chunkX = defaultChunkX;
							if (chunkZ == lastChunkZ) {
								// System.out.println("PALALALA");
								Bukkit.getScheduler().cancelTask(goingChunks);
								break;
							}

						} else {
							chunkX += (1 * dirMultiplierX);
						}

					} else {
						if (lastChunkX == chunkX - 1) {
							chunkZ += (1 * dirMultiplierZ);
							chunkX = defaultChunkZ;
							if (chunkZ == lastChunkZ) {
								// System.out.println("PALALALA");
								Bukkit.getScheduler().cancelTask(goingChunks);
								break;
							}

						} else {
							chunkX += (1 * dirMultiplierX);
						}
					}

				}

				if (counterChunk <= 0) {
					Bukkit.getScheduler().cancelTask(goingChunks);
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Chunks has been fixed");
				}

				counterChunk--;

			}

		}, 0, 20 * howManySecondDelay);

	}

	public void fixThis(Chunk c, int y, String DirecionStr, int howManyChunksOnce, int howManySecondDelay,
			int howManyChunks, boolean checkPlayers, boolean checkRegions) {
		Plugin plugin = WorldRestore.getPlugin(WorldRestore.class);

		DirecionStr = "ES";
		String worldname = "world";

		chunkX = c.getX();
		chunkZ = c.getZ();
		pluschunks = 0;
		if ((howManyChunks * howManyChunks) % howManyChunksOnce > 0) {
			pluschunks += (howManyChunks * howManyChunks) % howManyChunksOnce;
		}
		Bukkit.getScheduler().cancelTask(fixThis);
		counterChunk = (howManyChunks * howManyChunks) / howManyChunksOnce;
		int defaultChunkX = chunkX;
		int defaultChunkZ = chunkZ;

		// effected by directionES
		int dirMultiplierX = 1;
		int dirMultiplierZ = 1;

		lastChunkX = chunkX;
		lastChunkX += howManyChunks;
		lastChunkZ = chunkZ;
		lastChunkZ += howManyChunks;

		boolean firstIslowerX = firstISlowerThan(chunkX, lastChunkX);
		boolean firstIslowerZ = firstISlowerThan(chunkZ, lastChunkZ);

		fixThis = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {

				for (int i = 0; i < howManyChunksOnce + pluschunks; i++) {

					Chunk chunk = Bukkit.getServer().getWorld(worldname).getChunkAt(chunkX, chunkZ);

					int x = chunk.getBlock(0, 0, 0).getX();
					int z = chunk.getBlock(0, 0, 0).getZ();
					goingThroughChunks(worldname, x, y, z, 256, 1, checkPlayers, checkRegions);
					// System.out.println(chunkX+","+chunkZ);

					if (firstIslowerX) {
						if (chunkX == lastChunkX - 1) {
							// System.out.println("nyt");
							chunkZ++;
							chunkX = defaultChunkX;
							if (chunkZ == lastChunkZ) {
								// System.out.println("PALALALA");
								Bukkit.getScheduler().cancelTask(fixThis);
								break;
							}

						} else {
							chunkX++;
						}

					} else {
						if (chunkX == lastChunkX - 1) {
							chunkZ++;
							chunkX = defaultChunkZ;
							if (chunkZ == lastChunkZ) {
								// System.out.println("asdasdasd");
								Bukkit.getScheduler().cancelTask(fixThis);
								break;
							}

						} else {
							chunkX++;
						}
					}

				}

				if (counterChunk <= 0) {
					Bukkit.getScheduler().cancelTask(fixThis);
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Chunks has been fixed");
				}

				counterChunk--;

			}

		}, 0, 20 * howManySecondDelay);

	}

	public void addChunksNotFixedConfig(Chunk c) {
		Plugin plugin = WorldRestore.getPlugin(WorldRestore.class);
		int x = c.getBlock(0, 0, 0).getX();
		int z = c.getBlock(0, 0, 0).getZ();
		int chunkIdPart1 = c.getX();
		int chunkIdPart2 = c.getZ();
		String chunkid = ' ' + String.valueOf(chunkIdPart1) + ',' + String.valueOf(chunkIdPart2) + ' ';
		plugin.getConfig().set("Main." + "notFixed." + chunkid, x + "," + z);
		plugin.saveConfig();

	}

	public boolean checkIfchunkHasNoPlayers(Chunk c) {
		Plugin p = WorldRestore.getPlugin(WorldRestore.class);
		String section = "Main.playerQuitLocations";
		int chunk_x = c.getX();
		int chunk_z = c.getZ();

		for (Player player : p.getServer().getOnlinePlayers()) {
			Chunk playerChunk = player.getLocation().getChunk();
			if (chunk_x == playerChunk.getX() && chunk_z == playerChunk.getZ()) {

				addChunksNotFixedConfig(c);
				return false;

			}
		}

		if (p.getConfig().contains(section)) {

			for (String key : p.getConfig().getConfigurationSection(section).getKeys(false)) {
				ConfigurationSection quitInfo = p.getConfig().getConfigurationSection(section);

				String valuesInfo = quitInfo.getString(key);

				String[] valueParts = valuesInfo.split(":");

				// double placedTime = Double.parseDouble(valueParts[1]);

				String[] chunkCordinateParts = valueParts[0].split(",");
				int x = Integer.parseInt(chunkCordinateParts[0]);
				int z = Integer.parseInt(chunkCordinateParts[1]);

				if (chunk_x == x && chunk_z == z) {

					addChunksNotFixedConfig(c);
					return false;

				}
			}

			p.saveConfig();
		}
		return true;

	}

	public void goingThroughChunks(String worldname, int x, int y, int z, int chunkHeight, int chunksTogo,
			boolean checkPlayers, boolean checkRegions) {
		Plugin plugin = WorldRestore.getPlugin(WorldRestore.class);

		//		ArrayList<Block> torchBlocks = new ArrayList<>();
		//		ArrayList<Block> signBlocks = new ArrayList<>();
		//		List fastBlocks = Arrays.asList(1, 2, 3, 4, 7, 14, 15, 16, 13, 48, 82);
		//		List materialSign = Arrays.asList(Material.WALL_SIGN, Material.SIGN);

		String default_world = "default_world";
		Location chunkloc = new Location(Bukkit.getServer().getWorld(worldname), x, y, z);
		Chunk c = Bukkit.getServer().getWorld(worldname).getChunkAt(chunkloc);
		boolean loadEnabled=false;
		
		
		
		
		

		boolean canGo;
		if (checkPlayers) {

			if (checkIfchunkHasNoPlayers(c)) {

				canGo = true;
			} else {
				// System.out.println("player at" + c.getX() + "," + c.getZ());
				canGo = false;
			}
		} else {
			canGo = true;
		}

		int chunkSizex = 16;
		int chunkSizez = 16;

		int default_x = x;
		int default_y = y;
		int default_z = z;

		int chunkSizeX = x + chunkSizex;
		int chunkSizeZ = z + chunkSizez;

		Block b,b2,block,dblock;
		Material worldBlock,default_worldBlock;
		ArrayList<Block> inventories=new ArrayList<>();


		// going thorugh chunks by z
		for (int chunknumber1 = 0; chunknumber1 < chunksTogo; chunknumber1++) {

			// going through chunks by x
			for (int chunknumber2 = 0; chunknumber2 < chunksTogo; chunknumber2++) {
				if (canGo) {

					// going through x axel
					for (; x < chunkSizeX; x++) {

						// going thorugh z axel
						for (; z < chunkSizeZ; z++) {

							// going through y axel
							for (; y < chunkHeight; y++) 
							{


								//============================================================
								//============================================================
								//============================================================


								// Bukkit.getServer().getWorld(worldname).getBlockAt(x, y,
								// z).setType(Material.STONE);

								b = Bukkit.getServer().getWorld(worldname).getBlockAt(x, y, z);
								b2 = Bukkit.getServer().getWorld(default_world).getBlockAt(x, y, z);

								worldBlock = b.getType();
								default_worldBlock = b2.getType();

								block=b;
								dblock=b2;
								Material dType=default_worldBlock;
								boolean canPlace = true;
								boolean isChest=false;
								// here
								if(dblock.getState() instanceof InventoryHolder)
								{
									for (ProtectedRegion r : WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getServer().getWorld(worldname))).getApplicableRegions(BukkitAdapter.asBlockVector(b.getLocation())) ) 
									{
										if (r.getOwners().size() > 0) 
										{
											canPlace = false;
										}
									}
									
									// if there is chest, or any other inventory put to the array and put their invs laiter to them after loops.. and clear them and place with air to remove item drop
									inventories.add(dblock);
									
									if(canPlace && block.getType() == dType) 
									{										
										InventoryHolder inv=(InventoryHolder)block.getState();
										inv.getInventory().clear();
										block.setType(Material.AIR);
										isChest=true;
									}

								}




								if (worldBlock != default_worldBlock || isChest) 
								{

									canPlace = true;
									//b.getLocation
									for (ProtectedRegion r : WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getServer().getWorld(worldname))).getApplicableRegions(BukkitAdapter.asBlockVector(b.getLocation())) ) 
									{
										if (r.getOwners().size() > 0) 
										{
											canPlace = false;
										}
									}

									if (canPlace || checkRegions == false) 
									{


										


										//replace block from other world if there is difference

										//get main blocks front so it will speed up process
										if(dType == Material.STONE || dType == Material.DIRT || dType == Material.GRAVEL || dType == Material.SAND || dType== Material.COBBLESTONE)
										{
											block.setType(dType);
										}
										else 
										{
											// double blocks like doors need to be set to no physics so it can be placed
											if(dType.toString().contains("GRASS") || dType.toString().contains("DOOR") || dType.toString().contains("FLOWER") || dType.toString().contains("BUSH") | dType.toString().contains("BANNER"))
											{
												block.setType(dType,false);

											}
											else
											{
												block.setType(dType);
											}	

											block.setBlockData(dblock.getBlockData());


										}				





									}

								}

								//============================================================
								//============================================================
								//============================================================
							}
							y = default_y;

						}
						y = default_y;
						z = default_z;

					}
					chunkSizeX = x;

				}


				// one chunk has row has been done => going next

				x = default_x;
				default_z += 16;
				z = default_z;
				y = default_y;

				chunkSizeX = x;
				chunkSizeZ = z;

				if(!inventories.isEmpty())
				{
					InventoryHolder dInv;
					InventoryHolder inv;
					Location loc;
					for(Block bb:inventories)
					{
						loc=bb.getLocation();
						dInv=(InventoryHolder)bb.getState();
						inv=(InventoryHolder)Bukkit.getServer().getWorld(worldname).getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getState();
						inv.getInventory().setContents(dInv.getInventory().getContents());
					}

					//inventories.clear();

				}


			}
		}

		

		int chunkIdPart1 = c.getX();
		int chunkIdPart2 = c.getZ();
		String chunkid = ' ' + String.valueOf(chunkIdPart1) + ',' + String.valueOf(chunkIdPart2) + ' ';
		String section;
		if (y <= 0 && chunkHeight > 130) {
			section = "Main.wholeChunkIds." + chunkid;
			plugin.getConfig().set(section, null);

			section = "Main.surfaceChunkIds." + chunkid;
			plugin.getConfig().set(section, null);
			plugin.saveConfig();

		} else {
			section = "Main.surfaceChunkIds." + chunkid;
			plugin.getConfig().set(section, null);
			plugin.saveConfig();
		}
	}



	

}
