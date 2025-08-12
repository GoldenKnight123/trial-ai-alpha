package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import nz.ac.auckland.se206.controllers.ChatController;

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
  public static void openChat(MouseEvent event, String profession) throws IOException {
    SceneControllerPair pair = loadAndCacheScene("chat");
    ChatController chatController = (ChatController) pair.getController();
    chatController.setProfession(profession);

    scene = pair.getScene();
    primaryStage.setScene(scene);
    primaryStage.show();
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
    SceneControllerPair pair = loadAndCacheScene("room");
    scene = pair.getScene();
    stage.setScene(scene);
    stage.show();
    scene.getRoot().requestFocus();
  }
}
