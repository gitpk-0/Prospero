package pk.wgu.capstone.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * This class is a service component responsible for handling security-related operations.
 */
@Component
public class SecurityService {

    private final AuthenticationContext authenticationContext;

    /**
     * Constructs a SecurityService with the provided AuthenticationContext.
     *
     * @param authenticationContext The AuthenticationContext for handling authentication-related operations.
     */
    public SecurityService(AuthenticationContext authenticationContext) {
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

    /**
     * Logs out the currently authenticated user.
     */
    public void logout() {
        authenticationContext.logout();
    }
}
