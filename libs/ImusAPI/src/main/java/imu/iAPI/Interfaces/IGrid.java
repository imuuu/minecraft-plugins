package imu.iAPI.Interfaces;

public interface IGrid
{
    public void registerButtonHandler(IButtonHandler handler);
    public void unregisterButtonHandler();
    public void loadButtons();
}
