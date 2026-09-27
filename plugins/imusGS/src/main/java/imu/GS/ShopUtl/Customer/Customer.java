package imu.GS.ShopUtl.Customer;

import org.bukkit.entity.Player;

import imu.GS.Main.Main;
import imu.iAPI.Other.CustomInvLayout;

public class Customer 
{
	Main _main;
	public Player _player;
	private CustomInvLayout _inv;
	
	public Customer(Player player, CustomInvLayout inv)
	{
		_player = player;
		_inv = inv ;
	}
	
	public Customer Open()
	{
		//System.out.println("opening");
		_inv.openThis();
		return this;
	}
	
	public CustomInvLayout GetInv()
	{
		return _inv;
	}
	
	public void Close()
	{
		_player.closeInventory();
	}
	
}
