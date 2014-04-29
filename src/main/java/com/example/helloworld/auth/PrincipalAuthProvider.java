package com.example.helloworld.auth;

import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import java.security.Principal;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

public class PrincipalAuthProvider<T> implements InjectableProvider<Auth, Parameter>
{
	private static final Logger			LOGGER	= LoggerFactory.getLogger(PrincipalAuthProvider.class);

	private Authenticator<Principal, T>	authenticator;

	public PrincipalAuthProvider(Authenticator<Principal, T> authenticator)
	{
		this.authenticator = authenticator;
	}

	private class PrincipalAuthInjectable extends AbstractHttpContextInjectable<T>
	{
		private boolean	required;

		public PrincipalAuthInjectable(boolean required)
		{
			this.required = required;
		}

		@Override
		public T getValue(HttpContext c)
		{
			Principal userPrincipal = c.getRequest().getUserPrincipal();

			try
			{
				Optional<T> result = authenticator.authenticate(userPrincipal);
				if (result.isPresent())
				{
					return result.get();
				}
			}
			catch (AuthenticationException e)
			{
				LOGGER.warn("Error authenticating credentials", e);
				throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
			}

			if (required)
			{
				throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("Credentials are required to access this resource.").type(MediaType.TEXT_PLAIN_TYPE).build());
			}

			return null;
		}
	}

	@Override
	public ComponentScope getScope()
	{
		return ComponentScope.PerRequest;
	}

	@Override
	public Injectable<T> getInjectable(ComponentContext ic, Auth a, Parameter c)
	{
		return new PrincipalAuthInjectable(a.required());
	}

}
