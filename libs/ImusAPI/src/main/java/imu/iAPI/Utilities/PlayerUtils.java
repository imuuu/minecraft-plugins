package imu.iAPI.Utilities;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Utilities.ItemUtils;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversation.ConversationState;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Players: their armor, clickable chat commands, conversations.
 */
public final class PlayerUtils
{
	private PlayerUtils() {}

	/**
	 * 
	 * @param player
	 * @param table text is key and value would be command. Remember command: /command pal pla
	 * @param seperator
	 */
	public static void SendClickableCommands(Player player, String frontText, HashMap<String, String> table, String backText ,String seperator)
	{
		TextComponent main_msg = new TextComponent();
		main_msg.addExtra(frontText);
		for (Map.Entry<String, String> entry : table.entrySet()) 
		{
		    String text = entry.getKey();
		    String cmd_str = entry.getValue();
		    
		    TextComponent msg = new TextComponent(text);
		    msg.setBold(true);
		    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd_str));
		    
		    TextComponent msgSlash = new TextComponent(" "+seperator+" ");
			msgSlash.setColor(net.md_5.bungee.api.ChatColor.DARK_GREEN);
		    
		    main_msg.addExtra(msg);
		    main_msg.addExtra(msgSlash);
		    
		}
		main_msg.addExtra(backText);
		player.spigot().sendMessage(main_msg);
	}

	public static void StartConversation(Player player, StringPrompt conv)
	{
		ConversationFactory cf = new ConversationFactory(ImusAPI._instance);
		Conversation conversation = cf.withFirstPrompt(conv).withLocalEcho(true).buildConversation(player);
		conversation.begin();
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				if(conversation.getState() == ConversationState.STARTED)
				{
					if(player.isOnline())
						player.sendMessage(TextUtils.Color("&3Took too long to anwser. Please try again!"));
					
					conversation.abandon();
				}
			}
		}.runTaskLaterAsynchronously(ImusAPI._instance, 20 * 30);
	}

	public static int GetArmorEnchantCount(Player player, Enchantment searchEnch)
	{
		int value = 0;
		ItemStack[] armors = player.getInventory().getArmorContents();
		
		ItemStack helmet = armors[3];
		ItemStack chestplate = armors[2];
		ItemStack leggings = armors[1];
		ItemStack boots = armors[0];
		
		value += ItemUtils.GetEnchantCount(helmet, searchEnch);
		value += ItemUtils.GetEnchantCount(chestplate, searchEnch);
		value += ItemUtils.GetEnchantCount(leggings, searchEnch);
		value += ItemUtils.GetEnchantCount(boots, searchEnch);
		value += ItemUtils.GetEnchantCount(player.getInventory().getItemInOffHand(), searchEnch);

		
		return value;
	}
}
