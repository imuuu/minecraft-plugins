package imu.GS.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import imu.GS.Interfaces.IModData;
import imu.GS.Interfaces.IModDataValues;
import imu.GS.Invs.ShopBaseModify;
import imu.GS.ShopUtl.ShopModData;
import imu.iAPI.Other.CustomInvLayout;

public class ConvShopMod extends StringPrompt
{

	
	private String _question;
	private CustomInvLayout _inv;
	private IModData _modData;
	private IModDataValues _dataName;
		
	public ConvShopMod(IModDataValues dataName, CustomInvLayout inv ,IModData modData, String question)
	{
		_modData = modData;
		_dataName = dataName;
		_question = question;
		_inv = inv;
	}
	
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		ShopModData data = (ShopModData)_modData;
		if(!data.SetAndCheck(_dataName, anwser)) return this;
		
		((ShopBaseModify)_inv).SetModData(_modData);
		_inv.openThis();
		
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) 
	{		
		return _question;
	}

}
