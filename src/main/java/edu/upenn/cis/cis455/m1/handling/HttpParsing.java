package edu.upenn.cis.cis455.m1.handling;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis.cis455.exceptions.ClosedConnectionException;
import edu.upenn.cis.cis455.exceptions.HaltException;

public class HttpParsing {
final static Logger logger = LogManager.getLogger(HttpParsing.class);
    
    /**
     * Initial fetch buffer for the HTTP request header
     * 
     */
    static final int BUFSIZE = 8192;
    
    
    /**
     * Decodes the sent headers and loads the data into Key/value pairs
     */
    public static void decodeHeader(BufferedReader in, Map<String, String> pre, Map<String, List<String>> parms,
                                    Map<String, String> headers, Map<String, String> body) throws HaltException {
        try {
            // Read the request line
            String requestLine = in.readLine();
            if (requestLine == null) {
                throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html HTTP/1.1");
            }
            StringTokenizer st = new StringTokenizer(requestLine);

            // Get method
            if (!st.hasMoreTokens()) {
                throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html HTTP/1.1");
            }
            pre.put("method", st.nextToken());

            // Get URI and params
            if (!st.hasMoreTokens()) {
                throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html HTTP/1.1");
            }
            String uri = st.nextToken();
            int qmi = uri.indexOf('?');
            if (qmi >= 0) {
                pre.put("queryString", uri.substring(qmi + 1));
                pre.put("uri",decodePercent(uri.substring(0, qmi)));
                decodeParms(uri.substring(qmi + 1), parms);
            } else {
                pre.put("queryString", "");
                pre.put("uri",decodePercent(uri));
            }

            // Get HTTP protocol
            // NOTE: this now forces header names lower case since they are case insensitive and vary by client.
            if (!st.hasMoreTokens()) {
                throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html HTTP/1.1");
            }
            pre.put("protocolVersion", st.nextToken());

            // Get Headers
            String line = in.readLine();
            String lastKey = null;
            while (line != null && !line.trim().isEmpty()) {
                int p = line.indexOf(':');
                if (p >= 0) {
                	lastKey = line.substring(0, p).trim().toLowerCase(Locale.US);
                    headers.put(lastKey, line.substring(p + 1).trim());
                } else if (lastKey != null && line.startsWith(" ") || line.startsWith("\t")) {
                    String newPart = line.trim();
                    headers.put(lastKey, headers.get(lastKey) + newPart);
                }
                line = in.readLine();
            }

            headers.put("cookie", pre.get("cookie"));
            headers.put("protocolVersion", pre.get("protocolVersion"));
            headers.put("Method", pre.get("method"));
            
            // Get Body
            if (body != null) {
                if (headers.containsKey("content-length")) {
                	int length = Integer.parseInt(headers.get("content-length"));
                	char[] chars = new char[length+1];
                	for (int i = 0; i < length; i += 1) {
                		chars[i] = (char) in.read();
                	}
                    body.put("body",String.valueOf(chars));
                }
            }
        } catch (IOException ioe) {
            throw new HaltException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        }
    }
    
    /**
     * Decodes parameters in percent-encoded URI-format ( e.g.
     * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given Map.
     */
    public static String decodeParms(String parms, Map<String, List<String>> p) {
        String queryParameterString = "";
        
        if (parms == null) {
            return queryParameterString;
        }

        queryParameterString = parms;
        StringTokenizer st = new StringTokenizer(parms, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            String key = null;
            String value = null;

            if (sep >= 0) {
                key = decodePercent(e.substring(0, sep)).trim();
                value = decodePercent(e.substring(sep + 1));
            } else {
                key = decodePercent(e).trim();
                value = "";
            }

            List<String> values = p.computeIfAbsent(key, k -> new ArrayList<String>());
            if (!value.contains(",")) {
                values.add(value);
            } else {
                StringTokenizer stn = new StringTokenizer(value, ",");
                while (stn.hasMoreTokens()) {
                    String v = stn.nextToken();
                    values.add(v);
                }
            }

        }
        
        return queryParameterString;
    }


    /**
     * Decode percent encoded <code>String</code> values.
     * 
     * @param str
     *            the percent encoded <code>String</code>
     * @return expanded form of the input, for example "foo%20bar" becomes
     *         "foo bar"
     */
    public static String decodePercent(String str) {
        String decoded = null;
        try {
            decoded = URLDecoder.decode(str, "UTF8");
        } catch (UnsupportedEncodingException ignored) {
            logger.warn("Encoding not supported, ignored", ignored);
        }
        return decoded;
    }
    
    /**
     * Parse the initial request header
     * 
     * @param remoteIp IP address of client
     * @param inputStream Socket input stream (not yet read)
     * @param headers Map to receive header key/values
     * @param parms Map to receive parameter key/value-lists
     */
    public static String parseRequest(
                        String remoteIp, 
                        InputStream inputStream, 
                        Map<String, String> headers,
                        Map<String, List<String>> parms) throws IOException, HaltException {
        int splitbyte = 0;
        int rlen = 0;
        String uri = "";
        
        try {
            // Read the first 8192 bytes.
            // The full header should fit in here.
            // Apache's default header limit is 8KB.
            // Do NOT assume that a single read will get the entire header
            // at once!
            byte[] buf = new byte[BUFSIZE];
            splitbyte = 0;
            rlen = 0;

            int read = -1;
            inputStream.mark(BUFSIZE);
            try {
                read = inputStream.read(buf, 0, BUFSIZE);
            } catch (IOException e) {
                throw new ClosedConnectionException();
            }
            if (read == -1) {
                throw new HaltException(HttpServletResponse.SC_BAD_REQUEST);
            }
            while (read > 0) {
                rlen += read;
                splitbyte = findHeaderEnd(buf, rlen);
                if (splitbyte > 0) {
                    break;
                }
                read = inputStream.read(buf, rlen, BUFSIZE - rlen);
            }

            if (splitbyte < rlen) {
                inputStream.reset();
                inputStream.skip(splitbyte);
            }

            headers.clear();

            // Create a BufferedReader for parsing the header.
            BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, rlen)));

            // Decode the header into parms and header java properties
            Map<String, String> pre = new HashMap<String, String>();
            decodeHeader(hin, pre, parms, headers, null);

            if (null != remoteIp) {
                headers.put("remote-addr", remoteIp);
                headers.put("http-client-ip", remoteIp);
            }

            uri = pre.get("uri") + (pre.get("queryString").isEmpty() ? "" : "?" + pre.get("queryString"));

            headers.put("cookie", pre.get("cookie"));
            headers.put("protocolVersion", pre.get("protocolVersion"));
            headers.put("Method", pre.get("method"));
        } catch (SocketException e) {
            // throw it out to close socket object (finalAccept)
            throw new ClosedConnectionException();
        } catch (SocketTimeoutException ste) {
            // treat socket timeouts the same way we treat socket exceptions
            // i.e. close the stream & finalAccept object by throwing the
            // exception up the call stack.
            throw new ClosedConnectionException();
        }
        
        return uri;
    }
    
    /**
     * Find byte index separating header from body. It must be the last byte of
     * the first two sequential new lines.
     */
    static int findHeaderEnd(final byte[] buf, int rlen) {
        int splitbyte = 0;
        while (splitbyte + 1 < rlen) {

            // RFC2616
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && splitbyte + 3 < rlen && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n') {
                return splitbyte + 4;
            }

            // tolerance
            if (buf[splitbyte] == '\n' && buf[splitbyte + 1] == '\n') {
                return splitbyte + 2;
            }
            splitbyte++;
        }
        return 0;
    }

}
