package org.langrid.service.ml.interim.management;

public class Order {
	public Order(){}

	public Order(String fieldName, String direction) {
		this.fieldName = fieldName;
		this.direction = direction;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String field) {
		this.fieldName = field;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	private String fieldName;
	private String direction;
}
