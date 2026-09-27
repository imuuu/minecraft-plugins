package imu.DontLoseItems.other;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class StoredBlock {
    private final Location location;
    private final Material material;
    private final BlockData blockData;

    public StoredBlock(Location location, Material material, BlockData blockData) {
        this.location = location;
        this.material = material;
        this.blockData = blockData;
    }

    public Location getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    // Method to restore the block
    public void restore() {
        location.getBlock().setType(material);
        location.getBlock().setBlockData(blockData);
    }
}
