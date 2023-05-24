package pk.wgu.capstone.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pk.wgu.capstone.data.service.PfmService;

/**
 * This class is a service component responsible for handling security-related operations.
 */
@Component
public class SecurityService {

    private final AuthenticationContext authenticationContext;
    private static PfmService service;

    /**
     * Constructs a SecurityService with the provided AuthenticationContext.
     *
     * @param authenticationContext The AuthenticationContext for handling authentication-related operations.
     */
    public SecurityService(AuthenticationContext authenticationContext, PfmService service) {
        this.service = service;
        this.authenticationContext = authenticationContext;
    }

    /**
     * Retrieves the details of the currently authenticated user.
     *
     * @return The UserDetails of the authenticated user.
     */
    public UserDetails getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class).get();
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userPrincipal = (UserDetails) auth.getPrincipal();
            String usernameEmail = userPrincipal.getUsername();
            return service.findUserByEmail(usernameEmail).getId();
        }
        return null; // User not authenticated or user_id not available
    }

    /**
     * Logs out the currently authenticated user.
     */
    public void logout() {
        authenticationContext.logout();
    }
}
