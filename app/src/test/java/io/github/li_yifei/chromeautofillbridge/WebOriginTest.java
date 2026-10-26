package io.github.li_yifei.chromeautofillbridge;

import org.junit.Test;
import static org.junit.Assert.*;

public class WebOriginTest {
    @Test public void localServices() {
        assertEquals("http://192.168.0.177:8080", WebOrigin.fromUrl("http://192.168.0.177:8080/login?q=1#form"));
        assertEquals("http://localhost:3000", WebOrigin.fromUrl("http://localhost:3000/"));
        assertEquals("http://nas.lan", WebOrigin.fromUrl("HTTP://NAS.LAN:80/login"));
        assertEquals("http://[::1]:8080", WebOrigin.fromUrl("http://[::1]:8080/"));
        assertEquals("http://[fd00::1]", WebOrigin.fromUrl("http://[fd00::1]:80/"));
    }

    @Test public void canonicalOrigins() {
        assertEquals("https://example.com", WebOrigin.fromUrl("HTTPS://EXAMPLE.COM:443/path"));
        assertEquals(WebOrigin.fromUrl("http://nas/one"), WebOrigin.fromUrl("http://nas:80/two"));
        assertNotEquals(WebOrigin.fromUrl("http://nas"), WebOrigin.fromUrl("https://nas"));
        assertNotEquals(WebOrigin.fromUrl("http://nas:8080"), WebOrigin.fromUrl("http://nas:8081"));
        assertNotEquals(WebOrigin.fromUrl("http://nas"), WebOrigin.fromUrl("http://other"));
        assertEquals("http://nas:443", WebOrigin.fromUrl("http://nas:443/"));
        assertEquals("https://nas:80", WebOrigin.fromUrl("https://nas:80/"));
    }

    @Test public void unsupportedOrAmbiguousUrls() {
        for (String url : new String[]{null, "", "/login", "file:///login", "about:blank",
                "data:text/html,test", "javascript:alert(1)", "ftp://nas/",
                "http://user:password@nas/", "http:///login", "http://nas:abc/",
                "http://nas:65536/", "http://nas:-1/", "http://bad host/"}) {
            assertNull(url, WebOrigin.fromUrl(url));
        }
    }
}
