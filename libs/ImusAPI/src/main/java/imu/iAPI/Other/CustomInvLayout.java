package imu.iAPI.Other;


import imu.iAPI.Interfaces.CustomInv;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

//import chestcleaner.sorting.SortingEvent;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;

public abstract class CustomInvLayout implements Listener, CustomInv
{
	protected Plugin _plugin = null;
	protected Metods _metods = ImusAPI._metods;
	String _name="";
	protected int _size = 0;
	
	protected Inventory _inv = null;
	protected Player _player = null;
	boolean _hasRegisteredEvents = false;
	
	protected DENY_ITEM_MOVE _denyItemMove = DENY_ITEM_MOVE.BOTH;
	
	protected ItemStack _droppedStack = null;
	protected ItemStack _takenStack = null;
	protected int _droppedSlot = -1;
	protected int _takenSlot = -1;

	public CustomInvLayout(Plugin main, Player player, String name, int size)
	{
		_plugin = main;

		_name = Metods.msgC(name);
		_size = AdjustSize(size);
		_player = player;	
		_inv =  _plugin.getServer().createInventory(null, _size, _name);
		RegisterToEvents();
		//ProtocolManager pManager = ImusAPI._instance.GetProtocolManager();
		
		
		
		//pManager.removePacketListener(this);
		
		
	}
	protected enum DENY_ITEM_MOVE
	{
		UPPER_INV,
		LOWER_INV,
		BOTH,
		NONE,
	}
	
	//works only for example arraylists
	public int PageChance(int page, int rollAmount, int listSize, int slotAmount)
	{
		int maxPages =(int) Math.ceil(listSize/(double)slotAmount)-1;
		page += rollAmount;
		if(page < 0){page = maxPages; return page;}				
		if(page > maxPages) {page = 0; return page;}	
		return page;
	}
	
	public BukkitTask RenameWindow(String rename)
	{
		
		return new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				
				//TODO need to be fixed in future where packet is more clear. Some reason packet gives only array[1]
//				ProtocolManager pManager = ImusAPI._instance.GetProtocolManager();
//				PacketContainer packet = pManager.createPacket(PacketType.Play.Server.OPEN_WINDOW);
//				packet.getIntegers().write(0, ImusAPI._instance.GetProtocolLibUtil().GetInventoryID(_player));
//				//packet.getIntegers().write(1, ImusAPI._instance.GetProtocolLibUtil().GetInventoryType(_player));
//				packet.getChatComponents().write(0, WrappedChatComponent.fromText(Metods.msgC(rename)));
//				try 
//				{
//					pManager.sendServerPacket(_player, packet);
//				} 
//				catch (Exception e) 
//				{
//					Bukkit.getLogger().info("Couldnt rename window!");
//					//e.printStackTrace();
//				}
				_player.updateInventory();
			}
		}.runTaskLater(_plugin, 1);
		
		
		
	}
	
	private int AdjustSize(int size)
	{
		if(size <= 0)
		{
			size = 1;
		}
		
		int result = (size + 8) / 9 * 9;
	    if (result > 54) 
	    {
	        return 54; 
	    }
	    return result;
	}
	public Player GetPlayer()
	{
		return _player;
	}
	public void RegisterToEvents()
	{
		_hasRegisteredEvents = true;
		_plugin.getServer().getPluginManager().registerEvents(this, _plugin);
		
	}
	
	public boolean HasRegistered()
	{
		return _hasRegisteredEvents;
	}
	
	public Inventory GetInv()
	{
		return _inv;
	}
	
	public boolean isThisInv(InventoryEvent e) 
	{
		return isThisInv(e.getInventory());
	}
	
	public boolean isThisInv(Inventory inv)
	{
		return inv.equals(_inv);
	}
	
	public void onDisable()
	{
		_player.closeInventory();
	}
	
	public void openThis() 
	{	
		_player.openInventory(_inv);
		ImusAPI._instance.RegisterInvOpen(this);
		if(!HasRegistered())
			RegisterToEvents();
		
	}
	
	
//	@EventHandler
//	public void OnInvSort(SortingEvent e) //ChectCleaner.plugin support
//	{
//		if(isThisInv(e.getInventory()))
//		{
//			e.setCancelled(true);
//		}
//	}
	
	@EventHandler
	public void invClose(InventoryCloseEvent e)
	{
		if(isThisInv(e))
		{
			invClosed(e);
			HandlerList.unregisterAll(this);
			_hasRegisteredEvents = false;
			ImusAPI._instance.UnregisterInv(this);
		}
	}
	
	void SetDroppedTakenStacks(InventoryClickEvent e)
	{
		_takenStack = null;
		_takenSlot = -1;
		_droppedStack = null;
		_droppedSlot = -1;
		if(e.getCurrentItem() == null && e.getCursor() != null)
		{			
			_droppedStack = e.getCursor();
			_droppedSlot = e.getSlot();
			return;
		}
		
		if(e.getCurrentItem() != null && e.getCursor().getType() == Material.AIR)
		{		
			_takenStack = e.getCurrentItem();
			_takenSlot = e.getSlot();
			return;
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e)
	{

		if(isThisInv(e))
		{

			SetDroppedTakenStacks(e);
			switch (_denyItemMove) 
			{
			case BOTH:
				e.setCancelled(true);
				break;
			case LOWER_INV:
				if(e.getRawSlot() != e.getSlot())
					e.setCancelled(true);
				break;
			case UPPER_INV:
				if(e.getRawSlot() == e.getSlot())
					e.setCancelled(true);
				break;
			default:
				break;
			
			}
			
			if((e.getRawSlot() == e.getSlot()))
			{
				onClickInsideInv(e);
				return;
			}
			
			if(e.getRawSlot() != e.getSlot())
			{
				onClickPlayerInv(e);
				return;
			}
		}
		
		
	}
	
	protected void onClickPlayerInv(InventoryClickEvent e)
	{
		
	}
	
	void SetSlotPD(int slot, ItemStack stack)
	{
		_metods.setPersistenData(stack, "slottt", PersistentDataType.INTEGER, slot);
	}
	
	protected void SetITEM(int slot, ItemStack stack)
	{
		SetSlotPD(slot, stack);
		_inv.setItem(slot, stack);
	}
	
	protected void SetITEM(int slot, Material material)
	{
		SetITEM(slot,new ItemStack(material));
	}
	
	protected void SetITEM(ItemStack stack)
	{
		_inv.setItem(GetSLOT(stack), stack);
	}
	
	protected Integer GetSLOT(ItemStack stack)
	{
		return _metods.getPersistenData(stack, "slottt", PersistentDataType.INTEGER);
	}
	
	@Override
	public ItemStack SetButton(ItemStack stack, IButton b)
	{

		return _metods.setPersistenData(stack, pd_buttonType, PersistentDataType.STRING, b.toString());
		
	}
	
	@Override
	public String getButtonName(ItemStack stack)
	{
		String button = _metods.getPersistenData(stack, pd_buttonType, PersistentDataType.STRING);
		if(button != null)
			return button;
		
		return null;
	}
	
	@Override
	public ItemStack setupButton(IButton b, Material material, String displayName, Integer itemSlot)
	{
		ItemStack sbutton = new ItemStack(material);
		Metods.setDisplayName(sbutton, Metods.msgC(displayName));
		SetButton(sbutton, b);
		if(itemSlot != null)
		{
			SetSlotPD(itemSlot, sbutton);
			_inv.setItem(itemSlot, sbutton);
			return _inv.getItem(itemSlot);
		}
		return sbutton;
	}
	
	
	
	
	
}
