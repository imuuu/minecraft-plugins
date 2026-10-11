package imu.GS.engine;

/**
 * Which tab of the customer's side of the shop menu an item is listed in.
 */
public enum ItemCategory
{
    /** Everything that isn't gear or a shulker box: blocks, ores, food... */
    BLOCKS,
    /** Tools, weapons and armor */
    GEAR,
    /** Sold by their contents, one box at a time */
    SHULKER_BOX
}
