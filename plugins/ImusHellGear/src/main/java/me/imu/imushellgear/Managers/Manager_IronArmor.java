package me.imu.imushellgear.Managers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;

import me.imu.imushellgear.ImusHellGear;
import imu.iAPI.Utilities.ItemUtils;

public class Manager_IronArmor implements Listener
{
	private ImusHellGear Main = ImusHellGear.Instance;
	private final String PD_REINFORCED_IRON_ARMOR = "dli_pd_r_iron_armor";
	
	private boolean _enableReinforcedIronArmor = true;
	public Manager_IronArmor()
	{
		LoadSettings(Main.getConfig());
		
		if(!_enableReinforcedIronArmor) return;
		
		CreateAllReinforcedArmorRecipes();
	}
	
	private void CreateAllReinforcedArmorRecipes() 
	{
	    CreateReinforcedArmorRecipe(Material.IRON_HELMET, "reinforced_iron_helmet");
	    CreateReinforcedArmorRecipe(Material.IRON_CHESTPLATE, "reinforced_iron_chestplate");
	    CreateReinforcedArmorRecipe(Material.IRON_LEGGINGS, "reinforced_iron_leggings");
	    CreateReinforcedArmorRecipe(Material.IRON_BOOTS, "reinforced_iron_boots");
	}

	private void CreateReinforcedArmorRecipe(Material armorType, String keyName) 
	{
	    ItemStack reinforcedArmor = CreateReinforcedItem(armorType);

	    NamespacedKey key = new NamespacedKey(Main, keyName);
	    ShapedRecipe recipe = new ShapedRecipe(key, reinforcedArmor);

	    recipe.shape("III", "IAI", "III");
	    recipe.setIngredient('I', Material.IRON_INGOT);
	    recipe.setIngredient('A', armorType);

	    Main.getServer().addRecipe(recipe);
	}

	
	@EventHandler
	public void OnPlayerDamage(EntityDamageEvent event) 
	{
		if(!_enableReinforcedIronArmor) return;
		
	    if (!(event.getEntity() instanceof Player)) return;
	    
        Player player = (Player) event.getEntity();
        PlayerInventory inventory = player.getInventory();
        int count = 0;
        for (ItemStack item : inventory.getArmorContents()) 
        {
            if (!IsReinforcedArmor(item)) continue;
            
            int damageToDurability = (int) (event.getDamage() * 0.5);
            ItemUtils.GiveDamage(item, damageToDurability, true);
            count++;
        }
        double damage = event.getDamage() - count * 0.5;
        if(damage <= 0)
        	damage = 0;

        event.setDamage(damage);
	   
	}
	
	
	
	private ItemStack CreateReinforcedItem(Material material) 
	{
	    ItemStack item = new ItemStack(material);
	    ItemUtils.SetTag(item, PD_REINFORCED_IRON_ARMOR);
	    if(material == Material.IRON_CHESTPLATE) 
	    {
	    	ItemUtils.SetDisplayName(item, "&9Reinforced &7Iron Chestplate");
	    	
		    final String[] lore = 
		    	{
		    		" ",
		    		"&3Each dent tells a tale ",
		    		"&3of bravery, each scratch ",
		    		"&3a song of survival",
		    		" ",
		    		"&9- Reduce Damage Taken",
		    		" "
		    	};
		    ItemUtils.SetLores(item, lore, false);
	    }
	    if(material == Material.IRON_HELMET) 
	    {
	    	ItemUtils.SetDisplayName(item, "&9Reinforced &7Iron Helmet");
	    	final String[] lore = 
		    	{
		    		" ",
		    		"&3The iron may wear thin, but the ",
		    		"&3stories it harbors are timeless,",
		    		"&3echoing the wearer's undying courage",
		    		" ",
		    		"&9- Reduce Damage Taken",
		    		" "
		    	};
		    ItemUtils.SetLores(item, lore, false);
	    }
	    if(material == Material.IRON_LEGGINGS) 
	    {
	    	ItemUtils.SetDisplayName(item, "&9Reinforced &7Iron Leggings");
	    	final String[] lore = 
		    	{
		    		" ",
		    		"&3As the fabric of war frays, so",
		    		"&3does the armor, etching the ",
		    		"&3legacy of the relentless warrior",
		    		" ",
		    		"&9- Reduce Damage Taken",
		    		" "
		    	};
		    ItemUtils.SetLores(item, lore, false);
	    }
	    if(material == Material.IRON_BOOTS) 
	    {
	    	ItemUtils.SetDisplayName(item, "&9Reinforced &7Iron Boots");
	    	final String[] lore = 
		    	{
		    		" ",
		    		"&3Yet, as the miles stretch",
		    		"&3on, so too does the metal",
		    		"&3thin and fray ",
		    		" ",
		    		"&9- Reduce Damage Taken",
		    		" "
		    	};
		    ItemUtils.SetLores(item, lore, false);
	    }
	    
	    
	    return item;
	}

	private boolean IsReinforcedArmor(ItemStack item) 
	{
	   return ItemUtils.HasTag(item, PD_REINFORCED_IRON_ARMOR);
	}
	
	// config.yml; the recipes are added at startup, so turning the armor on later needs a restart
	public void LoadSettings(FileConfiguration config)
	{
		_enableReinforcedIronArmor = config.getBoolean("reinforced-iron-armor", true);
	}

}
