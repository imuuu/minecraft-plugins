package imu.GS.ShopUtl.ShopItems;


import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import imu.GS.Main.Main;
import imu.GS.Other.LogData;
import imu.GS.ShopUtl.ShopItemResult;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.Customer.ShopItemCustomer;
import imu.GS.ShopUtl.ItemPrice.ItemPrice;
import imu.GS.ShopUtl.ItemPrice.PriceMaterial;
import imu.iAPI.InvUtil.InventoryReaderStack;
import imu.iAPI.InvUtil.InventoryReaderStack.ItemInfo;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public class ShopItemCustomerShulkerBox extends ShopItemCustomer
{
	//ItemStack[] _shulkerContent = new ItemStack[27];
	ShopItemResult[] _shulkerContentSis;
	LinkedList<LogData> _logDatas = new LinkedList<>();
	public ShopItemCustomerShulkerBox(Main main, ShopNormal shopBase, Player player, ItemStack real, int amount) 
	{
		super(main, shopBase, player, real, amount);
		ItemStack outOfStock = new ItemStack(Material.PURPLE_STAINED_GLASS);
		Metods.setDisplayName(outOfStock, "&6Shulker Content &aCleared!");
		SetDisplayOutOfStock(outOfStock);
		//CalculateContent();
	}
	
	public List<LogData> GetLogData()
	{
		return _logDatas;
	}
	
	public ItemPrice GetCalculatedPriceFromContent()
	{
		double price = 0.0;
		BlockStateMeta bsm = (BlockStateMeta)_real_stack.getItemMeta();
		if(bsm.getBlockState() instanceof ShulkerBox)
		{
			ShulkerBox shulker = (ShulkerBox)bsm.getBlockState();
			Cal(shulker.getInventory().getContents());
			InventoryReaderStack invReader = new InventoryReaderStack(shulker.getInventory());
			for(ItemInfo info : invReader.GetAllData().values())
			{			
				ItemStack clonedStack = info._clonedStack;
				PriceMaterial pm =  _main.GetMaterialManager().GetPriceMaterialAndCheck(clonedStack);
				pm.SetCustomerPrice(pm.GetPrice() * _shopBase.get_buyM());
				
				for(ShopItemSeller[] sibs : _shopBase.get_items())
				{
					for(ShopItemSeller sib : sibs)
					{
						if(sib == null) continue;
						if(sib.IsSameKind(clonedStack))
						{
							pm.SetShopItem(sib);
							break;
						}
						
					}
					if(pm.HasShopitem()) break;
				}

				double pricee = pm.GetCustomerPrice(info._totalCount);
				ShopItemSeller logItem = new ShopItemSeller(_main,_shopBase,clonedStack.clone(),info._totalCount);
				_logDatas.add(new LogData(logItem, pricee, info._totalCount));
				
				price +=  pricee;
			}

		}
		if(price <= 0) price = 0;	
		
		PriceMaterial priceItem = new PriceMaterial(null);
		priceItem.SetPrice(price);

		return priceItem;
	}
	
	void Cal(ItemStack[] content)
	{
		LinkedList<ShopItemResult> items = new LinkedList<>();
		for(ItemStack stack : content)
		{
			if(stack == null) continue;
			
			boolean found = false;
			for(ShopItemResult sir : items)
			{
				if(sir._stack.isSimilar(stack)) 
				{
					sir.AddAmount(stack.getAmount());
					found = true;
					break;
				}
			}
			if(!found)
			{
				items.add(new ShopItemResult(stack.clone(),stack.getAmount()));
			}
		}
		
		ShopItemResult[] stacks = new ShopItemResult[items.size()];
		for(int i = 0; i < stacks.length; i++)
		{
			stacks[i] = items.get(i);
		}
		_shulkerContentSis = stacks;
	}
	
	@Override
	protected void toolTip() 
	{
		if(_amount <= 0)
		{
			_display_stack = GetDisplayOutOfStock();
			return;
		}
		
		String[] lores = {
				_lores[0]+Get_amount()+" &9____",
				"&9Sell Content with &5"+ GetItemPrice().GetCustomerPriceStr(1),
				"",

				
		};
		_display_stack = _real_stack.clone();
		_display_stack.setAmount(1);
		_metods.addLore(_display_stack, lores);
		return;
	}
	
	void ClearShulkerBox()
	{
		boolean b = ImusAPI._metods.SetShulkerBoxContent(_player_itemstack_refs.get(0), new ItemStack[27]); //clears
		
		if(!b) System.out.println("Couldnt clear the shulker box.. This message shouldnt never happen!");

	}
	@Override
	protected void MinusAmount(int amount) 
	{
		//System.out.println("shulerk minus");
	}
	
	@Override
	protected void PlusAmount(int amount)
	{
		//System.out.println("shulerk plus");
	}
	
	@Override
	public ShopItemResult[] GetTransactionResultItemStack() 
	{
		ClearShulkerBox();
		return _shulkerContentSis;
	}

}
