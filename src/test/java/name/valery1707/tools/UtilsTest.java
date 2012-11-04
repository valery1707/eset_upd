package name.valery1707.tools;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test
public class UtilsTest {
    @Test
    public void testByteCountForUser() {
        assertEquals(Utils.byteCountForUser(10), "10 B");
        assertEquals(Utils.byteCountForUser(100), "100 B");
        assertEquals(Utils.byteCountForUser(1000), "1000 B");
        assertEquals(Utils.byteCountForUser(2000), "1,95 KiB");
    }
}
