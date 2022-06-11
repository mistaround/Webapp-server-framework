package edu.upenn.cis.cis455.m1.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

import edu.upenn.cis.cis455.m1.handling.ResponseHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.handling.HttpIoHandler;
import edu.upenn.cis.cis455.m1.handling.HttpParsing;
import edu.upenn.cis.cis455.m2.server.HttpRequest;
import edu.upenn.cis.cis455.m2.server.HttpResponse;

import org.apache.logging.log4j.Level;

public class TestSendIcon {
    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }
    
    String sampleGetRequest = 
        "GET /favicon.ico HTTP/1.1\r\n" +
        "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
        "Host: www.cis.upenn.edu\r\n\r\n";
    
    @Test
    public void testSendIcon() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
            sampleGetRequest,
            byteArrayOutputStream);

        Map<String, String> pre = new HashMap<String, String>();
		Map<String, List<String>> parms = new HashMap<String, List<String>>();
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String> body = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        HttpParsing.decodeHeader(br, pre, parms, headers, body);

        HttpRequest request = new HttpRequest(s, pre, parms, headers, body);
        HttpResponse response = new HttpResponse();
        new ResponseHandler(request, response);

        HttpIoHandler.sendResponse(s, request, response);
        String result = byteArrayOutputStream.toString();
        System.out.println(result);

        assertTrue(result.startsWith("HTTP/1.1 200 OK"));
        assertTrue(result.contains("image/vnd.microsoft.icon"));
    }
    
    @After
    public void tearDown() {}
}
