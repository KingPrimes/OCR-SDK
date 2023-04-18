package org.ocr.sdk.ocr.detection;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public final class OcrV3Detection {

    private static final Logger logger = LoggerFactory.getLogger(OcrV3Detection.class);

    public OcrV3Detection() {
    }

    public Criteria<Image, NDList> detectCriteria() {
        //System.out.println(Paths.get("src/resources/ch_PP-OCRv3_det_infer.zip"));
        Criteria<Image, NDList> criteria =
                Criteria.builder()
                        .optEngine("PaddlePaddle")
                        .setTypes(Image.class, NDList.class)
                        //.optModelUrls("jar:///ch_PP-OCRv3_det_infer.zip")
                        .optModelPath(Paths.get("models/ch_PP-OCRv3_det_infer.zip"))
                        .optTranslator(new OCRDetectionTranslator(new ConcurrentHashMap<String, String>()))
                        .optProgress(new ProgressBar())
                        .build();

        return criteria;
    }
}
