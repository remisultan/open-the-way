package org.rsultan.video.source;

import static org.rsultan.video.Constants.BITRATE;
import static org.rsultan.video.Constants.CODEC;
import static org.rsultan.video.Constants.CRF;
import static org.rsultan.video.Constants.FORMAT;
import static org.rsultan.video.Constants.FRAME_RATE;
import static org.rsultan.video.Constants.HEIGHT;
import static org.rsultan.video.Constants.PRESET;
import static org.rsultan.video.Constants.ADDRESS;
import static org.rsultan.video.Constants.TUNE;
import static org.rsultan.video.Constants.WIDTH;

import java.util.Map;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;

public class StreamSource extends AbstractSource {

  public StreamSource(Map<String,?> properties) {
    super(
        new FFmpegFrameGrabber((String) properties.get(ADDRESS)),
        (int) properties.get(WIDTH),
        (int) properties.get(HEIGHT)
    );

    grabber.setVideoBitrate((int) properties.get(BITRATE));
    grabber.setVideoOption(TUNE, (String) properties.get(TUNE));
    grabber.setVideoOption(PRESET, (String) properties.get(PRESET));
    grabber.setVideoOption(CRF, (String) properties.get(CRF));
    grabber.setVideoCodec((int) properties.get(CODEC));
    grabber.setFormat((String) properties.get(FORMAT));
    grabber.setFrameRate((int) properties.get(FRAME_RATE));
  }

  @Override
  public Frame actualCapture() throws Exception {
    return ((FFmpegFrameGrabber) grabber).grabImage();
  }
}
