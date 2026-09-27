package imu.DontLoseItems.CustomItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import imu.DontLoseItems.CustomItems.RarityItems.Hell_Pickaxe;
import imu.DontLoseItems.Enums.CATEGORY;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;

public final class Hell_Pickaxe_Controller
{

	private final String _PD_HELL_PICKAXE = "HELL_PICKAXE";
	private HashMap<UUID, MINE_DIRECTION> _playerMineDir;
	private Cooldowns _cds;
	private Hell_Pickaxe[] _hellPickaxes = {
//			new Hell_Pickaxe(ITEM_RARITY.Common,	new double[] { 0 }), // not used
//			new Hell_Pickaxe(ITEM_RARITY.Uncommon,	new double[] { 0 }), // not used
//			new Hell_Pickaxe(ITEM_RARITY.Rare, 		new double[] { 0 }), // not used
			new Hell_Pickaxe(ITEM_RARITY.Epic, 		new double[] { 0 }),
			new Hell_Pickaxe(ITEM_RARITY.Mythic, 	new double[] { 0 }),
			new Hell_Pickaxe(ITEM_RARITY.Legendary, new double[] { 0 }), 
			new Hell_Pickaxe(ITEM_RARITY.Void, 		new double[] { 0 }), 
			};
	
	
	public Hell_Pickaxe_Controller()
	{
		_playerMineDir = new HashMap<>();
		_cds = new Cooldowns();
		for(RarityItem rarityItem : _hellPickaxes)
		{
			if(rarityItem == null) continue;
			
			Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Tools.toString(), CreateItem(rarityItem.Rarity));
		}
	}

	private enum MINE_DIRECTION
	{
		NONE, HORIZONTA, DOWN, UP,
	}

	public ItemStack CreateItem(ITEM_RARITY rarity)
	{
		Hell_Pickaxe rarityItem = _hellPickaxes[0];

		for (Hell_Pickaxe pick : _hellPickaxes)
		{
			if (pick.Rarity == rarity)
			{
				rarityItem = pick;
			}
		}

		ItemStack stack = rarityItem.GetItemStack();

		ArrayList<String> lores = new ArrayList<>();
		lores.add(" ");
		lores.add("&cLava &9is turned cobblestone ");
		lores.add("&9around mined block");
		lores.add(" ");
		lores.add("&9Changed &clava &9restores durability");
		lores.add(" ");

		// if(rarity == ITEM_RARITY.Epic) lores.add("&9Mines one random direction");
		if (rarity == ITEM_RARITY.Mythic)
		{
			lores.add("&9Mines &3two &9blocks vertically");
			lores.add(" ");
		}
			
		if (rarity == ITEM_RARITY.Legendary || rarity == ITEM_RARITY.Void)
		{
			lores.add("&9Mines &3three &9blocks vertically");
			lores.add(" ");
		}
		
		if (rarity == ITEM_RARITY.Void)
		{
			lores.add("&5Mines &33x3 when mined up or down");
			lores.add(" ");
			lores.add("&5Places torch under the player");
			lores.add("&5on low light level");
			lores.add(" ");
		}
			

		// if(rarity == ITEM_RARITY.Legendary) lores.add(" ");
		// if(rarity == ITEM_RARITY.Legendary) lores.add("&9Able to mine through lava");
		
		lores.add("&9Silk Touch doesn't work on this");
		lores.add(" ");
		lores.add("&7'One ancient tome claims that");
		lores.add("&7the Hell Picaxe was crafted by the");
		lores.add("&7devil himself, as a tool to reshape");
		lores.add("&7the underworld to his will'");

		Metods._ins.SetLores(stack, lores.toArray(new String[lores.size()]), false);

		ItemMeta meta = stack.getItemMeta();

		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_PICKAXE, PersistentDataType.INTEGER, rarity.GetIndex());

		return stack;
	}

	public boolean IsHellPickaxe(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, _PD_HELL_PICKAXE, PersistentDataType.INTEGER) != null;
	}
	
	public ITEM_RARITY GetRarity(ItemStack stack)
	{
		return ITEM_RARITY.GetRarity(Metods._ins.getPersistenData(stack, _PD_HELL_PICKAXE, PersistentDataType.INTEGER));
	}
	public boolean IsTier(ItemStack stack, ITEM_RARITY tier)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_PICKAXE, PersistentDataType.INTEGER);
		if(i == null ) return false;
		
		if(i == tier.GetIndex()) return true;
		
		return false;
	}
	
	public Hell_Pickaxe GetPickaxe(ItemStack stack)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_PICKAXE, PersistentDataType.INTEGER)-(_hellPickaxes.length-1);

		if(i == null || i < 0) return null;
		
		return _hellPickaxes[i];
	}
	public ItemStack RemoveHellPickaxe(ItemStack stack)
	{
		return Metods._ins.removePersistenData(stack, _PD_HELL_PICKAXE);
	}

	private boolean BreakTheBlock(Player player, Block b, int fortune)
	{
		if (b == null || b.getType().isAir())
			return false;

		if (b.getType().getBlastResistance() > 1500 || !b.getType().isBlock() || b.isPassable())
			return false;

		BlockBreakEvent event = new BlockBreakEvent(b, player);
		Bukkit.getServer().getPluginManager().callEvent(event);

		if (event.isCancelled())
			return false;

		// System.out.println("event: "+event.getBlock().getDrops());
		if (b.getType().isInteractable())
		{
			b.breakNaturally();
			return true;
		}

		if (fortune > 0)
		{
			for (ItemStack stack : b.getDrops())
			{
				ItemStack newStack = Metods._ins.FortuneSimulation(stack, fortune);
				b.getWorld().dropItemNaturally(b.getLocation(), newStack);
			}

			b.setType(Material.AIR);

			b.getDrops().clear();
			return true;
		}

		b.breakNaturally();
		return true;

	}

	private void PlaceLavaBlocks(Player player, Hell_Pickaxe hellPick,ItemStack pick,Block b)
	{
		ReplaceLava(player, hellPick, pick, b.getRelative(BlockFace.UP));
		ReplaceLava(player, hellPick, pick, b.getRelative(BlockFace.DOWN));
		ReplaceLava(player, hellPick, pick, b.getRelative(BlockFace.EAST));
		ReplaceLava(player, hellPick, pick, b.getRelative(BlockFace.WEST));
		ReplaceLava(player, hellPick, pick, b.getRelative(BlockFace.NORTH));
		ReplaceLava(player, hellPick, pick, b.getRelative(BlockFace.SOUTH));
	}

	private Block ReplaceLava(Player player, Hell_Pickaxe hellPick,ItemStack pick, Block b)
	{
		if (b == null || b.getType() != Material.LAVA)
			return b;

		Block now = b;
		b.setType(Material.COBBLESTONE);

		BlockPlaceEvent event = new BlockPlaceEvent(b, now.getState(), b, new ItemStack(Material.LAVA), player, false,
				EquipmentSlot.HAND);
		Bukkit.getServer().getPluginManager().callEvent(event);

		if (event.isCancelled())
		{
			b.setType(Material.LAVA);
			return b;
		}

		b.setType(Material.COBBLESTONE);
		Metods._ins.giveDamage(pick, -hellPick.GetDurabilityHealedByLava(), false);
		
		if (!_cds.isCooldownReady("LavaSound"))
			return b;

		_cds.setCooldownInSeconds("LavaSound", 0.1);
		player.playSound(player, Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
		
		return b;

	}

	public void OnBlockInteract(PlayerInteractEvent e)
	{
		if (e.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		BlockFace face = e.getBlockFace();
		
		switch (face)
		{
		case DOWN:
		{
			_playerMineDir.put(e.getPlayer().getUniqueId(), MINE_DIRECTION.UP);
			break;
		}
		case UP:
		{
			_playerMineDir.put(e.getPlayer().getUniqueId(), MINE_DIRECTION.DOWN);
			break;
		}
		case NORTH:
		{
			_playerMineDir.put(e.getPlayer().getUniqueId(), MINE_DIRECTION.HORIZONTA);
			break;
		}
		case SOUTH:
		{
			_playerMineDir.put(e.getPlayer().getUniqueId(), MINE_DIRECTION.HORIZONTA);
			break;
		}
		case WEST:
		{
			_playerMineDir.put(e.getPlayer().getUniqueId(), MINE_DIRECTION.HORIZONTA);
			break;
		}
		case EAST:
		{
			_playerMineDir.put(e.getPlayer().getUniqueId(), MINE_DIRECTION.HORIZONTA);
			break;
		}

		default:
			_playerMineDir.put(e.getPlayer().getUniqueId(), MINE_DIRECTION.HORIZONTA);
			break;

		}

	}

	public void OnHellPickBlockBreak(BlockBreakEvent e)
	{
		Player player = e.getPlayer();
		ItemStack stack = player.getInventory().getItemInMainHand();
		
		if(stack == null || stack.getType().isAir()) return;
		
		if (!IsHellPickaxe(stack))
			return;

		if (_playerMineDir.containsKey(player.getUniqueId())
				&& _playerMineDir.get(player.getUniqueId()) == MINE_DIRECTION.NONE)
		{
			//System.out.println("it was none");
			return;
		}

		Block minedBlock = e.getBlock();
		MINE_DIRECTION dir = _playerMineDir.get(player.getUniqueId());
		_playerMineDir.put(player.getUniqueId(), MINE_DIRECTION.NONE);

		if (stack.containsEnchantment(Enchantment.SILK_TOUCH))
			stack.removeEnchantment(Enchantment.SILK_TOUCH);

		int fortuneLevel = Metods._ins.GetEnchantLevel(stack, Enchantment.LOOT_BONUS_BLOCKS);
		Hell_Pickaxe hellPickAxe = GetPickaxe(stack);
		
		PlaceLavaBlocks(player, hellPickAxe,stack, minedBlock);
		
		if(hellPickAxe.Rarity == ITEM_RARITY.Void)
		{
			
			if(player.getLocation().getBlock().getLightLevel() < 11 
					&& player.getLocation().getBlock().getType().isAir() 
					&& player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid())
			{
				player.getLocation().getBlock().setType(Material.TORCH);
			}
		}
		
		if (dir == MINE_DIRECTION.HORIZONTA)
		{
			
			Metods._ins.giveDamage(stack, hellPickAxe.GetDurabilityLost(), false);
			OnHorizontalMine(player, fortuneLevel, minedBlock, hellPickAxe, stack);
			_playerMineDir.put(player.getUniqueId(), dir); // remeber this to be last!
			return;
		}
		
		if (hellPickAxe.Rarity == ITEM_RARITY.Void &&(dir == MINE_DIRECTION.DOWN || dir == MINE_DIRECTION.UP) )
		{
			Metods._ins.giveDamage(stack, hellPickAxe.GetDurabilityLost(), false);
			OnUpDownMine(player, fortuneLevel, minedBlock, hellPickAxe, stack);
			_playerMineDir.put(player.getUniqueId(), dir); // remeber this to be last!
			return;
		}
		
		_playerMineDir.put(player.getUniqueId(), dir);
		
	}
	private void OnUpDownMine(Player player, int fortuneLevel, Block minedBlock, Hell_Pickaxe hellPickAxe, ItemStack stack)
	{
		
		World world =minedBlock.getWorld();

		int x = minedBlock.getX();
		int y = minedBlock.getY();
		int z = minedBlock.getZ();
		Block b = null;
		
		//opened bc little bit faster this way
		b = world.getBlockAt(x+1, y, z);	
		if (BreakTheBlock(player, b, fortuneLevel)) { PlaceLavaBlocks(player, hellPickAxe,stack, b); }
		
		b = world.getBlockAt(x, y, z+1);	
		if (BreakTheBlock(player, b, fortuneLevel)) { PlaceLavaBlocks(player, hellPickAxe,stack, b); }
		
		b = world.getBlockAt(x+1, y, z+1);
		if (BreakTheBlock(player, b, fortuneLevel)) { PlaceLavaBlocks(player, hellPickAxe,stack, b); }
		
		b = world.getBlockAt(x-1, y, z);		
		if (BreakTheBlock(player, b, fortuneLevel)) { PlaceLavaBlocks(player, hellPickAxe,stack, b); }
		
		b = world.getBlockAt(x, y, z-1);
		if (BreakTheBlock(player, b, fortuneLevel)) { PlaceLavaBlocks(player, hellPickAxe,stack, b); }
		
		b = world.getBlockAt(x-1, y, z-1);		
		if (BreakTheBlock(player, b, fortuneLevel)) { PlaceLavaBlocks(player, hellPickAxe,stack, b); }
		
		b = world.getBlockAt(x-1, y, z+1);
		if (BreakTheBlock(player, b, fortuneLevel)) { PlaceLavaBlocks(player, hellPickAxe,stack, b); }
		
		b = world.getBlockAt(x+1, y, z-1);	
		if (BreakTheBlock(player, b, fortuneLevel)) { PlaceLavaBlocks(player, hellPickAxe,stack, b); }
	}
	@SuppressWarnings("incomplete-switch")
	private void OnHorizontalMine(Player player, int fortuneLevel,Block minedBlock, Hell_Pickaxe hellPickAxe, ItemStack stack)
	{
		MINE_DIRECTION dir = MINE_DIRECTION.HORIZONTA;
		
		
		switch (hellPickAxe.Rarity)
		{
		case Void: // this should drop below tier
		case Legendary:
		{
			Block upper = minedBlock.getRelative(BlockFace.UP);
			Block lower = minedBlock.getRelative(BlockFace.DOWN);
			if (BreakTheBlock(player, upper, fortuneLevel))
			{
				PlaceLavaBlocks(player, hellPickAxe,stack, upper);
			}

			if (lower.getLocation().getBlockY() >= player.getLocation().getBlockY())
			{
				if (BreakTheBlock(player, lower, fortuneLevel))
				{
					PlaceLavaBlocks(player, hellPickAxe,stack, lower);
				}
			}
		
			break;
		}

		case Mythic:
		{

			Block lower = minedBlock.getRelative(BlockFace.DOWN);

			if (lower.getLocation().getBlockY() < player.getLocation().getBlockY())
				break;

			if (BreakTheBlock(player, lower, fortuneLevel))
			{
				PlaceLavaBlocks(player, hellPickAxe,stack, lower);
			}
			_playerMineDir.put(player.getUniqueId(), dir);
			break;
		}

		case Epic:
		{
			break;
		}
		}
		return;
	}
}
