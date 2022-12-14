package it.polimi.ingsw.eriantys.client.gui.controllers;

import it.polimi.ingsw.eriantys.client.gui.SceneName;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * A class representing the controller for the {@code LOGIN} scene.
 * @see SceneName#LOGIN
 * @see javafx.scene.Scene
 */
public class LoginController extends Controller {
	@FXML private BorderPane pane;
	@FXML private TextField username;
	@FXML private Button login;
	@FXML private ImageView cranio, polimi;

	/**
	 * Initializes all the copyright images for the scene from the resource files.
	 * Associates the event handler with the button on the scene.
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		username.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.BACK_SPACE) {
				return;
			}
			if (event.getCode() != KeyCode.ENTER) {
				event.consume();
				return;
			}
			sendHandshake();
			event.consume();
		});

		login.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			this.sendHandshake();
			event.consume();
		});

		cranio.setImage(new Image(getClass().getResource("/copyright/cranio_creations.png").toExternalForm()));
		polimi.setImage(new Image(getClass().getResource("/copyright/polimi.png").toExternalForm()));
	}

	@Override
	public Pane getTopLevelPane() {
		return pane;
	}

	private void sendHandshake() {
		String chosenUsername = username.getText();
		client.sendHandshake(chosenUsername);
	}
}
