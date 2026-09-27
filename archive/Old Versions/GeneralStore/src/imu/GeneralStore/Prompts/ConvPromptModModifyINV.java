package imu.GeneralStore.Prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.GeneralStore.Other.Shop;
import imu.GeneralStore.main.Main;

public class ConvPromptModModifyINV extends StringPrompt
{

	Main _main;
	Player _player;
	String[] _answers;
	int _ans_id;
	
	ItemStack _stack;
	Shop _shop;
	
	String _question;
	public ConvPromptModModifyINV(Main main, Player p, ItemStack stack, Shop shop ,String[] answers, int ans_id, String question)
	{
		_main = main;
		_player  = p;
		_answers = answers;
		_ans_id = ans_id;
		_stack = stack;
		_shop = shop;
		_question = question;
		_shop.set_closed(true);
	}
	@Override
	public Prompt acceptInput(ConversationContext con, String anwser) 
	{
		//con.getForWhom().sendRawMessage("Thank you for ur awnser!: "+anwser );
		_answers[_ans_id] = anwser;
		_main.getShopModManager().openModShopModifyInv(_player, _stack, _shop, _answers);
		return null;
	}

	@Override
	public String getPromptText(ConversationContext arg0) 
	{		
		return _question;
	}

}
