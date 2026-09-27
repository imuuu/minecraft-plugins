package imu.iMiniGames.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import imu.iMiniGames.Arenas.Arena;
import imu.iMiniGames.Main.ImusMiniGames;

public class ConvPromptSetDescriptionArena extends StringPrompt
{

	ImusMiniGames _main;
	Player _player;

	int _ans_id;

	String _question;
	Arena _arena;
	public ConvPromptSetDescriptionArena(ImusMiniGames main, Player p, String question, Arena arena)
	{
		_main = main;
		_player  = p;

		_question = question;
		_arena = arena;

	}
	
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		_player.sendMessage("Description set!");
		_arena.set_description(anwser);
		
		//_arena.sendArenaCreationgINFO(_player);
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) 
	{				
		return _question;
	}
	
	

}
