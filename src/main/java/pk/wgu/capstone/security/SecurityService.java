package pk.wgu.capstone.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pk.wgu.capstone.data.service.PfmService;


@Component
@Service
public class SecurityService {

    private final AuthenticationContext authenticationContext;

    public SecurityService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public UserDetails getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class).get();
    }

    public Long getCurrentUserId(PfmService service) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userPrincipal = (UserDetails) auth.getPrincipal();
            String usernameEmail = userPrincipal.getUsername();
            return service.findUserByEmail(usernameEmail).getId();
        } else {
            logout();
            return null; // User not authenticated or user_id not available
        }
    }

    public void logout() {
        authenticationContext.logout();
    }
}
