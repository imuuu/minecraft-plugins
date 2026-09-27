package imu.iCards.Managers;


import java.util.ArrayList;
import imu.iCards.Other.Category;

public class CategoryManager
{
	public static CategoryManager _instance;
	
	private ArrayList<Category> _categorys;
	
	public CategoryManager()
	{
		_instance = this;
		_categorys = new ArrayList<>();
	}
	
	public void AddCategory(Category category)
	{

		if(HasCategory(category)) return;
		
		_categorys.add(category);
	}
	
	public boolean HasCategory(Category category)
	{
		String cat = category.GetName().toLowerCase();
		for (Category cate : _categorys)
		{
			if(cate.GetName().matches(cat)) return true;
		}
		
		return false;
	}
}
