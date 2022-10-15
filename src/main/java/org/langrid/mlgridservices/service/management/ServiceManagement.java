package org.langrid.mlgridservices.service.management;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.langrid.mlgridservices.service.group.ServiceGroup;
import org.langrid.service.ml.interim.management.MatchingCondition;
import org.langrid.service.ml.interim.management.Order;
import org.langrid.service.ml.interim.management.SearchServicesResult;
import org.langrid.service.ml.interim.management.ServiceEntry;
import org.langrid.service.ml.interim.management.ServiceManagementService;

public class ServiceManagement implements ServiceManagementService{
	public ServiceManagement( Map<String, ServiceGroup> serviceGroups, Map<String, Object> serviceImples){
		this.serviceGroups = serviceGroups;
		this.serviceImples = serviceImples;
	}

	@Override
	public synchronized SearchServicesResult searchServices(int startIndex, int maxCount, MatchingCondition[] conditions,
			Order[] orders, String scope) {
		if(serviceNameAndServiceTypes.size() == 0){
			initDB();
		}
		MatchingCondition stcond = null;
		for(var c : conditions){
			if(c.getFieldName().equals("ServiceType")){
				stcond = c;
				break;
			}
		}

		List<ServiceEntry> entries = new ArrayList<>();
		if(stcond != null){
			var stype = stcond.getMatchingValue();
			var sids = serviceTypeToServiceIds.get(stype);
			if(sids != null) for(var sid : sids){
				entries.add(new ServiceEntry(sid, stype));
			}
		} else{
			for(var e : serviceNameAndServiceTypes.entrySet()){
				entries.add(new ServiceEntry(e.getKey(), e.getValue()));
			}
		}
		return new SearchServicesResult(entries.toArray(new ServiceEntry[]{}),
			entries.size(), true);
	}

	private void initDB(){
		for(var g : serviceGroups.values()){
			for(var p : g.listServices()){
				var sid = p.getFirst();
				var stype = p.getSecond().getSimpleName();
				serviceNameAndServiceTypes.put(sid, stype);
				serviceTypeToServiceIds
					.computeIfAbsent(stype, k->new TreeSet<>())
					.add(sid);
				System.out.printf("%s: %s added.%n", sid, stype);
			}
		}
		for(var e : serviceImples.entrySet()){
			var sid = e.getKey();
			var stype = findInterface(e.getValue()).getSimpleName();
			serviceNameAndServiceTypes.put(sid, stype);
			serviceTypeToServiceIds
				.computeIfAbsent(stype, k->new TreeSet<>())
				.add(sid);
			System.out.printf("%s: %s added.%n", sid, stype);
		}
	}

	private Class<?> findInterface(Object service){
		return service.getClass().getInterfaces()[0];
	}

	private Map<String, String> serviceNameAndServiceTypes = new TreeMap<>();
	private Map<String, Set<String>> serviceTypeToServiceIds = new LinkedHashMap<>();

	private Map<String, ServiceGroup> serviceGroups;
	private Map<String, Object> serviceImples;
}
