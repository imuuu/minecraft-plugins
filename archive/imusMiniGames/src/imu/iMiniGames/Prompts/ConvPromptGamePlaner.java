package imu.iMiniGames.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import imu.iMiniGames.Invs.SpleefGamePlaner;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Other.SpleefDataCard;

public class ConvPromptGamePlaner extends StringPrompt
{

	ImusMiniGames _main;
	Player _player;

	int _ans_id;

	String _question;

	SpleefDataCard _card;
	public ConvPromptGamePlaner(ImusMiniGames main, Player p ,SpleefDataCard card, int ans_id, String question)
	{
		_main = main;
		_player  = p;
		_card = card;
		
		_ans_id = ans_id;
		_question = question;

	}
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		_card.putDataValue(_ans_id, anwser);
		new SpleefGamePlaner(_main, _player, _card);
		
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) 
	{		
		
		return _question;
	}
	
	

}
