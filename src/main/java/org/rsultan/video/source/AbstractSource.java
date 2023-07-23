package org.rsultan.video.source;

import java.util.Optional;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;

public abstract class AbstractSource implements Source {

  protected final FrameGrabber grabber;
  private boolean started;

  protected AbstractSource(FrameGrabber grabber, int width, int height) {
    this.grabber = grabber;
    this.grabber.setImageWidth(width);
    this.grabber.setImageHeight(height);
  }

  @Override
  public Optional<Frame> capture() {
    try {
      if (!started) {
        grabber.start();
        started = true;
      }
      Frame value = actualCapture();
      return Optional.ofNullable(value);
    } catch (java.lang.Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public Frame actualCapture() throws Exception {
    return grabber.grab();
  }

  @Override
  public void stop() throws Exception {
    if(started){
      grabber.close();
    }
    started = false;
  }

  @Override
  public FrameGrabber getFrameGrabber() {
    return grabber;
  }
}
