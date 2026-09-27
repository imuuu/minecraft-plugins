package imu.iAPI.Interfaces;

public interface ITouchHandler
{
    public void addTouch(int position);
    public void removeTouch(int position);
    public boolean isTouched(int position);
    public void clearTouches();
}
