package imu.GS.Other;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.scheduler.BukkitRunnable;

import imu.GS.Main.Main;

public class DenizenScriptCreator 
{
	final private String _nameTag="gs_assignments_shops.dsc";
	private String _path = "";
	
	ArrayList<String> example_script;
	String str_script_name = null;
	final private String SHOP_TOKEN = "%shop%";
	// Scripts made by older versions: execute as_op 'gs open shop <name>'
	final private Pattern OLD_OP_COMMAND = Pattern.compile("execute as_op '/?gs open shop (.+?)'");
	private Main _main;
	public DenizenScriptCreator(Main main)
	{
		_main = main;
		_path = "plugins/Denizen/scripts/"+_nameTag;
		example_script = ReadDenizenTemplate();

	}

	/**
	 * Older scripts opened the shop with "execute as_op", which makes the player op for the command.
	 * Rewrites them to run /gsopenfor as the server.
	 */
	public void MigrateOpScripts()
	{
		File f = new File(_path);
		if(!f.exists()) return;

		try
		{
			String content = Files.readString(f.toPath(), StandardCharsets.UTF_8);
			Matcher matcher = OLD_OP_COMMAND.matcher(content);
			if(!matcher.find()) return;

			String migrated = matcher.replaceAll(m -> Matcher.quoteReplacement("execute as_server 'gsopenfor <player.name> " + m.group(1) + "'"));
			Files.writeString(f.toPath(), migrated, StandardCharsets.UTF_8);
			_main.getLogger().warning("Updated the shop NPC scripts in "+_path+" to open shops without op. Run /ex reload if Denizen already loaded them.");
		}
		catch (Exception e)
		{
			_main.getLogger().log(Level.SEVERE, "Couldn't update the shop NPC scripts in "+_path+". They still use 'execute as_op', change them to 'execute as_server' with /gsopenfor by hand.", e);
		}
	}
	
	public String CreateAssignScript(String script_name, String shopname)
	{
		WriteTheNewScript(CoverLines(script_name,shopname), false);		
		return str_script_name;
	}
	
	ArrayList<String> CoverLines(String script_name, String shopname)
	{
		ArrayList<String> lines_for_new = new ArrayList<>();
		if(example_script == null || example_script.isEmpty())
		{
			System.out.println("couldnt get that");
			return null;
		}
		
		str_script_name = null;
		
		for(String line : example_script)
		{
			String mod_str = line;
			if(line.contains("generalstore"))
			{
				mod_str = mod_str.replace("generalstore", script_name);
				if(str_script_name == null)
				{
					str_script_name = mod_str.replace(":", "");
				}
			}
			
			mod_str = mod_str.replace(SHOP_TOKEN, shopname);
			lines_for_new.add(mod_str);
		}
		return lines_for_new;
	}
	
	void WriteTheNewScript(ArrayList<String> lines, boolean overide)
	{
		try 
		{
			//System.out.println("try to find: "+_main.getDataFolder()+"/Denizen/scripts/"+script_name+".dc");
			
			File f = new File(_path);
			BufferedWriter bw;
			if(!f.exists() || overide)
			{
				bw = new BufferedWriter(new FileWriter(_path));
			}else
			{
				bw = new BufferedWriter(new FileWriter(f,true));
			}
			
			if(!overide)
			{
				for(String line : lines)
				{
					bw.append(line);
				}
			}else
			{
				for(String line : lines)
				{
					bw.write(line);
					bw.newLine();
				}
			}
			
			bw.close();
		} catch (Exception e) 
		{
			System.out.println("Coulndt write denizen folderscript");
		}
	}
	public boolean IsFileExist()
	{
		return new File(_path).exists();
	}
	public void RenameShop(String oldShopName, String newShopName)
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				try 
				{
					//System.out.println("try to find: "+_main.getDataFolder()+"/Denizen/scripts/"+script_name+".dc");
					
					File f = new File(_path);
					
					if(!f.exists())
					{
						return;
					}
					BufferedReader br = new BufferedReader(new FileReader(f));
					ArrayList<String> lines = new ArrayList<>();
					String line;
					while(( line = br.readLine())!= null)
					{
						if(line.contains(oldShopName))
						{
							line = line.replace(oldShopName, newShopName);
						}
						//System.out.println("line: "+line);
						lines.add(line);
					}
					br.close();
					
					WriteTheNewScript(lines, true);	
					
				} catch (Exception e) 
				{
					System.out.println("Coulndt re write denizen folderscript");
				}
			}
		}.runTaskAsynchronously(_main);
	}
	
	ArrayList<String> ReadDenizenTemplate()
	{
		ArrayList<String> lines = new ArrayList<>();
		try 
    	{
			//File f = new File(_path);
			BufferedReader br;
			
			InputStream input = getClass().getResourceAsStream("/DenizenScriptTempelate.txt");
    	    InputStreamReader inputReader = new InputStreamReader(input);
			br = new BufferedReader(inputReader);
			
    		//"DenizenScriptTempelate.txt"
    		
			String str;
			while((str = br.readLine()) != null )
			{
				lines.add(str+"\n");
			}
			br.close();
			
		} catch (Exception e) {
			System.out.println("Coulnt find DenizenScriptTemplate");
		}
		
		return lines;
	}
}
