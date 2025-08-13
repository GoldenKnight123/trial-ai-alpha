package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;
import nz.ac.auckland.se206.speech.TextToSpeech;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class ChatController extends Controller {

  @FXML private TextArea txtaChat;
  @FXML private TextField txtInput;
  @FXML private Button btnSend;
  @FXML private Label lblSceneName;
  @FXML private AnchorPane chatRoom;

  private ChatCompletionRequest chatCompletionRequest;
  private String target;
  private HashMap<String, String> fixedDialogue = new HashMap<>();
  private long userMessageFinishTime = 0;
  private boolean waitingForGptResponse = false;
  private ChatMessage pendingGptResponse = null;
  private String currentSpeaker = ""; // Track who is currently displaying text ("user" or "gpt")

  /**
   * Initializes the chat view.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  public void initialize() throws ApiProxyException {
    txtInput.setVisible(false);
    btnSend.setVisible(false);
    fixedDialogue.put(
        "LOGOS-09",
        "It is currently 16-07-2027 22:17:32. I have detected a command from administrator to put"
            + " INDUS-07 to sleep.");
  }

  /**
   * Handles key press events on the text input field. Sends message on Enter key press.
   *
   * @param event the key event
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onKeyPressed(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode() == KeyCode.ENTER) {
      onSendMessage(null);
    }
  }

  public void fadeIn() {
    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(chatRoom);
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);
    fadeTransition.play();
  }

  public void fadeOut(ActionEvent event) {
    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(chatRoom);
    fadeTransition.setFromValue(1.0);
    fadeTransition.setToValue(0.0);

    fadeTransition.setOnFinished(
        e -> {
          try {
            App.openRoom(event);
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        });
    fadeTransition.play();
  }

  /**
   * Generates the system prompt based on the profession.
   *
   * @return the system prompt string
   */
  private String getSystemPrompt(String target) {
    Map<String, String> map = new HashMap<>();
    map.put("target", target);
    return PromptEngineering.getPrompt(target, map);
  }

  /**
   * Sets the target for the chat context and initializes the ChatCompletionRequest.
   *
   * @param target the target to set
   */
  public void setTarget(String target) {
    this.target = target;
    lblSceneName.setText("Flashback - " + target);
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setModel(Model.GPT_4_1_NANO)
              .setMaxTokens(200);
      currentSpeaker = "gpt";
      displayTextWithTypewriterEffect(txtaChat, fixedDialogue.get(target));
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    try {
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());

      // Update UI on JavaFX Application Thread
      Platform.runLater(
          () -> {
            if (userMessageFinishTime > 0) {
              System.out.println("User message has finished, displaying with delay");
              // User message has finished, display with delay
              displayGptResponseWithDelay(result.getChatMessage());
            } else {
              System.out.println("User message still typing, storing response for later");
              // User message still typing, store response for later
              pendingGptResponse = result.getChatMessage();
            }
          });

      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Displays the GPT response with a minimum 2-second delay from when the user's message finished.
   *
   * @param message the GPT response message to display
   */
  private void displayGptResponseWithDelay(ChatMessage message) {
    long elapsedSinceUserFinish = System.currentTimeMillis() - userMessageFinishTime;
    long minimumDelay = 2000; // 2 seconds minimum delay
    long additionalDelay = Math.max(0, minimumDelay - elapsedSinceUserFinish);

    if (additionalDelay > 0) {
      // Use Timeline to create a delay before showing GPT response
      javafx.animation.Timeline timeline = new javafx.animation.Timeline();
      timeline
          .getKeyFrames()
          .add(
              new javafx.animation.KeyFrame(
                  javafx.util.Duration.millis(additionalDelay),
                  e -> {
                    currentSpeaker = "gpt";
                    displayTextWithTypewriterEffect(txtaChat, message.getContent());
                    // Start text-to-speech in background
                    new Thread(() -> TextToSpeech.speak(message.getContent())).start();
                    waitingForGptResponse = false;
                  }));
      timeline.play();
    } else {
      // No additional delay needed
      currentSpeaker = "gpt";
      displayTextWithTypewriterEffect(txtaChat, message.getContent());
      // Start text-to-speech in background
      new Thread(() -> TextToSpeech.speak(message.getContent())).start();
      waitingForGptResponse = false;
    }
  }

  @Override
  protected void onTypewriterEffectFinish() {
    if (waitingForGptResponse && currentSpeaker.equals("user")) {
      // User message typewriter finished, record the time
      userMessageFinishTime = System.currentTimeMillis();

      // If GPT response is ready, display it after minimum delay
      if (pendingGptResponse != null) {
        displayGptResponseWithDelay(pendingGptResponse);
        pendingGptResponse = null;
      }
    } else if (currentSpeaker.equals("gpt")) {
      // GPT message typewriter finished - show input controls
      btnSend.setVisible(true);
      txtInput.setVisible(true);
      currentSpeaker = ""; // Reset speaker
    }
  }

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = txtInput.getText().trim();
    if (message.isEmpty()) {
      return;
    }

    // Clear input and disable controls to prevent multiple requests
    txtInput.clear();
    btnSend.setVisible(false);
    txtInput.setVisible(false);

    // Reset timing variables
    userMessageFinishTime = 0;
    pendingGptResponse = null;
    waitingForGptResponse = true;
    currentSpeaker = "user";

    ChatMessage msg = new ChatMessage("user", message);
    displayTextWithTypewriterEffect(txtaChat, message);

    // Create a background task to run GPT
    Task<ChatMessage> gptTask =
        new Task<ChatMessage>() {
          @Override
          protected ChatMessage call() throws Exception {
            return runGpt(msg);
          }
        };

    // Handle task completion
    gptTask.setOnSucceeded(
        e -> {
          // Task completed successfully - response handling is done in runGpt method
        });

    // Handle task failure
    gptTask.setOnFailed(
        e -> {
          Platform.runLater(
              () -> {
                // Re-enable controls even if the task failed
                btnSend.setVisible(true);
                txtInput.setVisible(true);
                txtInput.requestFocus();
                waitingForGptResponse = false;

                // Log the error
                Throwable exception = gptTask.getException();
                if (exception != null) {
                  exception.printStackTrace();
                }
              });
        });

    // Start the task in a background thread
    Thread gptThread = new Thread(gptTask);
    gptThread.start();
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    fadeOut(event);
  }
}
