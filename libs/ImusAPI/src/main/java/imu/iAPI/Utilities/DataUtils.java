package imu.iAPI.Utilities;

import imu.iAPI.Main.ImusAPI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Persistent data of items, blocks and entities, under the imusapi namespace.
 */
public final class DataUtils
{
	private DataUtils() {}

	/**
	 * Removes the value stored under the key ("imusapi:&lt;key&gt;").
	 */
	public static ItemStack RemovePersistentData(ItemStack stack, String keyName)
	{
		if(stack == null || stack.getType() == Material.AIR)
			return stack;

		NamespacedKey key = new NamespacedKey(ImusAPI._instance, keyName);
		ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().remove(key);
		stack.setItemMeta(meta);

		return stack;
	}

	/**
	 * Stores a value on the item under "imusapi:&lt;key&gt;"; it survives restarts and moving the item.
	 */
	public static <T> ItemStack SetPersistentData(ItemStack stack, String keyName, PersistentDataType<T, T> type, T data)
	{
		if(stack == null || stack.getType() == Material.AIR)
			return stack;
		
		NamespacedKey key = new NamespacedKey(ImusAPI._instance, keyName);
		ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().set(key, type, data);
		stack.setItemMeta(meta);
		return stack;
		
	}

	/**
	 * The value stored on the item under "imusapi:&lt;key&gt;", or null.
	 */
	public static <T> T GetPersistentData(ItemStack stack, String keyName, PersistentDataType<T, T> type)
	{
		T value = null;
		if(stack != null && stack.getType() != Material.AIR)
		{
			NamespacedKey key = new NamespacedKey(ImusAPI._instance, keyName);
			ItemMeta meta = stack.getItemMeta();
			PersistentDataContainer container = meta.getPersistentDataContainer();
			if(container.has(key, type))
			{
				value = container.get(key, type);
				return value;
			}
		}
		
				
		return value;
	}

	//works only below things
	//    Banner, Barrel, Beacon, Bed, Beehive, Bell, BlastFurnace, BrewingStand, Campfire, Chest, CommandBlock, 
	//Comparator, Conduit, Container, CreatureSpawner, DaylightDetector, Dispenser, Dropper, EnchantingTable, EnderChest, EndGateway, EntityBlockStorage<T>, 
	//Furnace, Hopper, Jigsaw, Jukebox, Lectern, SculkSensor, ShulkerBox, Sign, Skull, Smoker, Structure
	public static <T> Block SetPersistentData(Block block, String keyName, PersistentDataType<T, T> type, T data)
	{
		if(block == null || !(block.getState() instanceof TileState)) return null;
		
		TileState tileState = (TileState)block.getState();
		NamespacedKey key = new NamespacedKey(ImusAPI._instance, keyName);
		tileState.getPersistentDataContainer().set(key, type, data);
		tileState.update();
		//System.out.println( "persistent data set to block: "+block.getType());
		return block;
	}

	public static <T> T GetPersistentData(Block block, String keyName, PersistentDataType<T, T> type)
	{
		T value = null;
		if(block == null || !(block.getState() instanceof TileState)) return value;
		
		TileState tileState = (TileState)block.getState();
		NamespacedKey key = new NamespacedKey(ImusAPI._instance, keyName);
		PersistentDataContainer container = tileState.getPersistentDataContainer();
		if(container.has(key, type))
		{
			return container.get(key, type);
		}
				
		return value;
	}

	public static <T> Entity SetPersistentData(Entity entity, String keyName, PersistentDataType<T, T> type, T data)
	{
		if(entity == null) return null;
		NamespacedKey key = new NamespacedKey(ImusAPI._instance, keyName);
		entity.getPersistentDataContainer().set(key, type, data);

		return entity;
	}

	public static <T> T GetPersistentData(Entity entity, String keyName, PersistentDataType<T, T> type)
	{
		T value = null;
		if(entity == null) return value;
		
		NamespacedKey key = new NamespacedKey(ImusAPI._instance, keyName);
		if(entity.getPersistentDataContainer().has(key, type)) return entity.getPersistentDataContainer().get(key, type);
		
		return value;
	}
}
