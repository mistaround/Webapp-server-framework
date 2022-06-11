package edu.upenn.cis.cis455.m2.server;

import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.m1.handling.HttpIoHandler;
import edu.upenn.cis.cis455.m1.handling.HttpParsing;
import edu.upenn.cis.cis455.m1.handling.ResponseHandler;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.upenn.cis.cis455.SparkController.*;
import static org.junit.Assert.assertTrue;

public class TestParams {
    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        port(45555);
        staticFileLocation("./www");
        threadPool(4);
    }

    String sampleGetRequest1 =
        "GET /cis555/hello HTTP/1.1\r\n" +
        "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
        "Host: www.cis.upenn.edu\r\n\r\n";
    String sampleGetRequest2 =
            "GET /add/2/4 HTTP/1.1\r\n" +
            "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
            "Host: www.cis.upenn.edu\r\n\r\n";
    @Test
    public void testParams1() throws Exception {
        get("/:name/hello", (req, res) -> "Hello: " + req.params("name"));

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                sampleGetRequest1,
                byteArrayOutputStream);

        Map<String, String> pre = new HashMap<>();
        Map<String, List<String>> parms = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> body = new HashMap<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        HttpParsing.decodeHeader(br, pre, parms, headers, body);

        HttpRequest request = new HttpRequest(s, pre, parms, headers, body);
        HttpResponse response = new HttpResponse();
        new ResponseHandler(request, response);

        HttpIoHandler.sendResponse(s, request, response);
        String result = byteArrayOutputStream.toString();
        System.out.println(result);

        assertTrue(result.startsWith("HTTP/1.1 200 OK"));
        assertTrue(result.contains("cis555"));
    }

    @Test
    public void testParams2() throws Exception {
        get("/add/:x/:y", (req, res) -> Integer.parseInt(req.params("x")) + Integer.parseInt(req.params("y")));

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                sampleGetRequest2,
                byteArrayOutputStream);

        Map<String, String> pre = new HashMap<>();
        Map<String, List<String>> parms = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> body = new HashMap<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        HttpParsing.decodeHeader(br, pre, parms, headers, body);

        HttpRequest request = new HttpRequest(s, pre, parms, headers, body);
        HttpResponse response = new HttpResponse();
        new ResponseHandler(request, response);

        HttpIoHandler.sendResponse(s, request, response);
        String result = byteArrayOutputStream.toString();
        System.out.println(result);

        assertTrue(result.startsWith("HTTP/1.1 200 OK"));
        assertTrue(result.contains("6"));
    }

    @After
    public void tearDown() {
        stop();
    }
}
