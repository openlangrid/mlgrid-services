package org.langrid.mlgridservices.service.impl;

import java.io.File;

public class USETextSimilarityCalculation
extends AbstractTextSimilarityCalculationService{
	public USETextSimilarityCalculation(){
		super(new File("./procs/use"));
	}
}
