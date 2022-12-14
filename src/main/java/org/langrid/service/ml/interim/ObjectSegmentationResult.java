package org.langrid.service.ml.interim;

public class ObjectSegmentationResult {
	public ObjectSegmentationResult() {
	}
	public ObjectSegmentationResult(int width, int height, ObjectSegmentation[] segmentations) {
		this.width = width;
		this.height = height;
		this.segmentations = segmentations;
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
	public ObjectSegmentation[] getSegmentations() {
		return segmentations;
	}
	public void setSegmentations(ObjectSegmentation[] segmentations) {
		this.segmentations = segmentations;
	}

	private int width;
	private int height;
	private ObjectSegmentation[] segmentations;
}
