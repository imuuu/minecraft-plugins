package imu.iGeneralStore.Invs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iGeneralStore.Main.Main;
import imu.iGeneralStore.Other.CustomInvLayout;
import imu.iGeneralStore.ShopUtl.Shop;
import imu.iGeneralStore.ShopUtl.ShopItem;

public class ShopUI extends CustomInvLayout implements Listener
{
	String pd_buttonType = "shop_buttonType";
	Shop _shop;

	ArrayList<ShopItem> _shopItems;
	ShopItem[] _playerItemsNormal;
	ShopItem[] _playerItemsOthers; 
	
	int _player_slots_start = 36;
	int _shop_slot_start = 0;
	
	PLAYER_INV_STATE p_state = PLAYER_INV_STATE.NORMAL;
	
	ItemStack empty_display;
	ItemStack[] p_state_display = new ItemStack[2];
	
	int _playerInvPage = 0;
	int _shopInvPage = 0;
	
	int _inv_state = 0;
	
	HashMap<Material, HashMap<Integer,ItemStack>> _player_inv_refs = new HashMap<>();
	public ShopUI(Main main, Player player, Shop shop) 
	{
		super(main, player, shop.getNameWithColor(), 9*6);
		_main.getServer().getPluginManager().registerEvents(this, _main);
		_shop = shop;
		
		_playerItemsNormal = new ShopItem[_player.getInventory().getContents().length];
		_playerItemsOthers = new ShopItem[_player.getInventory().getContents().length];
		//_shopItems.add(new ShopItem[27]);
		_shopItems = shop.get_items(); //Referenssi !
		openThis();
		
		spawnLayout();
		loadPlayerInv();
		loadShopInv();
		
	}
	public enum PLAYER_INV_STATE
	{
		NORMAL,
		OTHER_STUFF;
	}
	public enum BUTTON
	{
		NONE,
		SHOP_ITEM,
		PLAYER_ITEM,
		GO_LEFT_SHOP,
		GO_RIGHT_SHOP,
		GO_LEFT_PLAYER,
		GO_RIGHT_PLAYER,
		STATE_PLAYER_INV;
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		if(isThisInv(e))
		{
			_shop.closeUI(_player);
			HandlerList.unregisterAll(this);
		}
	}
	
	ItemStack setButton(ItemStack stack, BUTTON b)
	{
		_ia.setPersistenData(stack, pd_buttonType, PersistentDataType.STRING, b.toString());
		return stack;
	}
	
	public BUTTON getButton(ItemStack stack)
	{
		String button = _ia.getPersistenData(stack, pd_buttonType, PersistentDataType.STRING);
		if(button != null)
			return BUTTON.valueOf(button);
		
		return BUTTON.NONE;
	}
	
	public ItemStack setupButton(BUTTON b, Material material, String displayName, int itemSlot)
	{
		ItemStack sbutton = new ItemStack(material);
		_ia.setDisplayName(sbutton, displayName);
		setButton(sbutton, b);
		_inv.setItem(itemSlot, sbutton);
		return _inv.getItem(itemSlot);
	}
	@SuppressWarnings("incomplete-switch")
	@EventHandler
	public void onClick(InventoryClickEvent e)
	{
		int rawSlot = e.getRawSlot();
		int slot = e.getSlot();
		
		if(isThisInv(e))
		{		
			e.setCancelled(true);
			if(rawSlot == slot)
			{
				ItemStack stack = e.getCurrentItem();
				if(stack == null)
					return;
				
				BUTTON button = getButton(e.getCurrentItem());
				int amount = 1;
				ClickType c_type = e.getClick();
				
				ShopItem si = getItem(slot);

				if(si != null)
				{
					if(si.isEmpty())
						return;
					
					switch(c_type)
					{
						case LEFT:
							amount = 1;
							break;
						case RIGHT:
							amount = 8;
							break;
						case SHIFT_LEFT:
							amount = 64;
							break;
						case SHIFT_RIGHT:
							amount = si.get_amount(); //all
							break;
					}
								
					if(si.get_amount() < amount)
					{
						amount = si.get_amount();
					}	
				}
				
				
				switch (button) 
				{
				case GO_LEFT_PLAYER:
					System.out.println("go left");

					chancePlayerPage(-1);
					refresPlayerInv();
					break;			
				case GO_RIGHT_PLAYER:
					System.out.println("go rihgt");
					chancePlayerPage(1);
					refresPlayerInv();
					break;
				case GO_LEFT_SHOP:
					break;
				case GO_RIGHT_SHOP:
					break;
				case NONE:
					break;
				case PLAYER_ITEM:
					Integer shop_slot_id = pushToShop(si,amount);
					
					if( shop_slot_id != null)
					{
						//pushing to shopslots
						ShopItem si_push = getItem(shop_slot_id);
						//_inv.setItem(shop_slot_id, si_push.getDisplayItem());
						invSetItem(shop_slot_id, si_push, BUTTON.SHOP_ITEM);
						//_shop.refresShopUIslotALLcustomers(shop_slot);
						
						
						//take from playerslots
						addAmountShopItem(si, amount*-1);
						//_inv.setItem(slot, si.getDisplayItem());
						invSetItem(slot, si, BUTTON.PLAYER_ITEM);
						
						removeItemFromPlayerInv(si, amount);
						
					}
									
					break;
				case SHOP_ITEM:
					//pushing to playerslots and refreshing it
					pushToPlayerUI(si, amount);
					
					addAmountShopItem(si, amount*-1);
					invSetItem(slot, si, BUTTON.SHOP_ITEM);
					
					//_ia.moveItemFirstFreeSpaceInv(copy, _player, true); // TÄÄ OMAKS
					addItemToPlayerInv(si, amount);
					
					break;
				case STATE_PLAYER_INV:
					
					if(p_state == PLAYER_INV_STATE.NORMAL)
					{
						p_state = PLAYER_INV_STATE.OTHER_STUFF;
					}else
					{
						p_state = PLAYER_INV_STATE.NORMAL;
					}
					refresPlayerInv();
					break;
				default:
					break;				
				}

			}
		}
		
	}
	
	void addItemToPlayerInv(ShopItem item, int amount)
	{
		ItemStack item_clone = item.getRealItem().clone();
		item_clone.setAmount(64);
		int num = amount / 64;
		int remainder = amount % 64;

		HashMap<Integer,ItemStack> refs = _player_inv_refs.get(item_clone.getType());
		Inventory playerInv = _player.getInventory();
		
		//aboce 64 amount
		for(int j = 0; j < num; ++j)
		{
			ItemStack item_spawn = item_clone.clone();
			//itemM.moveItemFirstFreeSpaceInv(item_spawn, player, true);
			Integer slot = playerInv.firstEmpty();
			System.out.println("Slot");
			playerInv.setItem(slot, item_spawn);
			
			if(refs != null)
			{
				refs.put(slot,playerInv.getItem(slot));
				continue;
			}

			HashMap<Integer,ItemStack> new_type = new HashMap<>();
			playerInv.setItem(slot, item_spawn);
			new_type.put(slot,playerInv.getItem(slot));
			_player_inv_refs.put(item_clone.getType(), new_type);
			refs = _player_inv_refs.get(item_clone.getType());
		}
		
		//under 64
		if(remainder > 0)
		{
			Integer slot = playerInv.firstEmpty();
			ItemStack item_spawn = item_clone.clone();
			item_spawn.setAmount(remainder);

			if(refs != null)
			{		
				boolean isSpace = false;
				int total_amount = remainder;
				for(Map.Entry<Integer,ItemStack> entry : _player_inv_refs.get(item_clone.getType()).entrySet())
				{ 
					//int hSlot = entry.getKey();
					ItemStack s = entry.getValue();
					
					int stackAmount = s.getAmount();
					if(stackAmount < s.getMaxStackSize())
					{
						int space = s.getMaxStackSize() - s.getAmount();
						System.out.println("Space: "+space);
						int remain = space - total_amount;
						//space -= total_amount;
						
						if(remain >= 0)
						{						
							s.setAmount(s.getAmount() + total_amount);
							isSpace = true;
							break;
						}
						
						if(remain < 0)
						{
							s.setAmount(64);
						}
						total_amount = Math.abs(remain);						
					}
					
					if(total_amount <= 0)
					{
						isSpace = true;
						break;
					}
						
				}
				
				if(!isSpace)
				{
					playerInv.setItem(slot, item_spawn);
					_player_inv_refs.get(item_clone.getType()).put(slot,playerInv.getItem(slot));
				}
				
			}
			else
			{
				HashMap<Integer,ItemStack> new_type = new HashMap<>();
				playerInv.setItem(slot, item_spawn);
				new_type.put(slot,playerInv.getItem(slot));
				_player_inv_refs.put(item_clone.getType(), new_type);
				
			}			
		}

	}
	
	void removeItemFromPlayerInv(ShopItem si, int remove_amount)
	{
		Material si_type = si.getRealItem().getType();
		if(_player_inv_refs.containsKey(si_type))
		{
			//ArrayList<Integer> remove_ids = new ArrayList<>();
			int total_removable_amount = remove_amount;
			
			int count = 0;
			for(ItemStack pInvItem : _player_inv_refs.get(si_type).values())
			{
				if(pInvItem != null && pInvItem.getType() != Material.AIR)
				{
					int pItemAmount = pInvItem.getAmount();
					int remain = pItemAmount - total_removable_amount;
				
					if(remain < 0)
					{
						total_removable_amount = Math.abs(remain);
						//remove_ids.add(count);
						System.out.println("remove id: "+count);
						pInvItem.setAmount(0);
					}
					else
					{
						pInvItem.setAmount(remain);
						break;
					}								
				}
				count++;
			}
//			for(int id : remove_ids)
//			{
//				_player_inv_refs.get(si.getRealItem().getType()).remove(id);
//			}
		}
	}
	
	final void spawnLayout()
	{
		setupButton(BUTTON.GO_LEFT_SHOP, Material.DARK_OAK_SIGN, _ia.msgC("&9<< Shop"), 27);
		setupButton(BUTTON.GO_RIGHT_SHOP, Material.DARK_OAK_SIGN, _ia.msgC("&9Shop >>"), 35);
		setupButton(BUTTON.GO_RIGHT_PLAYER, Material.BIRCH_SIGN, _ia.msgC("&9<< Inv"), 30);
		setupButton(BUTTON.GO_RIGHT_PLAYER, Material.BIRCH_SIGN, _ia.msgC("&9Inv >>"), 32);
		p_state_display[1] = setupButton(BUTTON.STATE_PLAYER_INV, Material.STONE, _ia.msgC("&9Blocks, Ores..."), 31);
		p_state_display[0] = _ia.hideAttributes(setupButton(BUTTON.STATE_PLAYER_INV, Material.NETHERITE_CHESTPLATE, _ia.msgC("&9Tools, Armor..."), 31));
		
		
		ItemStack redLine = _ia.setDisplayName(new ItemStack(Material.RED_STAINED_GLASS_PANE), " ");
		_inv.setItem(28, redLine);_inv.setItem(29, redLine);_inv.setItem(35-1, redLine);_inv.setItem(35-2, redLine);
	}
	
	final void loadShopInv()
	{
		for(int i = 0;  i < _shopItems.size(); ++i)
		{
			if(i > 27)
				break;
			
			invSetItem(i, _shopItems.get(i), BUTTON.SHOP_ITEM);
		}
		
	}
	
	final void loadPlayerInv() 
	{
		HashMap<Material, ShopItem> mats_i = new HashMap<>();
		//HashMap<Integer, Material> i_mats = new HashMap<>();
		_player_inv_refs = new HashMap<>();
		
		ItemStack[] content = _player.getInventory().getContents();
		for(int i = 0; i < content.length ;++i)
		{
			ItemStack s = content[i];
			
			if(s == null)
				continue;
			
			if(mats_i.containsKey(s.getType()) )
			{
				//System.out.println("found same type: "+s.getType());
				if(mats_i.get(s.getType()).getRealItem().isSimilar(s))
				{
					//System.out.println("found same type2: "+s.getType());
					mats_i.get(s.getType()).addAmount(s.getAmount());
					_player_inv_refs.get(s.getType()).put(i,s);
					continue;
				}
				
				
			}
			ShopItem shopitem = new ShopItem(_main,i ,s,s.getAmount());
			mats_i.put(s.getType(), shopitem);
			HashMap<Integer,ItemStack> ar = new HashMap<>();
			ar.put(i,s);
			_player_inv_refs.put(s.getType(), ar);
			//i_mats.put(i,s.getType());
			
		}
		
//		List<Material> test = new ArrayList<>(i_mats.values());
//		Collections.sort(test);
		int count = 0;
		int count2 = 0;
		for(ShopItem si : mats_i.values())
		{
			ItemStack displayItem = si.getDisplayItem();
			setButton(displayItem, BUTTON.PLAYER_ITEM);
			if(_ia.isArmor(si.getRealItem()) || _ia.isTool(si.getRealItem()))
			{
				 _playerItemsOthers[count2++] = si;
				continue;
			}
						
			_playerItemsNormal[count] = si;		
			if(count < 18)
			{
				_inv.setItem(count+_player_slots_start, displayItem);
			}
			count++;
//			if(++count > _playerItemsNormal.length)
//				break;
		}		
	}
	

	void addAmountShopItem(ShopItem shopItem, int amount)
	{
		shopItem.addAmount(amount);
	}
	
	void refresPlayerInv()
	{
		ShopItem[] items = getPlayerItems();
		System.out.println("items len: "+items.length);
		
		_inv.setItem(31, p_state_display[_inv_state]);
		
		for(int i = _player_slots_start; i < _size; ++i)
		{
			int slot = getPlayerSlot(i);
			
			if(slot < items.length)
			{
				ShopItem si = items[slot];
				if(si != null)
					_inv.setItem(i, si.getDisplayItem());
				continue;
			}
			
			_inv.setItem(i,new ItemStack( Material.BLACK_STAINED_GLASS_PANE) );
				
		}
	}
	
	ShopItem[] getPlayerItems()
	{
		if(p_state == PLAYER_INV_STATE.NORMAL)
		{
			_inv_state = 0;
			return  _playerItemsNormal;
			
		}
		_inv_state = 1;
		return _playerItemsOthers;
	}
	
	void chancePlayerPage(int page)
	{
		int maxPages =(int) Math.ceil(getPlayerItems().length/18.0)-1;
		System.out.println("max pages: "+maxPages);
		_playerInvPage += page;
		if(_playerInvPage < 0)
		{
			_playerInvPage = maxPages; return;
		}
			
				
		if(_playerInvPage > maxPages)
		{
			_playerInvPage = 0; return;
		}
		
		
			
		
		
	}
	int getShopSlot(int slot)
	{
		return slot+ (_shopInvPage * 27);
	}
	
	int getPlayerSlot(int slot)
	{
		System.out.println("asdked: "+slot+" got: "+(slot + (_playerInvPage* 18) -_player_slots_start));
		System.out.println("_page: "+_playerInvPage);
		return slot + (_playerInvPage* 18) -_player_slots_start;
	}
	ShopItem getItem(int slot)
	{
		if(slot >= 0 && slot < 27) //shopside
		{
			return _shopItems.get(getShopSlot(slot));
		}
		
		if(slot >= _player_slots_start && slot <_size)
		{
			if(p_state == PLAYER_INV_STATE.NORMAL)
			{
				return _playerItemsNormal[slot-_player_slots_start];
			}
			
			if(p_state == PLAYER_INV_STATE.OTHER_STUFF)
			{
				return _playerItemsOthers[slot-_player_slots_start];
			}
			
		}
		return null;
	}
	public void invSetItem(int slot, ShopItem item, BUTTON b)
	{
		item.setSlot(slot);
		_inv.setItem(slot, setButton(item.getDisplayItem(), b));
	}
//	void refreshItem(int slot)
//	{
//		getItem(slot).refresh();
//	}
	
	/*
	 * returns push shopitem to playerItems and setIt to UI(refres)
	 */
	public void pushToPlayerUI(ShopItem shopItem, Integer amount)
	{		
		System.out.println("Push to player");
		ShopItem[] pItems = _playerItemsNormal;
		int state = 0;
		if(shopItem.isToolArmor())
		{
			pItems = _playerItemsOthers;
			state = 1;
		}
					
		int count = 0;
		Integer id = null;
		int firstEmpty = -1;
		for(ShopItem si : pItems)
		{
			id = count;
			
			if(si != null && si.isSameKind(shopItem))
			{
				addAmountShopItem(si, amount);
				firstEmpty = -1;
				break;
			}
						
			
			if(si == null && firstEmpty < 0)
			{
				System.out.println("push to player: added last");
				firstEmpty = count;
				//pItems[count] = new ShopItem(_main, count,shopItem.getRealItem(), amount);		
			}
			
//			if(si.isEmpty() && firstEmpty < 0)
//			{
//				System.out.println("push to player: empty stack found");
//				//si.setData(count,shopItem.getRealItem(), amount);
//				firstEmpty = count;
//				break;
//			}
						
			count++;
		}
		
		if(firstEmpty > -1)
		{
			id = firstEmpty;
			pItems[id] = new ShopItem(_main, id, shopItem.getRealItem(), amount);
		}
		
		if(id != null &&  state == _inv_state)
		{
			if(count < 18)
			{
				System.out.println("Refresh p slot: "+(id+_player_slots_start));
				invSetItem(id+_player_slots_start,  pItems[id], BUTTON.PLAYER_ITEM);
			}
		}
		
		//Drop to ground
		return;
	}
	
	public Integer pushToShop(ShopItem shopItem, Integer amount)
	{
		int count = 0;
		//int firstEmpty = -1;
		for(ShopItem si : _shopItems)
		{
//			if(si.isEmpty() && firstEmpty < 0)
//				firstEmpty = count;
//			
			if(si != null && si.isSameKind(shopItem))
			{
				addAmountShopItem(si, amount);
				return count;
			}
			count++;
		}
		
//		if(firstEmpty > -1)
//		{
//			_shopItems.set(firstEmpty, new ShopItem(_main, firstEmpty,shopItem.getRealItem(), amount)); 
//			return firstEmpty;
//		}
		System.out.println("couldnt find spot.. search");
		_shopItems.add(new ShopItem(_main, _shopItems.size()-1 ,shopItem.getRealItem(), amount));
		
//		count = 0;
//		for(ShopItem si : _shopItems)
//		{
//			if(si == null)
//			{
//				_shopItems[count] = new ShopItem(_main, shopItem.getRealItem(), amount);
//				return count;
//			}
//
//		count++;
//		}
		
		return _shopItems.size()-1;
	}
}
