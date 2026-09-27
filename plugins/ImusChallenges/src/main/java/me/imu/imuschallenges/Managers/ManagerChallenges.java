package me.imu.imuschallenges.Managers;

import me.imu.imuschallenges.Enums.CHALLENGE_TYPE;
import me.imu.imuschallenges.Challenges.CCollectMaterial;
import me.imu.imuschallenges.Challenges.Challenge;
import org.bukkit.Material;

import java.util.ArrayList;

public class ManagerChallenges
{
    private static ManagerChallenges _instance;
    public static ManagerChallenges getInstance() { return _instance; }

    private ArrayList<Challenge> _challenges;

    public ManagerChallenges()
    {
        _instance = this;
        _challenges = new ArrayList<>();
        Initialize();
    }

    private void Initialize()
    {
        _challenges = new ArrayList<>();
        _challenges.add(new CCollectMaterial("Collect 10 dirt", new String[]{"Collect 10 dirt"}, CHALLENGE_TYPE.PERSONAL, Material.DIRT, 10));
        _challenges.add(new CCollectMaterial("Collect 10 cobblestone", new String[]{"Collect 10 cobblestone"}, CHALLENGE_TYPE.PERSONAL, Material.COBBLESTONE, 10));
    }
    public void loadChallenges()
    {

    }

    public void saveChallenges()
    {

    }

    public void addChallenge()
    {

    }

    public void removeChallenge()
    {

    }

    public void getChallenge()
    {

    }


}
