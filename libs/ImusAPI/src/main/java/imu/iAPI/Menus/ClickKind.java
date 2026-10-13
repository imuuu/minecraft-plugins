package imu.iAPI.Menus;

/**
 * How a slot was clicked.
 */
public enum ClickKind
{
    LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT, MIDDLE, DOUBLE_CLICK, NUMBER_KEY, DROP, CONTROL_DROP, SWAP_OFFHAND, OTHER;

    public boolean isLeft()
    {
        return this == LEFT || this == SHIFT_LEFT || this == DOUBLE_CLICK;
    }

    public boolean isRight()
    {
        return this == RIGHT || this == SHIFT_RIGHT;
    }

    public boolean isShift()
    {
        return this == SHIFT_LEFT || this == SHIFT_RIGHT;
    }
}
