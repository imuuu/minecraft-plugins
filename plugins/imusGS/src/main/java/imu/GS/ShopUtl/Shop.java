package imu.GS.ShopUtl;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import imu.GS.Main.Main;
import imu.GS.ShopUtl.Customer.Customer;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;

public abstract class Shop
{
	protected Main _main;
	private String _name;
	private String _displayName;
	private UUID _uuid;
	private HashMap<UUID, Customer> _hCustomers = new HashMap<>();

	private boolean _locked = false;
	public boolean _temp_lock = false;
	public boolean _temp_modifying_lock = false;

	public Shop(Main main, UUID uuid, String name)
	{
		_main = main;
		SetName(name);
		_uuid = uuid;
	}

	public abstract void OpenModify(Player player);

	public void Open(Player player)
	{
		if (_temp_lock || HasLocked() || _temp_modifying_lock)
		{
			player.sendMessage(Metods.msgC("&9The Shop is temporarily closed! Come back laiter!"));
			if (!player.isOp())
			{
				return;
			}
		}
		OpenShop(player);
	}

	protected abstract void OpenShop(Player player);

	public boolean HasCustomers()
	{
		return _hCustomers.size() > 0;
	}

	public Collection<Customer> GetCustomers()
	{
		return _hCustomers.values();
	}

	public void AddCustomer(Player player, CustomInvLayout inv)
	{
		//_hCustomers.put(player.getUniqueId(), new Customer(player, inv)).Open(); //not working in jdk19???
		_hCustomers.put(player.getUniqueId(), new Customer(player, inv));
		_hCustomers.get(player.getUniqueId()).Open();
	}
	
	public void RemoveCustomer(UUID uuid_player, boolean closeInv)
	{
		if (!_hCustomers.containsKey(uuid_player))
			return;

		Customer customer = _hCustomers.remove(uuid_player);
		if (closeInv)
			customer.Close();
	}

	public void RemoveCustomerALL()
	{
		for (Customer cus : GetCustomers())
		{
			cus.Close();
		}
		_hCustomers.clear();
	}

	public UUID GetUUID()
	{
		return _uuid;
	}

	public String GetNameWithColor()
	{
		return ChatColor.translateAlternateColorCodes('&', _displayName);
	}

	public void SetName(String name)
	{
		_displayName = name;
		_name = ImusAPI._metods.StripColor(name);
	}

	public String GetName()
	{
		return _name;
	}

	public String GetDisplayName()
	{
		return _displayName;
	}

	public boolean HasLocked()
	{
		return _locked;
	}

	public void SetLocked(boolean locked)
	{
		_locked = locked;
		if (_locked && HasCustomers())
		{
			for (Customer customer : GetCustomers())
			{
				RemoveCustomer(customer._player.getUniqueId(), true);
			}
		}
	}
}
