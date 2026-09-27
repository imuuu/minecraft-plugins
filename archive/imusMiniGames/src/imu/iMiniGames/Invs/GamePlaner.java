package imu.iMiniGames.Invs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iMiniGames.Main.ImusMiniGames;

public class GamePlaner extends CustomInvLayout
{
	String pd_buttonType = "sGP.buttonType";
	protected ImusMiniGames _main;
	public GamePlaner(ImusMiniGames main, Player player, String name) 
	{
		super(main, player, name, 9*3);
		_main = main;
		openThis();

	}
	

	public enum BUTTON
	{
		NONE,
		CONFIRM,
		EXIT,
		RESET,
		SET_ARENA,
		ADD_BET,
		ADD_PLAYERS,
		POTION_EFFECTS,
		ADD_BEST_OF_AMOUNT,
		SET_KIT,
		ADD_ATTRIBUTES;
	}
	
	
	void setButton(ItemStack stack, BUTTON b)
	{
		Metods._ins.setPersistenData(stack, pd_buttonType, PersistentDataType.STRING, b.toString());
	}
	
	public BUTTON getButton(ItemStack stack)
	{
		String button = Metods._ins.getPersistenData(stack, pd_buttonType, PersistentDataType.STRING);
		if(button != null)
			return BUTTON.valueOf(button);
		
		return BUTTON.NONE;
	}
	
	public ItemStack setupButton(BUTTON b, Material material, String displayName, int itemSlot)
	{
		ItemStack sbutton = new ItemStack(material);
		Metods.setDisplayName(sbutton, displayName);
		setButton(sbutton, b);
		_inv.setItem(itemSlot, sbutton);
		return _inv.getItem(itemSlot);
	}

	@Override
	public void invClosed(InventoryCloseEvent arg0) 
	{

	}

	@Override
	public void onClickInsideInv(InventoryClickEvent arg0) {
		
	}

	@Override
	public void setupButtons() {
		
	}

	

}
