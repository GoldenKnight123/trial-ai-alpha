package nz.ac.auckland.se206.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextArea;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

/**
 * Abstract base class for all controllers. Provides common functionality including typewriter
 * effects for text display.
 */
public abstract class Controller {

  private Timeline typewriterTimeline; // Timeline for typewriter effect
  private boolean isTyping = false;
  private String currentFullText = ""; // Store the full text being displayed
  private TextArea currentTextArea = null; // Store the current TextArea being used
  private AudioClip userTalkSound =
      new AudioClip(getClass().getResource("/sounds/userTalk.wav").toString());

  /**
   * Creates a typewriter effect for displaying text in a TextArea.
   *
   * @param textArea the TextArea to display text in
   * @param textToDisplay the text to display with typewriter effect
   * @param delayPerCharacter delay between each character in milliseconds
   * @param clearFirst whether to clear the TextArea before starting the effect
   * @param addSound whether to play the user talk sound effect
   */
  protected void displayTextWithTypewriterEffect(
      TextArea textArea,
      String textToDisplay,
      double delayPerCharacter,
      boolean clearFirst,
      boolean addSound) {
    // Stop any existing typewriter animation
    if (typewriterTimeline != null) {
      typewriterTimeline.stop();
    }

    // Store current typewriter state
    currentTextArea = textArea;
    currentFullText = textToDisplay;

    // Clear or preserve existing text
    if (clearFirst) {
      textArea.clear();
    }

    isTyping = true;

    String existingText = textArea.getText();

    // Create the typewriter animation
    typewriterTimeline = new Timeline();

    // Will add threading in the future
    for (int i = 0; i <= textToDisplay.length(); i++) {
      final int charIndex = i;
      KeyFrame keyFrame =
          new KeyFrame(
              Duration.millis(charIndex * delayPerCharacter),
              e -> {
                if (clearFirst) {
                  textArea.setText(textToDisplay.substring(0, charIndex));
                } else {
                  textArea.setText(existingText + textToDisplay.substring(0, charIndex));
                }

                // Play user talk sound if enabled
                if (addSound) {
                  userTalkSound.play();
                }

                // Auto-scroll to bottom after each character is added
                textArea.setScrollTop(Double.MAX_VALUE);

                // Check if this is the last character being displayed
                if (charIndex == textToDisplay.length()) {
                  isTyping = false;
                  onTypewriterEffectFinish();
                }
              });
      typewriterTimeline.getKeyFrames().add(keyFrame);
    }

    typewriterTimeline.play();
  }

  /**
   * Creates a typewriter effect for displaying text in a TextArea, clearing it first.
   *
   * @param textArea the TextArea to display text in
   * @param textToDisplay the text to display with typewriter effect
   * @param delayPerCharacter delay between each character in milliseconds
   */
  protected void displayTextWithTypewriterEffect(
      TextArea textArea, String textToDisplay, double delayPerCharacter) {
    displayTextWithTypewriterEffect(textArea, textToDisplay, delayPerCharacter, true, false);
  }

  /**
   * Creates a typewriter effect for displaying text in a TextArea with default speed (50ms per
   * character).
   *
   * @param textArea the TextArea to display text in
   * @param textToDisplay the text to display with typewriter effect
   */
  protected void displayTextWithTypewriterEffect(TextArea textArea, String textToDisplay) {
    displayTextWithTypewriterEffect(textArea, textToDisplay, 50, true, false);
  }

  /**
   * Appends text to a TextArea with typewriter effect.
   *
   * @param textArea the TextArea to append text to
   * @param textToAppend the text to append with typewriter effect
   * @param delayPerCharacter delay between each character in milliseconds
   */
  protected void appendTextWithTypewriterEffect(
      TextArea textArea, String textToAppend, double delayPerCharacter) {
    displayTextWithTypewriterEffect(textArea, textToAppend, delayPerCharacter, false, false);
  }

  /**
   * Appends text to a TextArea with typewriter effect using default speed (30ms per character).
   *
   * @param textArea the TextArea to append text to
   * @param textToAppend the text to append with typewriter effect
   */
  protected void appendTextWithTypewriterEffect(TextArea textArea, String textToAppend) {
    appendTextWithTypewriterEffect(textArea, textToAppend, 30);
  }

  /** Stops any currently running typewriter effect. */
  protected void stopTypewriterEffect() {
    if (typewriterTimeline != null) {
      typewriterTimeline.stop();
    }
  }

  /** Instantly finishes the current typewriter effect by displaying the full text. */
  protected void finishTypewriterEffectInstantly() {
    if (isTyping && currentTextArea != null && currentFullText != null) {
      // Stop the current animation
      if (typewriterTimeline != null) {
        typewriterTimeline.stop();
      }

      // Set the full text immediately
      currentTextArea.setText(currentFullText);

      // Auto-scroll to bottom when finishing instantly
      currentTextArea.setScrollTop(Double.MAX_VALUE);

      isTyping = false;
      onTypewriterEffectFinish();
    }
  }

  /** Called when the typewriter effect finishes. */
  protected void onTypewriterEffectFinish() {
    // Override this method in subclasses to handle the end of the typewriter effect
  }

  public boolean isTyping() {
    return isTyping;
  }
}
