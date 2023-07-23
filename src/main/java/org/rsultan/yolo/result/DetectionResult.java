package org.rsultan.yolo.result;

import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;

/**
 * Dataclass for object detection result.
 */
public record DetectionResult(int classId, String className, Scalar color,
                              float confidence, int x, int y,
                              int width, int height) {

  public DetectionResult(int clsId, String className, Scalar scalar, float confidence, Rect box) {
    this(clsId, className, scalar, confidence, box.x(), box.y(), box.width(), box.height());
  }
}