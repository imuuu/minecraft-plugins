package imu.GS.engine.shop;

/**
 * @param sellMultiplier        players buy from the shop for value * this
 * @param buyMultiplier         players sell to the shop for value * this
 * @param expirePercent         share of every normal listing that disappears each expire cooldown, 0.1 = 10%
 * @param expireCooldownMinutes minutes between expiries, 0 or less = never
 * @param locked                only admins can open the shop
 * @param customersCanOnlyBuy   players can't sell to the shop
 * @param absolutePositions     listings a player can't buy keep their slot instead of being left out
 * @param liveEvents            flash deals, rotating stock and items found by players come to this shop
 *                              (each also has to be on in config.yml)
 * @param kind                  the shop preset it was made from (blacksmith, farmer...), null for a general store
 */
public record ShopSettings(double sellMultiplier, double buyMultiplier, double expirePercent, int expireCooldownMinutes,
                           boolean locked, boolean customersCanOnlyBuy, boolean absolutePositions, boolean liveEvents,
                           String kind)
{
    public static final ShopSettings DEFAULT = new ShopSettings(5.0, 1.0, 0.1, 60, false, false, true, true);

    /**
     * A general store's settings.
     */
    public ShopSettings(double sellMultiplier, double buyMultiplier, double expirePercent, int expireCooldownMinutes,
                        boolean locked, boolean customersCanOnlyBuy, boolean absolutePositions, boolean liveEvents)
    {
        this(sellMultiplier, buyMultiplier, expirePercent, expireCooldownMinutes, locked, customersCanOnlyBuy, absolutePositions, liveEvents, null);
    }

    public boolean isGeneral()
    {
        return kind == null || kind.isBlank();
    }

    public ShopSettings withLocked(boolean value)
    {
        return new ShopSettings(sellMultiplier, buyMultiplier, expirePercent, expireCooldownMinutes, value, customersCanOnlyBuy, absolutePositions, liveEvents, kind);
    }

    public ShopSettings withKind(String value)
    {
        return new ShopSettings(sellMultiplier, buyMultiplier, expirePercent, expireCooldownMinutes, locked, customersCanOnlyBuy, absolutePositions, liveEvents, value);
    }
}
