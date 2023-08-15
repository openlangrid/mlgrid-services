package org.langrid.mlgridservices.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.langrid.mlgridservices.service.ServiceInvoker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.undercouch.bson4jackson.BsonFactory;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

@Component
@ServerEndpoint(value="/ws")
public class WebSocketServer implements ApplicationContextAware {
	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext context) {
		WebSocketServer.context = context;
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config, @PathParam("serviceId") String serviceId) {
		String httpSessionName = HttpSession.class.getName();
		HttpSession httpSession = (HttpSession)config.getUserProperties().get(httpSessionName);
		if(httpSession != null) {
			session.getUserProperties().put(httpSessionName, httpSession);
		}
	}

	@OnMessage
	public String onMessage(Session session, String message)
			throws JsonMappingException, JsonProcessingException, MalformedURLException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		int reqId = -1;
		try {
			var req = jmapper.readValue(message, WebSocketRequest.class);
			reqId = req.getReqId();
			var sid = req.getServiceId();
			if(sid.equals("__PingService")){
				return jmapper.writeValueAsString(new WebSocketResponse(
					reqId, new HashMap<>(), "Pong"
				));
			}
			try{
				var r = invoker().invoke(sid, req);
				var res = new WebSocketResponse(
					reqId, r.getHeaders(), r.getResult());
				return jmapper.writeValueAsString(res);
			} catch(InvocationTargetException e){
				throw e.getCause();
			}
		} catch(Throwable e) {
			e.printStackTrace();
			return jmapper.writeValueAsString(new WebSocketResponse(
				reqId, new Error(e.getClass().getSimpleName(), e.getMessage())));
		}
	}

	@OnMessage
	public byte[] onMessage(Session session, byte[] message)
			throws StreamReadException, DatabindException, IOException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, ProcessFailedException {
		int reqId = -1;
		try{
			var req = bmapper.readValue(message, WebSocketRequest.class);
			reqId = req.getReqId();
			try{
				var r = invoker().invoke(req.getServiceId(), req);
				return bmapper.writeValueAsBytes(new WebSocketResponse(
					reqId, r.getHeaders(), r.getResult()));
			} catch(InvocationTargetException e){
				throw e.getCause();
			}
		} catch(Throwable e) {
			e.printStackTrace();
			return bmapper.writeValueAsBytes(new WebSocketResponse(
				reqId, new Error(e.getClass().getSimpleName(), e.getMessage())));
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		System.out.println(reason);
	}

	@OnError
	public void onError(Session session, Throwable error) {
		error.printStackTrace();
	}

	private ServiceInvoker invoker() {
		try {
			return context.getBean(ServiceInvoker.class);
		} catch(RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ObjectMapper jmapper = new ObjectMapper();
	private ObjectMapper bmapper = new ObjectMapper(new BsonFactory());
}
