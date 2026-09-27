package imu.GS.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import imu.GS.Invs.CreateCustomPriceInv;
import imu.GS.Invs.CreateCustomPriceInv.CCPdata;
import imu.GS.Main.Main;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public class ConvCCPINV extends StringPrompt
{
	String _question;
	CreateCustomPriceInv _ccpInv;
	CCPdata _ccpData;
	Main _main;
	public ConvCCPINV(Main main,CreateCustomPriceInv ccpInv, CCPdata data,String question)
	{
		_main = main;
		//_player = p;
		_ccpData = data;
		_question = question;
		_ccpInv = ccpInv;
	}
	
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		if(!ImusAPI._metods.isDigit(anwser)) return this;
		if(Double.parseDouble(anwser) < 0) return this;
		
		_ccpData.value = Double.parseDouble(anwser);
		_ccpInv.SetData(_ccpData);
		_ccpInv.openThis();
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) 
	{		
		return Metods.msgC(_question);
	}

}
