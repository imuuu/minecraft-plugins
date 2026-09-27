package imu.GeneralStore.SubCommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.ItemMetods;
import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class subStorePayCmd implements CommandInterface
{
	Main _main = null;
	Economy _econ = null;
	ItemMetods _itemM;
	
	double _tax = 0.1;
	double _minPayment = 1.0;
	
	public subStorePayCmd(Main main) 
	{
		_main = main;
		_itemM = main.getItemM();
		_econ = main.getEconomy();
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
    	if(args.length < 3)
    	{
    		player.sendMessage("/pay <amount> <player>");
    		return false;
    	}
    	if(_econ == null)
    		return false;

    	if(_itemM.isDigit(args[1]))
    	{
    		Player receiver = Bukkit.getPlayer(args[2]);
    		if(receiver != null)
    		{
    			double payment = Double.parseDouble(args[1]);
    			if(payment > _minPayment)
    			{
    				if(_econ.getBalance(player) > payment)
    				{
    					_econ.withdrawPlayer(player, payment);
    					double afterTax = payment*(1.00-_tax);
    					_econ.depositPlayer(receiver, afterTax);
    					
    					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9You have paid to &b"+receiver.getName()+" &2"+payment+" &7"+_econ.currencyNamePlural()));
    					
    					receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9You have received from &b"+player.getName()+" &2"+(Math.round((afterTax*100.00))/100.00)+"&7("+payment+")"+" &7"+_econ.currencyNamePlural()+" &9after taxes!"));
    					return false;
    				}
    				player.sendMessage(ChatColor.RED + "Not enough balance!");
    				return false;
    			}
    			player.sendMessage(ChatColor.RED + "Payment need to be positive and larger than "+_minPayment+"!");
    			return false;
    		}
    		player.sendMessage(ChatColor.RED + "Couldnt find player with that name!");
    		return false;
    		
    	}
    	player.sendMessage(ChatColor.RED + "Thats not a number!");
    	
        return false;
    }

	public double get_tax() {
		return _tax;
	}

	public void set_tax(double _tax) {
		this._tax = _tax;
	}

	public double get_minPayment() {
		return _minPayment;
	}

	public void set_minPayment(double _minPayment) {
		this._minPayment = _minPayment;
	}
    
    
   
   
}