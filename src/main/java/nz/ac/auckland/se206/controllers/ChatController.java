package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
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
import javafx.scene.shape.Arc;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameTimer;
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
  @FXML private Button btnReturn;
  @FXML private Label lblSceneName;
  @FXML private Label lblWhoSpeaking;
  @FXML private Label lblThinking;
  @FXML private AnchorPane chatRoom;
  @FXML private Button btnHistory;
  @FXML private TextArea txtaHistory;
  @FXML private Rectangle rectHistory;
  @FXML private Label lblTimer;
  @FXML private Arc arcTimer;

  private ChatCompletionRequest chatCompletionRequest;
  private String target;
  private HashMap<String, String> fixedDialogue = new HashMap<>();
  private long userMessageFinishTime = 0;
  private boolean waitingForGptResponse = false;
  private ChatMessage pendingGptResponse = null;
  private String currentSpeaker = ""; // Track who is currently displaying text ("user" or "gpt")
  private HashMap<String, List<ChatMessage>> chatHistory = new HashMap<>();
  private String chatHistoryText = ""; // Store chat history text
  private String chatHistoryTextSnapShot = ""; // Store chat history text snapshot
  private boolean historyView = false;
  private boolean isAnimating = false; // Track if history animation is playing
  private boolean isFading = false; // Track if fade animation is playing
  private Timeline thinkingAnimation; // Track thinking animation

  /**
   * Initializes the chat view.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  public void initialize() throws ApiProxyException {
    // Register timer label with GameTimer
    GameTimer.getInstance().registerTimerLabel(lblTimer);
    GameTimer.getInstance().registerTimerArc(arcTimer);

    txtInput.setVisible(false);
    btnSend.setVisible(false);
    txtaHistory.setVisible(false);
    rectHistory.setVisible(false);
    rectHistory.setOpacity(0);
    rectHistory.setDisable(true);
    fixedDialogue.put(
        "LOGOS-09",
        "It is currently 16-07-2027 22:17:32. I have detected a message from INDUS-07 sent to site"
            + " workers to increase the output levels of the power plant by 15%.");
    fixedDialogue.put(
        "INDUS-07",
        "It is currently 16-07-2027 21:27:34. I am preparing and analysing a way to increase the"
            + " output of the power plant.");
    fixedDialogue.put(
        "Evan",
        "It's early in the morning. I just arrived at the Greenhill Power Plant site and heard a"
            + " huge explosion.");
    chatHistory.put("LOGOS-09", new ArrayList<>());
    chatHistory.put("INDUS-07", new ArrayList<>());
    chatHistory.put("Evan", new ArrayList<>());
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
    // Prevent starting a new fade if one is already in progress
    if (isFading) {
      return;
    }

    isFading = true; // Set flag to prevent further fade operations

    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(chatRoom);
    fadeTransition.setFromValue(1.0);
    fadeTransition.setToValue(0.0);

    fadeTransition.setOnFinished(
        e -> {
          isFading = false; // Reset flag when fade completes
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
    chatHistoryTextSnapShot = chatHistoryText; // Store a snapshot of the chat history
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setModel(Model.GPT_4o_MINI)
              .setMaxTokens(200);
      currentSpeaker = "gpt";
      lblWhoSpeaking.setText(target + ":");

      // If the chatHistorySnapshot does not contain the targets fixed dialogue display it
      if (!chatHistoryTextSnapShot.contains(fixedDialogue.get(target))) {
        txtInput.setVisible(false);
        btnSend.setVisible(false);
        String message = fixedDialogue.get(target);
        chatHistory.get(target).add(new ChatMessage("assistant", message));
        chatHistoryText += target + ": " + message + "\n\n"; // Update chat history text
        btnReturn.setDisable(true);
        displayTextWithTypewriterEffect(txtaChat, message);
      } else {
        // Find the last message sent by target in chatHistoryTextSnapShot
        String lastMessage =
            chatHistoryTextSnapShot.substring(
                chatHistoryTextSnapShot.lastIndexOf(target + ": ") + (target + ": ").length());
        lastMessage = lastMessage.substring(0, lastMessage.indexOf("\n\n"));
        txtaChat.setText(lastMessage);
      }
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
    // Add the system prompt and chat history snapshot with all "You" replaced with "Judge"
    chatCompletionRequest.addMessage(
        new ChatMessage(
            "system", getSystemPrompt(target) + chatHistoryTextSnapShot.replace("You", "Judge")));

    // Add all messages from the chat history
    for (ChatMessage message : chatHistory.get(target)) {
      chatCompletionRequest.addMessage(message);
    }

    try {
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());

      // Update UI on JavaFX Application Thread
      Platform.runLater(
          () -> {
            // Add message to history
            chatHistory.get(target).add(result.getChatMessage());
            chatHistoryText +=
                target
                    + ": "
                    + result.getChatMessage().getContent()
                    + "\n\n"; // Update chat history text
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
      Timeline timeline = new Timeline();
      timeline
          .getKeyFrames()
          .add(
              new KeyFrame(
                  Duration.millis(additionalDelay),
                  e -> {
                    currentSpeaker = "gpt";
                    lblWhoSpeaking.setText(target + ":");
                    stopThinkingAnimation();
                    displayTextWithTypewriterEffect(txtaChat, message.getContent());
                    // Start text-to-speech in background
                    new Thread(() -> TextToSpeech.speak(message.getContent())).start();
                    waitingForGptResponse = false;
                  }));
      timeline.play();
    } else {
      // No additional delay needed
      currentSpeaker = "gpt";
      lblWhoSpeaking.setText(target + ":");
      stopThinkingAnimation();
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
      startThinkingAnimation();

      // If GPT response is ready, display it after minimum delay
      if (pendingGptResponse != null) {
        displayGptResponseWithDelay(pendingGptResponse);
        pendingGptResponse = null;
      }
    } else if (currentSpeaker.equals("gpt")) {
      // GPT message typewriter finished - show input controls
      btnSend.setVisible(true);
      txtInput.setVisible(true);
      btnReturn.setDisable(false);
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
    btnReturn.setDisable(true);

    // Reset timing variables
    userMessageFinishTime = 0;
    pendingGptResponse = null;
    waitingForGptResponse = true;
    currentSpeaker = "user";
    lblWhoSpeaking.setText("You:");

    ChatMessage msg = new ChatMessage("user", message);

    // Add message to history
    chatHistory.get(target).add(msg);
    chatHistoryText += "You: " + msg.getContent() + "\n"; // Update chat history text

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
          stopThinkingAnimation();
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
    // Prevent going back if text is still displaying, GPT is running, or fade is in progress
    if (isTyping() || waitingForGptResponse || isFading) {
      System.out.println(
          "Cannot go back: Text is still displaying, GPT is processing, or fade animation is in"
              + " progress");
      return;
    }

    fadeOut(event);
  }

  /**
   * Toggles chat history view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onToggleHistory(ActionEvent event) throws ApiProxyException, IOException {
    // Prevent clicks during animation
    if (isAnimating) {
      return;
    }

    if (!historyView) {
      isAnimating = true; // Set flag to prevent further clicks

      txtaHistory.setText(chatHistoryText); // Set chat history text

      rectHistory.setVisible(true);
      // Fade rectHistory to 0.5 opacity
      FadeTransition fade = new FadeTransition(Duration.millis(300), rectHistory);
      fade.setFromValue(0);
      fade.setToValue(0.5);

      // Set initial position off-screen to the right
      txtaHistory.setTranslateX(txtaHistory.getWidth());

      // Create slide-in animation from the right
      TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), txtaHistory);
      slideIn.setFromX(txtaHistory.getWidth());
      slideIn.setToX(0);

      // Reset animation flag when complete
      slideIn.setOnFinished(e -> isAnimating = false);

      // Play animations
      fade.play();
      slideIn.play();

      txtaHistory.setVisible(true);
      historyView = true;
      btnHistory.setText("Hide History");
    } else {
      isAnimating = true; // Set flag to prevent further clicks

      // Fade rectHistory to 0 opacity
      FadeTransition fade = new FadeTransition(Duration.millis(300), rectHistory);
      fade.setFromValue(0.5);
      fade.setToValue(0);
      fade.play();

      // Create slide-out animation to the right
      TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), txtaHistory);
      slideOut.setFromX(0);
      slideOut.setToX(txtaHistory.getWidth());
      slideOut.play();

      // Hide elements after animation completes and reset state
      slideOut.setOnFinished(
          e -> {
            txtaHistory.setVisible(false);
            isAnimating = false; // Reset animation flag
          });
      fade.setOnFinished(e -> rectHistory.setVisible(false));

      historyView = false; // Reset the flag when closing is complete

      btnHistory.setText("Show History");
    }
  }

  /** Starts the thinking animation by cycling through dots appearing and disappearing. */
  private void startThinkingAnimation() {
    if (thinkingAnimation != null) {
      thinkingAnimation.stop();
    }

    lblThinking.setVisible(true);

    // Create timeline that cycles through different thinking states
    thinkingAnimation =
        new Timeline(
            new KeyFrame(Duration.millis(0), e -> lblThinking.setText("Thinking")),
            new KeyFrame(Duration.millis(250), e -> lblThinking.setText("Thinking.")),
            new KeyFrame(Duration.millis(500), e -> lblThinking.setText("Thinking..")),
            new KeyFrame(Duration.millis(750), e -> lblThinking.setText("Thinking...")));

    thinkingAnimation.setCycleCount(Timeline.INDEFINITE);
    thinkingAnimation.play();
  }

  /** Stops the thinking animation and hides the thinking label. */
  private void stopThinkingAnimation() {
    if (thinkingAnimation != null) {
      thinkingAnimation.stop();
    }
    lblThinking.setVisible(false);
  }

  /**
   * Gets the complete chat history text for analysis purposes.
   *
   * @return the complete chat history as a string
   */
  public String getChatHistoryText() {
    return chatHistoryText;
  }
}
