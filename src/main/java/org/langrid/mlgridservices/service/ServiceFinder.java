package org.langrid.mlgridservices.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

import org.yaml.snakeyaml.Yaml;

import jp.go.nict.langrid.commons.beanutils.Converter;
import jp.go.nict.langrid.commons.lang.ClassUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ServiceFinder {
	public ServiceFinder(Path rootPath){
		this.rootPath = rootPath;
	}

	public void find(BiConsumer<ServiceInfo, Object> onFound)
	throws IOException{
		var c = new Converter();
		try(var stream = Files.walk(rootPath)){
			stream.forEach(p -> {
				if(p.getFileName().toString().equals("services.yml")){
					try(var r = Files.newBufferedReader(p, StandardCharsets.UTF_8)){
						var sy = new Yaml().loadAs(r, ServicesYml.class);
						for(var s : sy.services){
							var si = new ServiceInfo();
							si.setDescription(sy.common.description);
							si.setLicense(sy.common.license);
							si.setUrl(sy.common.url);
							si.setServiceId(s.serviceId);
							var clazz = Class.forName(s.implementation);
							var impl = clazz.getConstructor(String.class).newInstance(p.getParent().toString());
							for(var kv : s.properties.entrySet()){
								var setter = ClassUtil.findSetter(clazz, kv.getKey());
								setter.invoke(impl, c.convert(kv.getValue(), setter.getParameterTypes()[0]));
							}
							onFound.accept(si, impl);
						}
					} catch(ClassNotFoundException | IllegalAccessException | InstantiationException |
						InvocationTargetException | IOException | NoSuchMethodException e){
						e.printStackTrace(System.err);
					}
				}
			});
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class ServicesYml {
		public Common common;
		public Service[] services;

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		static class Common {
			public String description;
			public String license;
			public String url;
		}

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		static class Service{
			public String serviceId;
			public String implementation;
			public Map<String, Object> properties;
		}
	}

	private Path rootPath;
}
