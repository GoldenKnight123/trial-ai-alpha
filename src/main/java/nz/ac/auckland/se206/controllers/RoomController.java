package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.GameTimer;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController extends Controller {

  private static boolean isFirstTimeInit = true;
  private static List<String> fixedDialogue = new ArrayList<>();
  private static int currentDialogueIndex = 0;
  private static GameStateContext context = new GameStateContext();

  /**
   * Initializes introductory dialogue for the trial. Each line is a separate item in the list. It
   * will be displayed one by one and is handled by the displayNextLine method
   */
  private static void initializeFixedDialogue() {
    // Introduction
    fixedDialogue.add(
        "Members of the jury - human and artificial. We shall now commence the trial of INDUS-07.");

    // Introducing defendant
    fixedDialogue.add(
        "The defendant is an industrial supervision AI, which the prosecution claims failed to"
            + " prevent the Greenhill Power Plant disaster.");

    // Explaining the incident
    fixedDialogue.add(
        "On the morning of June 17th 2027, an explosion occurred at the Greenhill Power Plant.");

    // Incident consequences
    fixedDialogue.add(
        "Fortunately, no one was killed in the incident. But two human workers who were on duty at"
            + " the time suffered injuries.");

    // Incident consequences
    fixedDialogue.add(
        "Furthermore, the site suffered severe damage to its infrastructure and technology"
            + " systems.");

    // What the defendant is accused of in regards to the incident
    fixedDialogue.add(
        "INDUS-07, the defendant, is accused of negligence, as it decided to increase the power"
            + " plant's output level to unsafe levels, causing the explosion.");

    // Introducing witnesses
    fixedDialogue.add("We have two witnesses with us today.");

    // Introducing LOGOS-09
    fixedDialogue.add(
        "LOGOS-09, the AI responsible for managing the plant system message and operation logs.");

    // Introducing Evan
    fixedDialogue.add(
        "And Evan, one of the human workers who were present at the time of the incident.");

    // Explaining how we will analyse the memories
    fixedDialogue.add(
        "Using the Omni-View-02, I shall analyse the memories of the witnesses and defendant to"
            + " come to a conclusion of whether or not the defendant is guilty or innocent.");
  }

  @FXML private Rectangle rectWitnessAi;
  @FXML private Rectangle rectDefendant;
  @FXML private Rectangle rectWitnessHuman;
  @FXML private TextArea txtaDialogue;
  @FXML private Label lblContinue;
  @FXML private Label lblWhoSpeaking;
  @FXML private Label lblInstructions;
  @FXML private Label lblTimer;
  @FXML private Button btnGuilty;
  @FXML private Button btnNotGuilty;
  @FXML private Arc arcTimer;
  @FXML private Pane room;

  private boolean isFading = false;
  private boolean isGuessing = false;

  public void initializeGuessingState() {
    txtaDialogue.setVisible(true);
    lblWhoSpeaking.setVisible(true);
    lblContinue.setVisible(false);
    lblInstructions.setVisible(false);
    lblTimer.setVisible(true);
    arcTimer.setVisible(true);
    GameTimer.getInstance().setMaxTime(10);
    GameTimer.getInstance().updateTimerDisplay();
    GameTimer.getInstance().start();
    GameTimer.getInstance()
        .setOnTimerExpired(
            () -> {
              try {
                onChooseGuilty(null);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
    fixedDialogue.clear();
    fixedDialogue.add("I have finished analysing the memories of the witness and defendants.");
    fixedDialogue.add("I shall now decide if defendant is GUILTY or NOT GUILTY.");
    currentDialogueIndex = 0;
    displayTextWithTypewriterEffect(
        txtaDialogue, fixedDialogue.get(currentDialogueIndex), 50, true, true);
    currentDialogueIndex++;
    isGuessing = true;
  }

  public void fadeIn() {
    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(room);
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);

    fadeTransition.setOnFinished(
        e -> {
          isFading = false; // Reset flag when fade completes
          if (GameTimer.getInstance().getTimeLeft() <= 0) {
            context.setState(context.getGuessingState());
          }
        });

    fadeTransition.play();
  }

  /** Ensures the room is visible when returning to it. */
  public void ensureVisible() {
    room.setOpacity(1.0);
  }

  public void fadeOut(MouseEvent event, String target) {
    // Prevent starting a new fade if one is already in progress
    if (isFading) {
      return;
    }

    isFading = true; // Set flag to prevent further fade operations

    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(room);
    fadeTransition.setFromValue(1.0);
    fadeTransition.setToValue(0.0);

    fadeTransition.setOnFinished(
        e -> {
          isFading = false; // Reset flag when fade completes
          try {
            App.openChat(event, target);
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        });
    fadeTransition.play();
  }

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    fadeIn();
    context.setRoomController(this); // Set reference to this controller

    lblTimer.setAlignment(Pos.CENTER);

    lblTimer.setVisible(true);
    arcTimer.setVisible(true);
    GameTimer.getInstance().start();

    btnGuilty.setVisible(false);
    btnNotGuilty.setVisible(false);

    // Register timer label and start timer if this is the first initialization
    GameTimer.getInstance().registerTimerLabel(lblTimer);
    GameTimer.getInstance().registerTimerArc(arcTimer);

    if (isFirstTimeInit) {
      lblContinue.setVisible(false);
      lblInstructions.setVisible(false);
      initializeFixedDialogue();
      displayTextWithTypewriterEffect(
          txtaDialogue, fixedDialogue.get(currentDialogueIndex), 50, true, true);
      currentDialogueIndex++;
      isFirstTimeInit = false;
    }
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClick(MouseEvent event) throws IOException {
    // Prevent rectangle clicks if fade animation is playing
    if (isFading) {
      System.out.println("Cannot click rectangle: Fade animation is in progress");
      return;
    }

    // If fixed dialogue is finished and the user clicks on a rectangle, take them to the chat scene
    // with the clicked individual as the flashback focus
    if (currentDialogueIndex >= fixedDialogue.size()) {
      context.handleRectangleClick(event, ((Rectangle) event.getSource()).getId());
    }
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    context.handleGuessClick();
  }

  /**
   * Handles mouse clicks anywhere in the game area.
   *
   * @param event the mouse event triggered by clicking in the game area
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGeneralClick(MouseEvent event) throws IOException {
    context.handleGeneralClick();
  }

  /**
   * Handles mouse click on GUILTY button
   *
   * @param event the mouse event triggered by clicking in the game area
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onChooseGuilty(ActionEvent event) throws IOException {
    App.openDebrief(null, false);
  }

  /**
   * Handles mouse click on GUILTY button
   *
   * @param event the mouse event triggered by clicking in the game area
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onChooseNotGuilty(ActionEvent event) throws IOException {
    App.openDebrief(null, true);
  }

  @Override
  protected void onTypewriterEffectFinish() {
    // Show the continue label when the typewriter effect finishes
    System.out.println("finished line");
    lblContinue.setVisible(true);
  }

  /**
   * Display the next line of fixed dialogue, if all fixed dialogue has been consumed then it will
   * trigger the different changes to the scene based on which state the user is currently in.
   */
  public void displayNextLine() {
    if (currentDialogueIndex < fixedDialogue.size()) {
      lblContinue.setVisible(false);
      displayTextWithTypewriterEffect(
          txtaDialogue, fixedDialogue.get(currentDialogueIndex), 50, true, true);
      currentDialogueIndex++;
    } else if (!isGuessing) {
      lblContinue.setVisible(false);
      txtaDialogue.setVisible(false);
      lblWhoSpeaking.setVisible(false);
      lblInstructions.setVisible(true);
      context.setState(context.getGameStartedState());
    } else {
      lblContinue.setVisible(false);
      txtaDialogue.setVisible(false);
      lblWhoSpeaking.setVisible(false);
      btnGuilty.setVisible(true);
      btnNotGuilty.setVisible(true);
    }
  }

  /** Finishes the current typewriter effect instantly. */
  public void finishTypingInstantly() {
    finishTypewriterEffectInstantly();
  }

  public Pane getRoom() {
    return room;
  }

  public GameStateContext getContext() {
    return context;
  }

  public Boolean isGuessing() {
    return isGuessing;
  }
}
