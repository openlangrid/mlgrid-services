package org.langrid.service.ml.interim;

import java.util.Map;

import org.langrid.service.ml.Point3d;

public class HumanPoseEstimation3dResult {
	public HumanPoseEstimation3dResult(){
	}
	public HumanPoseEstimation3dResult(int width, int height, Map<String, Point3d>[] poses) {
		this.width = width;
		this.height = height;
		this.poses = poses;
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
	public Map<String, Point3d>[] getPoses() {
		return poses;
	}
	public void setPoses(Map<String, Point3d>[] poses) {
		this.poses = poses;
	}

	private int width;
	private int height;
	private Map<String, Point3d>[] poses;
}
