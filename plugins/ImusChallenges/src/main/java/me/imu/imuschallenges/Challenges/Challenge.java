package me.imu.imuschallenges.Challenges;

import me.imu.imuschallenges.Interfaces.IChallenge;

public abstract class Challenge implements IChallenge
{
    private String _name;

    public Challenge(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }
}
