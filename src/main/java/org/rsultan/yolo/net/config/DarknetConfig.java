package org.rsultan.yolo.net.config;

import static org.bytedeco.opencv.global.opencv_dnn.readNetFromDarknet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.bytedeco.opencv.opencv_dnn.Net;

public record DarknetConfig(String configPath, String weightsPath, String namesPath) implements
    NetConfig {

  public DarknetConfig(String modelPath) {
    this(
        modelPath + File.separator + "config.cfg",
        modelPath + File.separator + "weights.weights",
        modelPath + File.separator + "names.txt"
    );
  }

  public Net buildNet() {
    return readNetFromDarknet(
        Paths.get(configPath)
            .toAbsolutePath().toString(),
        Paths.get(weightsPath)
            .toAbsolutePath().toString());
  }

  public List<String> getNames() {
    try {
      return Files.readAllLines(Paths.get(namesPath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
