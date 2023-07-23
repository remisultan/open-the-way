package org.rsultan.video.diffusion.config;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.rsultan.video.source.Source;

public class DirectConfiguration extends DiffusionConfiguration<CanvasFrame> {

  private Source source;

  public DirectConfiguration setSource(Source source) {
    this.source = source;
    return this;
  }

  public DirectConfiguration setName(String name) {
    super.setName(name);
    return this;
  }

  @Override
  public DirectConfiguration setWidth(int width) {
    super.setWidth(width);
    return this;
  }

  @Override
  public DirectConfiguration setHeight(int height) {
    super.setHeight(height);
    return this;
  }

  @Override
  public CanvasFrame build() {
    FrameGrabber frameGrabber = source.getFrameGrabber();
    return new CanvasFrame(name,
            CanvasFrame.getDefaultGamma() / (frameGrabber == null ? 1.0 : frameGrabber.getGamma()));
  }
}
