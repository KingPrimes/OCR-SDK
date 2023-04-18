package org.ocr.sdk.utils;

import ai.djl.MalformedModelException;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDList;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import org.ocr.sdk.ocr.common.RotatedBox;
import org.ocr.sdk.ocr.common.RotatedBoxCompX;
import org.ocr.sdk.ocr.detection.OcrV3Detection;
import org.ocr.sdk.ocr.recognition.OcrV3MultiThreadRecognition;
import org.ocr.sdk.ocr.recognition.OcrV3Recognition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ocr {

    /**
     * 多线程Ocr
     *
     * @param url 图片URL地址
     * @return 返回识别文字与文字坐标
     */
    public static List<Point> threadOcr(String url) throws IOException, ModelException, TranslateException {

        Image image = ImageFactory.getInstance().fromUrl(url);
        // 并发线程数，最大上限为 CPU 核数
        // Concurrent threads, with a maximum limit of CPU cores
        int threadNum = 4;
        List<Point> points = new ArrayList<>();
        OcrV3MultiThreadRecognition recognition = new OcrV3MultiThreadRecognition();
        try (ZooModel detectionModel = ModelZoo.loadModel(recognition.detectCriteria());
             Predictor<Image, DetectedObjects> detector = detectionModel.newPredictor()) {
            // 由于底层引擎原因，与其它引擎可以共享一个模型不同，paddle多线程需要每个线程加载一个模型
            // Due to engine limitations, unlike other engines that can share a model, paddle multi-threading requires each thread to load a model
            // 可以将paddle模型转换成ONNX，ONNX底层引擎支持的更好
            // Paddle models can be converted to ONNX, which is better supported by the ONNX engine.
            List<ZooModel> recModels = new ArrayList<>();
            try {
                for (int i = 0; i < threadNum; i++) {
                    ZooModel recognitionModel = ModelZoo.loadModel(recognition.recognizeCriteria());
                    recModels.add(recognitionModel);
                }

                long timeInferStart = System.currentTimeMillis();
                DetectedObjects detections = recognition.predict(image, recModels, detector, threadNum);
                long timeInferEnd = System.currentTimeMillis();
                System.out.println("time: " + (timeInferEnd - timeInferStart));
                List<DetectedObjects.DetectedObject> boxes = detections.items();
                for (DetectedObjects.DetectedObject result : boxes) {
                    points.add(new Point(result.getBoundingBox().getPoint().getX(), result.getBoundingBox().getPoint().getY(), result.getClassName()));
                }
                Collections.sort(points);
                return points;
            } catch (IOException | ModelNotFoundException | MalformedModelException e) {
                e.printStackTrace();
            } finally {
                for (ZooModel recognitionModel : recModels) {
                    recognitionModel.close();
                }
            }
            return points;
        }
    }

    /**
     * 单线程OCR识别文字
     * @param url 图片URL
     * @return 识别结果
     */
    public static String ocr(String url) throws IOException, ModelException, TranslateException{
        Image image = OpenCVImageFactory.getInstance().fromUrl(url);
        //Image image = OpenCVImageFactory.getInstance().fromFile(imageFile);

        OcrV3Detection detection = new OcrV3Detection();
        OcrV3Recognition recognition = new OcrV3Recognition();
        try (ZooModel detectionModel = ModelZoo.loadModel(detection.detectCriteria());
             Predictor<Image, NDList> detector = detectionModel.newPredictor();
             ZooModel recognitionModel = ModelZoo.loadModel(recognition.recognizeCriteria());
             Predictor<Image, String> recognizer = recognitionModel.newPredictor()) {

            long timeInferStart = System.currentTimeMillis();
            List<RotatedBox> detections = recognition.predict(image, detector, recognizer);

            long timeInferEnd = System.currentTimeMillis();
            System.out.println("time: " + (timeInferEnd - timeInferStart));


            // 对检测结果根据坐标位置，根据从上到下，从做到右，重新排序，下面算法对图片倾斜旋转角度较小的情形适用
            // 如果图片旋转角度较大，则需要自行改进算法，需要根据斜率校正计算位置。
            // Reorder the detection results based on the coordinate positions, from top to bottom, from left to right. The algorithm below is suitable for situations where the image is slightly tilted or rotated.
            // If the image rotation angle is large, the algorithm needs to be improved, and the position needs to be calculated based on the slope correction.
            List<RotatedBox> initList = new ArrayList<>();
            for (RotatedBox result : detections) {
                // put low Y value at the head of the queue.
                initList.add(result);
            }
            Collections.sort(initList);

            List<ArrayList<RotatedBoxCompX>> lines = new ArrayList<>();
            List<RotatedBoxCompX> line = new ArrayList<>();
            RotatedBoxCompX firstBox = new RotatedBoxCompX(initList.get(0).getBox(), initList.get(0).getText());
            line.add(firstBox);
            lines.add((ArrayList) line);
            for (int i = 1; i < initList.size(); i++) {
                RotatedBoxCompX tmpBox = new RotatedBoxCompX(initList.get(i).getBox(), initList.get(i).getText());
                float y1 = firstBox.getBox().toFloatArray()[1];
                float y2 = tmpBox.getBox().toFloatArray()[1];
                float dis = Math.abs(y2 - y1);
                if (dis < 32) { // 认为是同 1 行  - Considered to be in the same line
                    line.add(tmpBox);
                } else { // 换行 - Line break
                    firstBox = tmpBox;
                    Collections.sort(line);
                    line = new ArrayList<>();
                    line.add(firstBox);
                    lines.add((ArrayList) line);
                }
            }
            StringBuilder fullText = new StringBuilder();
            for (ArrayList<RotatedBoxCompX> rotatedBoxCompXES : lines) {
                for (RotatedBoxCompX rotatedBoxCompX : rotatedBoxCompXES) {
                    //System.out.println("j: "+lines.get(i).get(j).getText());
                    fullText.append(rotatedBoxCompX.getText()).append("\t");
                }
                fullText.append('\n');
            }

           return fullText.toString();
        }
    }
}
