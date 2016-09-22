package com.capgemini.chess.user.administration;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		primaryStage.setTitle("Search user profile");
		/*
		 * Load screen from FXML file.
		 */
		Parent root = FXMLLoader.load(getClass().getResource("/com/capgemini/chess/user/administration/view/chess-user-administration.fxml"));

		Scene scene = new Scene(root);

		/*
		 * Set the style sheet(s) for application.
		 */
		scene.getStylesheets().add(getClass().getResource("/com/capgemini/chess/user/administration/css/application.css").toExternalForm());

		primaryStage.setScene(scene);

		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
