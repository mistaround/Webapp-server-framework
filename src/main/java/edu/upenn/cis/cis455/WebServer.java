package edu.upenn.cis.cis455;

import static edu.upenn.cis.cis455.SparkController.*;

import org.apache.logging.log4j.Level;
/**
 * Initialization / skeleton class.
 * Note that this should set up a basic web server for Milestone 1.
 * For Milestone 2 you can use this to set up a basic server.
 * 
 * CAUTION - ASSUME WE WILL REPLACE THIS WHEN WE TEST MILESTONE 2,
 * SO ALL OF YOUR METHODS SHOULD USE THE STANDARD INTERFACES.
 * 
 * @author zives
 *
 */
public class WebServer {
    public static void main(String[] args) {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        
        // TODO: make sure you parse *BOTH* command line arguments properly
        
        int port = 45555;
        String root = "./www";
        if (args.length != 0) {
        	if (args.length == 1) {
        		port = Integer.parseInt(args[0]);
        	} else {
        		port = Integer.parseInt(args[0]);
        		root = args[1];
        	}
        }
        
        port(port);
        staticFileLocation(root);
        threadPool(4);
        // All user routes should go below here...
        get("hello", (request, response) -> "Hello World");
        // ... and above here. Leave this comment for the Spark comparator tool

//        System.out.println("Waiting to handle requests!");
//        awaitInitialization();
    }
}
