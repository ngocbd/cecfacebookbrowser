package net.cec.test;
/*
 * This Java source file was generated by the Gradle 'init' task.
 */
import org.junit.Test;

import net.cec.Secret;

import static org.junit.Assert.*;

import java.util.logging.Logger;

public class ConfigTest {
    @Test public void testSecretClassVisible() {
        Logger.getAnonymousLogger().warning("Config email:"+Secret.email);
    }
}
