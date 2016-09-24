package com.capgemini.chess.user.administration.controller;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.capgemini.chess.user.administration.data.UserProfileVO;
import com.capgemini.chess.user.administration.dataprovider.DataProvider;
import com.capgemini.chess.user.administration.model.UserProfileEditModel;

import javafx.beans.binding.BooleanBinding;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserProfileEditController {

	private static final Logger LOG = Logger.getLogger(UserProfileEditController.class);

	@FXML
	private TextField nameField;

	@FXML
	private TextField surnameField;

	@FXML
	private TextField emailField;

	@FXML
	private TextField passwordField;

	@FXML
	private TextArea aboutMeArea;

	@FXML
	private TextArea lifeMottoArea;

	@FXML
	private Button saveButton;

	@FXML
	private Button cancelButton;

	private final UserProfileEditModel model = new UserProfileEditModel();
	
	private final DataProvider dataProvider = DataProvider.INSTANCE;

	public UserProfileEditController() {

	}
	
	private void saveButtonInitialization() {
		BooleanBinding booleanBind = emailField.textProperty().isEmpty().or(nameField.textProperty().isEmpty())
                          .or(surnameField.textProperty().isEmpty()).or(passwordField.textProperty().isEmpty());
		saveButton.disableProperty().bind(booleanBind);
	}

	public void initData(UserProfileVO userProfile) {
		model.setEditedObject(userProfile);
		model.setName(new String(userProfile.getName()));
		model.setSurname(new String(userProfile.getSurname()));
		model.setId(new Long(userProfile.getId()));
		model.setEmail(new String(userProfile.getEmail()));
		model.setLogin(new String(userProfile.getLogin()));
		model.setPassword(new String(userProfile.getPassword()));
		model.setLifeMotto(new String(userProfile.getLifeMotto()));
		model.setAboutMe(new String(userProfile.getAboutMe()));
	}

	@FXML
	private void initialize() {
		LOG.debug("initialize()");

		nameField.textProperty().bindBidirectional(model.nameProperty());
		surnameField.textProperty().bindBidirectional(model.surnameProperty());
		emailField.textProperty().bindBidirectional(model.emailProperty());
		passwordField.textProperty().bindBidirectional(model.passwordProperty());
		aboutMeArea.textProperty().bindBidirectional(model.aboutMeProperty());
		lifeMottoArea.textProperty().bindBidirectional(model.lifeMottoProperty());
		
		saveButtonInitialization();
	}

	@FXML
	private void saveButtonAction(ActionEvent event) throws IOException {
		UserProfileVO userProfileCopy = copyUserProfile();
		Task<UserProfileVO> backgroundUpdate = new Task<UserProfileVO>() {
			@Override
			protected UserProfileVO call() throws Exception {
				return dataProvider.updateUserProfile(userProfileCopy);
			}
			@Override
			protected void succeeded() {
				LOG.debug("succeeded() called in save method");
				// REV: czy twoj serwer zmienia jakos dane?
				saveChanges(this.getValue());
				Node source = (Node) event.getSource();
				Stage stage = (Stage) source.getScene().getWindow();
				stage.close();
			}
			
			@Override
			protected void failed() {
				// REV: lepiej przekazac pelny obiekt exception
				LOG.error("An Error occured", this.getException());
				Alert alert = new Alert(AlertType.ERROR);
				// REV: teksty z bundla
				alert.setTitle("Error!");
				alert.setHeaderText("An error occured. Please try later or contact Administrator.");
				alert.setContentText(this.getException().getMessage());
				alert.showAndWait();
			}
		};
		new Thread(backgroundUpdate).start();
	}
	
	@FXML
	private void cancelButtonAction(ActionEvent event) {
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}
	
	private UserProfileVO copyUserProfile() {
        UserProfileVO userProfileCopy = new UserProfileVO();
        userProfileCopy.setId(new Long(model.getEditedObject().getId()));
        userProfileCopy.setName(nameField.getText());
        userProfileCopy.setSurname(surnameField.getText());
        userProfileCopy.setEmail(emailField.getText());
        userProfileCopy.setLogin(new String(model.getEditedObject().getLogin()));
        userProfileCopy.setPassword(passwordField.getText());
        userProfileCopy.setAboutMe(aboutMeArea.getText());
        userProfileCopy.setLifeMotto(lifeMottoArea.getText());
		return userProfileCopy;
	}
	
	private void saveChanges(UserProfileVO userProfileUpdated) {
		model.getEditedObject().setName(userProfileUpdated.getName());
		model.getEditedObject().setSurname(userProfileUpdated.getSurname());
		model.getEditedObject().setEmail(userProfileUpdated.getEmail());
		model.getEditedObject().setPassword(userProfileUpdated.getPassword());
		model.getEditedObject().setAboutMe(userProfileUpdated.getAboutMe());
		model.getEditedObject().setLifeMotto(userProfileUpdated.getLifeMotto());
	}

}
