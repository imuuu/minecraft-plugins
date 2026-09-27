package imu.DontLoseItems.Enums;


public enum DIFFICULT {
    NO_FEAR,
    FEAR;

    public static String[] getLore(DIFFICULT dif) 
    {
        String[] lores = {" "}; // Your lore logic goes here
        
        switch (dif)
		{
		case FEAR:
			lores = new String[]
					{
							" ",
							"&6LOOT MULTIPLIER &a12x!!!",
							" ",
							"&5- Fear &aGeneration",
							"&5- Shield reflection",
							"&5- Speed Zombie, and loot",
							"&5- pigling arrow knock back",
							"&5- Hoglin knockback",
							"&5- Fire double damage",
							"&5- Water Bottle removes fire",
							"&5- Ghast Explotion",
							"&5- Blaze Larger Fire area",
							"&5- Magma Slime spawn lava on death",
					};
			break;
		case NO_FEAR:
			lores = new String[]
					{
							" ",
							"&6LOOT MULTIPLIER &c1x",
							" ",
							"&a- No &5fear &aGeneration",
							"&a- No shield reflection",
							"&a- Speed Zombie dies on touch",
							"&a- No pigling arrow knock back",
							"&a- No hoglin knockback",
							"&a- No fire double damage",
							"&a- Water Bottle removes fire",
							" ",
							"&c- Enabled Ghast Explotion",
							"&c- Enabled Blaze Larger Fire area",
							"&c- Enabled Magma Slime spawn lava on death",
					};
			break;
		default:
			break;
		
		}
        return lores;
    }
}