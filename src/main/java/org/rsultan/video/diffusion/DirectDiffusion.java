package org.rsultan.video.diffusion;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.rsultan.exception.VideoEndedException;

public class DirectDiffusion implements Diffusion {

  private final CanvasFrame canvasFrame;

  public DirectDiffusion(CanvasFrame canvasFrame) {
    this.canvasFrame = canvasFrame;
  }

  public void show(Frame frame) {
    if (canvasFrame.isVisible()) {
      canvasFrame.showImage(frame);
    } else {
      throw new VideoEndedException("Video has ended");
    }
  }

  @Override
  public void stop() throws Exception {
    canvasFrame.setVisible(false);
  }
}
