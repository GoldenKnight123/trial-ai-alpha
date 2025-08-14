package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import nz.ac.auckland.se206.controllers.ChatController;
import nz.ac.auckland.se206.controllers.DebriefController;
import nz.ac.auckland.se206.controllers.RoomController;

/**
 * This is the entry point of the JavaFX application. This class initializes and runs the JavaFX
 * application.
 */
public class App extends Application {

  private static Scene scene;
  private static Stage primaryStage;

  // Cache for storing loaded scenes and their controllers
  private static Map<String, Scene> sceneCache = new HashMap<>();
  private static Map<String, Object> controllerCache = new HashMap<>();

  /** Inner class to hold both scene and controller together */
  private static class SceneControllerPair {
    private final Scene scene;
    private final Object controller;

    public SceneControllerPair(Scene scene, Object controller) {
      this.scene = scene;
      this.controller = controller;
    }

    public Scene getScene() {
      return scene;
    }

    public Object getController() {
      return controller;
    }
  }

  /**
   * The main method that launches the JavaFX application.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    launch();
  }

  /**
   * Loads and caches a scene if not already cached, then returns the cached scene.
   *
   * @param fxml the name of the FXML file (without extension)
   * @return the cached scene
   * @throws IOException if the FXML file is not found
   */
  private static SceneControllerPair loadAndCacheScene(String fxml) throws IOException {
    String cacheKey = fxml;

    if (!sceneCache.containsKey(cacheKey)) {
      System.out.println("loading");
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      Parent root = loader.load();
      Object controller = loader.getController();
      Scene newScene = new Scene(root);

      sceneCache.put(cacheKey, newScene);
      controllerCache.put(cacheKey, controller);
    }

    return new SceneControllerPair(sceneCache.get(cacheKey), controllerCache.get(cacheKey));
  }

  /**
   * Gets a cached controller for the specified FXML file.
   *
   * @param fxml the name of the FXML file (without extension)
   * @return the controller instance, or null if not cached
   */
  public static Object getController(String fxml) {
    return controllerCache.get(fxml);
  }

  /**
   * Sets the root of the scene to the specified FXML file using cached scenes.
   *
   * @param fxml the name of the FXML file (without extension)
   * @throws IOException if the FXML file is not found
   */
  public static void setRoot(String fxml) throws IOException {
    SceneControllerPair pair = loadAndCacheScene(fxml);
    scene = pair.getScene();
    primaryStage.setScene(scene);
  }

  /**
   * Opens the chat view and sets the profession in the chat controller using cached scenes.
   *
   * @param event the mouse event that triggered the method
   * @param profession the profession to set in the chat controller
   * @throws IOException if the FXML file is not found
   */
  public static void openChat(MouseEvent event, String target) throws IOException {
    SceneControllerPair pair = loadAndCacheScene("chat");
    ChatController chatController = (ChatController) pair.getController();
    chatController.setTarget(target);
    chatController.fadeIn();

    scene = pair.getScene();
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void openRoom(ActionEvent event) throws IOException {
    SceneControllerPair pair = loadAndCacheScene("room");
    System.out.println("Opening room scene...");

    // Ensure the room is visible when returning to it
    RoomController roomController = (RoomController) pair.getController();
    roomController.fadeIn();

    scene = pair.getScene();
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void openDebrief(ActionEvent event, boolean correct) throws IOException {
    SceneControllerPair pair = loadAndCacheScene("debrief");
    System.out.println("Opening debrief scene...");

    // Ensure the debrief is visible when returning to it
    DebriefController debriefController = (DebriefController) pair.getController();

    if (correct) {
      debriefController.setCorrectLabel("You were CORRECT!");
    } else {
      debriefController.setCorrectLabel("You were INCORRECT!");
    }

    // Get chat history from ChatController if available
    String chatHistory = "";
    if (controllerCache.containsKey("chat")) {
      ChatController chatController = (ChatController) controllerCache.get("chat");
      chatHistory = chatController.getChatHistoryText();
    }

    // Start GPT analysis with chat history and decision correctness
    debriefController.runGptDebrief(chatHistory, correct);

    scene = pair.getScene();
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /** Forces the user back to the room scene (used when timer expires). */
  public static void forceReturnToRoom() {
    try {
      if (primaryStage.getScene().getRoot().getId().equals("room")) {
        SceneControllerPair roomPair = loadAndCacheScene("room");
        RoomController roomController = (RoomController) roomPair.getController();
        System.out.println("setting context to guessing state");
        roomController.getContext().setState(roomController.getContext().getGuessingState());
        roomController.initializeGuessingState();
        return;
      }

      SceneControllerPair pair = loadAndCacheScene("chat");

      ChatController chatController = (ChatController) pair.getController();
      chatController.fadeOut(null);

      SceneControllerPair roomPair = loadAndCacheScene("room");
      RoomController roomController = (RoomController) roomPair.getController();
      System.out.println("setting context to guessing state");
      roomController.getContext().setState(roomController.getContext().getGuessingState());
      roomController.initializeGuessingState();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "room" scene using
   * the caching system.
   *
   * @param stage the primary stage of the application
   * @throws IOException if the "src/main/resources/fxml/room.fxml" file is not found
   */
  @Override
  public void start(final Stage stage) throws IOException {
    primaryStage = stage; // Store reference to primary stage

    // Set up timer expired callback to force return to room
    GameTimer.getInstance()
        .setOnTimerExpired(
            () -> {
              forceReturnToRoom();
            });

    SceneControllerPair pair = loadAndCacheScene("startMenu");
    scene = pair.getScene();
    stage.setScene(scene);
    stage.show();
    scene.getRoot().requestFocus();
  }
}
