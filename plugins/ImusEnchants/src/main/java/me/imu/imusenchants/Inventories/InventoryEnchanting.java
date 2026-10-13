package me.imu.imusenchants.Inventories;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.MenuSession;
import imu.iAPI.Menus.Paper.PaperIcons;
import imu.iAPI.Menus.Viewer;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.ItemUtils.DisplayNamePosition;
import me.imu.imusenchants.CONSTANTS;
import me.imu.imusenchants.Enchants.EnchantedItem;
import me.imu.imusenchants.Enchants.INode;
import me.imu.imusenchants.Enchants.Node;
import me.imu.imusenchants.Enchants.NodeSwapper;
import me.imu.imusenchants.Enums.TOUCH_TYPE;
import me.imu.imusenchants.ImusEnchants;
import me.imu.imusenchants.Managers.ManagerEnchants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The enchanting table: an item with slots goes in the bottom slot, its grid opens above, and books, boosters and
 * other pieces go in the open grid slots. Enchant saves the pieces into the item. Right-click removes a saved
 * piece or activates a swapper. The item and the pieces not yet saved come back when the table closes.
 */
public class InventoryEnchanting extends Menu
{
    public static final String PD_SWAPPER = "swapper";
    private static final String PD_LOCKED = "locked";

    static final int SLOT_ITEM = 54 - 5;
    static final int SLOT_BUY = 54 - 6;
    static final int SLOT_ENCHANT = 54 - 4;
    static final int SLOT_REROLL = 54 - 14;
    private static final int[] PURPLE = { 54 - 3, 54 - 7, 54 - 13, 54 - 15 };

    // Tables open right now. The item being enchanted lives only in the table while it is open.
    private static final Set<InventoryEnchanting> OPEN = new HashSet<>();

    private ItemStack _item;
    private EnchantedItem _enchantedItem;
    // What the grid shows: pieces, and open slots that take one
    private final Map<Integer, ItemStack> _grid = new HashMap<>();
    private final Set<Integer> _open = new HashSet<>();
    // Pieces put in since the last Enchant; they come back on close
    private final Set<Integer> _pending = new HashSet<>();
    private int _redMark = -1;
    private long _timeID;

    public InventoryEnchanting()
    {
        super("&0===== &5Enchanting Table &0=====", 6);
    }

    /**
     * Opens with the item already in the item slot. The item must be taken from the player before opening; it
     * comes back on close like any item put in the table.
     */
    public InventoryEnchanting(ItemStack preloadedItem)
    {
        this();
        _item = preloadedItem;
    }

    // On shutdown or reload the plugin's listeners are gone before the inventories close, so close the tables
    // here, while closing still hands the items back
    public static void CloseAll()
    {
        for (InventoryEnchanting table : new ArrayList<>(OPEN))
        {
            MenuSession session = table.session();
            if (session != null)
                session.close();
        }
        OPEN.clear();
    }

    /**
     * Drops the item without giving it back, when the table never opened and the player still has it.
     */
    public void Forget()
    {
        _item = null;
        _enchantedItem = null;
        _pending.clear();
    }

    private static Player PlayerOf(Viewer viewer)
    {
        return Bukkit.getPlayer(viewer.id());
    }

    @Override
    protected void onOpen(Viewer viewer)
    {
        boolean first = OPEN.add(this);
        if (first && _item != null && _enchantedItem == null)
            LoadItem(viewer, false);
    }

    @Override
    protected void onClose(Viewer viewer)
    {
        OPEN.remove(this);
        Player player = PlayerOf(viewer);
        if (player == null)
            return;
        for (ItemStack piece : TakePending())
            InvUtil.AddItemToInventoryOrDrop(player, piece);
        if (_item != null)
            InvUtil.AddItemToInventoryOrDrop(player, _item);
        _item = null;
        _enchantedItem = null;
    }

    // Drawing ========================================================================================================

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fill(Icon.filler(Material.BLACK_STAINED_GLASS_PANE));
        for (int slot : PURPLE)
            canvas.set(slot, Icon.filler(Material.PURPLE_STAINED_GLASS_PANE));

        Player player = PlayerOf(canvas.viewer());
        if (player != null && player.isOp())
            canvas.set(SLOT_REROLL, Icon.of(Material.NETHER_STAR, "&eREROLL Slots", "&6Visible OPs only"), this::Reroll);
        else
            canvas.set(SLOT_REROLL, Icon.filler(Material.PURPLE_STAINED_GLASS_PANE));
        canvas.set(SLOT_BUY, Icon.of(Material.BOOKSHELF, "&eBuy Enchants"), click -> click.open(new InventoryBuyEnchants(new PaperEnchantShopGame())));
        canvas.set(SLOT_ENCHANT, Icon.of(Material.ENCHANTING_TABLE, "&eEnchant"), this::Enchant);

        if (_item != null)
            canvas.set(SLOT_ITEM, PaperIcons.of(_item), this::ItemClick);
        else
            canvas.set(SLOT_ITEM, Button.empty(this::ItemDrop));

        for (int slot : _open)
            canvas.set(slot, Button.empty(click -> GridDrop(click, slot)));
        for (Map.Entry<Integer, ItemStack> piece : _grid.entrySet())
        {
            int slot = piece.getKey();
            canvas.set(slot, PaperIcons.of(piece.getValue()), click -> GridClick(click, slot));
        }
        if (_redMark >= 0)
            canvas.set(_redMark, Icon.filler(Material.RED_STAINED_GLASS_PANE));
    }

    private void ClearGrid()
    {
        _grid.clear();
        _open.clear();
    }

    // The item ========================================================================================================

    private void LoadItem(Viewer viewer, boolean forceReveal)
    {
        _enchantedItem = new EnchantedItem(_item, PlayerOf(viewer));
        _enchantedItem.Reveal(forceReveal);
        LoadNodes();
    }

    private void LoadNodes()
    {
        ClearGrid();
        for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
        {
            for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
            {
                INode node = _enchantedItem.Get_nodes()[i][j];
                int slot = i * CONSTANTS.ENCHANT_COLUMNS + j;

                // Black glass
                if (node.IsLocked())
                    continue;

                ItemStack shown = Shown(node.GetGUIitemLoad(_enchantedItem));
                if (shown == null)
                {
                    _open.add(slot);
                    continue;
                }
                if (node.IsFrozen())
                    Lock(shown);
                _grid.put(slot, shown);
            }
        }
    }

    /**
     * The grid item of a piece, or null when the item is no piece.
     */
    private ItemStack Shown(ItemStack stack)
    {
        INode node = ManagerEnchants.Instance.GetNode(stack, _enchantedItem);
        if (node == null)
            return null;
        ItemStack loaded = node.GetGUIitemLoad(_enchantedItem);
        return loaded.getType().isAir() ? stack : loaded;
    }

    private static void Lock(ItemStack stack)
    {
        ItemUtils.SetTag(stack, PD_LOCKED);
        ItemUtils.AddTextToDisplayName(stack, " &e(&cLOCKED&e)", DisplayNamePosition.BACK);
        ItemUtils.AddLore(stack, "&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        ItemUtils.AddLore(stack, "&cRemove by &eM2", true);
    }

    private void ItemDrop(Click click)
    {
        ItemStack cursor = (ItemStack) click.cursor();
        if (cursor == null)
            return;
        if (CONSTANTS.SLOT_CORE_MODE && !EnchantedItem.HasSlots(cursor))
        {
            click.message("&cUse a &dSlot Core &con the item first!");
            return;
        }
        if (!ManagerEnchants.IsValidToEnchant(cursor))
        {
            click.message("&cNot valid item to enchant!");
            return;
        }
        _item = TakeOne(click, cursor);
        LoadItem(click.viewer(), false);
    }

    private void ItemClick(Click click)
    {
        if (click.cursor() != null || !IsPlainClick(click))
            return;

        // Pieces not saved with Enchant go back with the item
        Player player = PlayerOf(click.viewer());
        for (ItemStack piece : TakePending())
            InvUtil.AddItemToInventoryOrDrop(player, piece);
        ItemStack item = _item;
        _item = null;
        _enchantedItem = null;
        ClearGrid();
        RefreshTimeID();
        Give(click, item);
    }

    private void Enchant(Click click)
    {
        if (_item == null)
            return;
        UpdateEnchantedItem(true);
        ClearGrid();
        _pending.clear();
    }

    private void Reroll(Click click)
    {
        Player player = PlayerOf(click.viewer());
        if (_enchantedItem == null || player == null || !player.isOp())
            return;
        _pending.clear();
        LoadItem(click.viewer(), true);
    }

    private void UpdateEnchantedItem(boolean applyEnchants)
    {
        if (_item == null)
            return;

        if (applyEnchants)
        {
            _enchantedItem.SaveUnlockedNodes();
            _enchantedItem.ApplyEnchantsToItem();
            RefreshTimeID();
        }
        _enchantedItem.SaveUnlockedNodes();
        _enchantedItem.SetTooltip();
        _item = _enchantedItem.GetItemStack();
    }

    // The grid =======================================================================================================

    private void GridDrop(Click click, int slot)
    {
        ItemStack cursor = (ItemStack) click.cursor();
        if (cursor == null || _enchantedItem == null)
            return;
        if (!ManagerEnchants.Instance.IsValidGUIitem(TOUCH_TYPE.DROP, _enchantedItem, cursor))
            return;
        INode node = ManagerEnchants.Instance.GetNode(cursor, _enchantedItem);
        if (node == null)
            return;

        ItemStack piece = TakeOne(click, cursor);
        _enchantedItem.SetNode(node, slot);
        ItemStack loaded = node.GetGUIitemLoad(_enchantedItem);
        _open.remove(slot);
        _grid.put(slot, loaded.getType().isAir() ? piece : loaded);
        _pending.add(slot);
    }

    private void GridClick(Click click, int slot)
    {
        ItemStack shown = _grid.get(slot);
        if (shown == null || _enchantedItem == null || click.cursor() != null || !IsPlainClick(click))
            return;
        boolean pickable = ManagerEnchants.Instance.IsValidGUIitem(TOUCH_TYPE.PICK_UP, _enchantedItem, shown);

        if (click.isRight() && !click.isShift() && pickable)
        {
            if (ItemUtils.HasTag(shown, PD_LOCKED))
            {
                // Removing saves the item with every piece in the grid, but the pieces placed since the last
                // Enchant would still be handed back on close
                if (HasPendingPieces())
                {
                    click.message("&cPress &eEnchant &cor take your pieces out first!");
                    return;
                }
                _enchantedItem.removeNode(slot);
                UpdateEnchantedItem(true);
                _grid.remove(slot);
                _open.add(slot);
                return;
            }
            if (ItemUtils.HasTag(shown, PD_SWAPPER))
            {
                ActivateSwapper(slot);
                return;
            }
        }

        if (!pickable || ItemUtils.HasTag(shown, PD_LOCKED))
            return;

        // A piece put in since the last Enchant comes back out
        ItemStack piece = _enchantedItem.GetNodeBySlot(slot).GetGUIitemUnLoad(_enchantedItem, shown);
        INode empty = new Node();
        empty.SetLock(false);
        _enchantedItem.SetNode(empty, slot);
        _grid.remove(slot);
        _open.add(slot);
        _pending.remove(slot);
        Give(click, piece);
    }

    // Books, boosters and other pieces placed since the last Enchant
    private boolean HasPendingPieces()
    {
        for (INode node : _enchantedItem.getUnlockedNodes())
        {
            if (!node.IsFrozen() && node.getClass() != Node.class)
                return true;
        }
        return false;
    }

    /**
     * Takes the pieces put in since the last Enchant out of the grid, as items.
     */
    private List<ItemStack> TakePending()
    {
        List<ItemStack> pieces = new ArrayList<>();
        if (_enchantedItem == null)
            return pieces;
        for (INode node : _enchantedItem.getUnlockedNodes())
        {
            int slot = node.GetFlatIndex();
            if (node.IsFrozen() || !_pending.contains(slot) || !_grid.containsKey(slot))
                continue;
            pieces.add(node.GetGUIitemUnLoad(_enchantedItem, _grid.get(slot)));
        }
        _pending.clear();
        return pieces;
    }

    private void ActivateSwapper(int slot)
    {
        final long id = _timeID;
        NodeSwapper node = (NodeSwapper) _enchantedItem.GetNodeBySlot(slot);
        node.Activate(_enchantedItem);

        INode swapped = node.GetSwappedNode();
        List<INode> detachedNodes = _enchantedItem.DetachNodes(
                nodee -> (!nodee.IsFrozen() && nodee.getClass() != Node.class), null);

        UpdateEnchantedItem(false);
        _enchantedItem.ReattachNodes(detachedNodes);
        LoadNodes();

        if (swapped != null && CONSTANTS.SWAPPER_ANIMATION)
        {
            _redMark = Node.GetFlatIndex(swapped.GetX(), swapped.GetY());
            Bukkit.getScheduler().scheduleSyncDelayedTask(ImusEnchants.Instance, () ->
            {
                if (id != _timeID)
                    return;
                _redMark = -1;
                refresh();
            }, CONSTANTS.DELAY_SWAPPER_ANIMATION);
        }
    }

    // Left or right, with or without shift; number keys, drops and the rest do nothing here
    private static boolean IsPlainClick(Click click)
    {
        return click.isLeft() && click.kind() != ClickKind.DOUBLE_CLICK || click.isRight();
    }

    // Items ==========================================================================================================

    /**
     * One item off the cursor.
     */
    private static ItemStack TakeOne(Click click, ItemStack cursor)
    {
        ItemStack one = cursor.clone();
        one.setAmount(1);
        ItemStack rest = cursor.clone();
        rest.setAmount(cursor.getAmount() - 1);
        click.setCursor(rest.getAmount() > 0 ? rest : null);
        return one;
    }

    /**
     * To the cursor, or with a shift click straight to the inventory.
     */
    private static void Give(Click click, ItemStack item)
    {
        if (click.isShift())
            InvUtil.AddItemToInventoryOrDrop(PlayerOf(click.viewer()), item);
        else
            click.setCursor(item);
    }

    private void RefreshTimeID()
    {
        _timeID = System.currentTimeMillis();
    }
}
