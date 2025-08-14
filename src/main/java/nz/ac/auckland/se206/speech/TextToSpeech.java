package nz.ac.auckland.se206.speech;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest.Provider;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest.Voice;
import nz.ac.auckland.apiproxy.tts.TextToSpeechResult;

/** A utility class for converting text to speech using the specified API proxy. */
public class TextToSpeech {

  /**
   * Converts the given text to speech and plays the audio.
   *
   * @param text the text to be converted to speech
   * @throws ApiProxyException
   * @throws IllegalArgumentException if the text is null or empty
   */
  public static Player speak(String text, Provider provider, Voice voice) throws ApiProxyException {
    Player player = null;
    if (text == null || text.isEmpty()) {
      throw new IllegalArgumentException("Text should not be null or empty");
    }
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();

      TextToSpeechRequest ttsRequest = new TextToSpeechRequest(config);
      ttsRequest.setText(text).setProvider(provider).setVoice(voice);

      System.out.println("Sending TTS request: " + ttsRequest);

      TextToSpeechResult ttsResult = ttsRequest.execute();
      String audioUrl = ttsResult.getAudioUrl();

      InputStream inputStream = new BufferedInputStream(new URL(audioUrl).openStream());
      player = new Player(inputStream);
    } catch (JavaLayerException | IOException e) {
      e.printStackTrace();
    }
    return player;
  }
}
