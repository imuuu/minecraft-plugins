package imu.iAPI.Utilities;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Tuple;
import java.util.LinkedList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;

/**
 * Crafting and smithing recipes.
 */
public final class RecipeUtils
{
	private RecipeUtils() {}

	public static void SetSmithingRecipe(String id, Material mat1, Material mat2, ItemStack resultStack)
	{
		NamespacedKey key = new NamespacedKey(ImusAPI._instance, id);
        RecipeChoice.MaterialChoice choice1 = new RecipeChoice.MaterialChoice(mat1);
        RecipeChoice.MaterialChoice choice2 = new RecipeChoice.MaterialChoice(mat2);
        
        
        Bukkit.addRecipe(new SmithingRecipe(
                key,
                resultStack,
                choice1,
                choice2));
	}

	public static void SetSmithingRecipe(String id, ItemStack stack1, ItemStack stack2, ItemStack resultStack)
	{
		NamespacedKey key = new NamespacedKey(ImusAPI._instance, id);
        
        
        RecipeChoice.ExactChoice choice1 = RecipeChoice.exactChoice(stack1);
        RecipeChoice.ExactChoice choice2 = RecipeChoice.exactChoice(stack2);
        
        Bukkit.getServer().addRecipe(new SmithingRecipe(
                key,
                resultStack,
                choice1,
                choice2));
//        System.out.println("");
//        System.out.println("Stack added: "+stack1.getItemMeta().getDisplayName());
//        System.out.println("with Stack added: "+stack2.getItemMeta().getDisplayName());
	}

	/**
	 * 
	 * @param stack
	 * @return Linkedlist with stacks and their ratio in recipe
	 */
	public static LinkedList<Tuple<ItemStack,Double>> GetRecipe(ItemStack stack)
	{
		LinkedList<Tuple<ItemStack,Double>> list = new LinkedList<>();
		Iterable<ItemStack> ingredients = null;
		Recipe re = null;
		for(Recipe recipe : Bukkit.getServer().getRecipesFor(stack))
		{
			re = recipe;
			if(recipe instanceof ShapedRecipe)
			{
				ShapedRecipe shapedRescipe = (ShapedRecipe)recipe;
				ingredients = shapedRescipe.getIngredientMap().values();
				break;
			}
			
			if(recipe instanceof ShapelessRecipe)
			{
				ShapelessRecipe shapedRescipe = (ShapelessRecipe)recipe;
				ingredients = shapedRescipe.getIngredientList();	
				break;
			}	
			
		}
		if(ingredients != null)
		{
			for(ItemStack ingredient : ingredients)
			{
				if(ingredient == null) continue;
				
				list.add(new Tuple<ItemStack, Double>(ingredient, 1.0/re.getResult().getAmount()));
			}
		}
		
		return list;
	}
}
