package imu.iWaystones.Other;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.Metods;
import imu.iWaystone.Interfaces.IModDataInv;
import imu.iWaystone.Interfaces.IModDataValues;
import imu.iWaystones.Enums.ConvUpgradeModData;
import imu.iWaystones.Main.ImusWaystones;

public class ConvUpgrade extends StringPrompt
{
	private String _question;
	private IModDataValues _value;
	private IModDataInv _inv;
	public ConvUpgrade(IModDataValues value, IModDataInv inv, String quetion) 
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
				_inv.SetModData(_value, anwser);
				_inv.openThis();
				

				if((ConvUpgradeModData)_value == ConvUpgradeModData.RENAME)
				{
					_inv.closeThis(); //only bc rename is not working correcly
				}
					
				
				
			}
		}.runTask(ImusWaystones._instance);
		
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) {
		return Metods.msgC(_question);
	}

}
