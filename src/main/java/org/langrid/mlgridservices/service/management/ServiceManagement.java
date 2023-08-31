package org.langrid.mlgridservices.service.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.langrid.mlgridservices.service.CompositeService;
import org.langrid.service.ml.interim.management.MatchingCondition;
import org.langrid.service.ml.interim.management.Order;
import org.langrid.service.ml.interim.management.SearchServicesResult;
import org.langrid.service.ml.interim.management.ServiceEntry;
import org.langrid.service.ml.interim.management.ServiceInvocation;
import org.langrid.service.ml.interim.management.ServiceManagementService;

public class ServiceManagement implements ServiceManagementService{
	public ServiceManagement(
		Map<String, ServiceEntry> serviceEntries,
		Map<String, Object> serviceImples){
		this.serviceEntries = new TreeMap<>(serviceEntries);

		for(var e : serviceEntries.entrySet()){
			var sid = e.getKey();
			var entry = e.getValue();
			serviceTypeToServiceIds
				.computeIfAbsent(entry.getServiceType(), k->new TreeSet<>())
				.add(sid);
			var simpl = serviceImples.get(sid);
			if(simpl instanceof CompositeService){
				serviceNameToServiceInvocations.put(sid, ((CompositeService)simpl).getInvocations());
				System.out.println("composite: " + sid);
			}
			System.out.printf("%s: %s added.%n", sid, entry.getServiceType());
		}
	}

	@Override
	public synchronized SearchServicesResult searchServices(
		int startIndex, int maxCount,
		MatchingCondition[] conditions, Order[] orders) {
		MatchingCondition stcond = null;
		if(conditions != null) for(var c : conditions){
			if(c.getFieldName().equals("serviceType")){
				stcond = c;
				break;
			}
		}

		Collection<ServiceEntry> ret = null;
		if(stcond != null){
			ret = new ArrayList<>();
			var stype = stcond.getMatchingValue();
			var sids = serviceTypeToServiceIds.get(stype);
			if(sids != null) for(var sid : sids){
				ret.add(serviceEntries.get(sid));
			}
		} else{
			ret = serviceEntries.values();
		}
		return new SearchServicesResult(ret.toArray(new ServiceEntry[]{}),
			ret.size(), true);
	}

	@Override
	public ServiceInvocation[] getServiceInvocations(String serviceId) {
		var ret = serviceNameToServiceInvocations.getOrDefault(serviceId, new ServiceInvocation[]{});
		System.out.printf("getServiceInvocations(\"%s\") -> %s%n", serviceId, Arrays.toString(ret));
		return ret;
	}

	private Map<String, ServiceEntry> serviceEntries;

	private Map<String, Set<String>> serviceTypeToServiceIds = new TreeMap<>();
	private Map<String, ServiceInvocation[]> serviceNameToServiceInvocations = new TreeMap<>();
}
