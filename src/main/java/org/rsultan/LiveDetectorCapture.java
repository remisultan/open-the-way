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
import static org.rsultan.video.Constants.INPUT_TYPE;
import static org.rsultan.video.Constants.ADDRESS;
import static org.rsultan.video.Constants.TUNE;
import static org.rsultan.video.Constants.WIDTH;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.Frame;
import org.rsultan.exception.VideoEndedException;
import org.rsultan.utils.DetectionUtils;
import org.rsultan.video.diffusion.Diffusion;
import org.rsultan.video.diffusion.OutputType;
import org.rsultan.video.source.Source;
import org.rsultan.video.source.InputType;
import org.rsultan.yolo.net.YoloNet;
import org.rsultan.yolo.net.config.ModelFormat;
import org.rsultan.yolo.net.config.NetConfig;
import org.rsultan.yolo.result.DetectionResult;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class LiveDetectorCapture implements Runnable {

  private static List<DetectionResult> predictions;
  private static final AtomicInteger index = new AtomicInteger(0);
  private static final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

  /*****************************************/
  /***                                   ***/
  /***             YOLO Config           ***/
  /***                                   ***/
  /*****************************************/
  @Option(names = {"--model-format"}, showDefaultValue = ALWAYS, description = {
      "enables yolo detection on the stream", "default: DARKNET"})
  private ModelFormat modelFormat = ModelFormat.DARKNET;
  @Option(names = {"--yolo-enabled"}, showDefaultValue = ALWAYS, description = {
      "enables yolo detection on the stream", "default: false"})
  private boolean yoloEnabled = false;

  @Option(names = {"--cuda-enabled"}, showDefaultValue = ALWAYS, description = {
      "enables cuda detection if present", "fallbacks to CPU otherwise"})
  private boolean cudaEnabled = false;

  @Option(names = {"--yolo-path"}, showDefaultValue = ALWAYS, description = {
      "path to the yolo files",
      "the files within that folder must be named: config.cfg, names.txt, weights.weights",
      "eg: /path/to/yolov7 is the path that will contain: /path/to/yolov7/config.cfg, /path/to/yolov7/names.txt, /path/to/yolov7/weights.weights",})
  private String modelPath =
      System.getProperty("user.dir") + File.separator + "model" + File.separator + "yolov7";

  @Option(names = {"--confidence-threshold"}, description = {"the confidence detection threshold",
      "default: 0.4"})
  private float confidenceThreshold = 0.4f;

  @Option(names = {"--nms-threshold"}, showDefaultValue = ALWAYS, description = {
      "the NMS (Non-maximum Suppression) threshold", "more info: https://arxiv.org/abs/1705.02950"})
  private float nmsThreshold = 0.4f;

  @Option(names = {"--yolo-width"}, description = {"the yolo image blob width used for detection"})
  private int yoloWidth = 608;

  @Option(names = {"--yolo-height"}, showDefaultValue = ALWAYS, description = {
      "the yolo image blob height used for detection"})
  private int yoloHeight = 608;

  /*****************************************/
  /***                                   ***/
  /***        Video Config Input         ***/
  /***                                   ***/
  /*****************************************/

  @Option(names = {"--in-address"}, showDefaultValue = ALWAYS, description = {
      "The input address to use when fetching from a URL",
      "The URL depends on the forman (flv, mjpeg, ...) and is optional if using --in-type=direct",})
  private String inputAddress = "rtmp://localhost:1935/stream/hello";

  @Option(names = {"--in-type"}, showDefaultValue = ALWAYS, description = {
      "The input type of the stream"})
  private InputType inputType = InputType.DIRECT;

  @Option(names = {"--in-width"}, showDefaultValue = ALWAYS, description = {
      "The input width of the stream"})
  private int inputWidth = 1280;

  @Option(names = {"--in-height"}, showDefaultValue = ALWAYS, description = {
      "The input height of the stream"})
  private int inputHeight = 720;

  @Option(names = {"--in-bitrate"}, showDefaultValue = ALWAYS, description = {
      "The input bitrate of the stream"})
  private int inputBitrate = 2000000;

  @Option(names = {"--in-tune"}, showDefaultValue = ALWAYS, description = {
      "The input tune of the stream"})
  private String inputTune = "zerolatency";

  @Option(names = {"--in-preset"}, showDefaultValue = ALWAYS, description = {
      "The input preset of the stream"})
  private String inputPreset = "ultrafast";

  @Option(names = {"--in-crf"}, showDefaultValue = ALWAYS, description = {
      "The input crf of the stream"})
  private String inputCrf = "28";

  @Option(names = {"--in-codec"}, description = {
      "The input codec of the stream (default: AV_CODEC_ID_H264)"})
  private int inputCodec = avcodec.AV_CODEC_ID_H264;

  @Option(names = {"--in-format"}, showDefaultValue = ALWAYS, description = {
      "The input format of the stream"})
  private String inputFormat = "flv";

  @Option(names = {"--in-frame-rate"}, showDefaultValue = ALWAYS, description = {
      "The input frame rate of the stream"})
  private int inputFrameRate = DEFAULT_FRAME_RATE;

  /*****************************************/
  /***                                   ***/
  /***       Video Config Output         ***/
  /***                                   ***/
  /*****************************************/

  @Option(names = {"--out-address"}, showDefaultValue = ALWAYS, description = {
      "The output address to use when streaming on a URL",
      "The URL depends on the format (flv, mjpeg, ...) and is optional if using --out-type=direct",})
  private String outputAddress = "rtmp://localhost:1935/stream/hello";

  @Option(names = {"--out-type"}, showDefaultValue = ALWAYS, description = {
      "The output type of the stream"})
  private OutputType outputType = OutputType.DIRECT;

  @Option(names = {"--out-width"}, showDefaultValue = ALWAYS, description = {
      "The output width of the stream"})
  private int outputWidth = 1280;

  @Option(names = {"--out-height"}, showDefaultValue = ALWAYS, description = {
      "The output height of the stream"})
  private int outputHeight = 720;

  @Option(names = {"--out-bitrate"}, showDefaultValue = ALWAYS, description = {
      "The output bitrate of the stream"})
  private int outputBitrate = 2000000;

  @Option(names = {"--out-tune"}, showDefaultValue = ALWAYS, description = {
      "The output tune of the stream"})
  private String outputTune = "zerolatency";

  @Option(names = {"--out-preset"}, showDefaultValue = ALWAYS, description = {
      "The output preset of the stream"})
  private String outputPreset = "ultrafast";

  @Option(names = {"--out-crf"}, showDefaultValue = ALWAYS, description = {
      "The output crf of the stream"})
  private String outputCrf = "28";

  @Option(names = {"--out-codec"}, description = {"The output codec of the stream (default)"})
  private int outputCodec = avcodec.AV_CODEC_ID_H264;

  @Option(names = {"--out-format"}, showDefaultValue = ALWAYS, description = {
      "The output format of the stream"})
  private String outputFormat = "flv";

  @Option(names = {"--out-frame-rate"}, showDefaultValue = ALWAYS, description = {
      "The output framerate of the stream"})
  private int outputFrameRate = DEFAULT_FRAME_RATE;
  @Option(names = {"--gop"}, showDefaultValue = ALWAYS, description = {
      "The output GOP (Group Of Pictures) size of the stream"})
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
    final YoloNet yoloNet = buildDNN();
    var input = Source.get(inputType,
        Map.ofEntries(Map.entry(WIDTH, inputWidth), Map.entry(HEIGHT, inputHeight),
            Map.entry(ADDRESS, inputAddress), Map.entry(DEVICE_NUMBER, DEFAULT_DEVICE_NUMBER),
            Map.entry(BITRATE, inputBitrate), Map.entry(TUNE, inputTune),
            Map.entry(PRESET, inputPreset), Map.entry(CRF, inputCrf), Map.entry(CODEC, inputCodec),
            Map.entry(FORMAT, inputFormat), Map.entry(FRAME_RATE, inputFrameRate)));
    var diffusion = Diffusion.get(outputType,
        Map.ofEntries(Map.entry(INPUT_TYPE, input), Map.entry(ADDRESS, outputAddress),
            Map.entry(WIDTH, outputWidth), Map.entry(HEIGHT, outputHeight),
            Map.entry(BITRATE, outputBitrate), Map.entry(TUNE, outputTune),
            Map.entry(PRESET, outputPreset), Map.entry(CRF, outputCrf),
            Map.entry(CODEC, outputCodec), Map.entry(FORMAT, outputFormat), Map.entry(GOP, gop),
            Map.entry(FRAME_RATE, outputFrameRate)));

    try {
      Stream.generate(input::capture).map(opt -> opt.orElseThrow(() -> THE_CAPTURE_HAD_ENDED))
          .map(matFrame -> predict(yoloNet, matFrame)).forEachOrdered(diffusion::show);
    } catch (VideoEndedException vee) {
      //Do Nothing
    } catch (Throwable e) {
      e.printStackTrace();
    } finally {
      terminate(yoloNet, input, diffusion);
    }
  }

  private static Frame predict(YoloNet yolo, Frame frame) {
    if (yolo == null) {
      return frame;
    }

    var matFrame = converterToMat.convert(frame);
    executorService.submit(() -> {
      predictions = yolo.predict(matFrame);
      index.set(0);
    });
    if (predictions != null) {
      DetectionUtils.draw(predictions, matFrame);
    }
    return converterToMat.convert(matFrame);
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

  private YoloNet buildDNN() {
    YoloNet yolo = null;
    if (yoloEnabled) {
      NetConfig netConfig = modelFormat.toConfig(modelPath);
      yolo = new YoloNet(netConfig, yoloWidth, yoloHeight, confidenceThreshold,
          nmsThreshold, cudaEnabled).initialize();
    }
    return yolo;
  }
}