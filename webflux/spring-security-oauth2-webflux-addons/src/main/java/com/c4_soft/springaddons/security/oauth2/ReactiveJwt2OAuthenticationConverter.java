/*
 * Copyright 2020 Jérôme Wacongne
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
package com.c4_soft.springaddons.security.oauth2;

import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;

import com.c4_soft.springaddons.security.oauth2.config.Jwt2AuthoritiesConverter;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * <p>
 * Turn a JWT into a spring-security Authentication instance.
 * </p>
 * Sample configuration for Keyclkoak, getting roles from "realm_access" claim:
 *
 * <pre>
 * &#64;Bean
 * public ReactiveJwt2GrantedAuthoritiesConverter authoritiesConverter() {
 * 	return (var jwt) -&gt; {
 * 		final var roles =
 * 				Optional
 * 						.ofNullable((JSONObject) jwt.getClaims().get("realm_access"))
 * 						.flatMap(realmAccess -&gt; Optional.ofNullable((JSONArray) realmAccess.get("roles")))
 * 						.orElse(new JSONArray());
 * 		return Flux.fromStream(roles.stream().map(Object::toString).map(role -&gt; new SimpleGrantedAuthority("ROLE_" + role)));
 * 	};
 * }
 *
 * &#64;Bean
 * public ReactiveJwt2OidcIdAuthenticationConverter authenticationConverter(ReactiveJwt2GrantedAuthoritiesConverter authoritiesConverter) {
 * 	return new ReactiveJwt2OidcIdAuthenticationConverter(authoritiesConverter);
 * }
 * </pre>
 *
 * @author ch4mp@c4-soft.com
 */
@RequiredArgsConstructor
public class ReactiveJwt2OAuthenticationConverter<T extends OpenidClaimSet> implements ReactiveJwt2AuthenticationConverter<OAuthentication<T>> {

	private final Jwt2AuthoritiesConverter authoritiesConverter;
	private final ReactiveJwt2OpenidClaimSetConverter<T> tokenConverter;

	@Override
	public Mono<OAuthentication<T>> convert(Jwt jwt) {
		return Optional
				.ofNullable(tokenConverter.convert(jwt))
				.orElse(Mono.empty())
				.map(token -> new OAuthentication<>(token, authoritiesConverter.convert(jwt), jwt.getTokenValue()));
	}
}