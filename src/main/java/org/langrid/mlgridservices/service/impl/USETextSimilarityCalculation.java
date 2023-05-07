package org.langrid.mlgridservices.service.impl;

import java.io.File;

public class USETextSimilarityCalculation
extends AbstractTextSimilarityCalculationService{
	public USETextSimilarityCalculation(String model){
		super(new File("./procs/use"), model);
	}
}
