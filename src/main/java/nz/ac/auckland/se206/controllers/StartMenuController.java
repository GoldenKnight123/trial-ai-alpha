package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;

public class StartMenuController {
  @FXML private Button btnStartGame;

  @FXML
  private void onStart(ActionEvent event) throws ApiProxyException, IOException {
    App.openRoom(null);
  }
}
