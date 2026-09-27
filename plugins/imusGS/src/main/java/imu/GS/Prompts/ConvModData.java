package imu.GS.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import imu.GS.Interfaces.IModData;
import imu.GS.Interfaces.IModDataInv;
import imu.GS.Interfaces.IModDataValues;
import imu.iAPI.Other.Metods;

public class ConvModData extends StringPrompt
{

	
	private String _question;
	private IModDataInv _inv;
	private IModData _modData;
	private IModDataValues _dataName;
		
	public ConvModData(IModDataValues dataName, IModDataInv inv ,IModData modData, String question)
	{
		;
		_modData = modData;
		_dataName = dataName;
		_question = question;
		_inv = inv;
	}
	
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		if(!_modData.SetAndCheck(_dataName, anwser)) return this;	
		_inv.SetModData(_modData);
		_inv.openThis();
		
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) 
	{		
		return Metods.msgC(_question);
	}

}
