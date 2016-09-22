package com.capgemini.chess.user.administration.dataprovider;

import java.util.List;

import com.capgemini.chess.user.administration.data.UserProfileVO;
import com.capgemini.chess.user.administration.dataprovider.impl.DataProviderImpl;

public interface DataProvider {

	DataProvider INSTANCE = new DataProviderImpl();
	
	List<UserProfileVO> findUserProfiles(String login, String name, String surname);
	
	void deleteUserProfile(Long id);
	
	UserProfileVO updateUserProfile(UserProfileVO userProfileToUpdate);
}
