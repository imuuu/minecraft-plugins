package imu.iAPI.Npc;

import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Prompt;
import imu.iAPI.Npc.NpcManager.NpcType;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Settings of one NPC: name, action, villager or player look, skin, position. Opened when an NPC is created
 * and by sneak-clicking one.
 */
public class NpcEditorInventory extends Menu
{
    private final NpcManager _npcs;
    private UUID _npcId;

    public NpcEditorInventory(NpcManager npcs, LivingEntity npc)
    {
        super("&9NPC", 3);
        _npcs = npcs;
        _npcId = npc.getUniqueId();
    }

    /**
     * The NPC, or null when it no longer exists.
     */
    private LivingEntity getNpc()
    {
        Entity entity = Bukkit.getEntity(_npcId);
        return entity instanceof LivingEntity npc && npc.isValid() ? npc : null;
    }

    private static Player playerOf(Click click)
    {
        return Bukkit.getPlayer(click.viewer().id());
    }

    /**
     * Runs the edit on the NPC, or closes the menu with a message when the NPC is gone.
     */
    private void withNpc(Click click, Consumer<LivingEntity> edit)
    {
        LivingEntity npc = getNpc();
        if (npc == null)
        {
            click.message("&cThe NPC is gone (removed or its chunk unloaded).");
            click.close();
            return;
        }
        edit.accept(npc);
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fill(Icon.filler(Material.BLACK_STAINED_GLASS_PANE));
        LivingEntity npc = getNpc();
        if (npc == null)
        {
            canvas.set(13, Icon.of(Material.BARRIER, "&cThe NPC is gone", "&7Removed or its chunk unloaded"), Click::close);
            return;
        }

        NpcType type = _npcs.getType(npc);
        NpcAction action = _npcs.getAction(_npcs.getActionId(npc));
        String data = _npcs.getData(npc);
        Player viewer = Bukkit.getPlayer(canvas.viewer().id());

        canvas.set(10, Icon.of(Material.NAME_TAG, "&6&lName", "&9Now: &r" + _npcs.getName(npc), "", "&bClick &7to change"),
                click -> withNpc(click, n -> askName(click, n)));
        canvas.set(11, Icon.of(Material.COMMAND_BLOCK, "&6&lWhat it does",
                "&9Now: &a" + (action == null ? "&c" + _npcs.getActionId(npc) + " (plugin not loaded)" : action.getName()),
                action == null ? "" : "&7" + action.describe(data),
                "", "&bLeft click &7next, &bright click &7previous"),
                click -> withNpc(click, n -> cycleAction(click, n)));
        if (action != null && action.getDataPrompt() != null)
        {
            boolean allowed = viewer != null && _npcs.canUseAction(viewer, action);
            canvas.set(12, Icon.of(Material.WRITABLE_BOOK, "&6&l" + action.getDataPrompt(),
                    "&7" + action.describe(data), "", allowed ? "&bClick &7to change" : "&cYou can't change this"),
                    click ->
                    {
                        if (allowed)
                            withNpc(click, n -> askData(click, n, action));
                    });
        }
        canvas.set(13, Icon.of(type == NpcType.PLAYER ? Material.PLAYER_HEAD : Material.VILLAGER_SPAWN_EGG, "&6&lLooks like",
                "&9Now: &a" + (type == NpcType.PLAYER ? "a player" : "a villager"), "", "&bClick &7to switch"),
                click -> withNpc(click, n -> _npcId = _npcs.changeType(n, type == NpcType.PLAYER ? NpcType.VILLAGER : NpcType.PLAYER,
                        playerOf(click)).getUniqueId()));

        if (npc instanceof Villager villager)
        {
            canvas.set(14, Icon.of(Material.EMERALD, "&6&lProfession",
                    "&9Now: &a" + keyName(NpcManager.professions(), villager.getProfession()), "", "&bLeft click &7next, &bright click &7previous"),
                    click -> _npcs.setProfession(villager, cycle(NpcManager.professions(), villager.getProfession(), click)));
            canvas.set(15, Icon.of(Material.GRASS_BLOCK, "&6&lClothes",
                    "&9Now: &a" + keyName(NpcManager.villagerTypes(), villager.getVillagerType()), "", "&bLeft click &7next, &bright click &7previous"),
                    click -> villager.setVillagerType(cycle(NpcManager.villagerTypes(), villager.getVillagerType(), click)));
        }
        if (npc instanceof Mannequin mannequin)
        {
            String skin = _npcs.getSkinName(mannequin);
            canvas.set(14, Icon.of(Material.PLAYER_HEAD, "&6&lSkin",
                    "&9Now: &a" + (skin == null ? "default" : skin), "", "&bLeft click &7to use a player's skin", "&bRight click &7for the default skin"),
                    click ->
                    {
                        if (click.isRight())
                            _npcs.resetSkin(mannequin);
                        else
                            askSkin(click, mannequin);
                    });
            canvas.set(15, Icon.of(Material.OAK_SIGN, "&6&lText under the name",
                    "&9Now: &r" + _npcs.getSubtitle(mannequin), "", "&bClick &7to change"),
                    click -> askSubtitle(click, mannequin));
        }

        canvas.set(19, Icon.of(Material.ENDER_PEARL, "&6&lMove here", "&7Moves the NPC to where you stand,", "&7facing the way you look"),
                click -> withNpc(click, n -> _npcs.moveTo(n, playerOf(click).getLocation())));
        canvas.set(20, Icon.of(Material.COMPASS, "&6&lFace me", "&7Turns the NPC to look at you.", "&7It faces this way when nobody is near"),
                click -> withNpc(click, n -> _npcs.face(n, playerOf(click).getLocation())));
        boolean looks = _npcs.looksAtPlayers(npc);
        canvas.set(21, Icon.of(looks ? Material.ENDER_EYE : Material.GRAY_DYE, "&6&lLooks at players",
                "&9Now: " + (looks ? "&aon" : "&coff"),
                _npcs.isLookingEnabled() ? "&7Turns to the nearest player close by" : "&cOff for every NPC in /ia config",
                "", "&bClick &7to switch"),
                click -> withNpc(click, n -> _npcs.setLooksAtPlayers(n, !looks)));
        canvas.set(22, Icon.of(Material.OAK_DOOR, "&6&lDone"), Click::close);
        canvas.set(26, Icon.of(Material.LAVA_BUCKET, "&c&lRemove NPC", "", "&bShift click &7to remove"), click ->
        {
            if (!click.isShift())
                return;
            withNpc(click, n ->
            {
                n.remove();
                click.message("&9The NPC was removed.");
                click.close();
            });
        });
    }

    // ==================== ACTION ====================

    private void cycleAction(Click click, LivingEntity npc)
    {
        Player player = playerOf(click);
        List<NpcAction> actions = new ArrayList<>();
        for (NpcAction action : _npcs.getActions())
        {
            if (_npcs.canUseAction(player, action))
                actions.add(action);
        }
        if (actions.isEmpty())
            return;

        int index = actions.indexOf(_npcs.getAction(_npcs.getActionId(npc)));
        NpcAction next = actions.get(Math.floorMod(index + (click.isRight() ? -1 : 1), actions.size()));
        _npcs.setAction(npc, next.getId(), "");
        if (next.getDataPrompt() != null)
            askData(click, npc, next);
    }

    private void askData(Click click, LivingEntity npc, NpcAction action)
    {
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
            click.prompt(prompt(action.getDataPrompt(), action.getDataHelp(), initial, 2000, edit).multiline(action.getDataRows()));
        else
            click.prompt(prompt(action.getDataPrompt(), action.getDataHelp(), initial, 256, edit));
    }

    // ==================== TEXT INPUT ====================

    private void askName(Click click, LivingEntity npc)
    {
        click.prompt(prompt("NPC name", List.of("Colour codes with & work"), _npcs.getName(npc), 64, (target, text) ->
        {
            if (text.isBlank())
                return "The name can't be empty";
            _npcs.setName(target, text);
            return null;
        }));
    }

    private void askSkin(Click click, Mannequin mannequin)
    {
        String current = _npcs.getSkinName(mannequin);
        Player player = playerOf(click);
        click.prompt(prompt("Skin", List.of("Name of the Minecraft player whose skin the NPC wears"), current == null ? "" : current, 16, (target, text) ->
        {
            String name = text.trim();
            if (!NpcManager.isValidPlayerName(name))
                return "Not a Minecraft player name";
            if (!(target instanceof Mannequin m))
                return null;
            click.message("&7Looking up the skin of &e" + name + "&7...");
            _npcs.setSkin(m, name, player);
            return null;
        }));
    }

    private void askSubtitle(Click click, Mannequin mannequin)
    {
        click.prompt(prompt("Text under the name", List.of("Colour codes with & work. Empty = no text"), _npcs.getSubtitle(mannequin), 64, (target, text) ->
        {
            if (target instanceof Mannequin m)
                _npcs.setSubtitle(m, text);
            return null;
        }));
    }

    private interface NpcEdit
    {
        /**
         * @return null when the text was accepted, otherwise the problem
         */
        String apply(LivingEntity npc, String text);
    }

    /**
     * Asks for text in place of this menu, which comes back after. The NPC is looked up again then, as it may be
     * gone.
     */
    private Prompt prompt(String title, List<String> lines, String initial, int maxLength, NpcEdit edit)
    {
        return Prompt.of(title, lines, initial, maxLength, text ->
        {
            LivingEntity npc = getNpc();
            return npc == null ? null : edit.apply(npc, text);
        });
    }

    // ==================== HELPERS ====================

    private static <T extends Keyed> String keyName(Registry<T> registry, T value)
    {
        return registry.getKeyOrThrow(value).getKey().replace('_', ' ');
    }

    /**
     * The next value of the registry, or the previous one on right click.
     */
    private static <T extends Keyed> T cycle(Registry<T> registry, T current, Click click)
    {
        List<T> values = registry.stream().toList();
        int index = values.indexOf(current);
        int step = click.isRight() ? -1 : 1;
        return values.get(Math.floorMod(index + step, values.size()));
    }
}
