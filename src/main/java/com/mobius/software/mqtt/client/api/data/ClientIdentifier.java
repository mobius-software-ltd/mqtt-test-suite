package com.mobius.software.mqtt.client.api.data;

public class ClientIdentifier
{
	private String username;
	private String password;
	private String identifier;

	public ClientIdentifier()
	{

	}

	public ClientIdentifier(String username, String password, String identifier)
	{
		this.username = username;
		this.password = password;
		this.identifier = identifier;
	}

	public boolean validate()
	{
		return username != null && password != null && identifier != null;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientIdentifier other = (ClientIdentifier) obj;
		if (identifier == null)
		{
			if (other.identifier != null)
				return false;
		}
		else if (!identifier.equals(other.identifier))
			return false;
		if (password == null)
		{
			if (other.password != null)
				return false;
		}
		else if (!password.equals(other.password))
			return false;
		if (username == null)
		{
			if (other.username != null)
				return false;
		}
		else if (!username.equals(other.username))
			return false;
		return true;
	}
}
