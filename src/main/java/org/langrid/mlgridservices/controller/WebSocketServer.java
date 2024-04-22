package org.langrid.mlgridservices.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
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
	public void onMessage(Session session, String message)
			throws JsonMappingException, JsonProcessingException, IOException, MalformedURLException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		int reqId = -1;
		try {
			var req = recvReq(message);
			reqId = req.getReqId();
			var sid = req.getServiceId();
			if(sid.equals("__PingService")){
				sendResTextSync(session, new WebSocketResponse(
					reqId, new HashMap<>(), "Pong"
				));
				return;
			}
			try{
				handleInvocation(req, res->{
					try {
						sendResTextSync(session, res);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			} catch(InvocationTargetException e){
				throw e.getCause();
			}
		} catch(Throwable e) {
			e.printStackTrace();
			sendResTextSync(session, new WebSocketResponse(
				reqId, new Error(e.getClass().getSimpleName(), e.getMessage())));
		}
	}

	@OnMessage
	public void onMessage(Session session, byte[] message)
			throws StreamReadException, DatabindException, IOException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, ProcessFailedException {
		int reqId = -1;
		try{
			var req = recvReq(message);
			reqId = req.getReqId();
			var sid = req.getServiceId();
			if(sid.equals("__PingService")){
				sendResBytesSync(session, new WebSocketResponse(
					reqId, new HashMap<>(), "Pong"
				));
				return;
			}
			try{
				handleInvocation(req, res->{
					try{
						sendResBytesSync(session, res);
					} catch(IOException e){
						e.printStackTrace();
					}
				});
			} catch(InvocationTargetException e){
				throw e.getCause();
			}
		} catch(Throwable e) {
			e.printStackTrace();
			sendResBytesSync(session, new WebSocketResponse(
				reqId, new Error(e.getClass().getSimpleName(), e.getMessage())));
		}
	}

	private ExecutorService es = Executors.newFixedThreadPool(19);
	private void handleInvocation(WebSocketRequest req, Consumer<WebSocketResponse> onResponse)
	throws MalformedURLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ProcessFailedException{
		es.submit((Runnable)()->{
			var reqId = req.getReqId();
			try {
				var r = invoker().invoke(req.getServiceId(), req);
				if(r.getError() != null){
					onResponse.accept(new WebSocketResponse(
						reqId, r.getHeaders(), r.getError()));
				} else{
					onResponse.accept(new WebSocketResponse(
						reqId, r.getHeaders(), r.getResult()));
				}
			} catch (MalformedURLException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
					| ProcessFailedException e) {
				e.printStackTrace();
				onResponse.accept(new WebSocketResponse(
						reqId, new Error("server.error", e.toString())
				));
			}
		});
	}

	private WebSocketRequest recvReq(String message)
	throws JsonMappingException, JsonProcessingException{
		return jmapper.readValue(message, WebSocketRequest.class);
	}

	private WebSocketRequest recvReq(byte[] message)
	throws IOException{
		return bmapper.readValue(message, WebSocketRequest.class);
	}

	private void sendResBytesSync(Session session, WebSocketResponse res) throws IOException{
		var bb = ByteBuffer.wrap(bmapper.writeValueAsBytes(res));
		session.getBasicRemote().sendBinary(bb);
	}

	private void sendResTextSync(Session session, WebSocketResponse res) throws IOException{
		session.getBasicRemote().sendText(jmapper.writeValueAsString(res));
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
