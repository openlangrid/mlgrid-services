package org.langrid.service.ml.interim;

import org.langrid.service.ml.Box2d;

public class ObjectSegmentation {
	public ObjectSegmentation() {
	}
	public ObjectSegmentation(String label, double accuracy, Box2d box,
		byte[] maskImage, String maskImageFormat) {
		this.label = label;
		this.accuracy = accuracy;
		this.box = box;
		this.maskImage = maskImage;
		this.maskImageFormat = maskImageFormat;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	public Box2d getBox() {
		return box;
	}
	public void setBox(Box2d box) {
		this.box = box;
	}
	public byte[] getMaskImage() {
		return maskImage;
	}
	public void setMaskImage(byte[] maskImage) {
		this.maskImage = maskImage;
	}
	public String getMaskImageFormat() {
		return maskImageFormat;
	}
	public void setMaskImageFormat(String maskImageFormat) {
		this.maskImageFormat = maskImageFormat;
	}

	private String label;
	private double accuracy;
	private Box2d box;
	private byte[] maskImage;
	private String maskImageFormat;
}
