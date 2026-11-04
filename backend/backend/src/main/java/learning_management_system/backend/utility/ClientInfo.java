package learning_management_system.backend.utility;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;

/** The caller's IP address and user agent, for sign-in records. */
public final class ClientInfo {

    private ClientInfo() {
    }

    public static InetAddress ip(HttpServletRequest http) {
        try {
            return InetAddress.getByName(http.getRemoteAddr());
        } catch (Exception e) {
            return null;
        }
    }

    public static String userAgent(HttpServletRequest http) {
        return http.getHeader("User-Agent");
    }
}
