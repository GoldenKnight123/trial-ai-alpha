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
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleRectangleClick'");
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
