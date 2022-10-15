package org.langrid.service.ml.interim.management;

public class SearchServicesResult {
	public SearchServicesResult(
		ServiceEntry[] entries, int totalCount,
		boolean totalCountFixed
		){
		this.entries = entries;
		this.totalCount = totalCount;
		this.totalCountFixed = totalCountFixed;
	}

	public ServiceEntry[] getEntries() {
		return entries;
	}
	public void setEntries(ServiceEntry[] entries) {
		this.entries = entries;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public boolean isTotalCountFixed() {
		return totalCountFixed;
	}
	public void setTotalCountFixed(boolean totalCountFixed) {
		this.totalCountFixed = totalCountFixed;
	}

	private ServiceEntry[] entries;
	private int totalCount;
	private boolean totalCountFixed;
}
