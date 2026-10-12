package imu.iAPI.Npc;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Npc.NpcManager.NpcType;
import imu.iAPI.Other.Methods;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.TextInputDialog;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Settings of one NPC: name, action, villager or player look, skin, position. Opened when an NPC is created
 * and by sneak-clicking one.
 */
public class NpcEditorInventory extends CustomInventory
{
    private final NpcManager _npcs;
    private UUID _npcId;

    public NpcEditorInventory(Plugin plugin, NpcManager npcs, LivingEntity npc)
    {
        super(plugin, "&9NPC", 3 * 9);
        _npcs = npcs;
        _npcId = npc.getUniqueId();
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_LOWER_INV;
    }

    @Override
    public void onAwake()
    {
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        refresh();
    }

    /**
     * The NPC, or null (and the menu closes) when it no longer exists.
     */
    private LivingEntity getNpc()
    {
        Entity entity = Bukkit.getEntity(_npcId);
        if (entity instanceof LivingEntity npc && npc.isValid())
            return npc;

        getPlayer().sendMessage(Methods.msgC("&cThe NPC is gone (removed or its chunk unloaded)."));
        close();
        return null;
    }

    private void close()
    {
        Bukkit.getScheduler().runTask(getPlugin(), () -> getPlayer().closeInventory());
    }

    private void refresh()
    {
        LivingEntity npc = getNpc();
        if (npc == null)
            return;

        clearButtons();
        for (int i = 0; i < getSize(); i++)
            addButton(getEmptyButton(i, Material.BLACK_STAINED_GLASS_PANE));

        NpcType type = _npcs.getType(npc);
        NpcAction action = _npcs.getAction(_npcs.getActionId(npc));
        String data = _npcs.getData(npc);

        addButton(new Button(10, icon(Material.NAME_TAG, "&6&lName",
                "&9Now: &r" + _npcs.getName(npc), "", "&bClick &7to change"),
                e -> askName()));

        addButton(new Button(11, icon(Material.COMMAND_BLOCK, "&6&lWhat it does",
                "&9Now: &a" + (action == null ? "&c" + _npcs.getActionId(npc) + " (plugin not loaded)" : action.getName()),
                action == null ? "" : "&7" + action.describe(data),
                "", "&bLeft click &7next, &bright click &7previous"),
                e -> cycleAction(npc, e.isRightClick())));

        if (action != null && action.getDataPrompt() != null)
        {
            boolean allowed = _npcs.canUseAction(getPlayer(), action);
            addButton(new Button(12, icon(Material.WRITABLE_BOOK, "&6&l" + action.getDataPrompt(),
                    "&7" + action.describe(data), "", allowed ? "&bClick &7to change" : "&cYou can't change this"),
                    e ->
                    {
                        if (allowed)
                            askData(action);
                    }));
        }

        addButton(new Button(13, icon(type == NpcType.PLAYER ? Material.PLAYER_HEAD : Material.VILLAGER_SPAWN_EGG, "&6&lLooks like",
                "&9Now: &a" + (type == NpcType.PLAYER ? "a player" : "a villager"), "", "&bClick &7to switch"),
                e ->
                {
                    _npcId = _npcs.changeType(npc, type == NpcType.PLAYER ? NpcType.VILLAGER : NpcType.PLAYER, getPlayer()).getUniqueId();
                    refresh();
                }));

        if (npc instanceof Villager villager)
        {
            addButton(new Button(14, icon(Material.EMERALD, "&6&lProfession",
                    "&9Now: &a" + keyName(NpcManager.professions(), villager.getProfession()), "", "&bLeft click &7next, &bright click &7previous"),
                    e ->
                    {
                        _npcs.setProfession(villager, cycle(NpcManager.professions(), villager.getProfession(), e));
                        refresh();
                    }));

            addButton(new Button(15, icon(Material.GRASS_BLOCK, "&6&lClothes",
                    "&9Now: &a" + keyName(NpcManager.villagerTypes(), villager.getVillagerType()), "", "&bLeft click &7next, &bright click &7previous"),
                    e ->
                    {
                        villager.setVillagerType(cycle(NpcManager.villagerTypes(), villager.getVillagerType(), e));
                        refresh();
                    }));
        }

        if (npc instanceof Mannequin mannequin)
        {
            String skin = _npcs.getSkinName(mannequin);
            addButton(new Button(14, icon(Material.PLAYER_HEAD, "&6&lSkin",
                    "&9Now: &a" + (skin == null ? "default" : skin), "", "&bLeft click &7to use a player's skin", "&bRight click &7for the default skin"),
                    e ->
                    {
                        if (e.isRightClick())
                        {
                            _npcs.resetSkin(mannequin);
                            refresh();
                            return;
                        }
                        askSkin();
                    }));

            addButton(new Button(15, icon(Material.OAK_SIGN, "&6&lText under the name",
                    "&9Now: &r" + _npcs.getSubtitle(mannequin), "", "&bClick &7to change"),
                    e -> askSubtitle()));
        }

        addButton(new Button(19, icon(Material.ENDER_PEARL, "&6&lMove here",
                "&7Moves the NPC to where you stand,", "&7facing the way you look"),
                e -> _npcs.moveTo(npc, getPlayer().getLocation())));

        addButton(new Button(20, icon(Material.COMPASS, "&6&lFace me", "&7Turns the NPC to look at you.", "&7It faces this way when nobody is near"),
                e -> _npcs.face(npc, getPlayer().getLocation())));

        boolean looks = _npcs.looksAtPlayers(npc);
        addButton(new Button(21, icon(looks ? Material.ENDER_EYE : Material.GRAY_DYE, "&6&lLooks at players",
                "&9Now: " + (looks ? "&aon" : "&coff"),
                _npcs.isLookingEnabled() ? "&7Turns to the nearest player close by" : "&cOff for every NPC in /ia config",
                "", "&bClick &7to switch"),
                e ->
                {
                    _npcs.setLooksAtPlayers(npc, !looks);
                    refresh();
                }));

        addButton(new Button(22, icon(Material.OAK_DOOR, "&6&lDone"), e -> close()));

        addButton(new Button(26, icon(Material.LAVA_BUCKET, "&c&lRemove NPC", "", "&bShift click &7to remove"),
                e ->
                {
                    if (!e.isShiftClick())
                        return;
                    npc.remove();
                    getPlayer().sendMessage(Methods.msgC("&9The NPC was removed."));
                    close();
                }));

        updateButtons(true);
    }

    // ==================== ACTION ====================

    private void cycleAction(LivingEntity npc, boolean backwards)
    {
        List<NpcAction> actions = new ArrayList<>();
        for (NpcAction action : _npcs.getActions())
        {
            if (_npcs.canUseAction(getPlayer(), action))
                actions.add(action);
        }
        if (actions.isEmpty())
            return;

        int index = actions.indexOf(_npcs.getAction(_npcs.getActionId(npc)));
        NpcAction next = actions.get(Math.floorMod(index + (backwards ? -1 : 1), actions.size()));
        _npcs.setAction(npc, next.getId(), "");

        if (next.getDataPrompt() != null)
            askData(next);
        else
            refresh();
    }

    private void askData(NpcAction action)
    {
        LivingEntity npc = getNpc();
        if (npc == null)
            return;

        String initial = action.toEditText(_npcs.getData(npc));
        NpcEdit edit = (target, text) ->
        {
            String data;
            try
            {
                data = action.parseData(text);
            } catch (IllegalArgumentException e)
            {
                return e.getMessage();
            }
            _npcs.setAction(target, action.getId(), data);
            return null;
        };

        if (action.getDataRows() > 0)
            TextInputDialog.showMultiline(getPlayer(), action.getDataPrompt(), action.getDataHelp(), initial, 2000,
                    action.getDataRows(), text -> applyEdit(edit, text), () -> open(getPlayer()));
        else
            showDialog(action.getDataPrompt(), action.getDataHelp(), initial, 256, edit);
    }

    // ==================== TEXT INPUT ====================

    private void askName()
    {
        LivingEntity npc = getNpc();
        if (npc == null)
            return;

        showDialog("NPC name", List.of("Colour codes with & work"), _npcs.getName(npc), 64, (target, text) ->
        {
            if (text.isBlank())
                return "The name can't be empty";
            _npcs.setName(target, text);
            return null;
        });
    }

    private void askSkin()
    {
        LivingEntity npc = getNpc();
        if (npc == null)
            return;

        String current = _npcs.getSkinName(npc);
        showDialog("Skin", List.of("Name of the Minecraft player whose skin the NPC wears"), current == null ? "" : current, 16, (target, text) ->
        {
            String name = text.trim();
            if (!NpcManager.isValidPlayerName(name))
                return "Not a Minecraft player name";
            if (!(target instanceof Mannequin mannequin))
                return null;

            getPlayer().sendMessage(Methods.msgC("&7Looking up the skin of &e" + name + "&7..."));
            _npcs.setSkin(mannequin, name, getPlayer());
            return null;
        });
    }

    private void askSubtitle()
    {
        if (!(getNpc() instanceof Mannequin mannequin))
            return;

        showDialog("Text under the name", List.of("Colour codes with & work. Empty = no text"), _npcs.getSubtitle(mannequin), 64, (target, text) ->
        {
            if (target instanceof Mannequin m)
                _npcs.setSubtitle(m, text);
            return null;
        });
    }

    private interface NpcEdit
    {
        /**
         * @return null when the text was accepted, otherwise the problem
         */
        String apply(LivingEntity npc, String text);
    }

    private String applyEdit(NpcEdit edit, String text)
    {
        Entity entity = Bukkit.getEntity(_npcId);
        if (!(entity instanceof LivingEntity npc) || !npc.isValid())
            return null;
        return edit.apply(npc, text);
    }

    /**
     * Asks for text in a dialog. The dialog replaces this menu, which reopens when the dialog closes.
     */
    private void showDialog(String title, List<String> lines, String initial, int maxLength, NpcEdit edit)
    {
        TextInputDialog.show(getPlayer(), title, lines, initial, maxLength, text -> applyEdit(edit, text), () -> open(getPlayer()));
    }

    // ==================== HELPERS ====================

    private static ItemStack icon(Material material, String name, String... lore)
    {
        ItemStack stack = new ItemStack(material);
        ItemUtils.SetDisplayName(stack, name);
        for (String line : lore)
            ItemUtils.AddLore(stack, line, true);
        ItemUtils.HideFlag(stack, ItemFlag.HIDE_ATTRIBUTES);
        return stack;
    }

    private static <T extends Keyed> String keyName(Registry<T> registry, T value)
    {
        return registry.getKeyOrThrow(value).getKey().replace('_', ' ');
    }

    /**
     * The next value of the registry, or the previous one on right click.
     */
    private static <T extends Keyed> T cycle(Registry<T> registry, T current, InventoryClickEvent e)
    {
        List<T> values = registry.stream().toList();
        int index = values.indexOf(current);
        int step = e.isRightClick() ? -1 : 1;
        return values.get(Math.floorMod(index + step, values.size()));
    }
}
