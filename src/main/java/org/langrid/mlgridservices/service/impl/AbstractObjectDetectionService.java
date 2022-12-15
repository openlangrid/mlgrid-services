package org.langrid.mlgridservices.service.impl;

import java.io.IOException;

import org.langrid.service.ml.Box2d;
import org.langrid.service.ml.interim.ObjectDetection;
import org.langrid.service.ml.interim.ObjectDetectionResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.util.ArrayUtil;
import lombok.Data;

public class AbstractObjectDetectionService {
	@Data
	static class Detection{
		private String label;
		private double conf;
		private double[] box;
	}
	@Data
	static class Result{
		private int width;
		private int height;
		private Detection[] results;
		private byte[][] masks;
	}

	protected ObjectDetectionResult buildResult(String result) throws IOException{
		var res = new ObjectMapper().createParser(result).readValueAs(Result.class);
		return new ObjectDetectionResult(res.width, res.height,
			ArrayUtil.collect(res.results, ObjectDetection.class, r->new ObjectDetection(
				r.getLabel(), r.getConf(),
				new Box2d(r.box[0], r.box[1], r.box[2] - r.box[0], r.box[3] - r.box[1])
				)));
	}
}
