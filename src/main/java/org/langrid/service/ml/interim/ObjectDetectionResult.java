package org.langrid.service.ml.interim;

public class ObjectDetectionResult {
	public ObjectDetectionResult(){
	}
	public ObjectDetectionResult(int width, int height, ObjectDetection[] detections) {
		this.width = width;
		this.height = height;
		this.detections = detections;
	}

	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public ObjectDetection[] getDetections() {
		return detections;
	}
	public void setDetections(ObjectDetection[] detections) {
		this.detections = detections;
	}

	private int width;
	private int height;
	private ObjectDetection[] detections;
}
