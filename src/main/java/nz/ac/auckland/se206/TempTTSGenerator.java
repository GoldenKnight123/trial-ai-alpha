package nz.ac.auckland.se206;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest.Provider;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest.Voice;
import nz.ac.auckland.apiproxy.tts.TextToSpeechResult;

/**
 * Temporary utility class for generating TTS audio files from text. This class downloads TTS audio
 * and saves it as local files.
 */
public class TempTTSGenerator {

  /**
   * Main method for testing the TTS file generation. Usage examples.
   *
   * @throws ApiProxyException
   * @throws IOException
   */
  public static void main(String[] args) throws ApiProxyException, IOException {
    ApiProxyConfig config = ApiProxyConfig.readConfig();
    Provider provider = Provider.OPENAI;
    Voice voice = Voice.OPENAI_ASH;

    TextToSpeechRequest ttsRequest = new TextToSpeechRequest(config);
    ttsRequest
        .setText(
            "It's early in the morning. I just arrived at the Greenhill Power Plant site and heard"
                + " a huge explosion.")
        .setProvider(provider)
        .setVoice(voice);

    try {
      TextToSpeechResult ttsResult = ttsRequest.execute();
      String audioUrl = ttsResult.getAudioUrl();

      // Save the audio file
      String outputPath = "generated_audio.mp3"; // You can change this filename

      // Create output directory if it doesn't exist
      Path outputFilePath = Paths.get(outputPath);
      Path parentDir = outputFilePath.getParent();
      if (parentDir != null && !Files.exists(parentDir)) {
        Files.createDirectories(parentDir);
      }

      // Download and save the audio file
      try (InputStream inputStream = new BufferedInputStream(new URL(audioUrl).openStream());
          FileOutputStream outputStream = new FileOutputStream(outputPath)) {

        byte[] buffer = new byte[1024];
        int bytesRead;

        System.out.println("Downloading audio from: " + audioUrl);
        System.out.println("Saving to: " + outputPath);

        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }

        System.out.println("Successfully saved TTS audio to: " + outputPath);

      } catch (IOException e) {
        System.err.println("Error downloading or saving audio file: " + e.getMessage());
        e.printStackTrace();
      }

    } catch (ApiProxyException e) {
      System.err.println("Error executing TTS request: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
