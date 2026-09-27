package imu.iAPI.Interfaces;

import imu.iAPI.Other.MySQL;

public interface IHasSql
{
    public MySQL getSQL();
    public boolean connectDataBase();
}
