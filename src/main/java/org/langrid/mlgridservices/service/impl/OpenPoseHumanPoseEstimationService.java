package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.service.ml.HumanPoseEstimation3dResult;
import org.langrid.service.ml.HumanPoseEstimation3dService;
import org.langrid.service.ml.Point3d;

import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class OpenPoseHumanPoseEstimationService implements HumanPoseEstimation3dService{
	public OpenPoseHumanPoseEstimationService(){
	}

	@Override
	@SuppressWarnings("unchecked")
	public HumanPoseEstimation3dResult estimate(byte[] image, String imageFormat)
	throws UnsupportedLanguageException{
		try {
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var infile = FileUtil.createUniqueFileWithDateTime(tempDir, "image-", ".jpg");
			var outJsonFile = infile.toString() + ".out.json";
			Files.write(infile.toPath(), image);
			run("run.py", infile);
			var poses = new ArrayList<Map<String, Point3d>>();
			var json = Files.readString(Path.of(outJsonFile));
			var result = mapper.readValue(json, Map.class);
			for(Map<String, List<Double>> r : (List<Map<String, List<Double>>>)result.get("poses")){
				var h = new HashMap<String, Point3d>();
				for(Map.Entry<String, List<Double>> e : r.entrySet()){
					h.put(e.getKey(), new Point3d(e.getValue().get(0),
						e.getValue().get(1), e.getValue().get(2)));
				}
				poses.add(h);
			}
			return new HumanPoseEstimation3dResult(
				((Number)result.get("width")).intValue(),
				((Number)result.get("height")).intValue(),
				poses.toArray(new Map[]{}));
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String run(String scriptName, File imgFile){
		try(var l = GPULock.acquire()){
			var cmd = "PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm "
					+ dockerServiceName + " python " + scriptName + " temp/" + imgFile.getName();
			var pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(baseDir);
			try(var t = ServiceInvokerContext.startServiceTimer()){
				var proc = pb.start();
				try {
					proc.waitFor();
					t.close();
					var res = proc.exitValue();
					if(res == 0) {
						var br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
						String line = null;
						while ((line = br.readLine()) != null) {
							if(!line.startsWith("DONE")) continue;
							return br.toString();
						}
						throw new RuntimeException("no results found");
					} else {
						var br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
						var lines = new StringBuilder();
						String line = null;
						while ((line = br.readLine()) != null) {
							lines.append(line);
						}
						throw new RuntimeException(lines.toString());
					}
				} finally {
					proc.destroy();
				}
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private String dockerServiceName = "openpose";
	private ObjectMapper mapper = new ObjectMapper();
	private File baseDir = new File("./procs/human_pose_estimation_openpose");
}
