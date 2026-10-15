package imu.iAPI.Packets;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Shows a player items that aren't there: a slot of the window they have open looks like another item on their
 * screen only. The real item on the server never changes, and {@link #restore(Player)} shows the real ones again.
 * <p>
 * Paper has no API for this, so it sends the game's own set-slot packet through Paper's internals. If those change
 * in an update, {@link #available()} turns false and nothing is shown, with one warning in the console.
 * <p>
 * Never for players in creative: their client decides what is in their inventory, and a fake item could become a
 * real one. {@link #allowed(Player)} says whether a player can be shown fakes.
 */
public final class ClientItems
{
    private static Method _getHandle;
    private static Field _containerMenu;
    private static Field _containerId;
    private static Method _getStateId;
    private static Field _connection;
    private static Method _send;
    private static Method _asNmsCopy;
    private static Constructor<?> _setSlotPacket;
    private static Boolean _available;

    private ClientItems() {}

    /**
     * Finds the internals once; false when this Paper version doesn't have them.
     */
    public static synchronized boolean available()
    {
        if (_available != null)
            return _available;
        try
        {
            ClassLoader loader = org.bukkit.Bukkit.getServer().getClass().getClassLoader();
            Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer", true, loader);
            Class<?> serverPlayer = Class.forName("net.minecraft.server.level.ServerPlayer", true, loader);
            Class<?> nmsPlayer = Class.forName("net.minecraft.world.entity.player.Player", true, loader);
            Class<?> menu = Class.forName("net.minecraft.world.inventory.AbstractContainerMenu", true, loader);
            Class<?> listener = Class.forName("net.minecraft.server.network.ServerCommonPacketListenerImpl", true, loader);
            Class<?> packet = Class.forName("net.minecraft.network.protocol.Packet", true, loader);
            Class<?> nmsItem = Class.forName("net.minecraft.world.item.ItemStack", true, loader);
            Class<?> craftItem = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack", true, loader);
            Class<?> setSlot = Class.forName("net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket", true, loader);

            _getHandle = craftPlayer.getMethod("getHandle");
            _containerMenu = nmsPlayer.getField("containerMenu");
            _containerId = menu.getField("containerId");
            _getStateId = menu.getMethod("getStateId");
            _connection = serverPlayer.getField("connection");
            _send = listener.getMethod("send", packet);
            _asNmsCopy = craftItem.getMethod("asNMSCopy", ItemStack.class);
            _setSlotPacket = setSlot.getConstructor(int.class, int.class, int.class, nmsItem);
            _available = true;
        } catch (ReflectiveOperationException | RuntimeException e)
        {
            Logger.getLogger("imusAPI").warning("Client-only items are off: this Paper version's internals differ (" + e + ")");
            _available = false;
        }
        return _available;
    }

    /**
     * Whether fakes can be shown to the player: the internals are there and the player isn't in creative.
     */
    public static boolean allowed(Player player)
    {
        return player.getGameMode() != GameMode.CREATIVE && available();
    }

    /**
     * Shows an item in a slot of the window the player has open, numbered as the window's raw slots.
     *
     * @return false when it couldn't be shown
     */
    public static boolean show(Player player, int rawSlot, ItemStack item)
    {
        if (!allowed(player))
            return false;
        try
        {
            Object handle = _getHandle.invoke(player);
            Object menu = _containerMenu.get(handle);
            int windowId = _containerId.getInt(menu);
            int stateId = (int) _getStateId.invoke(menu);
            Object packet = _setSlotPacket.newInstance(windowId, stateId, rawSlot, _asNmsCopy.invoke(null, item));
            _send.invoke(_connection.get(handle), packet);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e)
        {
            return false;
        }
    }

    /**
     * Shows an item in a slot of the player's own inventory (0-8 the hotbar, 9-35 the rest) as it is in the window
     * they have open.
     */
    public static boolean showOwn(Player player, int inventorySlot, ItemStack item)
    {
        int raw = rawSlotOf(player.getOpenInventory(), inventorySlot);
        return raw >= 0 && show(player, raw, item);
    }

    /**
     * Shows several of the player's own slots.
     */
    public static void showOwn(Player player, Map<Integer, ItemStack> items)
    {
        items.forEach((slot, item) -> showOwn(player, slot, item));
    }

    /**
     * Covers all 36 slots of the player's own inventory with the item.
     */
    public static void coverOwn(Player player, ItemStack cover)
    {
        if (!allowed(player))
            return;
        for (int slot = 0; slot < 36; slot++)
            showOwn(player, slot, cover);
    }

    /**
     * Covers the empty slots of the player's own inventory with the item; slots that hold something show it as it
     * is. A slot that fills shows its real item on its own, so call this again after the player's items change to
     * cover the slots that emptied.
     */
    public static void coverEmptyOwn(Player player, ItemStack cover)
    {
        if (!allowed(player))
            return;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int slot = 0; slot < Math.min(36, contents.length); slot++)
        {
            if (contents[slot] == null || contents[slot].getType().isAir())
                showOwn(player, slot, cover);
        }
    }

    /**
     * Sends the player their real items again.
     */
    public static void restore(Player player)
    {
        player.updateInventory();
    }

    /**
     * The window's raw slot of a slot of the player's own inventory, or -1.
     */
    static int rawSlotOf(InventoryView view, int inventorySlot)
    {
        int top = view.getTopInventory().getSize();
        for (int raw = top; raw < top + 36; raw++)
        {
            if (view.getInventory(raw) == view.getBottomInventory() && view.convertSlot(raw) == inventorySlot)
                return raw;
        }
        return -1;
    }
}
