package imu.iAPI.Npc;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * What an NPC does when a player clicks it. A plugin registers its actions with
 * {@link NpcManager#registerAction(NpcAction)}; each NPC stores the id of its action and a string of data
 * for it (a shop id, a list of commands...).
 *
 * <pre>
 * npcs.registerAction(new NpcAction()
 * {
 *     public String getId() { return "myplugin:menu"; }
 *     public String getName() { return "Opens my menu"; }
 *     public void use(Player player, LivingEntity npc, String data) { openMenu(player); }
 * });
 * npcs.createAndEdit(admin, "myplugin:menu", "", "&amp;6Menu");
 * </pre>
 */
public interface NpcAction
{
    /**
     * Unique id stored on the NPC, e.g. "imusgs:shop". Keep it the same between versions.
     */
    String getId();

    /**
     * Shown in the NPC settings, e.g. "Opens a general store".
     */
    String getName();

    /**
     * Runs on the main thread when a player clicks the NPC.
     *
     * @param data what was stored for the NPC, "" when nothing
     */
    void use(Player player, LivingEntity npc, String data);

    /**
     * One line telling what this NPC does with its data, for the settings menu. &amp; colours work.
     */
    default String describe(String data)
    {
        return data == null ? "" : data;
    }

    /**
     * The question asked for the data in the settings menu, or null when this action needs no data.
     */
    default String getDataPrompt()
    {
        return null;
    }

    /**
     * Lines shown under the question. &amp; colours work.
     */
    default List<String> getDataHelp()
    {
        return List.of();
    }

    /**
     * Rows of the text box, 0 for a single line.
     */
    default int getDataRows()
    {
        return 0;
    }

    /**
     * The stored data as the text the admin edits.
     */
    default String toEditText(String data)
    {
        return data == null ? "" : data;
    }

    /**
     * Turns typed text into the data to store.
     *
     * @throws IllegalArgumentException with a message for the admin when the text isn't valid
     */
    default String parseData(String text)
    {
        return text;
    }

    /**
     * Permission needed to give an NPC this action or change its data, on top of ia.npc.edit. Null for none.
     */
    default String getPermission()
    {
        return null;
    }
}
