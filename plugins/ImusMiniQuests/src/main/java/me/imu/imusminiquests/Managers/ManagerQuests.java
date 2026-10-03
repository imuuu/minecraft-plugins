package me.imu.imusminiquests.Managers;

import imu.iAPI.LootTables.ImusLootTable;
import imu.iAPI.Other.Metods;
import me.imu.imusminiquests.Enums.OBJECTIVE_TYPE;
import me.imu.imusminiquests.Quests.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Loads the quest definitions from quests.yml and looks them up by id or by quest item.
 */
public class ManagerQuests
{
    private static final String FILE_NAME = "quests.yml";

    private final JavaPlugin _plugin;
    private final Map<String, Quest> _quests = new LinkedHashMap<>();
    private final Set<Material> _breakTargets = EnumSet.noneOf(Material.class);
    private boolean _anyBlockIsBreakTarget;
    private ImusLootTable _dropTable = new ImusLootTable();

    public ManagerQuests(JavaPlugin plugin)
    {
        _plugin = plugin;
    }

    public void load()
    {
        File file = new File(_plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) _plugin.saveResource(FILE_NAME, false);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Logger log = _plugin.getLogger();

        _quests.clear();
        _breakTargets.clear();
        _anyBlockIsBreakTarget = false;
        _dropTable = new ImusLootTable();

        ConfigurationSection questsSection = config.getConfigurationSection("quests");
        if (questsSection == null)
        {
            log.warning(FILE_NAME + " has no quests section");
            return;
        }

        for (String id : questsSection.getKeys(false))
        {
            ConfigurationSection section = questsSection.getConfigurationSection(id);
            if (section == null) continue;

            Quest quest = parseQuest(id.toLowerCase(Locale.ROOT), section, log);
            if (quest == null) continue;

            _quests.put(quest.getId(), quest);
            if (quest.getDropWeight() > 0) _dropTable.add(quest, quest.getDropWeight());

            QuestObjective objective = quest.getObjective();
            if (objective.type() == OBJECTIVE_TYPE.BREAK_BLOCK)
            {
                if (objective.targets().isAny()) _anyBlockIsBreakTarget = true;
                _breakTargets.addAll(objective.targets().getMaterials());
            }
        }
    }

    private Quest parseQuest(String id, ConfigurationSection section, Logger log)
    {
        String context = FILE_NAME + " quest " + id;

        Material material = Material.matchMaterial(section.getString("material", "WHITE_STAINED_GLASS_PANE"));
        if (material == null || !material.isItem())
        {
            log.warning(context + ": unknown material " + section.getString("material") + ", using WHITE_STAINED_GLASS_PANE");
            material = Material.WHITE_STAINED_GLASS_PANE;
        }

        NamespacedKey model = null;
        if (section.isString("model"))
        {
            model = NamespacedKey.fromString(section.getString("model").toLowerCase(Locale.ROOT));
            if (model == null) log.warning(context + ": invalid model " + section.getString("model"));
        }

        ConfigurationSection objectiveSection = section.getConfigurationSection("objective");
        if (objectiveSection == null)
        {
            log.warning(context + ": has no objective, skipped");
            return null;
        }

        OBJECTIVE_TYPE type;
        try
        {
            type = OBJECTIVE_TYPE.valueOf(objectiveSection.getString("type", "").toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException e)
        {
            log.warning(context + ": unknown objective type " + objectiveSection.getString("type")
                    + ", use one of " + Arrays.toString(OBJECTIVE_TYPE.values()));
            return null;
        }

        List<String> rawTargets = objectiveSection.getStringList("targets");
        TargetSet targets = type.targetsEntities()
                ? TargetSet.parseEntities(rawTargets, log, context)
                : TargetSet.parseMaterials(rawTargets, log, context);

        int amount = objectiveSection.getInt("amount", 0);
        if (amount <= 0)
        {
            log.warning(context + ": objective amount must be above 0, skipped");
            return null;
        }

        QuestObjective objective = new QuestObjective(type, targets, amount, objectiveSection.getString("text"));

        int rolls = section.getInt("rewards.rolls", 1);
        List<QuestReward> pool = parseRewards(section, "rewards.pool", log, context);
        List<QuestReward> guaranteed = parseRewards(section, "rewards.guaranteed", log, context);

        if (pool.isEmpty() && guaranteed.isEmpty())
            log.warning(context + ": has no rewards");

        String nameFormat = _plugin.getConfig().getString("item.name-format", "&6MiniQuest &8- %name%");
        return new Quest(
                id,
                Metods.msgC(nameFormat.replace("%name%", section.getString("name", id))),
                material,
                model,
                section.getStringList("lore").stream().map(Metods::msgC).toList(),
                Math.max(0, section.getInt("drop-weight", 0)),
                objective,
                Math.max(0, rolls),
                pool,
                guaranteed);
    }

    private List<QuestReward> parseRewards(ConfigurationSection section, String path, Logger log, String context)
    {
        List<QuestReward> rewards = new ArrayList<>();
        List<Map<?, ?>> entries = section.getMapList(path);
        for (int i = 0; i < entries.size(); i++)
        {
            YamlConfiguration entry = new YamlConfiguration();
            for (Map.Entry<?, ?> e : entries.get(i).entrySet())
            {
                entry.set(String.valueOf(e.getKey()), e.getValue());
            }

            QuestReward reward = QuestReward.fromConfig(entry, log, context + " " + path + "[" + i + "]");
            if (reward != null) rewards.add(reward);
        }
        return rewards;
    }

    public Quest getQuest(String id)
    {
        return id == null ? null : _quests.get(id.toLowerCase(Locale.ROOT));
    }

    /**
     * The quest of a quest item, or null when the item isn't one or its quest was removed.
     */
    public Quest getQuest(ItemStack stack)
    {
        return getQuest(QuestItem.getQuestId(stack));
    }

    public Collection<Quest> getQuests() {return Collections.unmodifiableCollection(_quests.values());}

    public Set<String> getQuestIds() {return Collections.unmodifiableSet(_quests.keySet());}

    /**
     * True when breaking this material can move a BREAK_BLOCK quest forward.
     */
    public boolean isBreakTarget(Material material)
    {
        return _anyBlockIsBreakTarget || _breakTargets.contains(material);
    }

    /**
     * A random quest weighted by drop-weight, or null when no quest can drop.
     */
    public Quest rollDrop()
    {
        if (_dropTable.getTotalWeight() <= 0) return null;
        return (Quest) _dropTable.getLoot();
    }
}
