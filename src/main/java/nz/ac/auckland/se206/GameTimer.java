package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.shape.Arc;
import javafx.util.Duration;

/** This class manages a game timer that can be displayed on multiple UI labels. */
public class GameTimer {

  /** Callback interface for when the timer runs out. */
  public interface TimerExpiredCallback {
    void onTimerExpired();
  }

  /**
   * Gets the singleton instance of the GameTimer.
   *
   * @return the GameTimer instance
   */
  public static GameTimer getInstance() {
    if (instance == null) {
      instance = new GameTimer();
    }
    return instance;
  }

  private static GameTimer instance;
  private Timeline timeline;
  private int secondsLeft = 120;
  private int maxTime = 120;
  private boolean isRunning = false;
  private List<Label> timerLabels = new ArrayList<>();
  private List<Arc> timerArcs = new ArrayList<>();
  private TimerExpiredCallback expiredCallback;

  /** Private constructor for singleton pattern */
  private GameTimer() {
    initializeTimer();
  }

  /** Initializes the JavaFX Timeline for the timer. */
  private void initializeTimer() {
    timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                e -> {
                  secondsLeft--;
                  updateTimerDisplay();
                  if (secondsLeft <= 0) {
                    stop();
                    // Call the callback when timer expires
                    if (expiredCallback != null) {
                      Platform.runLater(() -> expiredCallback.onTimerExpired());
                    }
                  }
                }));
    timeline.setCycleCount(Timeline.INDEFINITE);
  }

  /** Starts the timer. */
  public void start() {
    if (!isRunning) {
      timeline.play();
      isRunning = true;
    }
  }

  /** Stops the timer. */
  public void stop() {
    if (isRunning) {
      timeline.stop();
      isRunning = false;
    }
  }

  /** Get the time left */
  public int getTimeLeft() {
    return secondsLeft;
  }

  /** Get the maximum time */
  public int setMaxTime(int maxTime) {
    this.maxTime = maxTime;
    secondsLeft = maxTime;
    return maxTime;
  }

  /**
   * Sets the callback to be called when the timer expires.
   *
   * @param callback the callback to execute when timer runs out
   */
  public void setOnTimerExpired(TimerExpiredCallback callback) {
    this.expiredCallback = callback;
  }

  /**
   * Registers a Label to display the timer. Multiple labels can be registered to show the timer in
   * different scenes.
   *
   * @param label the Label to display the timer
   */
  public void registerTimerLabel(Label label) {
    if (!timerLabels.contains(label)) {
      timerLabels.add(label);
      // Update the label immediately with current time (formatted with fixed width)
      Platform.runLater(() -> label.setText(String.format("%3d", secondsLeft)));
    }
  }

  /**
   * Registers a Label to display the timer. Multiple labels can be registered to show the timer in
   * different scenes.
   *
   * @param label the Label to display the timer
   */
  public void registerTimerArc(Arc arc) {
    if (!timerArcs.contains(arc)) {
      timerArcs.add(arc);
      // Update the arc immediately with current time
      Platform.runLater(() -> arc.setLength(360.0 * secondsLeft / maxTime));
    }
  }

  /** Updates all registered timer labels with the current time. */
  public void updateTimerDisplay() {
    String timeString = String.format("%3d", secondsLeft);
    Platform.runLater(
        () -> {
          for (Label label : timerLabels) {
            label.setText(timeString);
          }
          for (Arc arc : timerArcs) {
            arc.setLength(360.0 * secondsLeft / maxTime);
          }
        });
  }
}
