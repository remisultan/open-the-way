package org.rsultan.yolo.net;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.bytedeco.opencv.global.opencv_core.doubleRand;
import static org.bytedeco.opencv.global.opencv_dnn.NMSBoxes;
import static org.bytedeco.opencv.helper.opencv_core.RGB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_text.FloatVector;
import org.bytedeco.opencv.opencv_text.IntVector;
import org.rsultan.yolo.result.DetectionResult;

class ResultExtractor {

  private final float confidenceThreshold;
  private final float nmsThreshold;
  private final List<String> names;
  private final Map<String, Scalar> colors;

  public ResultExtractor(float confidenceThreshold, float nmsThreshold, String namesPath) {
    this.confidenceThreshold = confidenceThreshold;
    this.nmsThreshold = nmsThreshold;
    this.names = getNames(namesPath);
    this.colors = getColors(this.names);
  }

  private static Map<String, Scalar> getColors(List<String> names) {
    return names.stream()
        .map(name -> Map.entry(name,
            RGB(doubleRand() * 255, doubleRand() * 255, doubleRand() * 255)))
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  private static List<String> getNames(String namesPath) {
    try {
      return Files.readAllLines(Paths.get(namesPath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<DetectionResult> getDetectionResults(Mat frame, MatVector outs) {
    final IntVector classIds = new IntVector();
    final FloatVector confidences = new FloatVector();
    final RectVector boxes = new RectVector();

    extractRelevantBoxes(frame, outs, classIds, confidences, boxes);

    IntPointer indices = new IntPointer(confidences.size());
    FloatPointer confidencesPointer = new FloatPointer(confidences.size());

    removeOverlappingBoxes(confidences, boxes, indices, confidencesPointer);

    var detections = range(0, (int) indices.limit()).mapToObj(i -> {
      var idx = indices.get(i);
      var box = boxes.get(idx);
      var classId = classIds.get(idx);

      var className = names.get(classId);
      var detectionResult = new DetectionResult(classId, className, colors.get(className),
          confidences.get(idx),
          box);
      box.releaseReference();
      return detectionResult;
    }).toList();

    releaseReferences(classIds, confidences, boxes, indices, confidencesPointer);

    return detections;
  }

  private static void releaseReferences(IntVector classIds, FloatVector confidences, RectVector boxes,
      IntPointer indices, FloatPointer confidencesPointer) {
    indices.releaseReference();
    confidencesPointer.releaseReference();
    classIds.releaseReference();
    confidences.releaseReference();
    boxes.releaseReference();
  }

  private void removeOverlappingBoxes(FloatVector confidences, RectVector boxes, IntPointer indices,
      FloatPointer confidencesPointer) {
    confidencesPointer.put(confidences.get());
    NMSBoxes(boxes, confidencesPointer, confidenceThreshold, nmsThreshold, indices, 1.f, 0);
  }

  private void extractRelevantBoxes(Mat frame, MatVector outs, IntVector classIds,
      FloatVector confidences,
      RectVector boxes) {
    for (int i = 0; i < outs.size(); ++i) {
      Mat result = outs.get(i);
      FloatIndexer data = result.createIndexer();
      for (int j = 0; j < result.rows(); j++) {
        var minMaxLoc = getMinmaxLocResult(result, data, j);
        if (minMaxLoc.maxScore() > confidenceThreshold) {
          pushDetectedObject(frame, classIds, confidences, boxes, data, j, minMaxLoc);
        }
      }
      data.release();
      result.release();
    }
  }

  private static MinmaxLocResult getMinmaxLocResult(Mat result, FloatIndexer data, int j) {
    int maxIndex = -1;
    float maxScore = Float.MIN_VALUE;
    for (int k = 5; k < result.cols(); k++) {
      float score = data.get(j, k);
      if (score > maxScore) {
        maxScore = score;
        maxIndex = k - 5;
      }
    }
    return new MinmaxLocResult(maxIndex, maxScore);
  }

  private record MinmaxLocResult(int maxIndex, float maxScore) {}

  private static void pushDetectedObject(
      Mat frame,
      IntVector classIds,
      FloatVector confidences,
      RectVector boxes,
      FloatIndexer data,
      int j,
      MinmaxLocResult result) {
    int centerX = (int) (data.get(j, 0) * frame.cols());
    int centerY = (int) (data.get(j, 1) * frame.rows());
    int width = (int) (data.get(j, 2) * frame.cols());
    int height = (int) (data.get(j, 3) * frame.rows());
    int left = centerX - width / 2;
    int top = centerY - height / 2;

    classIds.push_back(result.maxIndex);
    confidences.push_back(result.maxScore);

    boxes.push_back(new Rect(left, top, width, height));
  }
}
