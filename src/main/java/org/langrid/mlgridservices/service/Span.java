package org.langrid.mlgridservices.service;

import java.util.ArrayList;
import java.util.List;

import org.langrid.mlgridservices.controller.Error;

import lombok.Data;

@Data
public class Span {
	public Span(){
		startedAt = System.nanoTime() / 1000000;
	}

	public Span(String type, String target, Object... args){
		this();
		this.type = type;
		this.target = target;
		this.args = args;
	}

	public Span newChild(String type, String target, Object... args){
		var s = new Span(type, target, args);
		if(children == null) children = new ArrayList<>();
		children.add(s);
		return s;
	}

	public void finishWithResult(Object result){
		this.result = result;
		this.finishedAt = System.nanoTime() / 1000000;
		this.ellapsedMillis = this.finishedAt - this.startedAt;
	}

	public void finishWithException(Throwable exception){
		this.error = new Error("error", exception.toString());
		this.finishedAt = System.nanoTime() / 1000000;
		this.ellapsedMillis = this.finishedAt - this.startedAt;
	}

	private String type; // call
	private String target;
	private Object[] args;
	private Object result;
	private Error error;
	private long startedAt;
	private long finishedAt;
	private long ellapsedMillis;
	private List<Span> children;
}
