package imu.TokenTp.Events;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import imu.TokenTp.Enums.TeleState;
import imu.TokenTp.Enums.TeleTokenType;
import imu.TokenTp.Enums.TokenType;
import imu.TokenTp.Managers.TeleTokenManager;
import imu.TokenTp.main.Main;
import imu.iAPI.Main.ImusAPI;

public class TokenEvents implements Listener
{
	Main _main;
	TeleTokenManager _ttManager;
	
	HashMap<UUID, REPAIR_RESULT> rd_for_result = new HashMap<UUID, REPAIR_RESULT>();
	public TokenEvents(Main main) 
	{
		_main = main;
		_ttManager = _main.getTeleTokenManager();
	}
	
	enum REPAIR_RESULT
	{
		NONE,
		TELETOKEN;
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent event)
	{
		if(event.getWhoClicked() instanceof Player)
		{
			Player p = (Player) event.getWhoClicked();
			int slot = event.getRawSlot();
			
			if(event.getView().getType() == InventoryType.ANVIL && slot == 2 && rd_for_result.containsKey(p.getUniqueId()))
			{

				AnvilInventory anvil_inv = (AnvilInventory) event.getInventory();
				ItemStack[] content = anvil_inv.getContents();

				if(_ttManager.isTeleToken(content[0]) && _ttManager.isTeleToken(content[1]) && rd_for_result.get(p.getUniqueId()) == REPAIR_RESULT.TELETOKEN)
				{
					if(isRightCombinationToken(content[0], content[1]))
					{
						ItemStack token = _ttManager.makeToken(_ttManager.getTheCard(content[0], content[1]));
	
						//ImusAPI._metods.moveItemFirstFreeSpaceInv(token, p, true);
						ImusAPI._metods.InventoryAddItemOrDrop(token, p);
						for(ItemStack s : event.getInventory())
						{
							if(s!= null && s.getAmount()> 0)
							{
								s.setAmount(s.getAmount()-1);
							}							
						}
					}
					
				}


			}
		}
	}
	
	@EventHandler
	public void onInvClic(PrepareAnvilEvent event)
	{
		if(event.getView().getPlayer() instanceof Player)
		{
			AnvilInventory anvil_inv = (AnvilInventory) event.getInventory();
			ItemStack[] content = anvil_inv.getContents();
			rd_for_result.put(event.getView().getPlayer().getUniqueId(), REPAIR_RESULT.NONE);
			rd_for_result.put(event.getView().getPlayer().getUniqueId(), null);
			if(isRightCombinationToken(content[0], content[1]))
			{
				rd_for_result.put(event.getView().getPlayer().getUniqueId(), REPAIR_RESULT.TELETOKEN);
				ItemStack stack = _ttManager.makeToken(_ttManager.getTheCard(content[0], content[1]));
				event.setResult(stack);
			}
		}
		
	}
	
	@EventHandler
	public void onUse(PlayerInteractEvent event)
	{
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			ItemStack stack = event.getItem();
			
			if(_ttManager.isTeleToken(stack) && (_ttManager.getTeleTokenType(stack) == TeleTokenType.CARD || _ttManager.getTeleTokenType(stack) == TeleTokenType.TOKEN) && !_ttManager.hasLocation(stack))
			{
				//TODO permission taha
				event.getPlayer().sendMessage(ChatColor.AQUA + "Location has been set to this card(s)!");
				_ttManager.setLocPd(stack, event.getPlayer().getLocation());
			}
			else if(_ttManager.isToken(stack) && _ttManager.getTeleTokenType(stack) == TeleTokenType.TOKEN)
			{

				if(event.getClickedBlock() == null  || (!(event.getClickedBlock().getState() instanceof InventoryHolder) && (event.getClickedBlock().getType() != Material.ANVIL)))
				{
					TokenType t = _ttManager.getTokenType(stack);
					Player player = event.getPlayer();
					if(_ttManager.isTokenCDready(player.getUniqueId()) || t == TokenType.TOKEN_TO_PLAYER)
					{
						_ttManager.addTokenCD(player.getUniqueId());
						_ttManager.setTeleState(player.getUniqueId(),TeleState.ACTIVATED);
						
						switch (t) 
						{
						case TOKEN_TO_LOCATION:
							_ttManager.startTeleport(player, _ttManager.getLocFromPd(stack));
							stack.setAmount(stack.getAmount()-1);
							break;
						case TOKEN_TO_PLAYER:
							if(!_ttManager.hasRequestAnwsered(player.getUniqueId()))
							{
								player.chat("/tttp list");
							}else
							{
								if(_ttManager.getRequestAnwser(player.getUniqueId()) == true)
								{
									Player target = Bukkit.getPlayer(_ttManager.getRequestTarget(player.getUniqueId()));
									if(target != null)
									{
										_ttManager.setLocPd(stack, target.getLocation());
										_ttManager.startTeleport(player, _ttManager.getLocFromPd(stack));
										stack.setAmount(stack.getAmount()-1);
									}
									
								}
							}
								
							break;

						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "You need to wait little bit before using this again");
					}
				}
				
				
			}
			
			
		}
	}
	
	
	
	boolean isRightCombinationToken(ItemStack stack1,ItemStack stack2)
	{
		if(_ttManager.isTeleToken(stack1) && _ttManager.isTeleToken(stack2))
		{
			if((_ttManager.getTeleTokenType(stack1) == TeleTokenType.BASE && _ttManager.getTeleTokenType(stack2) == TeleTokenType.CARD)
					|| (_ttManager.getTeleTokenType(stack1) == TeleTokenType.CARD && _ttManager.getTeleTokenType(stack2) == TeleTokenType.BASE))
			{
				return true;
			}
		}		
		return false;
	}
}
