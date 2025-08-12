package nz.ac.auckland.se206.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

/**
 * Abstract base class for all controllers. Provides common functionality including typewriter
 * effects for text display.
 */
public abstract class Controller {

  private Timeline typewriterTimeline; // Timeline for typewriter effect

  /**
   * Creates a typewriter effect for displaying text in a TextArea.
   *
   * @param textArea the TextArea to display text in
   * @param textToDisplay the text to display with typewriter effect
   * @param delayPerCharacter delay between each character in milliseconds
   * @param clearFirst whether to clear the TextArea before starting the effect
   */
  protected void displayTextWithTypewriterEffect(
      TextArea textArea, String textToDisplay, double delayPerCharacter, boolean clearFirst) {
    // Stop any existing typewriter animation
    if (typewriterTimeline != null) {
      typewriterTimeline.stop();
    }

    // Clear or preserve existing text
    if (clearFirst) {
      textArea.clear();
    }

    String existingText = textArea.getText();

    // Create the typewriter animation
    typewriterTimeline = new Timeline();

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
    displayTextWithTypewriterEffect(textArea, textToDisplay, delayPerCharacter, true);
  }

  /**
   * Creates a typewriter effect for displaying text in a TextArea with default speed (50ms per
   * character).
   *
   * @param textArea the TextArea to display text in
   * @param textToDisplay the text to display with typewriter effect
   */
  protected void displayTextWithTypewriterEffect(TextArea textArea, String textToDisplay) {
    displayTextWithTypewriterEffect(textArea, textToDisplay, 50, true);
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
    displayTextWithTypewriterEffect(textArea, textToAppend, delayPerCharacter, false);
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
}
