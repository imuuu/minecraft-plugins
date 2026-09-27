package imu.UCI.Handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.UCI.Interfaces.CommandInterface;
 
public class CommandHandler implements CommandExecutor
{
 
    static HashMap<String, CommandInterface> commands = new HashMap<String, CommandInterface>();
    static HashMap<String, ArrayList<String>> subCommands = new HashMap<String,ArrayList<String>>();
    HashMap<String, String> commandsPermissions = new HashMap<String, String>();
    
    String lastCmdName = "";
    
    int maxLenSub = 0;
    
    public void registerCmd(String name, CommandInterface cmd) 
    {
    	lastCmdName = name;
        commands.put(name, cmd);
    }
    
    public void registerSubCmd(String mainCmd, String subCmdName, CommandInterface cmd)
    {
    	ArrayList<String> subs = subCommands.get(mainCmd);
    	int subLen = subCmdName.split(" ").length;
    	subCmdName = mainCmd.toLowerCase()+" "+subCmdName.toLowerCase();
    	
    	if (maxLenSub <= subLen)
    		maxLenSub = subLen;
    	    	
    	if(subs == null)
    	{
    		subs = new ArrayList<String>();
    	}
    	subs.add(subCmdName);
    	
    	subCommands.put(mainCmd, subs);
    	registerCmd(subCmdName, cmd);
    		
    }
    
    public void setPermissionOnLastCmd(String permissionName)
    {
    	if(lastCmdName == "") 		
    	{
    		System.out.println("lastCmdName is empty");
    		return;
    	}
    	
    	setPermissionOnCmd(lastCmdName, permissionName);
    	lastCmdName="";
    	
    }
    
    public void setPermissionOnCmd(String cmdName, String permissionName)
    {
    	
    	commandsPermissions.put(cmdName.toLowerCase(), permissionName.toLowerCase());
    }
    
    public String arrayConcatenate(String[] args)
    {
    	if(args.length <= 0)
    		return "";
    	
    	String arg = args[0];
    	
    	for(int i = 1 ; i < args.length ; i++)
    	{
    		arg += " " + args[i];
    	}

    	return arg;
    }
    
    public String findSubCmd(String mainCmd, String[] args)
    {
    	if(!subCommands.containsKey(mainCmd))
    		return null;
    	
    	List<String> strs = Arrays.asList(args);
    	
    	int len_args = args.length;
    	
    	if(len_args > maxLenSub)
    		len_args = maxLenSub;
    	
    	for(int i = len_args ; i-- > 0;)
    	{
    		strs = strs.subList(0, i+1);
    		String[] array= new String[strs.size()];
    		strs.toArray(array);
    		
    		String arg = mainCmd+" "+arrayConcatenate(array).toLowerCase();
    		if(subCommands.get(mainCmd).contains(arg))
    			return arg;
    	}
    	
    	//System.out.println("not found here");
    	return null;
    	
    }
    
    public boolean exists(String name) 
    {
        return commands.containsKey(name);
    }
    
    public boolean existsSub(String mainCmd,String subName)
    {
    	if(!subCommands.containsKey(mainCmd))
    		return false;
    	
    	return subCommands.get(mainCmd).contains(subName);
    }
    
    public CommandInterface getExecutor(String name) 
    {
        return commands.get(name);
    }
    
    boolean checkPermissions(Player player, String cmdName)
    {
    	if(commandsPermissions.containsKey(cmdName))
		{
			if(!player.hasPermission(commandsPermissions.get(cmdName)))
			{
				return true;
			}
		}
    	return false;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
        if(sender instanceof Player) 
        {   
        	boolean noPermission=false;
        	Player player = ((Player) sender).getPlayer();
        	
        	String cmd_name = cmd.getName().toLowerCase();
        	if(exists(cmd_name))
        	{       		
        		noPermission = checkPermissions(player, cmd_name);
        		
        		if(!noPermission)
        		{
        			if(!getExecutor(cmd_name).onCommand(sender, cmd, commandLabel, args)) 
                    {
            			if(args.length > 0)
            			{
            				String foundSub = findSubCmd(cmd_name, args);
                            
            				if(foundSub != null)
                            {
            					noPermission=checkPermissions(player, foundSub);
            					if(!noPermission)
            					{
            						getExecutor(foundSub).onCommand(sender, cmd, commandLabel, args);
                                    return true;
            					}
                                
                            }else
                            {
                            	 return false;
                            }                           
            			}                     
                     }	
        		}
        		
        		
        		if(noPermission)
        		{
        			player.sendMessage("You don't have permissions to do that");
        		}
        		 
        		return true;
        	}
        	else
        	{
        		sender.sendMessage("This command doesn't exist!");               
        	}
        	    	 
        } 
        else 
        {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            return true;
        }
        return false;
    }
 
}