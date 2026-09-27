package imu.def.Events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class EventClass implements Listener
{
	HashMap<Block, Block> placedBlocks = new HashMap<Block, Block>();
	
	ArrayList<Material> blocked = new ArrayList<Material>();
	
	Material[] blocked_materials = new Material[] {Material.PISTON, Material.STICKY_PISTON};
	
	public EventClass()
	{
		blocked.addAll(Arrays.asList(blocked_materials));
	}

	@EventHandler
	public void onMove(BlockBreakEvent e)
	{

	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent event)
	{
		Block b = event.getBlock();
			
		if(blocked.contains(b.getType()) && !event.getPlayer().isOp())
		{
			System.out.println("cancel");
			event.setCancelled(true);
			return;			
		}
		
		placedBlocks.put(b, b);

	}

	
	@EventHandler
	public void onItemDrop(BlockDropItemEvent event)
	{
		if(placedBlocks.containsKey(event.getBlock()))
		{
			placedBlocks.remove(event.getBlock());
			System.out.println("Already ex");
			return;
		}
			
		
		Item[] items = event.getItems().toArray(new Item[0]);
		
		for(Item i: items)
		{
			
			ItemStack stack = i.getItemStack();
			int count = stack.getAmount() * 2;
			event.getPlayer().sendMessage(String.valueOf(count));
			stack.setAmount(count);
		}
		
	}
}
