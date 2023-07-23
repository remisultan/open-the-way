package org.rsultan;

import static org.bytedeco.javacv.OpenCVFrameConverter.*;
import static org.rsultan.video.Constants.BITRATE;
import static org.rsultan.video.Constants.CODEC;
import static org.rsultan.video.Constants.CRF;
import static org.rsultan.video.Constants.DEFAULT_DEVICE_NUMBER;
import static org.rsultan.video.Constants.DEVICE_NUMBER;
import static org.rsultan.video.Constants.DEFAULT_FRAME_RATE;
import static org.rsultan.video.Constants.DEFAULT_GOP_LENGTH_IN_FRAMES;
import static org.rsultan.video.Constants.FORMAT;
import static org.rsultan.video.Constants.FRAME_RATE;
import static org.rsultan.video.Constants.GOP;
import static org.rsultan.video.Constants.HEIGHT;
import static org.rsultan.video.Constants.PRESET;
import static org.rsultan.video.Constants.SOURCE;
import static org.rsultan.video.Constants.DIFFUSION_ADDRESS;
import static org.rsultan.video.Constants.SOURCE_ADDRESS;
import static org.rsultan.video.Constants.TUNE;
import static org.rsultan.video.Constants.WIDTH;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.Frame;
import org.rsultan.exception.VideoEndedException;
import org.rsultan.utils.DetectionUtils;
import org.rsultan.video.diffusion.Diffusion;
import org.rsultan.video.diffusion.DiffusionType;
import org.rsultan.video.source.Source;
import org.rsultan.video.source.SourceType;
import org.rsultan.yolo.net.YoloNet;
import org.rsultan.yolo.result.DetectionResult;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class LiveDetectorCapture implements Runnable {

  private static List<DetectionResult> predictions;
  private static final AtomicInteger index = new AtomicInteger(0);
  private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

  /*****************************************/
  /***                                   ***/
  /***             YOLO Config           ***/
  /***                                   ***/
  /*****************************************/
  @Option(names = {"--yolo-enabled"})
  private boolean yoloEnabled = false;

  @Option(names = {"--cuda-enabled"})
  private boolean cudaEnabled = false;

  @Option(names = {"--yolo-path"})
  private String yoloPath =
      System.getProperty("user.dir") + File.separator + "model" + File.separator + "yolov7x";

  @Option(names = {"--confidence-threshold"})
  private float confidenceThreshold = 0.4f;

  @Option(names = {"--nms-threshold"})
  private float nmsThreshold = 0.4f;

  @Option(names = {"--yolo-width"})
  private int yoloWidth = 608;

  @Option(names = {"-yolo-height"})
  private int yoloHeight = 608;

  /*****************************************/
  /***                                   ***/
  /***          Video Config             ***/
  /***                                   ***/
  /*****************************************/

  @Option(names = {"--source-address"})
  private String sourceAddress = "rtmp://localhost:1935/stream/hello";

  @Option(names = {"--source-type"})
  private SourceType sourceType = SourceType.DIRECT;

  @Option(names = {"--width"})
  private int width = 1280;

  @Option(names = {"--height"})
  private int height = 720;

  @Option(names = {"--diffusion-address"})
  private String diffusionAddress = "rtmp://localhost:1935/stream/hello";

  @Option(names = {"--diffusion-type"})
  private DiffusionType diffusionType = DiffusionType.DIRECT;

  @Option(names = {"--bitrate"})
  private int bitrate = 2000000;

  @Option(names = {"--tune"})
  private String tune = "zerolatency";

  @Option(names = {"--preset"})
  private String preset = "ultrafast";

  @Option(names = {"--crf"})
  private String crf = "28";

  @Option(names = {"--codec"})
  private int codec = avcodec.AV_CODEC_ID_H264;

  @Option(names = {"--format"})
  private String format = "flv";

  @Option(names = {"--frame-rate"})
  private int frameRate = DEFAULT_FRAME_RATE;

  @Option(names = {"--gop"})
  private int gop = DEFAULT_GOP_LENGTH_IN_FRAMES;

  public static final VideoEndedException THE_CAPTURE_HAD_ENDED = new VideoEndedException(
      "The capture had ended");

  private static final ToMat converterToMat = new ToMat();

  public static void main(String[] args) {
    CommandLine cmd = new CommandLine(
        new LiveDetectorCapture()).setCaseInsensitiveEnumValuesAllowed(true);
    System.exit(cmd.execute(args));
  }

  @Override
  public void run() {
    final YoloNet yoloNet = buildYolo();
    var source = Source.get(sourceType, Map.ofEntries(
        Map.entry(WIDTH, width),
        Map.entry(HEIGHT, height),
        Map.entry(SOURCE_ADDRESS, sourceAddress),
        Map.entry(DEVICE_NUMBER, DEFAULT_DEVICE_NUMBER),
        Map.entry(BITRATE, bitrate),
        Map.entry(TUNE, tune),
        Map.entry(PRESET, preset),
        Map.entry(CRF, crf),
        Map.entry(CODEC, codec),
        Map.entry(FORMAT, format),
        Map.entry(GOP, gop),
        Map.entry(FRAME_RATE, frameRate)
    ));
    var diffusion = Diffusion.get(diffusionType, Map.ofEntries(
        Map.entry(SOURCE, source),
        Map.entry(DIFFUSION_ADDRESS, diffusionAddress),
        Map.entry(WIDTH, width),
        Map.entry(HEIGHT, height),
        Map.entry(BITRATE, bitrate),
        Map.entry(TUNE, tune),
        Map.entry(PRESET, preset),
        Map.entry(CRF, crf),
        Map.entry(CODEC, codec),
        Map.entry(FORMAT, format),
        Map.entry(GOP, gop),
        Map.entry(FRAME_RATE, frameRate)
    ));

    try {
      Stream.generate(source::capture)
          .map(LiveDetectorCapture::getFrame)
          .map(matFrame -> predict(yoloNet, matFrame))
          .forEachOrdered(diffusion::show);
    } catch (VideoEndedException vee) {
      //Do Nothing
    } catch (Throwable e) {
      e.printStackTrace();
    } finally {
      terminate(yoloNet, source, diffusion);
    }
  }

  private static Frame predict(YoloNet yolo, Frame frame) {
    if (yolo == null) {
      return frame;
    }

    var matFrame = converterToMat.convert(frame);
    if (index.incrementAndGet() % 8 != 0) {
      executorService.submit(() -> {
        predictions = yolo.predict(matFrame);
        index.set(0);
      });
    }
    if (predictions != null) {
      DetectionUtils.draw(predictions, matFrame);
    }
    return converterToMat.convert(matFrame);
  }

  private static Frame getFrame(Optional<Frame> opt) {
    return opt.orElseThrow(() -> THE_CAPTURE_HAD_ENDED);
  }

  private static void terminate(YoloNet yoloNet, Source source, Diffusion diffusion) {
    try {
      source.stop();
      diffusion.stop();
      if (yoloNet != null) {
        yoloNet.stop();
      }
      executorService.shutdownNow();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private YoloNet buildYolo() {
    YoloNet yolo = null;
    if (yoloEnabled) {
      yolo = new YoloNet(
          yoloPath + File.separator + "config.cfg",
          yoloPath + File.separator + "weights.weights",
          yoloPath + File.separator + "names.txt",
          yoloWidth, yoloHeight, confidenceThreshold, nmsThreshold, cudaEnabled);

      yolo.initialize();
    }
    return yolo;
  }
}