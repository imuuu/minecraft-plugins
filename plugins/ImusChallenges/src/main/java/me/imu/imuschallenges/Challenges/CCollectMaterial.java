package me.imu.imuschallenges.Challenges;

import imu.iAPI.Utilities.ItemUtils;
import me.imu.imuschallenges.Enums.CHALLENGE_TYPE;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CCollectMaterial extends Challenge
{
    private Material _material;
    private int _amount;
    private String[] _lores;
    private CHALLENGE_TYPE _challengeType;

    public CCollectMaterial(String name, String[] lore, CHALLENGE_TYPE challenge_type, Material material, int amount)
    {
        super(name);
        _material = material;
        _amount = amount;
        _lores = lore;
    }


    @Override
    public ItemStack getDisplayItemStack()
    {
        ItemStack stack = new ItemStack(_material);
        ItemUtils.SetDisplayName(stack, getName());
        ItemUtils.SetLores(stack, _lores, false);
        return stack;
    }
}
