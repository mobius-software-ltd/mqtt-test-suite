package com.mobius.software.mqtt.client.api.json;

public interface ResponseData
{
	String ERROR = "ERROR";
	String SUCCESS = "SUCCESS";
	String TIMEOUT = "Request timeout";
	String INVALID_PARAMETERS = "One of the required fields is missing or invalid";
	String AUTHENTICATION_FAILURE = "Authentication failure";
	String NOT_FOUND = "Requested resource not found";
	String INTERNAL_SERVER_ERROR = "Internal server error, ";
	String UNAUTHORIZED = "Unauthorized";
	String FILE_WRITE_ERROR = "an error occured while storing file ";
	String FILE_READ_ERROR = "an error occured while reading file ";
	String FILE_DELETE_ERROR = "an error occured while deleting file ";
}
