package org.langrid.mlgridservices.service.impl.management;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.langrid.mlgridservices.service.group.LangridServiceGroup;
import org.langrid.mlgridservices.service.group.ServiceGroup;
import org.langrid.mlgridservices.service.impl.StableDiffusion051TextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.management.ServiceManagement;
import org.langrid.service.ml.interim.management.MatchingCondition;

public class ServiceManagementTest {
	@Test
	public void test() throws Throwable{
		var sgs = new HashMap<String, ServiceGroup>();
		var sis = new HashMap<String, Object>();
		sgs.put("Langrid", new LangridServiceGroup());
		sis.put("StableDiffusionSD051", new StableDiffusion051TextGuidedImageGenerationService());
		sis.put("DiscoDiffusionSD051", new StableDiffusion051TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
		var s = new ServiceManagement(sgs, sis);
		var r = s.searchServices(0, 100, null, null);
		for(var e : r.getEntries()){
			System.out.printf("%s: %s%n", e.getServiceId(), e.getServiceType());
		}
	}

	@Test
	public void test_TextGuidedImageGenerationService() throws Throwable{
		var sgs = new HashMap<String, ServiceGroup>();
		var sis = new HashMap<String, Object>();
		sgs.put("Langrid", new LangridServiceGroup());
		sis.put("StableDiffusionSD051", new StableDiffusion051TextGuidedImageGenerationService());
		sis.put("DiscoDiffusionSD051", new StableDiffusion051TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
		var s = new ServiceManagement(sgs, sis);
		var r = s.searchServices(0, 100,
			new MatchingCondition[]{
				new MatchingCondition("serviceType", "TextGuidedImageGenerationService", "COMPLETE" )
			}, null);
		for(var e : r.getEntries()){
			System.out.printf("%s: %s%n", e.getServiceId(), e.getServiceType());
		}
	}
}
