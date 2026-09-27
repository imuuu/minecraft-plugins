package imu.iAPI.Interfaces;


import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public interface IHasSqlSource
{
    public ConnectionSource getSource() throws SQLException;
}
