package com.c4_soft.springaddons.samples.webmvc_jwtauthenticationtoken;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.c4_soft.springaddons.security.oauth2.UnmodifiableClaimSet;
import com.c4_soft.springaddons.security.oauth2.config.OAuth2AuthoritiesConverter;
import com.c4_soft.springaddons.security.oauth2.config.synchronised.ExpressionInterceptUrlRegistryPostProcessor;
import com.c4_soft.springaddons.security.oauth2.config.synchronised.ServletSecurityBeans.Jwt2AuthenticationConverter;

@SpringBootApplication
public class SampleApi {
	public static void main(String[] args) {
		new SpringApplicationBuilder(SampleApi.class).web(WebApplicationType.SERVLET).run(args);
	}

	@EnableGlobalMethodSecurity(prePostEnabled = true)
	public static class WebSecurityConfig {
		@Bean
		ExpressionInterceptUrlRegistryPostProcessor expressionInterceptUrlRegistryPostProcessor() {
			return (ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) -> registry.antMatchers("/secured-route")
					.hasRole("AUTHORIZED_PERSONNEL").anyRequest().authenticated();
		}

		@Bean
		Jwt2AuthenticationConverter<JwtAuthenticationToken> authenticationConverter(OAuth2AuthoritiesConverter authoritiesConverter) {
			return jwt -> new JwtAuthenticationToken(jwt, authoritiesConverter.convert(new UnmodifiableClaimSet(jwt.getClaims())));
		}
	}
}
