package imu.imusEnchants.Enchants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Utilities.ItemUtils;
import imu.imusEnchants.Enums.MATERIAL_SLOT_RANGE;
import imu.imusEnchants.Managers.ManagerEnchants;
import imu.imusEnchants.main.CONSTANTS;

public class EnchantedItem
{
	private ManagerEnchants _managerEnchants = ManagerEnchants.Instance;
	private INode[][] _nodes;
	private int _slots = 0;
	private boolean _isReveaveled = false;
	private ItemStack _stack;

	private Random random = new Random();

	private final String PD_REVEALED = "ie_pd_revealed";
	private final static String PD_UPRADED = "ie_pd_upgraded";
	private final static String PD_SLOTS = "ie_pd_slots";
	private static final String PD_PRECRAFTER = "ie_precrafted";
	
	private int _totalUnlocked = 0;

	private Player _player;

	public EnchantedItem(ItemStack stack)
	{
		Read(stack);
	}

	public EnchantedItem(ItemStack stack, Player player)
	{
		_player = player;
		Read(stack);
	}

	public Player GetPlayer()
	{
		return _player;
	}

	private void Read(ItemStack stack)
	{
		_stack = stack;
		_isReveaveled = IsRevealed(stack);

		int slots = GetSlots(_stack);
		if (slots > 0)
			_slots = slots;
		else
			SetSlots(ManagerEnchants.GetMaterialSlotsRange(_stack).GetRandomSlots());

	}

	public void Reveal(boolean force)
	{
		if (force || !LoadUnlockedNodes(_stack))
		{
			System.out.println(" STACK IS NOT LOADED, GENERATING NODES, force: " + force);
			GenerateNodes();
			SaveUnlockedNodes(_stack);

		}

		if (IsUpgraded(_stack))
		{
			SetUpgraded(_stack, false);
			// SetTooltip();
			int currentSlots = Get_slots();
			int maxSlots = ManagerEnchants.GetMaterialSlotsRange(_stack).GetMaxSlots();

			int newSlotAmount = MATERIAL_SLOT_RANGE.GetRandomSlots(currentSlots, maxSlots);
			System.out.println("===> ADDING NEW SLOTS1: " + (currentSlots));
			System.out.println("===> ADDING NEW SLOTS2: " + (maxSlots));
			System.out.println("===> ADDING NEW SLOTS3: " + (newSlotAmount));
			System.out.println("===> ADDING NEW SLOTS4: " + (newSlotAmount - currentSlots));
			AddSlots(newSlotAmount - currentSlots);

		}
		SetRevealed(true);
		SetTooltip();

	}

	public ItemStack GetItemStack()
	{
		return _stack;
	}

	public void GenerateNodes()
	{

		CountUnlockedNodes();

		if (_totalUnlocked >= _slots)
		{
			return;
		}

		while (_totalUnlocked < _slots)
		{
			int row = random.nextInt(CONSTANTS.ENCHANT_ROWS);
			int column = random.nextInt(CONSTANTS.ENCHANT_COLUMNS);

			if (_nodes[row][column].IsLocked()
					&& !ManagerEnchants.REDSTRICTED_SLOTS.contains(row * CONSTANTS.ENCHANT_COLUMNS + column))
			{
				_nodes[row][column].SetLock(false);
				_totalUnlocked++;

				//TryUnlockNeighbors(_nodes[row][column], 0.20, 1);
				TryUnlockNeighbors(_nodes[row][column], CONSTANTS.UNLOCKING_NODES_RECURSIVE_START_CHANCE, 1);
			}
		}

	}

	private void GenenrateEmptyNodeArray()
	{
		_nodes = new Node[CONSTANTS.ENCHANT_ROWS][CONSTANTS.ENCHANT_COLUMNS];

		for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
		{
			for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
			{
				Node node = new Node(i, j);
				node.SetLock(true);
				_nodes[i][j] = node;
			}
		}

	}

	private void CountUnlockedNodes()
	{
		if (_nodes == null)
		{
			GenenrateEmptyNodeArray();
		}

		_totalUnlocked = 0;
		for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
		{
			for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
			{
				if (!_nodes[i][j].IsLocked())
				{
					_totalUnlocked++;
				}
			}
		}
	}

	public INode[] GetNeighbors(INode node)
	{
		return GetNeighbors(node.GetX(), node.GetY());
	}

	public INode[] GetNeighbors(int x, int y)
	{
		INode[] neighbors = new INode[4];
		neighbors[0] = (y > 0) ? _nodes[x][y - 1] : null;

		neighbors[1] = (y < CONSTANTS.ENCHANT_COLUMNS - 1) ? _nodes[x][y + 1] : null;

		neighbors[2] = (x > 0) ? _nodes[x - 1][y] : null;

		neighbors[3] = (x < CONSTANTS.ENCHANT_ROWS - 1) ? _nodes[x + 1][y] : null;

		return neighbors;
	}

	public void PrintNodes()
	{
		for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
		{
			for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
			{
				INode node = _nodes[i][j];
				if (node != null)
				{
					if (node.IsLocked())
						continue;

					System.out.println("Node at [" + i + "," + j + "]: " + (node.IsLocked() ? "Locked" : "Unlocked")
							+ ", Details: " + node.toString());

				} else
				{
					System.out.println("Node at [" + i + "," + j + "]: null");
				}
			}
		}
	}

	private void TryUnlockNeighbors(INode node, double chance, int depth)
	{
		if (depth > CONSTANTS.UNLOCKING_NODES_RECURSIVE_DEPTH || _totalUnlocked >= _slots)
		{
			return;
		}

		for (INode neighbor : GetNeighbors(node))
		{
			if (_totalUnlocked >= _slots)
				continue;

			if (neighbor == null)
				continue;

			if (!neighbor.IsLocked())
				continue;

			if (ManagerEnchants.REDSTRICTED_SLOTS
					.contains(neighbor.GetX() * CONSTANTS.ENCHANT_COLUMNS + neighbor.GetY()))
				continue;

			if (random.nextDouble() > chance)
				continue;

			neighbor.SetLock(false);
			_totalUnlocked++;

			if (_totalUnlocked < _slots)
			{
				//TryUnlockNeighbors(neighbor, chance * 0.8, depth + 1); 
				TryUnlockNeighbors(neighbor, chance * CONSTANTS.UNLOCKING_NODES_RECURSIVE_REDUCE, depth + 1); // Reduce chance by 20% in each recursive call
			}

		}
	}

	public INode GetNodeBySlot(int slot)
	{
		int row = slot / CONSTANTS.ENCHANT_COLUMNS;
		int column = slot % CONSTANTS.ENCHANT_COLUMNS;

		if (row >= 0 && row < CONSTANTS.ENCHANT_ROWS && column >= 0 && column < CONSTANTS.ENCHANT_COLUMNS)
		{
			return _nodes[row][column];
		}

		return null;
	}
	
	public INode GetNode(int x, int y)
	{
		return _nodes[x][y];
	}

	public void SetNode(INode node)
	{
		_nodes[node.GetX()][node.GetY()] = node;
	}

	public void SetNode(INode node, int position)
	{
		int row = position / CONSTANTS.ENCHANT_COLUMNS;
		int column = position % CONSTANTS.ENCHANT_COLUMNS;

		if (row >= 0 && row < CONSTANTS.ENCHANT_ROWS && column >= 0 && column < CONSTANTS.ENCHANT_COLUMNS)
		{

			node.SetPosition(row, column);
			_nodes[row][column] = node;
		}
	}

	public void RemoveNode(INode node)
	{
		if (node == null)
			return;

		RemoveNode(node.GetX(), node.GetY());
	}

	public void RemoveNode(int flatIndex)
	{
		int row = flatIndex / CONSTANTS.ENCHANT_COLUMNS;
		int column = flatIndex % CONSTANTS.ENCHANT_COLUMNS;

		RemoveNode(row, column);
	}

	public void RemoveNode(int x, int y)
	{
		if (x >= 0 && x < CONSTANTS.ENCHANT_ROWS && y >= 0 && y < CONSTANTS.ENCHANT_COLUMNS)
		{
			_nodes[x][y] = new Node(x, y); // Replace with a new, locked node
		}
	}

	public INode[] GetUnlockedNodes()
	{
		INode[] unlockedNodes = new INode[_slots];
		int index = 0;

		for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
		{
			for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
			{
				if (_nodes[i][j].IsLocked())
					continue;

				unlockedNodes[index++] = _nodes[i][j];

			}
		}

		return unlockedNodes;
	}

	public void SwapNode(INode node1, INode node2)
	{

		if (node1 == null || node2 == null)
		{
			System.out.println("Cannot swap null nodes");
			return;
		}

		int x1 = node1.GetX();
		int y1 = node1.GetY();
		int x2 = node2.GetX();
		int y2 = node2.GetY();

		// Swapping nodes in the array
		INode temp = _nodes[x1][y1];
		_nodes[x1][y1] = _nodes[x2][y2];
		_nodes[x2][y2] = temp;

		node1.SetPosition(x2, y2);
		node2.SetPosition(x1, y1);
	}

	@SuppressWarnings("unused")
	public void ApplyEnchantsToItem()
	{
		Map<Enchantment, Integer> allEnchants = new HashMap<>();

		for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
		{
			for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
			{
				INode node = _nodes[i][j];
				node.Activate(this);
			}
		}

		for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
		{
			for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
			{
				INode node = _nodes[i][j];
				// node.Activate(this);

				if (node instanceof NodeEnchant)
				{
					NodeEnchant nodeEnchant = (NodeEnchant) node;
					int totalBoost = 0;

					for (INode neighbor : GetNeighbors(node))
					{
						if (neighbor instanceof NodeBooster)
	                    {
	                        NodeBooster booster = (NodeBooster) neighbor;
	                        if (booster.IsBoostingThis(node)) 
	                        {
	                            totalBoost += booster.GetPower();
	                        }
	                    }
					}

					final int boost = totalBoost;

					nodeEnchant.GetEnchantments().forEach((enchant, level) ->
					{
						int boostedLevel = CONSTANTS.ENABLE_MULTIPLE_SAME_ENCHANTS
								? allEnchants.getOrDefault(enchant, 0) + level + totalBoost
								: Math.max(allEnchants.getOrDefault(enchant, 0), level + boost);

						int maximumLevel = ManagerEnchants.GetEnchantMaxLevelCap(enchant);
						if (maximumLevel > 0 && boostedLevel > maximumLevel)
						{
							boostedLevel = maximumLevel;
						}
						allEnchants.put(enchant, boostedLevel);
					});
				}
				
				if(node.getClass() == Node.class) continue;
				
				node.SetFrozen(true);
			}
		}

		if (!_stack.getType().equals(Material.AIR))
		{
			_stack.getEnchantments().keySet().forEach(_stack::removeEnchantment);
			allEnchants.forEach((enchant, level) -> _stack.addUnsafeEnchantment(enchant, level));
		}

	}
	
//	private boolean IsDirectionApplicable(NodeBooster booster, NodeEnchant nodeEnchant) {
//	    int deltaX = nodeEnchant.GetX() - booster.GetX();
//	    int deltaY = nodeEnchant.GetY() - booster.GetY();
//
//	    // Check if the booster is adjacent to the nodeEnchant
//	    if (Math.abs(deltaX) > 1 || Math.abs(deltaY) > 1) {
//	        return false; // Not adjacent
//	    }
//
//	    // Check if the booster's direction applies to the nodeEnchant based on their relative position
//	    for (DIRECTION direction : booster.GetDirections()) {
//	        switch (direction) {
//	            case UP:
//	                if (deltaY == -1 && deltaX == 0) return true;
//	                break;
//	            case DOWN:
//	                if (deltaY == 1 && deltaX == 0) return true;
//	                break;
//	            case LEFT:
//	                if (deltaX == -1 && deltaY == 0) return true;
//	                break;
//	            case RIGHT:
//	                if (deltaX == 1 && deltaY == 0) return true;
//	                break;
//	            // Include cases for other directions if applicable
//	        }
//	    }
//
//	    return false;
//	}

	public int ContainsEnchant(ItemStack itemStack)
	{
		if (itemStack == null)
		{
			return 0;
		}

		Map<Enchantment, Integer> itemEnchants = ItemUtils.GetEnchantsWithLevels(itemStack);

		int counter = 0;
		for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
		{
			for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
			{
				INode node = _nodes[i][j];
				if (node instanceof NodeEnchant)
				{

					NodeEnchant nodeEnchant = (NodeEnchant) node;
					for (Enchantment enchant : nodeEnchant.GetEnchantments().keySet())
					{
						if (itemEnchants.containsKey(enchant))
						{
							counter++;
						}
					}
				}
			}
		}

		return counter;
	}

	public ItemStack SetTooltip()
	{
		final String str_revealed = IsRevealed() ? "" : "&a&k#";
		final String str_upgrade = IsUpgraded(_stack) ? "&e+ &5&k##" : "";
		// ■

		ItemUtils.AddOrReplaceLore(_stack, "&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		ItemUtils.AddOrReplaceLore(_stack,
				"&6░ ► " + str_revealed + "&r&a" + _slots + "&r" + str_revealed + str_upgrade);

		return _stack;
	}
	
	public static ItemStack SetPrecraftTooltip(ItemStack stack)
	{
		ItemUtils.AddOrReplaceLore(stack, "&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		ItemUtils.AddOrReplaceLore(stack,
				"&6░ ► &r&a &k##");
		SetPrecraftedEnchantablePD(stack);
		return stack;
	}

	public void SetSlots(int slots)
	{
		if (slots < 0)
		{
			slots = 0;
		}
		if (slots > CONSTANTS.MAX_SLOTS)
		{
			slots = CONSTANTS.MAX_SLOTS;
		}
		this._slots = slots;
		ItemUtils.SetPersistenData(_stack, PD_SLOTS, PersistentDataType.INTEGER, slots);
	}

	public void AddSlots(int additionalSlots)
	{
		if (additionalSlots <= 0)
		{
			return;
		}

		int oldSlotCount = _slots;
		int newSlotCount = oldSlotCount + additionalSlots;
		if (newSlotCount > CONSTANTS.MAX_SLOTS)
		{
			newSlotCount = CONSTANTS.MAX_SLOTS;
		}

		_slots = newSlotCount;
		SetSlots(_slots);
		GenerateNodes();
		SaveUnlockedNodes(_stack);
	}

	public static Integer GetSlots(ItemStack stack)
	{
		Integer slots = ItemUtils.GetPersistenData(stack, PD_SLOTS, PersistentDataType.INTEGER);
		return (slots != null) ? slots : 0;
	}

	public static boolean HasSlots(ItemStack stack)
	{
		return GetSlots(stack) > 0;
	}

	public static boolean IsUpgraded(ItemStack stack)
	{
		Integer data = ItemUtils.GetPersistenData(stack, PD_UPRADED, PersistentDataType.INTEGER);
		return data != null && data == 1;
	}

	public static void SetUpgraded(ItemStack stack, boolean upgraded)
	{
		ItemUtils.SetPersistenData(stack, PD_UPRADED, PersistentDataType.INTEGER, upgraded ? 1 : 0);
	}

	public boolean IsRevealed()
	{
		return _isReveaveled;
	}

	private void SetRevealed(boolean revealed)
	{
		_isReveaveled = revealed;
		ItemUtils.SetPersistenData(_stack, PD_REVEALED, PersistentDataType.INTEGER, revealed ? 1 : 0);
	}

	private boolean IsRevealed(ItemStack stack)
	{
		String data = ItemUtils.GetPersistenData(stack, "unlocked_nodes", PersistentDataType.STRING);
		return data != null;
	}
	public static boolean IsPrecraftedEnchatable(ItemStack stack)
	{
		return ItemUtils.HasTag(stack, PD_PRECRAFTER);
	}
	
	public static void RemovePrecraftedEnchatable(ItemStack stack)
	{
		ItemUtils.RemoveTag(stack, PD_PRECRAFTER);
	}
	
	public static void SetPrecraftedEnchantablePD(ItemStack stack)
	{
		ItemUtils.SetTag(stack, PD_PRECRAFTER);
	}
	public INode[][] Get_nodes()
	{
		if (_nodes == null)
			_nodes = new Node[CONSTANTS.ENCHANT_ROWS][CONSTANTS.ENCHANT_COLUMNS];
		return _nodes;
	}

	public void Set_nodes(Node[][] nodes)
	{
		this._nodes = nodes;
	}

	public int Get_slots()
	{
		return _slots;
	}

	public List<INode> DetachNodes(Class<?> nodeType, List<Integer> exceptionNodePositions)
	{
		if (exceptionNodePositions == null)
			exceptionNodePositions = new ArrayList<>();

		List<INode> detachedNodes = new ArrayList<>();

		for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
		{
			for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
			{
				int flatIndex = i * CONSTANTS.ENCHANT_COLUMNS + j;
				INode node = _nodes[i][j];

				if (nodeType.isInstance(node) && !exceptionNodePositions.contains(flatIndex))
				{
					detachedNodes.add(node);
					INode newNode = new Node(i, j);
					newNode.SetLock(node.IsLocked());
					_nodes[i][j] = newNode; // Replace with a new locked node
				}
			}
		}

		return detachedNodes;
	}
	
	public List<INode> DetachNodes(Predicate<INode> detachCondition, List<INode> exceptionNodes) 
	{
        List<INode> detachedNodes = new ArrayList<>();

        for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++) 
        {
            for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++) 
            {
                INode node = _nodes[i][j];

                if (detachCondition.test(node) && (exceptionNodes == null || !exceptionNodes.contains(node))) 
                {
                    detachedNodes.add(node);
                    INode newNode = new Node(i, j);
                    newNode.SetLock(node.IsLocked());
                    _nodes[i][j] = newNode;
                }
            }
        }

        return detachedNodes;
    }

	public void ReattachNodes(List<INode> nodesToReattach)
	{
		for (INode node : nodesToReattach)
		{
			int x = node.GetX();
			int y = node.GetY();

			if (x >= 0 && x < CONSTANTS.ENCHANT_ROWS && y >= 0 && y < CONSTANTS.ENCHANT_COLUMNS)
			{
				_nodes[x][y] = node;
			}
		}
	}

	public void SaveUnlockedNodes()
	{
		SaveUnlockedNodes(_stack);
	}

	public void SaveUnlockedNodes(ItemStack stack)
	{
		//PrintNodes();
		StringBuilder serializedData = new StringBuilder();

		for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
		{
			for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
			{
				INode node = _nodes[i][j];
				if (!node.IsLocked())
				{
					if (serializedData.length() > 0)
						serializedData.append(";");

					serializedData.append(node.Serialize());
				}
			}
		}

		ItemUtils.SetPersistenData(stack, "ie_unlocked_nodes", PersistentDataType.STRING, serializedData.toString());

		//System.out.println("item SAVED: " + stack);
	}

	public boolean LoadUnlockedNodes()
	{
		return LoadUnlockedNodes(_stack);
	}

	public boolean LoadUnlockedNodes(ItemStack stack)
	{
		String serializedData = ItemUtils.GetPersistenData(stack, "ie_unlocked_nodes", PersistentDataType.STRING);

		//System.out.println("LOADING DATA: " + stack + " =========> DATA: " + serializedData);

		if (serializedData == null || serializedData.isEmpty())
			return false;

		GenenrateEmptyNodeArray();

		String[] nodeDataArray = serializedData.split(";");
		for (String nodeData : nodeDataArray)
		{
			String[] parts = nodeData.split(":");
			String type = parts[0];
			INode node = null;

			switch (type)
			{
			case "NodeBooster":
				node = new NodeBooster();
				break;
			case "NodeEnchant":
				node = new NodeEnchant();
				break;
			case "NodeSwapper":
				node = new NodeSwapper();
				break;
			default:
				node = new Node();
				break;
			}

			if (node != null)
			{
				node.Deserialize(nodeData);
				node.SetLock(false);
				_nodes[node.GetX()][node.GetY()] = node;
			}
		}

		return true;
	}

//	public boolean LoadUnlockedNodes(ItemStack stack) 
//	{
//	    String serializedData = ItemUtils.GetPersistenData(stack, "unlocked_nodes", PersistentDataType.STRING);
//	    
//	    if (serializedData == null || serializedData.isEmpty()) return false;
//
//	    String[] nodeDataArray = serializedData.split(";");
//
//	    try 
//        {
//	    	for (String nodeData : nodeDataArray) 
//	 	    {
//	 	        String[] parts = nodeData.split(":");
//	 	        String className = parts[0];
//
//	 	        Class<?> clazz = Class.forName("imu.imusEnchants.Enchants." + className);
//	            if (INode.class.isAssignableFrom(clazz)) 
//	            {
//	                INode node = (INode) clazz.getDeclaredConstructor().newInstance();
//	                node.Deserialize(nodeData);
//	                _nodes[node.GetX()][node.GetY()] = node;
//	            }
//	 	    }
//        }
//	    catch (Exception e) 
//        {
//            e.printStackTrace(); // Handle exceptions appropriately
//            return false;
//        }
//	   
//	    
//	    return true;
//	}

	// WORKING
//	public void SaveUnlockedNodes(ItemStack stack) 
//	{
//	    Node[] unlockedNodes = GetUnlockedNodes();
//	    StringBuilder serializedData = new StringBuilder();
//
//	    for (Node node : unlockedNodes) 
//	    {
//	        if (serializedData.length() > 0)
//	            serializedData.append(","); // delimiter
//	        serializedData.append(node.GetFlatIndex());
//	    }
//
//	    ItemUtils.SetPersistenData(stack, "unlocked_nodes", PersistentDataType.STRING, serializedData.toString());
//	}
//	
//	public boolean LoadUnlockedNodes(ItemStack stack) 
//	{
//		
//	    String serializedData = ItemUtils.GetPersistenData(stack, "unlocked_nodes", PersistentDataType.STRING);
//	    if (serializedData == null || serializedData.isEmpty()) return false;
//	    
//	    GenenrateEmptyNodeArray();
//	    
//	    String[] nodeIndices = serializedData.split(",");
//	    for (String indexStr : nodeIndices) {
//	        int index = Integer.parseInt(indexStr);
//	        int row = index / CONSTANTS.ENCHANT_COLUMNS;
//	        int column = index % CONSTANTS.ENCHANT_COLUMNS;
//
//	        if (row >= 0 && row < CONSTANTS.ENCHANT_ROWS && column >= 0 && column < CONSTANTS.ENCHANT_COLUMNS) {
//	            _nodes[row][column].SetLock(false);
//	        }
//	    }
//	    
//	    return true;
//	}

}
