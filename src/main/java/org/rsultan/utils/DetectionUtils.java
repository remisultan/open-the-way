package org.rsultan.utils;

import static org.bytedeco.opencv.global.opencv_core.addWeighted;
import static org.bytedeco.opencv.global.opencv_imgproc.putText;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.FILLED;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_DUPLEX;

import java.util.List;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
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
    Mat overlay = new Mat(matFrame.size(), matFrame.type());
    matFrame.copyTo(overlay);
    rectangle(overlay, topLeft, bottomRight, result.color(), FILLED, LINE_TYPE, SHIFT);

    double alpha = 0.5d;
    double beta = 1 - alpha;
    addWeighted(overlay, alpha, matFrame, beta, 0.0, matFrame);
  }

  private static void addText(Mat matFrame, DetectionResult result, Point topLeft) {
    var roundedConfidence = Math.round(result.confidence() * 10000f) / 100f;
    var textTopLeft = new Point(topLeft.x(), topLeft.y() + 10);
    String labelAndConfidence = result.className() + " - " + roundedConfidence;
    putText(matFrame, labelAndConfidence, textTopLeft, FONT_HERSHEY_DUPLEX, FONT_SCALE, new Scalar(0));
  }

}
