package imu.DontLoseItems.Managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.DontLoseItems.CustomItems.VoidTotemController;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.iAPI.FastInventory.Fast_Inventory;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Other.Metods;

public class Manager_LegendaryUpgrades
{
	public static Manager_LegendaryUpgrades Instance;

	public final String PD_HELL_HELMET = "PD_UPGRADE_HELL_HELMET";
	public final String PD_HELL_CHEST = "PD_UPGRADE_HELL_CHEST";
	public final String PD_HELL_LEGG = "PD_UPGRADE_HELL_LEGG";
	public final String PD_HELL_BOOTS = "PD_UPGRADE_HELL_BOOTS";

	public final String PD_HELL_HOE = "PD_UPGRADE_HELL_HOE";
	public final String PD_HELL_PICKAXE = "PD_UPGRADE_HELL_PICKAXE";
	public final String PD_HELL_SWORD = "PD_UPGRADE_HELL_SWORD";
	public final String PD_HELL_AXE = "PD_UPGRADE_HELL_AXE";
	public final String PD_HELL_SHIELD = "PD_UPGRADE_HELL_SHIELD";
	
	
	public Manager_LegendaryUpgrades()
	{
		Instance = this;
		
	}
	
	public void SetTestItems()
	{
		Fast_Inventory fastInv = new Fast_Inventory("Upgrade Items", "&eUpgrades", null);
		
		fastInv.AddStack(Get_UpgradeHellAxe());
		fastInv.AddStack(Get_UpgradeHellPickaxe());
		fastInv.AddStack(Get_UpgradeHellHoe());
		fastInv.AddStack(Get_UpgradeHellShield());
		fastInv.AddStack(Get_UpgradeHellSword());
		
		fastInv.AddStack(Get_UpgradeHellHelmet());
		fastInv.AddStack(Get_UpgradeHellChest());
		fastInv.AddStack(Get_UpgradeHellLeggings());
		fastInv.AddStack(Get_UpgradeHellBoots());
		
		fastInv.AddStack(VoidTotemController.GetVoidtotemItem());
		
		Manager_FastInventories.Instance.RegisterFastInventory(fastInv);
		
	}
	public ItemStack AddSyntax(ItemStack s)
	{
		Metods._ins.addDisplayName(s, ChatColor.AQUA+"UPGRADE &5for ", true);
		ItemStack stack = new ItemStack(s.getType(),1);
		Metods.setDisplayName(stack, s.getItemMeta().getDisplayName());
		
		stack = Manager_HellArmor.Instance.RemoveArmorData(stack);
		
		Metods._ins.RemoveLores(stack);
		List<String> lores = new ArrayList<>();
		
		lores.add(" ");
		lores.add("&3Combine &6with an equivalent &elegendary &6item");
		lores.add("&6in &3a Smithing Table &6to &5complete &6this &bitem.");
		lores.add(" ");
		lores.add("&4&k# "+"&4This item alone doesn't have any legendary effects!");
		
//		ItemMeta meta = stack.getItemMeta();
//		 		
//		meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", 0,AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
//		meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), "generic.health", 0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
//		meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor",0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
//		meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness",0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
//
//		stack.setItemMeta(meta);
		stack = Metods._ins.addLore(stack, lores);
		
		
		return stack;
	}
	public ItemStack Get_UpgradeHellHelmet()
	{
		ItemStack stack = Manager_HellArmor.Instance.CreateHellHelmet(ITEM_RARITY.Legendary);
		stack = AddSyntax(stack);
		Metods._ins.setPersistenData(stack, PD_HELL_HELMET, PersistentDataType.INTEGER, 1);
		
		stack.setType(Material.GOLDEN_HELMET);
		return stack;
	}

	public ItemStack Get_UpgradeHellChest()
	{
		ItemStack stack = Manager_HellArmor.Instance.CreateHellChestplate(ITEM_RARITY.Legendary);
		stack = AddSyntax(stack);
		Metods._ins.setPersistenData(stack, PD_HELL_CHEST, PersistentDataType.INTEGER, 1);
		
		stack.setType(Material.GOLDEN_CHESTPLATE);
		return stack;
	}

	public ItemStack Get_UpgradeHellLeggings()
	{
		ItemStack stack = Manager_HellArmor.Instance.CreateHellLeggins(ITEM_RARITY.Legendary);
		stack = AddSyntax(stack);
		Metods._ins.setPersistenData(stack, PD_HELL_LEGG, PersistentDataType.INTEGER, 1);
		
		stack.setType(Material.GOLDEN_LEGGINGS);
		return stack;
	}

	public ItemStack Get_UpgradeHellBoots()
	{
		ItemStack stack = Manager_HellArmor.Instance.CreateHellBoots(ITEM_RARITY.Legendary);
		stack = AddSyntax(stack);
		Metods._ins.setPersistenData(stack, PD_HELL_BOOTS, PersistentDataType.INTEGER, 1);
		
		stack.setType(Material.GOLDEN_BOOTS);
		return stack;
	}
	
	

	public ItemStack Get_UpgradeHellHoe()
	{
		ItemStack stack = Manager_HellTools.Instance.CreateHellHoe(ITEM_RARITY.Legendary);
		stack = AddSyntax(stack);
		Metods._ins.setPersistenData(stack, PD_HELL_HOE, PersistentDataType.INTEGER, 1);
		stack = Manager_HellTools.Instance.HellHoe_Controller.RemoveHellHoe(stack);
		stack.setType(Material.GOLDEN_HOE);
		return stack;
	}

	public ItemStack Get_UpgradeHellPickaxe()
	{
		ItemStack stack = Manager_HellTools.Instance.CreateHellPickaxe(ITEM_RARITY.Legendary);
		stack = AddSyntax(stack);
		Metods._ins.setPersistenData(stack, PD_HELL_PICKAXE, PersistentDataType.INTEGER, 1);
		stack = Manager_HellTools.Instance.HellPickController.RemoveHellPickaxe(stack);
		stack.setType(Material.GOLDEN_PICKAXE);
		return stack;
	}

	public ItemStack Get_UpgradeHellSword()
	{
		ItemStack stack = Manager_HellTools.Instance.CreateHellTripleSword(ITEM_RARITY.Legendary);
		stack = AddSyntax(stack);
		Metods._ins.setPersistenData(stack, PD_HELL_SWORD, PersistentDataType.INTEGER, 1);
		stack = Manager_HellTools.Instance.HellSword_Controller.RemoveHellSword(stack);
		stack.setType(Material.GOLDEN_SWORD);
		return stack;
	}

	public ItemStack Get_UpgradeHellAxe()
	{
		ItemStack stack = Manager_HellTools.Instance.CreateHellDoubleAxe(ITEM_RARITY.Legendary);
		stack = AddSyntax(stack);
		Metods._ins.setPersistenData(stack, PD_HELL_AXE, PersistentDataType.INTEGER, 1);
		stack = Manager_HellTools.Instance.HellAxe_Controller.RemoveHellAxe(stack);
		stack.setType(Material.GOLDEN_AXE);
		return stack;
	}

	public ItemStack Get_UpgradeHellShield()
	{
		//ItemStack stack = Manager_HellArmor.Instance.CreateHellReflectShield(ITEM_RARITY.Legendary);
		ItemStack stack = Manager_HellTools.Instance.CreateHellReflectShield(ITEM_RARITY.Legendary);
		stack = AddSyntax(stack);
		Metods._ins.setPersistenData(stack, PD_HELL_SHIELD, PersistentDataType.INTEGER, 1);
		stack = Manager_HellTools.Instance.Hell_ReflectShield_Controller.RemoveShield(stack);
		stack.setType(Material.SHIELD);
		return stack;
	}
	public boolean IsUpgrade(ItemStack stack) 
	{
	    return IsUpgradeHellHelmet(stack) || IsUpgradeHellChest(stack) || IsUpgradeHellLegg(stack) || 
	           IsUpgradeHellBoots(stack) || IsUpgradeHellHoe(stack) || IsUpgradeHellPickaxe(stack) || 
	           IsUpgradeHellSword(stack) || IsUpgradeHellAxe(stack) || IsUpgradeHellShield(stack);
	}

	
	public boolean IsUpgradeHellHelmet(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, PD_HELL_HELMET, PersistentDataType.INTEGER) != null;
	}
	public boolean IsUpgradeHellChest(ItemStack stack)
	{
	    return Metods._ins.getPersistenData(stack, PD_HELL_CHEST, PersistentDataType.INTEGER) != null;
	}

	public boolean IsUpgradeHellLegg(ItemStack stack)
	{
	    return Metods._ins.getPersistenData(stack, PD_HELL_LEGG, PersistentDataType.INTEGER) != null;
	}

	public boolean IsUpgradeHellBoots(ItemStack stack)
	{
	    return Metods._ins.getPersistenData(stack, PD_HELL_BOOTS, PersistentDataType.INTEGER) != null;
	}

	public boolean IsUpgradeHellHoe(ItemStack stack)
	{
	    return Metods._ins.getPersistenData(stack, PD_HELL_HOE, PersistentDataType.INTEGER) != null;
	}

	public boolean IsUpgradeHellPickaxe(ItemStack stack)
	{
	    return Metods._ins.getPersistenData(stack, PD_HELL_PICKAXE, PersistentDataType.INTEGER) != null;
	}

	public boolean IsUpgradeHellSword(ItemStack stack)
	{
	    return Metods._ins.getPersistenData(stack, PD_HELL_SWORD, PersistentDataType.INTEGER) != null;
	}

	public boolean IsUpgradeHellAxe(ItemStack stack)
	{
	    return Metods._ins.getPersistenData(stack, PD_HELL_AXE, PersistentDataType.INTEGER) != null;
	}

	public boolean IsUpgradeHellShield(ItemStack stack)
	{
	    return Metods._ins.getPersistenData(stack, PD_HELL_SHIELD, PersistentDataType.INTEGER) != null;
	}
	

}
