package imu.TokenTp.CustomItems;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.TokenTp.Enums.TeleTokenType;
import imu.TokenTp.Enums.TokenType;
import imu.TokenTp.Managers.TeleTokenManager;
import imu.TokenTp.main.Main;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;
import net.md_5.bungee.api.ChatColor;

public class ItemTeleToken extends ItemStack
{
	Main _main = null;
	Metods _itemM = null;
	TeleTokenManager _ttManager = null;
	
	Material _mat = Material.PAPER;
	
	String _desc="";
	
	String _addressName="";
	String _displayName="";
	Location _loc;
	
	TeleTokenType _type;
	TokenType _tokenType;
	
	
	
	
	public ItemTeleToken(Main main) 
	{
		super(Material.STONE);
		_main = main;
		_ttManager = main.getTeleTokenManager();
		_itemM = ImusAPI._metods;
		setTokenType(TokenType.TOKEN_TO_LOCATION);
		setData();
	}
	
	public ItemTeleToken(Main main, ItemStack stack) 
	{
		super(stack);
		_main = main;
		_ttManager = main.getTeleTokenManager();
		_itemM =  ImusAPI._metods;
	}
		
	void setData()
	{
		this.setType(_mat);
		
		//String str = ChatColor.AQUA +" "+ ChatColor.MAGIC+ "#" +ChatColor.DARK_PURPLE +" "+ ChatColor.BOLD + "Teletoken"+ ChatColor.AQUA +" "+ ChatColor.MAGIC+ "#";
		//setDisplayName(str);
	}
	
	public void setAllData(String desc, String addressName, Location loc, TeleTokenType type, TokenType token_type)
	{
		setDesc(desc);
		setAddress(addressName, loc);
		setTeleTokenType(type);
		setTokenType(token_type);
	}
	
	void setDisplayName(String str)
	{
		_displayName = str;
		_itemM.setDisplayName(this, _displayName);
	}
	
	public void setDesc(String desc)
	{
		int i =_itemM.findLoreIndex(this, "Desc:");
		String lore = ChatColor.AQUA + "Desc: "+ChatColor.DARK_PURPLE + desc;
		if(i > 0 )
		{
			_itemM.reSetLore(this, lore, i);
		}else
		{
			_itemM.addLore(this, lore, false);
		}
		_desc = desc;
		
		_itemM.setPersistenData(this, _ttManager.getPDword("desc"), PersistentDataType.STRING, desc);
	}
	
	public void removeDesc()
	{
		_itemM.removeLore(this, "Desc:");
	}
	
	public void setAddress(String addressName, Location loc)
	{
		int i =_itemM.findLoreIndex(this, "Destination:");
		String lore = ChatColor.AQUA + "Destination: "+ ChatColor.DARK_PURPLE + addressName;
		if(i > 0 )
		{
			_itemM.reSetLore(this, lore, i);
		}else
		{
			_itemM.addLore(this, lore, false);
		}
		
		_addressName = addressName;
		_itemM.setPersistenData(this, _ttManager.getPDword("addressname"), PersistentDataType.STRING, addressName);
		if(loc != null)
		{
			_loc = loc;
			_ttManager.setLocPd(this, loc);
			
		}
	}
	
	public String getDisplayName()
	{
		return _displayName;
	}
	
	public String getAddressName()
	{
		return _addressName;
	}
	
	void setTeleTokenType(TeleTokenType type)
	{
		
		_type = type;
		String typeString = ChatColor.AQUA + "Type: "+ ChatColor.DARK_PURPLE + type.toString();
		_itemM.addLore(this, typeString, false);
		_itemM.setPersistenData(this, _ttManager.getPDword("type"), PersistentDataType.STRING, type.toString());
	}
	
	void setTokenType(TokenType type)
	{
		_itemM.setPersistenData(this, _ttManager.getPDword("tokentype"), PersistentDataType.STRING, type.toString());
	}

}
