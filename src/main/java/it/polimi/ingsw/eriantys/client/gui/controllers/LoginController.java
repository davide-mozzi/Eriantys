package it.polimi.ingsw.eriantys.client.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController extends Controller {
	@FXML private BorderPane pane;
	@FXML private TextField username;
	@FXML private Button login;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		username.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
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
