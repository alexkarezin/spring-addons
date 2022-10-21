/*
 * Copyright 2019 Jérôme Wacongne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.c4_soft.springaddons.samples.webflux_jwtauthenticationtoken;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockBearerTokenAuthentication;
import com.c4_soft.springaddons.security.oauth2.test.webflux.WebTestClientSupport;
import com.c4_soft.springaddons.security.oauth2.test.webflux.jwt.AutoConfigureAddonsWebSecurity;

import reactor.core.publisher.Mono;

/**
 * <h2>Unit-test a secured controller</h2>
 *
 * @author Jérôme Wacongne &lt;ch4mp&#64;c4-soft.com&gt;
 */

@WebFluxTest(GreetingController.class) // Use WebFluxTest or WebMvcTest
@AutoConfigureAddonsWebSecurity // If your web-security depends on it, setup spring-addons security
@Import({ SecurityConfig.class }) // Import your web-security configuration
class GreetingControllerAnnotatedTest {

	// Mock controller injected dependencies
	@MockBean
	private MessageService messageService;

	@Autowired
	WebTestClientSupport api;

	@BeforeEach
	public void setUp() {
		when(messageService.greet(any())).thenAnswer(invocation -> {
			final BearerTokenAuthentication auth = invocation.getArgument(0, BearerTokenAuthentication.class);
			return Mono.just(String.format("Hello %s! You are granted with %s.", auth.getName(), auth.getAuthorities()));
		});
		when(messageService.getSecret()).thenReturn(Mono.just("Secret message"));
	}

	@Test
	void greetWitoutAuthentication() throws Exception {
		api.get("https://localhost/greet").expectStatus().isUnauthorized();
	}

	@Test
	@WithMockAuthentication(authType = BearerTokenAuthentication.class, principalType = OAuth2AccessToken.class, authorities = "ROLE_AUTHORIZED_PERSONNEL")
	void greetWithDefaultMockAuthentication() throws Exception {
		api.get("https://localhost/greet").expectBody(String.class).isEqualTo("Hello user! You are granted with [ROLE_AUTHORIZED_PERSONNEL].");
	}

	@Test
	@WithMockBearerTokenAuthentication()
	void greetWithDefaultAuthentication() throws Exception {
		api.get("https://localhost/greet").expectBody(String.class).isEqualTo("Hello user! You are granted with [ROLE_USER].");
	}

	@Test
	@WithMockAuthentication(
			authType = BearerTokenAuthentication.class,
			principalType = OAuth2AccessToken.class,
			name = "Ch4mpy",
			authorities = "ROLE_AUTHORIZED_PERSONNEL")
	void greetMockCh4mpy() throws Exception {
		api.get("https://localhost/greet").expectBody(String.class).isEqualTo("Hello Ch4mpy! You are granted with [ROLE_AUTHORIZED_PERSONNEL].");
	}

	@Test
	@WithMockBearerTokenAuthentication(authorities = "ROLE_AUTHORIZED_PERSONNEL", attributes = @OpenIdClaims(sub = "Ch4mpy"))
	void greetCh4mpy() throws Exception {
		api.get("https://localhost/greet").expectBody(String.class).isEqualTo("Hello Ch4mpy! You are granted with [ROLE_AUTHORIZED_PERSONNEL].");
	}

	@Test
	@WithMockAuthentication(authType = BearerTokenAuthentication.class, principalType = OAuth2AccessToken.class)
	void securedRouteWithoutAuthorizedPersonnelIsForbidden() throws Exception {
		api.get("https://localhost/secured-route").expectStatus().isForbidden();
	}

	@Test
	@WithMockAuthentication(authType = BearerTokenAuthentication.class, principalType = OAuth2AccessToken.class, authorities = "ROLE_AUTHORIZED_PERSONNEL")
	void securedRouteWithAuthorizedPersonnelIsOk() throws Exception {
		api.get("https://localhost/secured-route").expectStatus().isOk();
	}

	@Test
	@WithMockAuthentication(authType = BearerTokenAuthentication.class, principalType = OAuth2AccessToken.class)
	void securedMethodWithoutAuthorizedPersonnelIsForbidden() throws Exception {
		api.get("https://localhost/secured-method").expectStatus().isForbidden();
	}

	@Test
	@WithMockAuthentication(authType = BearerTokenAuthentication.class, principalType = OAuth2AccessToken.class, authorities = "ROLE_AUTHORIZED_PERSONNEL")
	void securedMethodWithAuthorizedPersonnelIsOk() throws Exception {
		api.get("https://localhost/secured-method").expectStatus().isOk();
	}
}