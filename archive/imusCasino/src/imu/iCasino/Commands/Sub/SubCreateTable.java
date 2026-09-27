package imu.iCasino.Commands.Sub;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iCasino.Games.MainMenu.CreatingMainMenuUI;
import imu.iCasino.Main.CasinoMain;

public class SubCreateTable implements CommandInterface
{
	CasinoMain _main = null;
	String _subCmd = "";

	public SubCreateTable(CasinoMain main) 
	{
		_main = main;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
        new CreatingMainMenuUI(_main, _main.getMetods(), player).openThis();
        
		
        return false;
    }


	@Override
	public void FailedMsg(CommandSender sender, String msg) {
		sender.sendMessage(msg);
		
	}
    
   
   
}