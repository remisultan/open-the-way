package org.rsultan.video.source;

import java.util.Map;
import java.util.Optional;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;

public interface Source {

  Optional<Frame> capture();

  Frame actualCapture() throws InterruptedException, Exception;

  void stop() throws Exception;

  FrameGrabber getFrameGrabber();


  static Source get(SourceType sourceType, Map<String, ?> properties) {
    return switch (sourceType) {
      case DIRECT -> new DeviceSource(properties);
      case STREAM -> new StreamSource(properties);
    };
  }
}
