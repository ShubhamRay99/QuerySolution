package base;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import base.dao.PublisherRepo;
import base.service.PublisherService;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	DataSource dataSource;

	// admin login class
	@Configuration
	@Order(1)
	public class AdminAuthorization extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http.antMatcher("/admin/**").authorizeRequests().anyRequest().hasRole("ADMIN")
					.and().formLogin().loginPage("/adminLogin").loginProcessingUrl("/admin/dashboard").and().csrf()
					.disable();
		}

		// for authentication
		@Autowired
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.inMemoryAuthentication().withUser("admin").password(encoder().encode("admin"))
			.roles("ADMIN");
		}
	}

	// Publisher login class
	@Configuration
	@Order(2)
	public class PublisherAuthorization extends WebSecurityConfigurerAdapter {
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			
			http.csrf().disable().httpBasic().and().authorizeRequests().antMatchers("/publisher/**")
					.hasRole("PUBLISHER").and().formLogin().loginPage("/login").loginProcessingUrl("/login");
		
		}

		// for authentication
		@Autowired
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.inMemoryAuthentication().withUser("mike.andrew2020.21@gmail.com")
			.password(encoder().encode("mike@2020"))
					.roles("PUBLISHER");
			auth.inMemoryAuthentication().withUser("ajay.ojha99@gmail.com").password(encoder().encode("ajay@99"))
			.roles("PUBLISHER");
			auth.inMemoryAuthentication().withUser("shubham.lyray@gmail.com").password(encoder().encode("shubham@99"))
			.roles("PUBLISHER");
			
//			try {
//				auth.jdbcAuthentication().dataSource(dataSource)
//						.usersByUsernameQuery("select username, password, active" + " from publisher where username=?")
//						.authoritiesByUsernameQuery("select username, authority " + "from authorities where username=?");
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}


	@Bean
	public static PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
}
