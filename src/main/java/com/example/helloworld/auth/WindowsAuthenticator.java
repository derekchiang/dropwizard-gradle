package com.example.helloworld.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import waffle.servlet.WindowsPrincipal;

import com.example.helloworld.core.User;
import com.google.common.base.Optional;

public class WindowsAuthenticator implements Authenticator<Principal, User>
{
	private static final Logger	LOGGER	= LoggerFactory.getLogger(WindowsAuthenticator.class);

	@Override
	public Optional<User> authenticate(Principal credentials) throws AuthenticationException
	{
		WindowsPrincipal windowsPrincipal = (WindowsPrincipal) credentials;
		String fqn = windowsPrincipal.getIdentity().getFqn();
		LOGGER.info("Windows user: " + fqn);
		return Optional.of(new User(fqn));
	}

}
