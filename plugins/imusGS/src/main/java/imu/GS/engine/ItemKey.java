package imu.GS.engine;

/**
 * Identity of a kind of item: two stacks with the same key are the same item in every way but their amount
 * (what Bukkit calls isSimilar). The engine never looks inside it; the Bukkit side builds it from an ItemStack.
 */
public record ItemKey(String id)
{
    public ItemKey
    {
        if (id == null || id.isEmpty())
            throw new IllegalArgumentException("ItemKey id can't be empty");
    }

    @Override
    public String toString()
    {
        return id.length() <= 32 ? id : id.substring(0, 32) + "...";
    }
}
