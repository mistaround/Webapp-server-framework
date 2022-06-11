package edu.upenn.cis.cis455.m1.handling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.upenn.cis.cis455.SparkController;
import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.server.WebService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.m2.server.HttpRequest;

public class FileRequestHandler {
	static final Logger logger = LogManager.getLogger(FileRequestHandler.class);
	private byte[] content;
	private int statusCode = 200;
	private String contentType;

	public FileRequestHandler(String pathname, String method) throws HaltException {
		if (method.equals("GET") || method.equals("HEAD")) {
			try {
				String root = WebService.directory == null ? System.getProperty("user.dir") + "/www" : WebService.directory;
				Path path = Paths.get(root, pathname);
				if (Files.exists(path)) {
					if (Files.isDirectory(path)) {
						path = path.resolve("index.html");
						if (Files.exists(path)) {
							try {
								this.contentType = Files.probeContentType(path);
								this.content = Files.readAllBytes(path);
								if (method.equals("HEAD")) {
									this.content = null;
								}
							} catch (IOException e) {
								throw new HaltException(500);
							}
						} else {
							throw new HaltException(404);
						}
					} else {
						try {
							this.contentType = Files.probeContentType(path);
							this.content = Files.readAllBytes(path);
						} catch (IOException e) {
							throw new HaltException(500);
						}
					}
				} else {
					throw new HaltException(404);
				}
			}
			catch (InvalidPathException e) {
				throw new HaltException(400);
			}

		} else {
			throw new HaltException(501);
		}
	}
	
	public byte[] getFileContent() {
		return this.content;
	}

	public int getStatusCode() {
		return this.statusCode;
	}
	
	public String getContentType() {
		return this.contentType;
	}
}
