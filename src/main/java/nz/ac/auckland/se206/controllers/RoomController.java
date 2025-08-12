package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.speech.TextToSpeech;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController extends Controller {

  @FXML private Rectangle rectWitnessAI;
  @FXML private Rectangle rectDefendant;
  @FXML private Rectangle rectWitnessHuman;
  @FXML private TextArea txtaDialogue;
  @FXML private Label lblContinue;
  @FXML private Button btnGuess;

  private static boolean isFirstTimeInit = true;
  private static List<String> fixedDialogue = new ArrayList<>();
  private static int currentDialogueIndex = 0;
  private static GameStateContext context = new GameStateContext();

  /** Initializes the fixed dialogue options. */
  private static void initializeFixedDialogue() {
    fixedDialogue.add(
        "Members of the jury - human and artificial. We shall now commence the trial of INDUS-07.");
    fixedDialogue.add(
        "The defendant is an industrial supervision AI, which the prosecution claims failed to"
            + " prevent the Manukau Power Plant disaster.");
    fixedDialogue.add(
        "On the morning of June 17th 2027, an explosion occurred at the Manukau Power Plant.");
    fixedDialogue.add(
        "Fortunately, no one was killed in the incident. But two human workers who were on duty at"
            + " the time suffered injuries.");
    fixedDialogue.add(
        "Furthermore, the site suffered severe damage to its infrastructure and technology"
            + " systems.");
    fixedDialogue.add(
        "INDUS-07, the defendant, is accused of negligence, as it allegedly failed to follow safety"
            + " protocols, which would have prevented the incident.");
    fixedDialogue.add("We have two witnesses with us today.");
    fixedDialogue.add("LOGOS-09, the AI responsible for managing the plant system operation logs.");
    fixedDialogue.add(
        "And Evan, one of the human workers who were present at the time of the incident.");
    fixedDialogue.add("We will now hear what each individual has to say.");
  }

  /**
   * Creates a typewriter effect for displaying text in the dialogue area.
   *
   * @param textToDisplay the text to display with typewriter effect
   * @param delayPerCharacter delay between each character in milliseconds
   */
  public void displayTextWithTypewriterEffect(String textToDisplay, double delayPerCharacter) {
    super.displayTextWithTypewriterEffect(txtaDialogue, textToDisplay, delayPerCharacter);
  }

  /**
   * Displays text with typewriter effect using default speed (50ms per character).
   *
   * @param textToDisplay the text to display with typewriter effect
   */
  public void displayTextWithTypewriterEffect(String textToDisplay) {
    super.displayTextWithTypewriterEffect(txtaDialogue, textToDisplay, 50);
  }

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    context.setRoomController(this); // Set reference to this controller
    if (isFirstTimeInit) {
      lblContinue.setVisible(false);
      System.out.println("Initializing room view...");
      initializeFixedDialogue();
      displayTextWithTypewriterEffect(fixedDialogue.get(currentDialogueIndex));
      TextToSpeech.speak(fixedDialogue.get(currentDialogueIndex));
      currentDialogueIndex++;
      isFirstTimeInit = false;
    }
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " released");
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClick(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    context.handleRectangleClick(event, clickedRectangle.getId());
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

  @Override
  protected void onTypewriterEffectFinish() {
    // Show the continue label when the typewriter effect finishes
    lblContinue.setVisible(true);
  }

  public void displayNextLine() {
    if (currentDialogueIndex < fixedDialogue.size()) {
      lblContinue.setVisible(false);
      displayTextWithTypewriterEffect(fixedDialogue.get(currentDialogueIndex));
      TextToSpeech.speak(fixedDialogue.get(currentDialogueIndex));
      currentDialogueIndex++;
    }
  }

  /** Finishes the current typewriter effect instantly. */
  public void finishTypingInstantly() {
    finishTypewriterEffectInstantly();
  }

  public boolean isTyping() {
    return isTyping;
  }
}
