package com.capgemini.chess.user.administration.dataprovider.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;

import com.capgemini.chess.user.administration.data.UserProfileVO;
import com.capgemini.chess.user.administration.dataprovider.DataProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DataProviderImpl implements DataProvider {
	
	private static final Logger LOG = Logger.getLogger(DataProviderImpl.class);

	private final Properties restURLProperties = new Properties();
	
	public DataProviderImpl() {
		setProperties();
	}
	
	private void setProperties() {
		InputStream fileStream;
		try {
			fileStream = new FileInputStream("src/main/resources/chess.rest.urls.properties");
			restURLProperties.load(fileStream);
			fileStream.close();
			LOG.debug("Successfully loaded properties");
		} catch (IOException e) {
			LOG.debug("Error while loading chess rest properties", e);
			LOG.debug("Exiting system...");
			System.exit(1);
		}
	}

	@Override
	public List<UserProfileVO> findUserProfiles(String login, String name, String surname) throws WebApplicationException {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("login", login);
		params.add("name", name);
		params.add("surname", surname);
		ClientConfig clientConfig = new DefaultClientConfig();
		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		Client client = Client.create(clientConfig);
		WebResource webResourceGet = client.resource(restURLProperties.getProperty("chess.rest.users.search.url")).queryParams(params);
		ClientResponse response = webResourceGet.get(ClientResponse.class);
		List<UserProfileVO> list = webResourceGet.get(new GenericType<List<UserProfileVO>>(){});
		for (UserProfileVO lol : list) {
			LOG.debug(lol);
		}
		if (response.getStatus() != 200) {
			LOG.debug("Error");
			throw new WebApplicationException();
		}
		return list;
	}

	@Override
	public void deleteUserProfile(Long id) throws WebApplicationException {
		Client client = Client.create();
		WebResource webResource = client.resource(restURLProperties.getProperty("chess.rest.users.update.delete.url") + id);
		ClientResponse response = webResource.delete(ClientResponse.class);
		if (response.getStatus() != 200) {
			LOG.debug("Error");
			throw new WebApplicationException();
		}
	}

	@Override
	public UserProfileVO updateUserProfile(UserProfileVO userProfileToUpdate) {
		ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        
        UserProfileVO userProfileUpdatedCopy = new UserProfileVO();
        userProfileUpdatedCopy.setId(userProfileToUpdate.getId());
        userProfileUpdatedCopy.setName(userProfileToUpdate.getName());
        userProfileUpdatedCopy.setSurname(userProfileToUpdate.getSurname());
        userProfileUpdatedCopy.setEmail(userProfileToUpdate.getEmail());
        userProfileUpdatedCopy.setLogin(userProfileToUpdate.getLogin());
        userProfileUpdatedCopy.setPassword(userProfileToUpdate.getPassword());
        userProfileUpdatedCopy.setAboutMe(userProfileToUpdate.getAboutMe());
        userProfileUpdatedCopy.setLifeMotto(userProfileToUpdate.getLifeMotto());
        
        WebResource webResourcePost = client.resource(restURLProperties.getProperty("chess.rest.users.update.delete.url"));
        ClientResponse response = webResourcePost.type(MediaType.APPLICATION_JSON).put(ClientResponse.class, userProfileUpdatedCopy);
        UserProfileVO responseEntity = response.getEntity(UserProfileVO.class);
        
        return responseEntity;
	}
	
	
}
