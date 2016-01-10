package co.mide.translator;

import java.util.ArrayList;

/**
 * Class used in gson for parsing the json response from google.
 * Created by Olumide on 1/3/2016.
 */
public class LangDetectionResult {
    public DetectionDataResult data;
}

class DetectionDataResult {
    public ArrayList<ArrayList<DetectedLanguage>> detections = new ArrayList<>();
}