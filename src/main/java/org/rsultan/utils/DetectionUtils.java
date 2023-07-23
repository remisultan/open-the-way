package org.rsultan.utils;

import static org.bytedeco.opencv.global.opencv_imgproc.putText;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_DUPLEX;

import java.util.List;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.rsultan.yolo.result.DetectionResult;

public class DetectionUtils {

  private static final float FONT_SCALE = 0.5f;
  private static final int THICKNESS = 2;
  private static final int LINE_TYPE = 12;
  private static final int SHIFT = 0;

  public static Mat draw(List<DetectionResult> results, Mat matFrame) {
    results.forEach(result -> {
      Point topLeft = new Point(result.x(), result.y());
      addText(matFrame, result, topLeft);
      addRectangle(matFrame, result, topLeft);
    });
    return matFrame;
  }

  private static void addRectangle(Mat matFrame, DetectionResult result, Point topLeft) {
    var bottomRight = new Point(result.x() + result.width(), result.y() + result.height());
    rectangle(matFrame, topLeft, bottomRight, result.color(), THICKNESS, LINE_TYPE, SHIFT);
  }

  private static void addText(Mat matFrame, DetectionResult result, Point topLeft) {
    var roundedConfidence = Math.round(result.confidence() * 10000f) / 100f;
    var textTopLeft = new Point(topLeft.x(), topLeft.y() - 5);
    String labelAndConfidence = result.className() + " - " + roundedConfidence;
    putText(matFrame, labelAndConfidence, textTopLeft, FONT_HERSHEY_DUPLEX, FONT_SCALE,
        result.color());
  }

}
