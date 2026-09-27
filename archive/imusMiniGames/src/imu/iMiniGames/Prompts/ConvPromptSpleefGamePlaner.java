package imu.iMiniGames.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import imu.iMiniGames.Invs.SpleefGamePlaner;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Other.SpleefDataCard;

public class ConvPromptSpleefGamePlaner extends StringPrompt
{

	ImusMiniGames _main;
	Player _player;

	int _ans_id;

	String _question;

	public ConvPromptSpleefGamePlaner(ImusMiniGames main, Player p, int ans_id, String question)
	{
		_main = main;
		_player  = p;

		_ans_id = ans_id;
		_question = question;

	}
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		SpleefDataCard card = (SpleefDataCard)_main.get_spleefManager().getPlayerDataCard(_player);
		card.putDataValue(_ans_id, anwser);
		new SpleefGamePlaner(_main, _player, card);
		
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) 
	{		
		
		return _question;
	}
	
	

}
