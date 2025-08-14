package nz.ac.auckland.se206.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class DebriefController {
  @FXML private TextArea txtaDebrief;
  @FXML private Label lblCorrect;
  @FXML private Pane debrief;

  public void fadeIn() {
    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(debrief);
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);

    fadeTransition.play();
  }

  @FXML
  public void initialize() {
    fadeIn();
  }

  public void setDebriefText(String text) {
    txtaDebrief.setText(text);
  }

  public void setCorrectLabel(String text) {
    lblCorrect.setText(text);
  }
}
