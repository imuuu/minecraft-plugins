package imu.GS.text;

import imu.iAPI.Other.Methods;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Every text players see, from plugins/imusGS/messages_&lt;language&gt;.yml ("language" in config.yml). The files
 * of the languages imusGS comes with are written there on the first start, to edit. A text missing from the file
 * comes from the plugin's own copy, so an update's new texts work before the file has them.
 *
 * <p>Texts use &amp; colour codes and {placeholders}: {@code get("shop.amount", "amount", 64)}.</p>
 */
public final class Messages
{
    public static final List<String> LANGUAGES = List.of("en", "fi");

    private static YamlConfiguration _texts = new YamlConfiguration();

    private Messages() {}

    /**
     * Reads the texts of the language config.yml names. Call it on start and when settings change.
     */
    public static void load(JavaPlugin plugin)
    {
        String language = plugin.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);
        for (String bundled : LANGUAGES)
        {
            if (!new File(plugin.getDataFolder(), file(bundled)).exists())
                plugin.saveResource(file(bundled), false);
        }

        File file = new File(plugin.getDataFolder(), file(language));
        if (!file.exists())
        {
            plugin.getLogger().warning("No " + file.getName() + ", using English. Copy messages_en.yml to make it.");
            file = new File(plugin.getDataFolder(), file("en"));
        }
        YamlConfiguration texts = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defaults = bundled(plugin, LANGUAGES.contains(language) ? language : "en");
        if (defaults != null)
        {
            // English under the language's own defaults, for texts neither has yet
            YamlConfiguration english = bundled(plugin, "en");
            if (english != null && defaults != english)
                defaults.setDefaults(english);
            texts.setDefaults(defaults);
        }
        _texts = texts;
    }

    private static String file(String language)
    {
        return "messages_" + language + ".yml";
    }

    private static YamlConfiguration bundled(JavaPlugin plugin, String language)
    {
        try (InputStream in = plugin.getResource(file(language)))
        {
            if (in == null)
                return null;
            return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e)
        {
            plugin.getLogger().log(Level.WARNING, "Couldn't read the plugin's " + file(language), e);
            return null;
        }
    }

    /**
     * The text with its placeholders filled in, with &amp; colour codes. The key itself when there is no text.
     *
     * @param placeholders name, value, name, value...
     */
    public static String get(String key, Object... placeholders)
    {
        return fill(_texts.getString(key, key), placeholders);
    }

    /**
     * A list of lines, e.g. a lore; a single text is a list of one line.
     */
    public static List<String> lines(String key, Object... placeholders)
    {
        List<String> lines = new ArrayList<>();
        if (_texts.isList(key))
        {
            for (String line : _texts.getStringList(key))
                lines.add(fill(line, placeholders));
        } else
            lines.add(get(key, placeholders));
        return lines;
    }

    /**
     * Sends the text, coloured. An empty text sends nothing, so a message can be turned off.
     */
    public static void send(CommandSender to, String key, Object... placeholders)
    {
        String text = get(key, placeholders);
        if (!text.isEmpty())
            to.sendMessage(Methods.msgC(text));
    }

    static String fill(String text, Object... placeholders)
    {
        for (int i = 0; i + 1 < placeholders.length; i += 2)
            text = text.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        return text;
    }
}
