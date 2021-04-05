import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.pistonmaster.pistonchat.PistonChat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("CommentedOutCode")
public class ChatSpamTest {
    private ServerMock server;
    private PistonChat plugin;

    @Before
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(PistonChat.class);
        server.getPluginManager().registerEvents(new TestChatListener(), plugin);
    }

    /*
    @Test
    public void spamSingle() {
        PlayerMock player = server.addPlayer();
        player.chat("Hello world!");
        player.assertSaid("Hello world!");
    }
    */

    @After
    public void tearDown() {
        MockBukkit.unmock();
    }
}
