package imu.iAPI.Database;

/**
 * The database couldn't be opened; the message says why, for the console.
 */
public class DatabaseException extends RuntimeException
{
    public DatabaseException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
