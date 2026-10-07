package me.imu.imusunstablefearsystem.other;

import java.util.Date;

import me.imu.imusunstablefearsystem.Enums.DIFFICULT;

public class PlayerDifficultSetting 
{
    public DIFFICULT NetherDifficulty = DIFFICULT.FEAR;
    public Date setDate = new Date();
    
    public PlayerDifficultSetting(boolean setToNow) 
    {
        if (setToNow) {
            this.setDate = new Date(); // Sets to current date and time
        }
    }
    
    
    
}
