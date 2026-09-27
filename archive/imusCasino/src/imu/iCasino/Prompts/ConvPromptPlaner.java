package imu.iCasino.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import imu.iCasino.Interfaces.CreatingTable;
import net.md_5.bungee.api.ChatColor;

public class ConvPromptPlaner extends StringPrompt
{

	CreatingTable _table;
	Integer _dataID;
	String _question;
	public ConvPromptPlaner(CreatingTable table, Integer dataID, String question)
	{
		_table = table;
		_dataID = dataID;
		_question = question;
		
	}
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		if(!_table.getDataCard().setData(_dataID, anwser))
		{
			_table.getPlayer().sendMessage(ChatColor.RED + "Invalid value!");
		}
		_table.reOpen();
		
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) 
	{		
		
		return _question;
	}
	
	

}
