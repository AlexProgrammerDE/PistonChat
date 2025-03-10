import net.pistonmaster.pistonfilter.utils.StringHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LeetTest {
    @Test
    void leetTest() {
        String original = "test12345-/(";
        Assertions.assertNotEquals(original, StringHelper.revertLeet(original));
    }
}
