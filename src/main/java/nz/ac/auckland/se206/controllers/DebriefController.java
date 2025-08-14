package nz.ac.auckland.se206.controllers;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

public class DebriefController extends Controller {
  @FXML private TextArea txtaDebrief;
  @FXML private Label lblCorrect;
  @FXML private Pane debrief;

  private ChatCompletionRequest chatCompletionRequest;

  public void fadeIn() {
    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(debrief);
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);

    fadeTransition.play();
  }

  public void runGptDebrief(String chatHistory, boolean wasCorrect) {
    // Create a background task for GPT analysis
    Task<String> gptTask =
        new Task<String>() {
          @Override
          protected String call() throws Exception {
            // Add the system prompt and chat history snapshot with all "You" replaced with "Judge"
            chatCompletionRequest.addMessage(
                new ChatMessage(
                    "system",
                    buildAnalysisPrompt(chatHistory.replace("You", "Judge"), wasCorrect)));

            try {
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
              Choice result = chatCompletionResult.getChoices().iterator().next();
              chatCompletionRequest.addMessage(result.getChatMessage());

              return result.getChatMessage().getContent();
            } catch (ApiProxyException e) {
              e.printStackTrace();
              return null;
            }
          }
        };

    // Handle success
    gptTask.setOnSucceeded(
        e -> {
          String analysis = gptTask.getValue();
          Platform.runLater(
              () -> {
                displayTextWithTypewriterEffect(txtaDebrief, analysis, 10);
              });
        });

    // Handle failure
    gptTask.setOnFailed(
        e -> {
          Platform.runLater(
              () -> {
                setDebriefText("Analysis failed. Please try again.");
              });
        });

    // Start the task in a new thread
    Thread gptThread = new Thread(gptTask);
    gptThread.setDaemon(true);
    gptThread.start();
  }

  /**
   * Creates a prompt to feed into GPT using the chat history and whether or not the user chose the
   * correct verdict between guilty and not guilty. It's designed to use specific quotes and
   * evidence to give detailed feedback
   */
  private String buildAnalysisPrompt(String chatHistory, boolean wasCorrect) {
    StringBuilder prompt = new StringBuilder();
    // Added context
    prompt.append(
        "You are an expert trial analyst. Analyze the following conversation between a judge and"
            + " characters in a trial simulation.\n\n");

    // Added chat history
    prompt.append("Chat History:\n").append(chatHistory).append("\n\n");

    // If the judge was correct then we give positive feedback
    if (wasCorrect) {
      prompt.append(
          "The judge made a CORRECT decision. That INDUS-07 was not responsible for the explosion,"
              + " and it was the higher ups that pressured it to increase output under the threat"
              + " of being fired. Explain why their decision was right based on the evidence and"
              + " conversations shown. Highlight key pieces of evidence or character responses that"
              + " supported the correct verdict.");
    } else { // If the judge was incorrect then we give negative feedback
      prompt.append(
          "The judge made an INCORRECT decision. That INDUS-07 was responsible for the explosion"
              + " even though it was the higher ups that pressured it to increase output under the"
              + " threat of being fired. Explain why their decision was wrong based on the evidence"
              + " and conversations shown. Highlight key pieces of evidence or character responses"
              + " that they may have missed or misinterpreted.");
    }

    // Added focus on specific details
    prompt.append(
        "Focus on specific details from the conversations that were crucial to determining guilt or"
            + " innocence.");

    return prompt.toString();
  }

  @FXML
  public void initialize() {
    fadeIn();
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setModel(Model.GPT_4o_MINI)
              .setMaxTokens(2000);
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  public void setDebriefText(String text) {
    txtaDebrief.setText(text);
  }

  public void setCorrectLabel(String text) {
    lblCorrect.setText(text);
  }
}
