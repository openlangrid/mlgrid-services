package org.langrid.jsservicegw.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

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

import org.langrid.jsservicegw.service.ServiceInvoker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.undercouch.bson4jackson.BsonFactory;

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
		try {
			System.out.println(message);
			var req = jmapper.readValue(message, WebSocketRequest.class);
			var res = new WebSocketResponse(
					req.getReqId(),
					invoker().invoke(req.getServiceId(), req).getResult());
			var resMsg = jmapper.writeValueAsString(res);
			return resMsg;
		} catch(RuntimeException e) {
			e.printStackTrace();
			throw e;
		} catch(Error e) {
			e.printStackTrace();
			throw e;
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@OnMessage
	public byte[] onMessage(Session session, byte[] message)
			throws StreamReadException, DatabindException, IOException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		System.out.println("bin message");
		var i = bmapper.readValue(message, WebSocketRequest.class);
		return bmapper.writeValueAsBytes(invoker().invoke(i.getServiceId(), i));
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
