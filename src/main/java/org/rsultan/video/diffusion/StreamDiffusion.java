package org.rsultan.video.diffusion;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.rsultan.exception.VideoEndedException;

public class StreamDiffusion implements Diffusion {

  private final FrameRecorder recorder;
  private boolean started;
  private long startTime;

  public StreamDiffusion(FrameRecorder recorder) {
    this.recorder = recorder;
  }

  @Override
  public void show(Frame frame) {
    try {
      if (!started) {
        recorder.start();
        startTime = System.currentTimeMillis();
        recorder.setTimestamp(startTime);
        started = true;
      }
      // Create timestamp for this frame
      long videoTS = 1000 * (System.currentTimeMillis() - startTime);

      if (videoTS > recorder.getTimestamp()) {
        recorder.setTimestamp(videoTS);
      }

      recorder.record(frame);
    } catch (Throwable e) {
      throw new VideoEndedException(e.getMessage());
    }
  }

  @Override
  public void stop() throws Exception {
    if (started) {
      recorder.close();
    }
    started = false;
  }
}
