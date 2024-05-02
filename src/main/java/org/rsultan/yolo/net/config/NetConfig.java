package org.rsultan.yolo.net.config;

import java.util.List;
import org.bytedeco.opencv.opencv_dnn.Net;

public interface NetConfig {

  Net buildNet();

  List<String> getNames();

}
