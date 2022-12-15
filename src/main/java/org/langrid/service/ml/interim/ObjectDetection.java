package org.langrid.service.ml.interim;

import org.langrid.service.ml.Box2d;

public class ObjectDetection {
	public ObjectDetection() {
	}
	public ObjectDetection(String label, double accuracy, Box2d box) {
		this.box = box;
		this.label = label;
		this.accuracy = accuracy;
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

	private String label;
	private double accuracy;
	private Box2d box;
}
