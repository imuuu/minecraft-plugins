package imu.iAPI.Interfaces;

import java.util.Set;

public interface ISnapshotHandler
{
    public void takeSnapshot(String snapshotName);
    public void restoreSnapshot(String snapshotName);
    public boolean hasSnapshot(String snapshotName);
    public void removeSnapshot(String snapshotName);
    public IBUTTONN getButtonFromSnapshot(String snapshotName, int slotId);
    public Set<String> listSnapshots();
}