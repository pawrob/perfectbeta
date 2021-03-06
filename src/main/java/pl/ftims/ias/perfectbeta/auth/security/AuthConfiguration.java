package pl.ftims.ias.perfectbeta.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pl.ftims.ias.perfectbeta.auth.service.AuthUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableTransactionManagement
@EnableGlobalMethodSecurity(securedEnabled = true)
public class AuthConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthUserDetailsService userDetailsService;
    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    @Autowired
    private JwtAuthExceptionHandler jwtAuthExceptionHandler;


    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .antMatchers("/users/request_reset_password").permitAll()
                .antMatchers("/users/reset_password").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/actuator").permitAll()
                .antMatchers("/users/verify").permitAll()
                .antMatchers("/users/token_verify").permitAll()
                .antMatchers("/users/register").permitAll()
                .antMatchers("/auth/refreshtoken").hasAnyRole("CLIMBER", "MANAGER", "ADMINISTRATOR")
                .antMatchers("/auth/authenticate").permitAll()
                .antMatchers("/gym/verified/**").permitAll()
                .antMatchers("/route/**").permitAll().anyRequest().authenticated()
                .and().exceptionHandling().authenticationEntryPoint(jwtAuthExceptionHandler)
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }


}
