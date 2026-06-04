package top.ortus.timemark.backend.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserIdFormatterTest {

    @Test
    public void testFormat16() {
        Assertions.assertEquals("0000000000000123", UserIdFormatter.format16("123"));
        Assertions.assertEquals("0000000000000000", UserIdFormatter.format16("0"));
        Assertions.assertEquals("1234567890123456", UserIdFormatter.format16("1234567890123456"));
        Assertions.assertEquals("abc", UserIdFormatter.format16("abc"));
    }
}

