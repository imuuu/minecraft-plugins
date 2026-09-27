package imu.GS.ShopUtl.Customer;

import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;

import imu.GS.Main.Main;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopItemResult;
import imu.GS.ShopUtl.ItemPrice.ItemPrice;
import imu.GS.ShopUtl.ItemPrice.PriceMaterial;
import imu.GS.ShopUtl.ItemPrice.PriceMoney;
import imu.iAPI.Main.ImusAPI;

public class ShopItemCustomer extends ShopItemBase
{
	protected ArrayList<ItemStack> _player_itemstack_refs = new ArrayList<>();
	Player _player;
	//public int itemSlot = slot;
	public ShopItemCustomer(Main main, ShopNormal shopBase,Player player,ItemStack real, int amount) 
	{
		super(main,shopBase,real, amount);
		_player = player;	
	}
	public Player GetOwner()
	{
		return _player;
	}
	
	@Override
	public void SetTargetShopitem(ShopItemBase sib) 
	{
		_customerShopitemTargets.put(_player.getUniqueId(), sib);
	}
	
	public void RefreshAmount()
	{
		int count = 0;
		for(ItemStack stack : _player_itemstack_refs)
		{
			if(stack != null && stack.isSimilar(_real_stack))
			{
				count += stack.getAmount();
			}
		}
		Set_amount(count);
		toolTip();
	}
	
	@Override
	protected void SetShowPrice(ItemPrice price) 
	{
		if(price instanceof PriceMaterial)
		{
			double p = price.GetPrice();
			((PriceMoney)price).SetCustomerPrice(p * _shopBase.get_buyM());
			return;
		}
		
	}
	
	@Override
	protected LinkedList<String> GetLores()
	{
		LinkedList<String> lores = super.GetLores();
		
		if(_player != null  && GetItemPrice() instanceof PriceMaterial)
		{
			PriceMaterial pm = (PriceMaterial)GetItemPrice(); 
			
			
			if(pm.HasOverflow())
			{
				int amount_in_shop = pm.GetAmountInShop();//pm.HasShopitem() ? GetTargetShopitem(_player.getUniqueId()).Get_amount() : 0;
				int priceDropsIn = pm.GetOverflow().get_softCap()-amount_in_shop;
				lores.add(" ");
				if(priceDropsIn <= 0)
				{
					
					lores.add("&b&k# &e&nThe Price Drops&b&k #");
				}
				else
				{
					lores.add("&e&nThe Price Drops in:&2&l "+ (priceDropsIn));
				}
				
			}
			
		}
		
		return lores;
	}
	
	public void AddPlayerItemStackRef(ItemStack stack)
	{
		//System.out.println("stack: "+stack+" added to: "+_player_itemstack_refs.size()+ " id: "+id);
		_player_itemstack_refs.add(stack);
	}
	
	void RemovePlayerRef(int index)
	{
		//System.out.println("index: "+index+" removed");
		_player_itemstack_refs.remove(index);
	}
	
	public void AddAmountToPlayer(int amount)
	{
		AddAmount(amount);
		//System.out.println("overide addmount");
		if(amount >= 0)
		{
			PlusAmount(amount);
			return;
		}
		MinusAmount(amount);	
	}
	
	public boolean EnoughItems(int amount)
	{
		int count = 0;
		for(ItemStack stack : _player_itemstack_refs)
		{
			if(stack != null && stack.isSimilar(_real_stack))
			{
				count += stack.getAmount();
			}
			
			if(count >= amount) return true;
		}
		return false;
	}
	
	protected void MinusAmount(int amount)
	{
		int left = Math.abs(amount);
		for(int i = _player_itemstack_refs.size()-1; i >= 0 ; --i)
		{
			ItemStack s = _player_itemstack_refs.get(i); //=> s is null
			int num = s.getAmount() - left;
			if(num <= 0)
			{
				s.setAmount(0);
				_player_itemstack_refs.remove(i);
				left = Math.abs(num);
				if(left == 0)
				{
					break;
				}				
			}
			else
			{
				s.setAmount(num);
				break;
			}		
		}

		
	}
	
	protected void PlusAmount(int amount)
	{
//		System.out.println("plus amount");
//		int left = amount;
		Integer[] slots = ImusAPI._metods.InventoryAddItemOrDrop(_real_stack.clone(), _player,amount);
		for(Integer slot : slots)
		{
			//System.out.println("slot: "+slot);
			if(slot == null) continue;
			AddPlayerItemStackRef(_player.getInventory().getItem(slot));
		}
//		for(ItemStack s : _player_itemstack_refs)
//		{
//			int num = 64 - s.getAmount();
//			if(num == 0)
//				continue;
//			
//			if(num > left)
//				num = left;
//			
//			s.setAmount(s.getAmount() + num);
//			
//			left -= num;
//			
//			if(left <= 0)
//				break;
//		}
//
//		int leftOver;
//		if(left > 64)
//		{
//			leftOver = left % 64;
//		}
//		else
//		{
//			leftOver = left;
//		}
//
//		int full_stacks_amount = (left -leftOver) == 0 ? 0 : (left -leftOver) / 64;
//		ItemStack newStack;
//
//		for(int i = 0; i < full_stacks_amount; ++i)
//		{
//			newStack= _real_stack.clone();
//			newStack.setAmount(64);
//			
//			Integer[] slots = _metods.InventoryAddItemOrDrop(newStack, _player);
//			if(slots.length == 0)
//			{
//				//System.out.println("not space found minus: "+64);
//				AddAmount(64 * -1);
//				continue;
//			}
//				
//			
//			//total_amount_setted += newStack.getAmount();
//			for(int slot = 0; slot < slots.length; slot++)
//			{
//				AddPlayerItemStackRef("plus amount1",_player.getInventory().getItem(slot));
//			}
//			
//		}
//		//AddAmount(total_amount_setted);
//		if(leftOver == 0)
//			return;
//		//total_amount_setted = 0;
//		//System.out.println("N4: "+n4);
//		newStack = _real_stack.clone();
//		newStack.setAmount(leftOver);
//		Integer[] slots = _metods.InventoryAddItemOrDrop(newStack, _player);
//		if(slots.length == 0)
//		{
//			System.out.println("not space found minus2: "+leftOver);
//			AddAmount(leftOver * -1);
//			return;
//		}
//		
//		for(int slot = 0; slot < slots.length; slot++)
//		{
//			AddPlayerItemStackRef("plus amount2",_player.getInventory().getItem(slot));
//		}
	

	}

	@Override
	public JsonObject GetJsonData() {
		
		return null;
	}

	@Override
	public void ParseJsonData(JsonObject data) {
		
		
	}

	@Override
	public ShopItemResult[] GetTransactionResultItemStack() {
		
		return new ShopItemResult[] {new ShopItemResult(GetRealItem(), GetRealItem().getAmount())};
	}
	

	
	

}
