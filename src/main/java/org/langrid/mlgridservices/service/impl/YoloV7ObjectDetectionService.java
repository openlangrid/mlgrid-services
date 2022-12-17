package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.service.ml.interim.ObjectDetectionResult;
import org.langrid.service.ml.interim.ObjectDetectionService;

import jp.go.nict.langrid.commons.io.FileNameUtil;
import jp.go.nict.langrid.commons.io.StreamUtil;

public class YoloV7ObjectDetectionService
extends AbstractObjectDetectionService
implements ObjectDetectionService{
	public YoloV7ObjectDetectionService(){
	}
	public YoloV7ObjectDetectionService(String modelName){
		this.modelName = modelName;
	}

	@Override
	public ObjectDetectionResult detect(byte[] image, String imageFormat, 
			String labelLanguage) {
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFileWithDateTime(tempDir, "image-", ".jpg");
			Files.write(temp.toPath(), image);
			return buildResult(run(modelName, temp.getName()));
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public String run(String modelName, String imgFile){
		try(var l = GPULock.acquire()){
			var cmd = "PATH=$PATH:/usr/local/bin /usr/local/bin/"
					+ "docker-compose run --rm "
					+ "-v $(pwd)/detect.py:/workspace/yolov7/detect.py "
					+ "-v $(pwd)/temp/" + imgFile + ":/workspace/yolov7/" + imgFile + " "
					+ "-v $(pwd)/temp/" + imgFile + "_result:/workspace/yolov7/runs/detect/prj "
					+ "service "
					+ "python detect.py --source " + imgFile + " "
					+ "--weights /work/weights/" + modelName + " "
					+ "--conf 0.25 --img-size 1280 --device 0 --save-txt --nosave "
					+ "--name prj --no-trace --save-conf"
				  ;
			var pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(baseDir);
			pb.redirectErrorStream(true);
			try(var t = ServiceInvokerContext.startServiceTimer()){
				var proc = pb.start();
				try {
					proc.waitFor();
					t.close();
					var res = proc.exitValue();
					var outDir = new File(new File(baseDir, "temp"), imgFile + "_result");
					var outFile = new File(outDir, FileNameUtil.changeExt(imgFile, "txt"));
					if(res == 0) {
						if(outFile.exists()){
							return new String(Files.readAllBytes(outFile.toPath()), StandardCharsets.UTF_8);
						} else{
							throw new RuntimeException("result not generated: " +
								StreamUtil.readAsString(proc.getInputStream(), "UTF-8")
							);
						}
					} else {
						throw new RuntimeException(StreamUtil.readAsString(
							proc.getInputStream(), "UTF-8"));
					}
				} finally {
					proc.destroy();
				}
			}
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private String modelName = "yolov7s";
	private File baseDir = new File("./procs/yolov7");
}
