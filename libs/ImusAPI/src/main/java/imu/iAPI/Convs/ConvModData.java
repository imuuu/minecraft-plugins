package imu.iAPI.Convs;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Interfaces.IModDataInv;
import imu.iAPI.Interfaces.IModDataValue;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public class ConvModData extends StringPrompt
{
	private String _question;
	private IModDataValue _value;
	private IModDataInv _inv;
	public ConvModData(IModDataValue value, IModDataInv inv, String quetion) 
	{
		_value= value;
		_question = quetion;
		_inv = inv;
	}	
	
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				if(!_inv.SetModData(_value, anwser)) 
				{
					_inv.SetModDataFAILED(_value, _question,anwser);
				}
			}
		}.runTask(ImusAPI._instance);
		
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) {
		return Metods.msgC(_question);
	}
}
