package imu.iAPI.Menus.Testing;

import imu.iAPI.Menus.Text;
import imu.iAPI.Menus.Viewer;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A viewer for tests: keeps the messages it gets, and has every permission unless given a list.
 */
public final class TestViewer implements Viewer
{
    private final UUID _id;
    private final String _name;
    private final Set<String> _permissions;
    private final List<String> _messages = new ArrayList<>();

    private TestViewer(String name, Set<String> permissions)
    {
        _id = UUID.nameUUIDFromBytes(name.getBytes());
        _name = name;
        _permissions = permissions;
    }

    /**
     * A viewer with every permission.
     */
    public static TestViewer admin(String name)
    {
        return new TestViewer(name, null);
    }

    /**
     * A viewer with only these permissions.
     */
    public static TestViewer with(String name, String... permissions)
    {
        return new TestViewer(name, new HashSet<>(List.of(permissions)));
    }

    @Override
    public UUID id()
    {
        return _id;
    }

    @Override
    public String name()
    {
        return _name;
    }

    @Override
    public boolean hasPermission(String permission)
    {
        return _permissions == null || _permissions.contains(permission);
    }

    @Override
    public void message(Component text)
    {
        _messages.add(Text.plain(text));
    }

    /**
     * The messages so far, without colours.
     */
    public List<String> messages()
    {
        return Collections.unmodifiableList(_messages);
    }

    public String lastMessage()
    {
        return _messages.isEmpty() ? null : _messages.getLast();
    }

    public void clearMessages()
    {
        _messages.clear();
    }
}
