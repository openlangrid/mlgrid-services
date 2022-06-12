package org.langrid.service.ml;

import java.util.Map;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public interface HumanPoseEstimation3dService {
	Map<String, Point3d>[] estimate(
		String format, byte[] image, int maxResults
	)
	throws InvalidParameterException, ProcessFailedException;
}
