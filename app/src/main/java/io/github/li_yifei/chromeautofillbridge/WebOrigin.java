package io.github.li_yifei.chromeautofillbridge;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Canonical HTTP(S) origin shared by structure capture and fill-time checks. */
final class WebOrigin {
    private WebOrigin() {}

    static String fromUrl(String spec) {
        if (spec == null) return null;
        try {
            URI uri = new URI(spec);
            String scheme = uri.getScheme();
            if (scheme == null) return null;
            scheme = scheme.toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) return null;
            if (uri.getHost() == null || uri.getRawUserInfo() != null) return null;
            int port = uri.getPort();
            if (port < -1 || port > 65535) return null;
            int defaultPort = scheme.equals("http") ? 80 : 443;
            return scheme + "://" + uri.getHost().toLowerCase(Locale.ROOT)
                    + (port == -1 || port == defaultPort ? "" : ":" + port);
        } catch (URISyntaxException ignored) {
            return null;
        }
    }
}
