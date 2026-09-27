package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import imu.DontLoseItems.CustomEnd.EndEvents;
import imu.iAPI.Utilities.ImusUtilities;

public class EndEvent_UnstableGround extends EndEvent
{

	private LinkedList<Chunk> _chunks = new LinkedList<>();
	private int _ticks = 0;
	private final int _delayTicksToSpawn = 20 * 1;

	private final int _chunkKeeps = 9;
	private final int _chunkChecks = 10;

	public EndEvent_UnstableGround()
	{
		super("Unstable Ground", 60);

		ChestLootAmount = 2;
	}

	@Override
	public void OnEventStart()
	{
		_ticks = 0;
		_chunks.clear();
	}

	@Override
	public void OnEventEnd()
	{
		_ticks = 0;
		_chunks.clear();
		AddChestLootBaseToAll(ChestLootAmount);

	}

	private void SpawnUnstableAround(Player player)
	{
		Chunk[] chunks = ImusUtilities.Get9ChunksAround(player.getLocation());
		// Chunk[] chunks = ImusUtilities.GetChunksAround(player.getLocation(), 2);

		int player_y = player.getLocation().getBlockY();
		Chunk chunk = null;

		for (int i = 0; i < _chunkChecks; i++)
		{
			chunk = chunks[ThreadLocalRandom.current().nextInt(chunks.length)];

			if (!_chunks.contains(chunk))
			{
				break;
			}

		}

		if (_chunks.size() >= _chunkKeeps) // keep three
		{
			_chunks.removeFirst();
		}

		_chunks.add(chunk);
		int x = ThreadLocalRandom.current().nextInt(16);

		int z = ThreadLocalRandom.current().nextInt(16);
		Block b = chunk.getBlock(x, player_y + 8, z);

		while (b.getType().isAir() && b.getY() > 0)
		{
			b = b.getRelative(BlockFace.DOWN);
		}

		if (b.getType().isAir())
		{
			if (!_chunks.isEmpty())
				_chunks.removeLast();

			return;
		}

		Location loc = b.getLocation();

		SpawnUnstableGround(loc.add(0, 2, 0));
	}

	private void SpawnUnstableGround(Location loc)
	{
		if (loc.getBlock() == null)
			return;

		EndEvents.Instance.CreateUnstableFallingBlocks(loc, -10, 200, 10, true, true);
	}

	@Override
	public void OnOneTickLoop()
	{
		_ticks++;

		if (_ticks % _delayTicksToSpawn != 0)
			return;

		for (Player p : GetPlayers())
		{
			SpawnUnstableAround(p);
		}

	}

	@Override
	public String GetEventName()
	{

		return GetName();
	}

	@Override
	public String GetRewardInfo()
	{

		return "Chestloot base by &2+" + ChestLootAmount;
	}

	@Override
	public String GetDescription()
	{
		return "&6The ground seems unstable";
	}

	@Override
	public void OnPlayerLeftMiddleOfEvent(Player player)
	{

	}

	@Override
	public void OnPlayerJoinMiddleOfEvent(Player player)
	{

	}

}
