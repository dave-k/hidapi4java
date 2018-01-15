package net.dksy.msr.utils;


import org.apache.log4j.*;

public class Logging {
	static Logger logger= Logger.getLogger(Logging.class.getName());
	
	public static void init(){
		PropertyConfigurator.configure("logging.properties");
		logger.info("Logging enabled!");
	}
	
	public static void consoleOnly(){
		BasicConfigurator.configure();
		logger.info("Logging enabled!");	
	}
	
	public static void init(String filename){
		PropertyConfigurator.configure(filename);
		logger.info("Logging enabled!");
	}
}
