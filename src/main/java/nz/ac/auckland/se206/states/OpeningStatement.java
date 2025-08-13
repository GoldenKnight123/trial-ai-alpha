package nz.ac.auckland.se206.states;

import java.io.IOException;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.controllers.RoomController;

public class OpeningStatement implements GameState {

  private final GameStateContext context;

  /**
   * Constructs a new OpeningStatement state with the given game state context.
   *
   * @param context the context of the game state
   */
  public OpeningStatement(GameStateContext context) {
    this.context = context;
  }

  @Override
  public void handleRectangleClick(MouseEvent event, String rectangleId) throws IOException {
    // Take the user to the chat scene with the clicked individual as the flashback focus
    RoomController roomController = context.getRoomController();

    System.out.println("Clicked rectangle ID: " + rectangleId);

    switch (rectangleId) {
      case "rectWitnessAI":
        System.out.println("Clicked on AI witness rectangle");
        roomController.fadeOut(event, "LOGOS-09");
        break;

      case "rectWitnessHuman":
        System.out.println("Clicked on Human witness rectangle");
        roomController.fadeOut(event, "Evan");
        break;

      case "rectDefendant":
        System.out.println("Clicked on Defendant rectangle");
        roomController.fadeOut(event, "INDUS-07");
        break;
    }
  }

  @Override
  public void handleGuessClick() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleGuessClick'");
  }

  @Override
  public void handleGeneralClick() throws IOException {
    RoomController roomController = context.getRoomController();
    if (roomController.isTyping()) {
      roomController.finishTypingInstantly();
    } else {
      roomController.displayNextLine();
    }
  }
}
