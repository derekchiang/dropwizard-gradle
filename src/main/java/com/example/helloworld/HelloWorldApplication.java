package com.example.helloworld;

import io.dropwizard.Application;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.EnumSet;
import java.util.logging.Level;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;

import org.eclipse.jetty.server.session.SessionHandler;

import waffle.servlet.NegotiateSecurityFilter;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;

import com.example.helloworld.auth.DevelopmentAuthenticator;
import com.example.helloworld.auth.PrincipalAuthProvider;
import com.example.helloworld.auth.WindowsAuthenticator;
import com.example.helloworld.core.User;
import com.example.helloworld.health.TemplateHealthCheck;
import com.example.helloworld.resources.HelloWorldResource;

public class HelloWorldApplication extends Application<HelloWorldConfiguration>
{
	public static void main(String[] args) throws Exception
	{
		new HelloWorldApplication().run(args);
	}

	@Override
	public String getName()
	{
		return "hello-world";
	}

	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap)
	{
		// nothing to do yet
	}

	@Override
	public void run(HelloWorldConfiguration configuration, Environment environment)
	{
		enableAuthentication(environment);

		final HelloWorldResource resource = new HelloWorldResource(configuration.getTemplate(), configuration.getDefaultName());
		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
		environment.healthChecks().register("template", healthCheck);
		environment.jersey().register(resource);
	}

	private void enableAuthentication(Environment environment)
	{
		if (isOsWindows())
		{
			enableSingleSignOn(environment);
		}
		else
		{
			enableDeveloperLogin(environment);
		}
	}

	private void enableSingleSignOn(Environment environment)
	{
		environment.jersey().register(new PrincipalAuthProvider<User>(new WindowsAuthenticator()));

		NegotiateSecurityFilter negotiateSecurityFilter = new NegotiateSecurityFilter();
		negotiateSecurityFilter.setAuth(new WindowsAuthProviderImpl());
		try
		{
			negotiateSecurityFilter.init(null);
		}
		catch (ServletException e)
		{
			throw new RuntimeException(e);
		}

		// NegotiateSecurityFilter require sessions to be enabled!
		environment.servlets().setSessionHandler(new SessionHandler());
		environment.servlets().addFilter("SecurityFilter", negotiateSecurityFilter).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

		java.util.logging.Logger logger = java.util.logging.Logger.getLogger("waffle.servlet.NegotiateSecurityFilter");
		logger.setLevel(Level.SEVERE);
		logger = java.util.logging.Logger.getLogger("waffle.servlet.spi.NegotiateSecurityFilterProvider");
		logger.setLevel(Level.SEVERE);
	}

	private void enableDeveloperLogin(Environment environment)
	{
		environment.jersey().register(new BasicAuthProvider<>(new DevelopmentAuthenticator(), "Development Environment"));
	}

	public static boolean isOsWindows()
	{
		return System.getProperty("os.name").startsWith("Windows");
	}

}