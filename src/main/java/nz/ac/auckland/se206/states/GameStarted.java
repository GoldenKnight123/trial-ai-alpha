package nz.ac.auckland.se206.states;

import java.io.IOException;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.GameTimer;
import nz.ac.auckland.se206.controllers.RoomController;

/**
 * The GameStarted state of the game. Handles the initial interactions when the game starts,
 * allowing the player to chat with characters and prepare to make a guess.
 */
public class GameStarted implements GameState {

  private final GameStateContext context;

  /**
   * Constructs a new GameStarted state with the given game state context.
   *
   * @param context the context of the game state
   */
  public GameStarted(GameStateContext context) {
    this.context = context;
  }

  /**
   * Handles the event when a rectangle is clicked. Depending on the clicked rectangle, it either
   * provides an introduction or transitions to the chat view.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @param rectangleId the ID of the clicked rectangle
   * @throws IOException if there is an I/O error
   */
  @Override
  public void handleRectangleClick(MouseEvent event, String rectangleId) throws IOException {
    if (GameTimer.getInstance().getTimeLeft() <= 2) {
      return;
    }

    // Take the user to the chat scene with the clicked individual as the flashback focus
    RoomController roomController = context.getRoomController();

    switch (rectangleId) {
      case "rectWitnessAi":
        roomController.fadeOut(event, "LOGOS-09");
        break;

      case "rectWitnessHuman":
        roomController.fadeOut(event, "Evan");
        break;

      case "rectDefendant":
        roomController.fadeOut(event, "INDUS-07");
        break;
    }
  }

  /**
   * Handles the event when the guess button is clicked. Prompts the player to make a guess and
   * transitions to the guessing state.
   *
   * @throws IOException if there is an I/O error
   */
  @Override
  public void handleGuessClick() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleGuessClick'");
  }

  @Override
  public void handleGeneralClick() throws IOException {
    // TODO Auto-generated method stub
    System.out.println("General click handled");
  }
}
