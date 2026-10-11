package me.imu.imuschallenges.Shop;

import imu.iAPI.Npc.NpcAction;
import me.imu.imuschallenges.Managers.ManagerChallengeShop;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * ImusAPI NPC action: opens the challenge shop. Players don't need ic.shop, placing the NPC is the admin's choice.
 */
public class ChallengeShopNpcAction implements NpcAction
{
    public static final String ID = "imuschallenges:shop";

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getName()
    {
        return "Opens the challenge shop";
    }

    @Override
    public void use(Player player, LivingEntity npc, String data)
    {
        ManagerChallengeShop.getInstance().openShop(player);
    }

    @Override
    public String describe(String data)
    {
        return "";
    }

    @Override
    public String getPermission()
    {
        return "ic.create.npc";
    }
}
