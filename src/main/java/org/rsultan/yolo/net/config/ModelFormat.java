package org.rsultan.yolo.net.config;

public enum ModelFormat {
  DARKNET;

  public NetConfig toConfig(String modelPath) {
    return switch (this) {
      case DARKNET -> new DarknetConfig(modelPath);
    };
  }
}
