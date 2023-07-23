package org.rsultan.video.source;

import static org.rsultan.video.Constants.DEFAULT_DEVICE_NUMBER;
import static org.rsultan.video.Constants.DEVICE_NUMBER;
import static org.rsultan.video.Constants.HEIGHT;
import static org.rsultan.video.Constants.WIDTH;

import java.util.Map;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class DeviceSource extends AbstractSource {

  public DeviceSource(Map<String, ?> properties) {
    super(
        new OpenCVFrameGrabber((int) properties.get(DEVICE_NUMBER)),
        (int) properties.get(WIDTH),
        (int) properties.get(HEIGHT)
    );
  }
}
