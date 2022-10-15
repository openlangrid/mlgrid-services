package org.langrid.service.ml.interim.management;

public class MatchingCondition {
	public MatchingCondition(){}

	public MatchingCondition(
			String fieldName, String matchingValue, String matchingMethod){
		this.fieldName = fieldName;
		this.matchingValue = matchingValue;
		this.matchingMethod = matchingMethod;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String attributeName){
		this.fieldName = attributeName;
	}

	public String getMatchingValue() {
		return matchingValue;
	}

	public void setMatchingValue(String matchingValue){
		this.matchingValue = matchingValue;
	}

	public String getMatchingMethod() {
		return matchingMethod;
	}

	public void setMatchingMethod(String matchingMethod){
		this.matchingMethod = matchingMethod;
	}

	private String fieldName;
	private String matchingValue;
	private String matchingMethod;
}
