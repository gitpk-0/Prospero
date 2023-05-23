package pk.wgu.capstone.security;


import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import pk.wgu.capstone.views.LoginView;

/**
 * This class is a configuration class for Spring Security in a Vaadin web application.
 * It extends the VaadinWebSecurity class, which provides default security configurations for Vaadin applications.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    /**
     * Configures the HTTP security settings, including the login view.
     * Calls the superclass method to inherit the default configurations.
     *
     * @param http The HttpSecurity object to configure.
     * @throws Exception if an error occurs while configuring the security.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests().requestMatchers("/h2-console/**").permitAll();

        super.configure(http);

        setLoginView(http, LoginView.class); // Set the custom login view for the application

        http.csrf().disable();
        http.headers().frameOptions().disable();
    }

    /**
     * Configures the web security settings, allowing certain requests to be ignored.
     * Calls the superclass method to inherit the default configurations.
     *
     * @param web The WebSecurity object to configure.
     * @throws Exception if an error occurs while configuring the security.
     */
    @Override
    protected void configure(WebSecurity web) throws Exception {
        web.ignoring().requestMatchers("/images/**"); // ignore these requests
        super.configure(web);
    }

    /**
     * Defines the UserDetailsService bean, which provides user details for authentication.
     * In this method, an in-memory user with a username, password, and role is created.
     * The password is stored as plain text using the "{noop}" prefix, which means no encryption or hashing is applied.
     *
     * @return The UserDetailsService bean.
     */
    @Bean
    protected UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("test")
                        .password("{noop}testpass")
                        .roles("USER")
                        .build(),
                User.withUsername("admin")
                        .password("{noop}admin")
                        .roles("ADMIN")
                        .build()
        );
    }
}

