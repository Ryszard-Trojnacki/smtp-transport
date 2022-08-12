package pl.rtprog.smtptransport.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Configuration loading tests.
 * 
 * @author Ryszard Trojnacki
 */
public class ConfigurationTests extends Assert {
	@Test
	public void loadSimpleConfiguration() throws IOException {
		var c=Configuration.load(ConfigurationTests.class.getResourceAsStream("/config1.yaml")).getSmtp();
		assertNotNull(c);
		assertEquals("host1", c.getServer());
		assertNotNull(c.getPort());
		assertEquals(25,c.getPort());
		assertEquals(SMTPEncryptionMode.SSL, c.getMode());
	}
	
	@Test
	public void defaultConfigurationTest() throws IOException {
		var c=Configuration.load(ConfigurationTests.class.getResourceAsStream("/config2.yaml")).getSmtp();
		assertNotNull(c);
		assertEquals("host2", c.getServer());
		assertEquals(SMTPEncryptionMode.NORMAL, c.getMode());
		assertEquals(25,c.getPort());
		assertEquals("ryszard.trojnacki",c.getUsername());
		assertEquals("TopSecretPassword",c.getPassword());
		
	}
	
}
