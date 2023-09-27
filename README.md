# Open the Way
This is a repository to play with OpenCV and yolo object detection model

Inspired by: 
  - [YOLONet example](https://github.com/bytedeco/javacv/blob/master/samples/YOLONet.java)
  - [Webcam Capture](https://github.com/bytedeco/javacv/blob/master/samples/WebcamAndMicrophoneCapture.java)

## Requirements

- Java 20
- A default video camera (for demo purposes)
- If using CUDA, you will need the appropriate GPU with the correct drivers
- Docker, if you wish to run a local rtmp server

## Quick start

First run
```
$ ./mvnw clean install
```

And execute with
```
$ ./mvnw exec:java
```

You should see an Canvas Frame appearing

## YOLO object detection

In order to use object detection, download the official [YOLOv7 files](https://github.com/pjreddie/darknet/issues/2557#issue-1296705813)
Once downloaded put them at the root of the project this way
```
 model/yolov7/config.cfg
 model/yolov7/names.txt
 model/yolov7/weights.weights
```

Then execute:

```
$ ./mvnw exec:java "-Dexec.args=--yolo-enabled --cuda-enabled"
```

If you wish to set your own path you can set it with the `--yolo-path` option 


## Read or write from a stream

You can read/write from a rtmp stream using respectively the option 
`--input-type=stream` `--output-type=stream` 

In order to read from a rtmp server or to stream on it you can use
this project: [nginx-rtmp-docker](https://github.com/tiangolo/nginx-rtmp-docker/)

Start the project in a container this way:
```
docker run  -it --cpus 1 --memory 512m  -p 1935:1935 -p 8080:80 --rm alfg/nginx-rtmp
```
Then:
- push streaming content to your server:
```
$ ./mvnw exec:java "-Dexec.args=--output-type=stream"
```

- read that content, apply object detection and display it:
```
$ ./mvnw exec:java "-Dexec.args=--input-type=stream --yolo-enabled --cuda-enabled"
```

You should see the content streamed from your webcam

## Full command usage

```
Usage: <main class> [--cuda-enabled] [--yolo-enabled]
                    [--confidence-threshold=<confidenceThreshold>]
                    [--gop=<gop>] [--in-address=<inputAddress>]
                    [--in-bitrate=<inputBitrate>] [--in-codec=<inputCodec>]
                    [--in-crf=<inputCrf>] [--in-format=<inputFormat>]
                    [--in-frame-rate=<inputFrameRate>]
                    [--in-height=<inputHeight>] [--in-preset=<inputPreset>]
                    [--in-tune=<inputTune>] [--in-type=<inputType>]
                    [--in-width=<inputWidth>] [--nms-threshold=<nmsThreshold>]
                    [--out-address=<outputAddress>]
                    [--out-bitrate=<outputBitrate>] [--out-codec=<outputCodec>]
                    [--out-crf=<outputCrf>] [--out-format=<outputFormat>]
                    [--out-frame-rate=<outputFrameRate>]
                    [--out-height=<outputHeight>] [--out-preset=<outputPreset>]
                    [--out-tune=<outputTune>] [--out-type=<outputType>]
                    [--out-width=<outputWidth>] [--yolo-height=<yoloHeight>]
                    [--yolo-path=<yoloPath>] [--yolo-width=<yoloWidth>]
      --confidence-threshold=<confidenceThreshold>
                            the confidence detection threshold
                            default: 0.4
      --cuda-enabled        enables cuda detection if present
                            fallbacks to CPU otherwise
                              Default: false
      --gop=<gop>           The output GOP (Group Of Pictures) size of the
                              stream
                              Default: 60
      --in-address=<inputAddress>
                            The input address to use when fetching from a URL
                            The URL depends on the forman (flv, mjpeg, ...) and
                              is optional if using --in-type=direct
                              Default: rtmp://localhost:1935/stream/hello
      --in-bitrate=<inputBitrate>
                            The input bitrate of the stream
                              Default: 2000000
      --in-codec=<inputCodec>
                            The input codec of the stream (default:
                              AV_CODEC_ID_H264)
      --in-crf=<inputCrf>   The input crf of the stream
                              Default: 28
      --in-format=<inputFormat>
                            The input format of the stream
                              Default: flv
      --in-frame-rate=<inputFrameRate>
                            The input frame rate of the stream
                              Default: 60
      --in-height=<inputHeight>
                            The input height of the stream
                              Default: 720
      --in-preset=<inputPreset>
                            The input preset of the stream
                              Default: ultrafast
      --in-tune=<inputTune> The input tune of the stream
                              Default: zerolatency
      --in-type=<inputType> The input type of the stream
                              Default: DIRECT
      --in-width=<inputWidth>
                            The input width of the stream
                              Default: 1280
      --nms-threshold=<nmsThreshold>
                            the NMS (Non-maximum Suppression) threshold
                            more info: https://arxiv.org/abs/1705.02950
                              Default: 0.4
      --out-address=<outputAddress>
                            The output address to use when streaming on a URL
                            The URL depends on the format (flv, mjpeg, ...) and
                              is optional if using --out-type=direct
                              Default: rtmp://localhost:1935/stream/hello
      --out-bitrate=<outputBitrate>
                            The output bitrate of the stream
                              Default: 2000000
      --out-codec=<outputCodec>
                            The output codec of the stream (default)
      --out-crf=<outputCrf> The output crf of the stream
                              Default: 28
      --out-format=<outputFormat>
                            The output format of the stream
                              Default: flv
      --out-frame-rate=<outputFrameRate>
                            The output framerate of the stream
                              Default: 60
      --out-height=<outputHeight>
                            The output height of the stream
                              Default: 720
      --out-preset=<outputPreset>
                            The output preset of the stream
                              Default: ultrafast
      --out-tune=<outputTune>
                            The output tune of the stream
                              Default: zerolatency
      --out-type=<outputType>
                            The output type of the stream
                              Default: DIRECT
      --out-width=<outputWidth>
                            The output width of the stream
                              Default: 1280
      --yolo-enabled        enables yolo detection on the stream
                            default: false
                              Default: false
      --yolo-height=<yoloHeight>
                            the yolo image blob height used for detection
                              Default: 608
      --yolo-path=<yoloPath>
                            path to the yolo files
                            the files within that folder must be named: config.
                              cfg, names.txt, weights.weights
                            eg: /path/to/yolov7 is the path that will contain:
                              /path/to/yolov7/config.cfg, /path/to/yolov7/names.
                              txt, /path/to/yolov7/weights.weights
                              Default: C:
                              \Users\33646\dev\open-the-way\model\yolov7
      --yolo-width=<yoloWidth>
                            the yolo image blob width used for detection
```
