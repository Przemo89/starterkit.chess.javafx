package com.capgemini.chess.user.administration.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;

import com.capgemini.chess.user.administration.data.UserProfileVO;
import com.capgemini.chess.user.administration.dataprovider.DataProvider;
import com.capgemini.chess.user.administration.model.UserProfileSearchModel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class UserProfileSearchController {
	
	private static final Logger LOG = Logger.getLogger(UserProfileSearchController.class);

	@FXML
	private TextField loginField;
	
	@FXML
	private TextField nameField;
	
	@FXML
	private TextField surnameField;
	
	@FXML
	private Button searchButton;
	
	@FXML
	private Button editButton;
	
	@FXML
	private Button deleteButton;
	
	@FXML
	private Button exitButton;
	
	@FXML
	private TableView<UserProfileVO> resultTable;
	
	@FXML
	private TableColumn<UserProfileVO, Number> idColumn;
	
	@FXML
	private TableColumn<UserProfileVO, String> loginColumn;
	
	@FXML
	private TableColumn<UserProfileVO, String> nameColumn;
	
	@FXML
	private TableColumn<UserProfileVO, String> surnameColumn;
	
	@FXML
	private TableColumn<UserProfileVO, String> emailColumn;

	
	private final UserProfileSearchModel model = new UserProfileSearchModel();

	private final DataProvider dataProvider = DataProvider.INSTANCE;
	
	public UserProfileSearchController() {
		
	}
	
	@FXML
	private void initialize() {
		LOG.debug("initialize()");
		
		/*Bind controls properties to model properties.
		 */
		loginField.textProperty().bindBidirectional(model.loginProperty());
		nameField.textProperty().bindBidirectional(model.nameProperty());
		surnameField.textProperty().bindBidirectional(model.surnameProperty());
		
		// initialization of table and binding to the model
		initializeResultTable();
		resultTable.itemsProperty().bind(model.resultProperty());
		
		// disabling edit and delete buttons if no row is selected
		editButton.disableProperty().bind(Bindings.isEmpty(resultTable.getSelectionModel().getSelectedItems()));
		deleteButton.disableProperty().bind(Bindings.isEmpty(resultTable.getSelectionModel().getSelectedItems()));
		
		searchButtonInitialization();
	}
	
	/**Disabling search button if all text fields are empty
	 */
	private void searchButtonInitialization() {
		BooleanBinding booleanBind = loginField.textProperty().isEmpty()
                .and(nameField.textProperty().isEmpty())
                          .and(surnameField.textProperty().isEmpty());
		searchButton.disableProperty().bind(booleanBind);
	}
	
	@FXML
	private void searchButtonAction(ActionEvent event) throws WebApplicationException {
		Task<List<UserProfileVO>> backgroundSearch = new Task<List<UserProfileVO>>() {
			@Override
			protected List<UserProfileVO> call() throws WebApplicationException {
				return dataProvider.findUserProfiles(loginField.getText(), nameField.getText(), surnameField.getText());
			}

			@Override
			protected void succeeded() {
				LOG.debug("succeeded() called in search method");
				List<UserProfileVO> result = getValue();
				model.setResult(new ArrayList<UserProfileVO>(result));
				resultTable.getSortOrder().clear();
			}
			
			@Override
			protected void failed() {
				LOG.error("An Error occured", this.getException());
				displayErrorBox(this.getException().getMessage());
			}
		};
		new Thread(backgroundSearch).start();
	}
	
	@FXML
	private void deleteButtonAction(ActionEvent event) throws WebApplicationException {
		long idUserProfileDeleted = resultTable.getSelectionModel().getSelectedItem().getId();
		Task<Void> backgroundDelete = new Task<Void>() {
			@Override
			protected Void call() throws WebApplicationException {
				dataProvider.deleteUserProfile(idUserProfileDeleted);
				return null;
			}
			
			@Override
			protected void failed() {
				LOG.error("An Error occured", this.getException());
				displayErrorBox(this.getException().getMessage());
			}

			@Override
			protected void succeeded() {
				LOG.debug("succeeded() in delete called");
				model.getResult().remove(resultTable.getSelectionModel().getSelectedItem());
				resultTable.getSortOrder().clear();
			}
		};
		new Thread(backgroundDelete).start();
	}
	
	@FXML
	private void editButtonAction(ActionEvent event) throws IOException {
		showUserProfileEditView();
	}
	
	private void initializeResultTable() {
		idColumn.setCellValueFactory(cellData -> new ReadOnlyLongWrapper(cellData.getValue().getId()));
		loginColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getLogin()));
		nameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getName()));
		surnameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSurname()));
		emailColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEmail()));
	}
	
	private Stage showUserProfileEditView() throws IOException {
		// REV: nowe okno lepiej utworzyc w konstruktorze
		FXMLLoader loader = new FXMLLoader(getClass()
				.getResource("/com/capgemini/chess/user/administration/view/chess-user-edit.fxml"));
		Stage stage = new Stage(StageStyle.DECORATED);
		stage.setScene(new Scene((Pane) loader.load()));
		stage.initModality(Modality.WINDOW_MODAL);
		UserProfileEditController userProfileEditController = loader.<UserProfileEditController>getController();
		userProfileEditController.initData(resultTable.getSelectionModel().getSelectedItem());
		stage.setOnHiding(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				resultTable.refresh();
			}
		});
		
		stage.show();

		return stage;
	}
	
	@FXML
	private void exitButtonAction(ActionEvent event) {
		// REV: Platform.exit(); 
		System.exit(0);
	}

	private void displayErrorBox(String errorMessage) {
		Alert alert = new Alert(AlertType.ERROR);
		// REV: teksty z bundla
		alert.setTitle("Error!");
		alert.setHeaderText("An error occured. Please try later or contact Administrator.");
		alert.setContentText(errorMessage);
		
		alert.showAndWait();
	}
}
