package imu.iCasino.Games.Crash;

import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iCasino.DataCards.CrashDataPlanerCard;
import imu.iCasino.Games.MainMenu.CreatingMainMenuUI;
import imu.iCasino.Interfaces.CreatingTable;
import imu.iCasino.Interfaces.IPlanerCard;
import imu.iCasino.Prompts.ConvPromptPlaner;
import net.md_5.bungee.api.ChatColor;

public class CreateTableCrashUI extends CustomInvLayout implements CreatingTable
{
	CrashDataPlanerCard _dataCard;
	public CreateTableCrashUI(Plugin main, Metods metods, Player player) 
	{
		super(main, metods, player, "Create Crash Table", 3*9);
		
	}
	public void INIT(CrashDataPlanerCard dataCard)
	{		
		System.out.println("INIT");
		_dataCard = dataCard;
		if(dataCard == null)
		{
			_dataCard = new CrashDataPlanerCard();
			System.out.println("New datacard made");
		}
			
		setupButtons();
		openThis();
	}
	
	
	enum BUTTON implements IButton
	{
		NONE,
		BACK,
		CONFIRM,
		SET_NAME;
		
	}
	public BUTTON getButton(ItemStack stack)
	{
		String name = getButtonName(stack);
		if(name != null)
			return BUTTON.valueOf(name);
		
		return BUTTON.NONE;
	}
	@Override
	public void setupButtons() 
	{
		ItemStack displayItem;
		displayItem = setupButton(BUTTON.SET_NAME, Material.NAME_TAG, ChatColor.GOLD + "Set table name", null);
		_metods.addLore(displayItem, ChatColor.DARK_BLUE + "Press to set name", true);
		if(_dataCard.get_tableName() != null)
		{
			_metods.addLore(displayItem, _metods.msgC("&9Name set to: &5"+_dataCard.get_tableName()), true);
		}
		_inv.setItem(0, displayItem);
		
		displayItem = setupButton(BUTTON.BACK, Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "BACK", null);
		_metods.addLore(displayItem, ChatColor.DARK_BLUE + "Go back and dont save!", true);
		_inv.setItem(18, displayItem);
		
		displayItem = setupButton(BUTTON.CONFIRM, Material.GREEN_WOOL, ChatColor.GREEN + "CONFIRM", null);
		_metods.addLore(displayItem, ChatColor.DARK_BLUE + "Make Crash game table by pressing confim", true);
		_inv.setItem(26, displayItem);
		
	}
		
	@Override
	public void invClosed(InventoryCloseEvent e) 
	{
			
	}

	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		e.setCancelled(true);
		BUTTON button = getButton(e.getCurrentItem());
		
		ConversationFactory cf = null;
		String question = null;
		Conversation conv = null;
		
		switch (button) 
		{
		case NONE:			
			break;
		case SET_NAME:
			cf = new ConversationFactory(_main);
			question = "Give a Table name";
			conv = cf.withFirstPrompt(new ConvPromptPlaner(this, 0, question)).withLocalEcho(true).buildConversation(_player);
			conv.begin();
			_player.closeInventory();
			break;
		case BACK:
			new CreatingMainMenuUI(_main, _metods, _player).openThis();
			break;
		case CONFIRM:
			confirm();
			break;

		}
	}
	
	void confirm()
	{
		
	}
	
	@Override
	public void reOpen() 
	{
		new CreateTableCrashUI(_main, _metods, _player).INIT(_dataCard);
	}
	@Override
	public IPlanerCard getDataCard() 
	{
		return _dataCard;
	}
	@Override
	public Player getPlayer() 
	{
		return _player;
	}

	

}
