package imu.GS.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.google.common.base.Strings;

import imu.GS.Invs.CreateCustomPriceInv;
import imu.GS.Main.Main;
import imu.GS.ShopUtl.ItemPrice.PriceCustom;
import imu.iAPI.Other.Metods;

public class ConvCCPINVsavePC extends StringPrompt
{
	String _question;
	CreateCustomPriceInv _ccpInv;
	Main _main;
	PriceCustom _pc;
	public ConvCCPINVsavePC(Main main, CreateCustomPriceInv ccpInv,PriceCustom pc,String question)
	{
		_main = main;
		//_player = p;
		_question = question;
		_ccpInv = ccpInv;
		_pc = pc;
	}
	
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		String anw = anwser;
		if(Strings.isNullOrEmpty(anwser)) anw = " ";

		_main.get_shopManager().SavePriceCustom(_ccpInv.GetPlayer().getUniqueId(), anw,_pc);
		_ccpInv.GetPlayer().sendMessage(Metods.msgC("&6Custom Price saved with name: &9"+anw));
		_ccpInv.openThis();
		
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) 
	{		
		return Metods.msgC(_question);
	}

}
