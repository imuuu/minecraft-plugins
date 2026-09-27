package imu.iMiniGames.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import imu.iMiniGames.Arenas.CombatArena;
import imu.iMiniGames.Main.ImusMiniGames;
import net.md_5.bungee.api.ChatColor;

public class ConvPromptSetRadiusCombatArena extends StringPrompt
{

	ImusMiniGames _main;
	Player _player;

	int _ans_id;

	String _question;
	CombatArena _arena;
	public ConvPromptSetRadiusCombatArena(ImusMiniGames main, Player p, String question, CombatArena arena)
	{
		_main = main;
		_player  = p;

		_question = question;
		_arena = arena;

	}
	
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		if(_main.get_itemM().isDigit(anwser))
		{
			_player.sendMessage("Radius hasbeen set");
			_arena.setArena_radius(Integer.parseInt(anwser));
			return null;
		}
		_player.sendMessage(ChatColor.RED+"Invalid syntax!");
		return null;

	}

	@Override
	public String getPromptText(ConversationContext arg0) 
	{				
		return _question;
	}
	
	

}
