package learning_management_system.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Copies the verified token's claims into {@link RequestContext} for the duration of the request. */
public class RequestContextFilter extends OncePerRequestFilter {

    private static final Set<String> PLATFORM_ADMIN_ROLES = Set.of("SUPER_ADMIN", "PLATFORM_ADMIN");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                var jwt = jwtAuth.getToken();
                String tid = jwt.getClaimAsString(TokenService.CLAIM_TENANT);
                String said = jwt.getClaimAsString(TokenService.CLAIM_SUPPORT_ACCESS);
                boolean system = Boolean.TRUE.equals(jwt.getClaimAsBoolean(TokenService.CLAIM_SYSTEM_USER));
                String prole = jwt.getClaimAsString(TokenService.CLAIM_PLATFORM_ROLE);
                List<String> perms = jwt.getClaimAsStringList(TokenService.CLAIM_PERMISSIONS);
                // Platform-wide visibility only for platform admins outside a tenant; support sessions are tenant-scoped.
                boolean platformAdmin = system && tid == null && prole != null && PLATFORM_ADMIN_ROLES.contains(prole);
                RequestContext.set(new RequestContext(
                        tid == null ? null : UUID.fromString(tid),
                        UUID.fromString(jwt.getSubject()),
                        platformAdmin,
                        jwt.getClaimAsString(TokenService.CLAIM_USER_TYPE),
                        said == null ? null : UUID.fromString(said),
                        perms == null ? Set.of() : new HashSet<>(perms)));
            }
            chain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }
}
