package org.rsultan.video.diffusion;

import static org.rsultan.video.Constants.BITRATE;
import static org.rsultan.video.Constants.CODEC;
import static org.rsultan.video.Constants.CRF;
import static org.rsultan.video.Constants.DEFAULT_FRAME_RATE;
import static org.rsultan.video.Constants.DEFAULT_GOP_LENGTH_IN_FRAMES;
import static org.rsultan.video.Constants.FORMAT;
import static org.rsultan.video.Constants.FRAME_RATE;
import static org.rsultan.video.Constants.GOP;
import static org.rsultan.video.Constants.HEIGHT;
import static org.rsultan.video.Constants.PRESET;
import static org.rsultan.video.Constants.SOURCE;
import static org.rsultan.video.Constants.DIFFUSION_ADDRESS;
import static org.rsultan.video.Constants.TUNE;
import static org.rsultan.video.Constants.WIDTH;

import java.util.Map;
import java.util.UUID;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.rsultan.video.diffusion.config.DirectConfiguration;
import org.rsultan.video.diffusion.config.StreamConfiguration;
import org.rsultan.video.source.Source;

public interface Diffusion {

  void show(Frame frame);

  void stop() throws Exception;

  static Diffusion get(DiffusionType type, Map<String, ?> properties) {
    return switch (type) {
      case DIRECT -> new DirectDiffusion(
          new DirectConfiguration()
              .setName("My Application - " + UUID.randomUUID())
              .setWidth((int) properties.get(WIDTH))
              .setHeight((int) properties.get(HEIGHT))
              .setSource((Source) properties.get(SOURCE)).build());
      case STREAM -> new StreamDiffusion(
          new StreamConfiguration()
              .setAddress((String) properties.get(DIFFUSION_ADDRESS))
              .setWidth((int) properties.get(WIDTH))
              .setHeight((int) properties.get(HEIGHT))
              .setInterleaved(true)
              .setVideoBitrate((int) properties.get(BITRATE))
              .addVideoOption(TUNE, (String) properties.get(TUNE))
              .addVideoOption(PRESET, (String) properties.get(PRESET))
              .addVideoOption(CRF, (String) properties.get(CRF))
              .setVideoCodec((int) properties.get(CODEC))
              .setFormat((String) properties.get(FORMAT))
              .setFrameRate((int) properties.get(FRAME_RATE))
              .setGopSizeInFrames((int) properties.get(GOP))
              .build()
      );
    };
  }
}
