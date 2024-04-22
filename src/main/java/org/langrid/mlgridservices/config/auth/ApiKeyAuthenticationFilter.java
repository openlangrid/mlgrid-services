package org.langrid.mlgridservices.config.auth;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyAuthenticationFilter extends GenericFilterBean{
	public ApiKeyAuthenticationFilter(String[] apiKeys, String contextPath, String... paths){
		this.apiKeys = apiKeys;
		this.contextPath = contextPath;
		this.paths = paths;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
        try {
			var r = (HttpServletRequest)request;
			var reqPath = r.getRequestURI().substring(contextPath.length());
			var target = false;
			for(var p : paths){
				if(reqPath.startsWith(p)){
					target = true;
					break;
				}
			}
			var keyHeader = r.getHeader("X-APIKEY");
			var keyParam = r.getParameter("apikey");
			String foundKey = null;
			if(apiKeys.length == 0) {
				var auth = new ApiKeyAuthentication("NO_APIKEY_DEFINITIONS", AuthorityUtils.NO_AUTHORITIES);
				SecurityContextHolder.getContext().setAuthentication(auth);
				System.out.println("no api keys");
			} else if(target){
				for(var k : new String[]{keyHeader, keyParam}){
					if(k != null) {
						for(var apikey : apiKeys){
							if(apikey.equals(k)){
								foundKey = apikey;
							}
						}
					}
				}
				if(foundKey != null){
					var auth = new ApiKeyAuthentication(foundKey, AuthorityUtils.NO_AUTHORITIES);
					SecurityContextHolder.getContext().setAuthentication(auth);
				} else{
					throw new BadCredentialsException("Invalid API Key");
				}
			}
			chain.doFilter(request, response);
		} catch (Exception e) {
			var r = (HttpServletResponse) response;
			r.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			r.setContentType(MediaType.APPLICATION_JSON_VALUE);
			var w = r.getWriter();
			w.print(e.getMessage());
			w.flush();
			w.close();
		}
	}

	private String contextPath;
	private String[] apiKeys;
	private String[] paths;
}
