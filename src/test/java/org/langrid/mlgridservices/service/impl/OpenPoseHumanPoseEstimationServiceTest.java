package org.langrid.mlgridservices.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenPoseHumanPoseEstimationServiceTest {
	@Test
    public void test() throws Throwable{
        var s = new OpenPoseHumanPoseEstimationService();
        var ret = s.estimate(
            Files.readAllBytes(Path.of("./procs/human_pose_estimation_openpose/IP210215TAN000004000.jpeg")),
            "image/jpeg");
        System.out.println(new ObjectMapper().writeValueAsString(ret));
        assertEquals(10, ret.getPoses().length);
    }
}
