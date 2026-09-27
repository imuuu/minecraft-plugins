package imu.GS.Invs;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


import imu.GS.ShopUtl.ShopItems.ShopItemSeller;
import imu.iAPI.Other.CustomInvLayout;

public abstract class CustomerInv extends CustomInvLayout
{

	public CustomerInv(Plugin main, Player player, String name, int size) 
	{
		super(main, player, name, size);
		
	}
	
	//public abstract void UpdateCustomerSlot(ShopItemCustomer sic, int page, int slot);
	
	public abstract void SetShopSlot(ShopItemSeller sis, int page, int slot);

}
