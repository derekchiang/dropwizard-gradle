package com.example.helloworld.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import com.example.helloworld.core.User;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class DevelopmentAuthenticator implements Authenticator<BasicCredentials, User>
{
	private static String	SALT	= "PlumpLindrigtTandem";
	private HashFunction	hf;

	public DevelopmentAuthenticator()
	{
		hf = Hashing.sha256();
	}

	@Override
	public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException
	{
		HashCode hc = hf.hashBytes((SALT + ":" + credentials.getPassword()).getBytes(Charsets.UTF_8));

		if (credentials.getUsername().equals("developer") && hc.toString().equals("2b6b28d4abae69648fe5173ec6c63400759d954c7d248ce3e870c7c3aee4cd7e"))
		{
			return Optional.of(new User(credentials.getUsername()));
		}

		return Optional.absent();
	}

}
