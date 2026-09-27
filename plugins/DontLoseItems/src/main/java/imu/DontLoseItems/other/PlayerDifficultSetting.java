package imu.DontLoseItems.other;

import java.util.Date;

import imu.DontLoseItems.Enums.DIFFICULT;

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
