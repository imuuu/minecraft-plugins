package imu.GS.engine.shop;

/**
 * Makes a listing stockable: it keeps its slot, never expires and refills on its own.
 *
 * @param maxAmount        most units the listing holds
 * @param fillAmount       units added every refill. 0 or less = unlimited: buying never lowers the amount
 * @param fillDelayMinutes minutes between refills, 0 or less = never refills
 */
public record StockRules(int maxAmount, int fillAmount, int fillDelayMinutes)
{
    public boolean isUnlimited()
    {
        return fillAmount <= 0;
    }

    public boolean refills()
    {
        return fillAmount > 0 && fillDelayMinutes > 0;
    }
}
