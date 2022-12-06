package org.langrid.mlgridservices.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Timer implements AutoCloseable{
	public static Timer start(String name){
		return new Timer(name);
	}

	public Timer(String name){
		this.name = name;
		this.start = System.currentTimeMillis();
	}

	public Timer startChild(String name){
		if(children.size() == 0){
			children = new ArrayList<Timer>();
		}
		var c = new Timer(name);
		children.add(c);
		return c;
	}

	@Override
	public void close() {
		if(start != 0){
			millis = System.currentTimeMillis() - start;		
			start = 0;
		}
	}

	private String name = "";
	private long millis;
	private List<Timer> children = Collections.emptyList();
	private transient long start;
}
