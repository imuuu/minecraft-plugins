package imu.GS.Invs;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Strings;

import imu.GS.ENUMs.ModDataShopStockable;
import imu.GS.Main.Main;
import imu.GS.Prompts.ConvPromptModModifyINV;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopItemModData;
import imu.GS.ShopUtl.ShopItems.ShopItemSeller;
import imu.GS.ShopUtl.ShopItems.ShopItemStockable;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;

public class ChangePriceINV extends CustomInvLayout
{
	Main _main;
	ShopItemBase _sib;
	ShopStocableModifyINV _smmi;
	ShopItemModData _modData;
	public ChangePriceINV(Plugin main, Player player, ShopStocableModifyINV smmi,ShopItemSeller sib, ShopItemModData modData) 
	{
		super(main, player, Metods.msgC("&1Change item price"), 9);
		_main = (Main)main;
		_sib = sib;
		_smmi = smmi;
		_modData = modData;
		setupButtons();
	}
	
	enum BUTTON implements IButton
	{
		NONE,
		BACK,
		PRICE_INV,
		WRITE_PRICE,
		CUSTOM_PRICE
	}
	
	@Override
	public void setupButtons() 
	{
		setupButton(BUTTON.BACK, Material.RED_STAINED_GLASS_PANE,"BACK", 0);
		setupButton(BUTTON.PRICE_INV, Material.DIAMOND,"Open price inv to adjust the price", 2);
		setupButton(BUTTON.WRITE_PRICE, Material.PAPER,"Write the price", 4);
		setupButton(BUTTON.CUSTOM_PRICE, Material.BEACON,"Set custom price", 6);
	}
	@Override
	public void invClosed(InventoryCloseEvent e) 
	{
		
	}
	
	BUTTON GetBUTTON(ItemStack stack)
	{
		if(stack == null) return BUTTON.NONE;
		String bName = getButtonName(stack);		
		if(Strings.isNullOrEmpty(bName)) return BUTTON.NONE;
		return BUTTON.valueOf(bName);
	}
	
	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{

		switch (GetBUTTON(e.getCurrentItem())) 
		{
		case NONE:
			break;
		case BACK:
			_player.closeInventory();
			_smmi.openThis();
			break;
		case CUSTOM_PRICE:
			_player.closeInventory();
			new CreateCustomPriceInv(_main, _player, _smmi, (ShopItemStockable)_sib, _modData).openThis();
			return;	
		case PRICE_INV:
			break;
		case WRITE_PRICE:
			ConversationFactory cf = new ConversationFactory(_main);
			String question = null;
			Conversation conv = null;
			question = ChatColor.DARK_PURPLE + "Give Own Price?";
			conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_player, ModDataShopStockable.CUSTOM_PRICE, _smmi, _modData, question)).withLocalEcho(true).buildConversation(_player);
			conv.begin();
			_player.closeInventory();
			return;
		default:
			break;
		
		}

		
		
	}

	

}
