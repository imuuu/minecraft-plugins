package imu.GeneralStore.Commands;


//Imports for the base command class.
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;
 
public class GeneralStoreCmd implements CommandInterface
{
	Main _main = null;
	ShopManager _shopM = null;
	public GeneralStoreCmd(Main main)
	{
		_main = main;
		_shopM = _main.getShopManager();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 

    	if(!_main.getShopManager().isReady())
    	{
    		sender.sendMessage(ChatColor.DARK_RED+"You can't use that right now");
    		return true;
    	}
    	
    	if(_shopM.isShopsLocked())
    	{
    		if(!sender.isOp() && !sender.hasPermission("gs"))
    		{
    			sender.sendMessage(ChatColor.RED + "Closed");
    			return true;
    		}
    		
    		sender.sendMessage(ChatColor.YELLOW + "This plugin commands are locked from normal player..");
    		
    		
    	}
    	//Player p = (Player)sender;
    	//System.out.println("Player in hand: "+ p.getInventory().getItemInMainHand());
    	
//    	try 
//    	{
//
//    		//InputStream input = getClass().getResourceAsStream("/DenizenScriptTempelate.txt");
//    		InputStream input = getClass().getResourceAsStream("/DenizenScriptTempelate.txt");
//    	    InputStreamReader inputReader = new InputStreamReader(input);
//			BufferedReader br = new BufferedReader(inputReader);
//			String str;
//			while((str = br.readLine()) != null )
//			{
//				System.out.println(str);
//			}
//			br.close();
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//			System.out.println("EXPT");
//		}
//    	
//    	System.out.println("assign name: "+_main.getDenSC().CreateTestSample("testiS", "markonkulju"));
    	
    	if(args.length > 0)
    		return false;
        
    	
    	
        return true;
    }
 
}