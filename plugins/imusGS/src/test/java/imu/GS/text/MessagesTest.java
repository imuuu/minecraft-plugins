package imu.GS.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest
{
    @Test
    void placeholdersAreFilledIn()
    {
        assertEquals("&aSold 64 for 12.5", Messages.fill("&aSold {amount} for {price}", "amount", 64, "price", 12.5));
    }

    @Test
    void unknownPlaceholdersStay()
    {
        assertEquals("Hi {name}", Messages.fill("Hi {name}", "other", 1));
    }

    @Test
    void aMissingTextIsItsKey()
    {
        assertEquals("shop.nothing", Messages.get("shop.nothing"));
    }
}
