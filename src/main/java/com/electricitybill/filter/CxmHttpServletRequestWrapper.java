package com.electricitybill.filter;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CxmHttpServletRequestWrapper extends HttpServletRequestWrapper {
 
	private byte[] requestBody = new byte[0];
	private boolean bufferFilled = false;
	
	private ConcurrentHashMap<String, String> cxmHeaders = new ConcurrentHashMap<>();
 
	public CxmHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
	}
 
	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new CachedServletInputStream(getRequestBody());
	}
	
	@Override
    public String getHeader(String name) {
		String headerValue = cxmHeaders.get(name);
		if (headerValue != null) {
			return headerValue;
		} else {
			return super.getHeader(name);
		}
    }
	
	public void setHeader(String name, String value){
        this.cxmHeaders.put(name, value);
    }
 
	public byte[] getRequestBody() throws IOException {
		if (bufferFilled) {
			return Arrays.copyOf(requestBody, requestBody.length);
		}
		InputStream inputStream = super.getInputStream();
		requestBody = IOUtils.toByteArray(inputStream);
		bufferFilled = true;
		return requestBody;
	}
}