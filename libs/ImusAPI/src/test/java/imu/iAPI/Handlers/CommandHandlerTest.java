package imu.iAPI.Handlers;

import imu.iAPI.Interfaces.CommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandHandlerTest
{
    private static final CommandInterface NOTHING = new CommandInterface()
    {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
        {
            return true;
        }

        @Override
        public void FailedMsg(CommandSender sender, String msg)
        {
        }
    };

    @Test
    void theLongestSubCommandWins()
    {
        CommandHandler handler = new CommandHandler(null);
        handler.registerCmd("gs", NOTHING);
        handler.registerSubCmd("gs", "create", NOTHING);
        handler.registerSubCmd("gs", "create shop", NOTHING);

        assertEquals("gs create shop", handler.findSubCmd("gs", new String[] { "create", "shop", "Smith" }));
        assertEquals("gs create", handler.findSubCmd("gs", new String[] { "create", "npc" }));
        assertNull(handler.findSubCmd("gs", new String[] { "remove" }));
    }

    @Test
    void subCommandsAreFoundWhateverTheCase()
    {
        CommandHandler handler = new CommandHandler(null);
        handler.registerSubCmd("gs", "reset limits", NOTHING);
        assertEquals("gs reset limits", handler.findSubCmd("gs", new String[] { "RESET", "Limits", "Shop" }));
    }

    @Test
    void eachPluginsHandlerHasItsOwnCommands()
    {
        CommandHandler first = new CommandHandler(null);
        CommandHandler second = new CommandHandler(null);
        first.registerCmd("shop", NOTHING);
        first.registerSubCmd("shop", "open", NOTHING);

        assertTrue(first.exists("shop open"));
        assertFalse(second.exists("shop open"), "another plugin's handler doesn't see it");
        assertNull(second.findSubCmd("shop", new String[] { "open" }));
    }
}
