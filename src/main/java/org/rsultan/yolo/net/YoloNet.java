/*
  MIT License

  Copyright (c) 2021 Florian Bruggisser

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
*/
package org.rsultan.yolo.net;

import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.opencv_core.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.rsultan.video.Constants;
import org.rsultan.yolo.result.DetectionResult;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;
import static org.bytedeco.opencv.global.opencv_core.getCudaEnabledDeviceCount;
import static org.bytedeco.opencv.global.opencv_dnn.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

public class YoloNet {

  private final Path configPath;
  private final Path weightsPath;
  private final int width;
  private final int height;
  private final boolean cudaEnabled;
  private Net net;
  private StringVector outNames;
  private final ResultExtractor resultExtractor;

  public YoloNet(String configPath, String weightsPath, String namesPath,
      int width, int height,
      float confidenceThreshold, float nmsThreshold, boolean cudaEnabled) {
    this.configPath = Paths.get(configPath);
    this.weightsPath = Paths.get(weightsPath);
    this.width = width;
    this.height = height;
    this.cudaEnabled = cudaEnabled;
    resultExtractor = new ResultExtractor(confidenceThreshold, nmsThreshold, namesPath);

  }

  public boolean initialize() {
    net = readNetFromDarknet(
        configPath.toAbsolutePath().toString(),
        weightsPath.toAbsolutePath().toString());

    outNames = net.getUnconnectedOutLayersNames();

    if (cudaEnabled && getCudaEnabledDeviceCount() > 0) {
      net.setPreferableBackend(opencv_dnn.DNN_BACKEND_CUDA);
      net.setPreferableTarget(opencv_dnn.DNN_TARGET_CUDA);
    }

    boolean netIsNotEmpty = !net.empty();
    if (netIsNotEmpty) {
      // We arbitrarily initialize the first image since the first forward takes time
      Path tmpFile = null;
      try {
        tmpFile = Files.createTempFile(null, "jpg");
        Files.write(tmpFile, getClass().getClassLoader().getResourceAsStream(Constants.IMAGE).readAllBytes());
        var image = imread(tmpFile.toString());
        netForward(getBlobFromImage(image)).releaseReference();
        image.release();
        Files.deleteIfExists(tmpFile);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return netIsNotEmpty;
  }

  private MatVector netForward(Mat frame) {
    net.setInput(frame);
    MatVector outs = new MatVector(outNames.size());
    net.forward(outs, outNames);
    return outs;
  }

  private Mat getBlobFromImage(Mat frame) {
    return blobFromImage(frame,
        1 / 255.0,
        new Size(width, height),
        new Scalar(0.0),
        true, false, CV_32F);
  }

  public synchronized List<DetectionResult> predict(Mat frame) {
    Mat image = getBlobFromImage(frame);
    var outs = netForward(image);

    var result = resultExtractor.getDetectionResults(frame, outs);

    outs.releaseReference();
    image.release();

    return result;
  }

  public void stop() {
    net.close();
  }
}