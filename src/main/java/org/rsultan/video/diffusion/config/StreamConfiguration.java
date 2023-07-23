package org.rsultan.video.diffusion.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;

public class StreamConfiguration extends DiffusionConfiguration<FrameRecorder> {

  private boolean interleaved;
  private final Map<String, String> videoOptions = new HashMap<>();
  private int videoBitrate;
  private int videoCodec;

  private String format;
  private int frameRate;
  private int gopSizeInFrames;

  @Override
  public FrameRecorder build() {
    final FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
        address,
        width, height, 2);
    recorder.setInterleaved(interleaved);
    videoOptions.forEach(recorder::setVideoOption);
    recorder.setVideoBitrate(videoBitrate);
    recorder.setVideoCodec(videoCodec);
    recorder.setFormat(format);
    recorder.setFrameRate(frameRate);
    recorder.setGopSize(gopSizeInFrames);

    recorder.setAudioOption("crf", "0");
    recorder.setAudioQuality(0);
    recorder.setAudioBitrate(192000);
    recorder.setSampleRate(44100);
    recorder.setAudioChannels(2);
    recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
    return recorder;
  }

  public StreamConfiguration setAddress(String address) {
    this.address = address;
    return this;
  }

  public StreamConfiguration setWidth(int width) {
    this.width = width;
    return this;
  }

  public StreamConfiguration setHeight(int height) {
    this.height = height;
    return this;
  }

  public StreamConfiguration setInterleaved(boolean interleaved) {
    this.interleaved = interleaved;
    return this;
  }

  public StreamConfiguration setVideoBitrate(int videoBitrate) {
    this.videoBitrate = videoBitrate;
    return this;
  }

  public StreamConfiguration setVideoCodec(int videoCodec) {
    this.videoCodec = videoCodec;
    return this;
  }

  public StreamConfiguration setFormat(String format) {
    this.format = format;
    return this;
  }

  public StreamConfiguration setFrameRate(int frameRate) {
    this.frameRate = frameRate;
    return this;
  }

  public StreamConfiguration setGopSizeInFrames(int gopSizeInFrames) {
    this.gopSizeInFrames = gopSizeInFrames;
    return this;
  }

  public StreamConfiguration addVideoOption(String key, String value) {
    this.videoOptions.put(key, value);
    return this;
  }
}
