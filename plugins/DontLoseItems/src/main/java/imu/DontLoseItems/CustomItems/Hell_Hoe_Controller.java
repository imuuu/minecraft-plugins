package imu.DontLoseItems.CustomItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.DontLoseItems.CustomItems.RarityItems.Hell_Hoe;
import imu.DontLoseItems.Enums.CATEGORY;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;

public final class Hell_Hoe_Controller
{

	private final String _PD_HELL_HOE= "HELL_HOE";
	private Cooldowns _cds;
	
	private HashSet<Material> _seedsMaterials;
	HashMap<Material, Material> cropDataMaterials = new HashMap<>();
	
	private RarityItem[] _hellHoe = 
		{

			new Hell_Hoe(ITEM_RARITY.Epic, 		    new double[] {}),
			new Hell_Hoe(ITEM_RARITY.Mythic, 		new double[] {}),
			new Hell_Hoe(ITEM_RARITY.Legendary,	    new double[] {}), 
			new Hell_Hoe(ITEM_RARITY.Void,	    	new double[] {}), 
			};
	
	
	public Hell_Hoe_Controller()
	{
		_cds = new Cooldowns();
		InitSeedMaterials();
		
		for(RarityItem rarityItem : _hellHoe)
		{
			Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Tools.toString(), CreateItem(rarityItem.Rarity));
		}
	}
	
	private void InitSeedMaterials()
	{
		_seedsMaterials = new HashSet<>();
		
		for (Material material : Material.values()) {

		    if (material.name().endsWith("_SEEDS") || material.name().endsWith("SEEDS")) {
		        _seedsMaterials.add(material);
		    }
		}
		
		_seedsMaterials.add(Material.NETHER_WART);
		_seedsMaterials.add(Material.CARROT);
		_seedsMaterials.add(Material.BEETROOT_SEEDS);
		_seedsMaterials.add(Material.POTATO);
		
		cropDataMaterials.put(Material.MELON_SEEDS, Material.MELON_STEM.createBlockData().getClass().equals(Ageable.class) ? Material.MELON_STEM : Material.ATTACHED_MELON_STEM);
	    cropDataMaterials.put(Material.WHEAT_SEEDS, Material.WHEAT.createBlockData().getClass().equals(Ageable.class) ? Material.WHEAT : Material.WHEAT);
	    cropDataMaterials.put(Material.BEETROOT_SEEDS, Material.BEETROOTS.createBlockData().getClass().equals(Ageable.class) ? Material.BEETROOTS : Material.BEETROOTS);
	    cropDataMaterials.put(Material.NETHER_WART, Material.NETHER_WART.createBlockData().getClass().equals(Ageable.class) ? Material.NETHER_WART : Material.NETHER_WART);
	    cropDataMaterials.put(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM.createBlockData().getClass().equals(Ageable.class) ? Material.PUMPKIN_STEM : Material.ATTACHED_PUMPKIN_STEM);
	    cropDataMaterials.put(Material.CARROT, Material.CARROTS.createBlockData().getClass().equals(Ageable.class) ? Material.CARROTS : Material.CARROTS);
	    cropDataMaterials.put(Material.POTATO, Material.POTATOES.createBlockData().getClass().equals(Ageable.class) ? Material.POTATOES : Material.POTATOES);
	    //cropDataMaterials.put(Material.BEETROOT, Material.BEETROOTS.createBlockData().getClass().equals(Ageable.class) ? Material.BEETROOTS : Material.BEETROOTS);
	}
	
	public RarityItem GetRarityItem(ITEM_RARITY rarity)
	{
		for (RarityItem hoe : _hellHoe)
		{
			if (hoe.Rarity == rarity)
			{
				return hoe;
			}
		}
		return _hellHoe[0];
	}
	public ItemStack CreateItem(ITEM_RARITY rarity)
	{
		Hell_Hoe rarityItem = (Hell_Hoe) GetRarityItem(rarity);
		ItemStack stack = rarityItem.GetItemStack();

		ArrayList<String> lores = new ArrayList<>();
		lores.add(" ");
		
		if(rarityItem.Rarity != ITEM_RARITY.Void)
		{
			lores.add("&9Able to place seeds over a &elarge &9area");
			lores.add(" ");
		}else
		{
			lores.add("&5Able to place seeds over a &emassive &9area");
			lores.add(" ");
			
		}

		lores.add("&9Uses seeds from the off-hand and");
		lores.add("&3refills &9the same seeds from the off-hand");
		lores.add(" ");
		lores.add("&9Uses &c" + rarityItem.GetSeedUsage() + " &9seeds for each plant");
		lores.add(" ");
		lores.add("&7'With this hoe in hand, a skilled farmer  ");
		lores.add("&7can plant an entire field in seconds,");
		lores.add("&7 leaving behind nothing but charred earth");
		lores.add("&7 and a trail of flames'");


		Metods._ins.SetLores(stack, lores.toArray(new String[lores.size()]), false);

		Metods._ins.setPersistenData(stack, _PD_HELL_HOE, PersistentDataType.INTEGER, rarity.GetIndex());

		return stack;
	}


	public boolean IsHellHoe(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, _PD_HELL_HOE, PersistentDataType.INTEGER) != null;
	}
	
	public ITEM_RARITY GetRarity(ItemStack stack)
	{
		return ITEM_RARITY.GetRarity(Metods._ins.getPersistenData(stack, _PD_HELL_HOE, PersistentDataType.INTEGER));
	}
	public boolean IsTier(ItemStack stack, ITEM_RARITY tier)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_HOE, PersistentDataType.INTEGER);
		
		if(i == null ) return false;
		
		if(i == tier.GetIndex()) return true;
		
		return false;
	}
	
	public ItemStack RemoveHellHoe(ItemStack stack)
	{
		return Metods._ins.removePersistenData(stack, _PD_HELL_HOE);
	}
	
	public void OnDisable()
	{
		
	}
	
	
	public void OnUse(PlayerInteractEvent e)
	{
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		if(!IsHellHoe(e.getItem())) return;
				
		Block middleBlock = e.getClickedBlock();
		
		if(middleBlock == null) return;
		
		Player player = e.getPlayer();
		if(!_cds.isCooldownReady(player.getUniqueId().toString())) return;
		
		_cds.addCooldownInSeconds(player.getUniqueId().toString(), 0.1);
		
		ITEM_RARITY rarity = RarityItem.GetRarity(e.getItem());
		Hell_Hoe hoe = (Hell_Hoe)GetRarityItem(rarity);
		
		if(middleBlock.getType() == Material.FARMLAND || middleBlock.getType() == Material.SOUL_SAND)
		{
			middleBlock = middleBlock.getRelative(BlockFace.UP);
		}
		
		ItemStack stack = player.getInventory().getItemInOffHand();
		
		if(stack == null) return;
		
		if(!_seedsMaterials.contains(stack.getType())) return;
		

		PlantSeeds(player, middleBlock.getLocation(), stack,hoe.GetSeedUsage(), hoe.GetAreaRadius());
		
		
				
	}
	
	public ItemStack GetNewSeeds(Player player, Material mat)
	{
		for( ItemStack newStack : player.getInventory().getStorageContents())
		{
			if(newStack == null || newStack.getType().isAir()) continue;
			
			if(newStack.getType() == mat) return newStack;
 		}
		
		return null;
	}
	public int PlantSeeds(Player player, Location middleBlock, ItemStack stack, int usage,int radius) {
	    
		int amount = 0;
		if(stack.getType().isAir()) return amount;
		
		World world = middleBlock.getWorld();
		Material stackMat = stack.getType();

	    for (int x = middleBlock.getBlockX() - radius; x <= middleBlock.getBlockX() + radius; x++) 
	    {
	        for (int z = middleBlock.getBlockZ() - radius; z <= middleBlock.getBlockZ() + radius; z++) 
	        {
	        	if(stack == null || stack.getType().isAir() || stack.getAmount() <= 0) 
	        	{
	        		ItemStack newStack = GetNewSeeds(player, stackMat);
	        		
	        		if(newStack == null) return amount;
	        		
	        		stack.setAmount(newStack.getAmount());
	        		newStack.setAmount(0);
	        		continue;
	        	}
	            // Create a new Location for the current block and set it to stone
	            Location blockLocation = new Location(world, x, middleBlock.getBlockY(), z);
	            Block block = blockLocation.getBlock();
	            Material mat = block.getType();
	            if(!mat.isAir()) continue;
	                        
	            if(stack.getType() == Material.NETHER_WART && block.getRelative(BlockFace.DOWN).getType() != Material.SOUL_SAND)
	            {
	            	continue;
	            }
           
	            if(stack.getType() != Material.NETHER_WART && block.getRelative(BlockFace.DOWN).getType() != Material.FARMLAND)
	            {
	            	continue;
	            }
	            
	            PlantCrop(block,stack.getType());
	            amount++;
	            
	            int newAmount = stack.getAmount()-usage;
	            if(newAmount < 0) newAmount = 0;
	            
	            stack.setAmount(newAmount);
	        }
	    }
	    
	    return amount;
	}
	
	private void PlantCrop(Block block, Material mat) {

	    Ageable cropData = (Ageable) cropDataMaterials.get(mat).createBlockData();

	    cropData.setAge(0);

	    block.setBlockData(cropData);
	}
	
	


	
}
