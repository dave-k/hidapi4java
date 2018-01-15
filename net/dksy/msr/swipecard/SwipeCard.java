/* Copyright (c) 2009 David Kennedy, All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2 
 * alternative Open Source/Free licenses: LGPL 2.1 or later and 
 * Apache License 2.0.
 * 
 * You can freely decide which license you want to apply to 
 * the project.
 * 
 * You may obtain a copy of the LGPL License at:
 * 
 * http://www.gnu.org/licenses/licenses.html
 * 
 * A copy is also included in the downloadable source code package,
 * in file "LGPL2.1".
 * 
 * You may obtain a copy of the Apache License at:
 * 
 * http://www.apache.org/licenses/
 * 
 * A copy is also included in the downloadable source code package,
 * in file "AL2.0".
 */
package net.dksy.msr.swipecard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

import net.dksy.msr.utils.Logging;
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class SwipeCard implements Runnable{
	static Logger logger = Logger.getLogger(SwipeCard.class.getName());
	protected EventListenerList listenerList = new EventListenerList();
	//windows reader
	MSR winreader=null;
	LinuxMSR linreader=null;
	public SwipeCard(){
		//detect environment and setup card reader
		if(isWindows()){
			//windows platform
			logger.debug("Windows detected");
			winreader=new MSR();
		}else if(isUnix()){
			//linux platform
			logger.debug("Linux detected");
			linreader=new LinuxMSR();
		}else{
			logger.error("Platform detection failed for:"+System.getProperty("os.name"));
		}
		Thread t=new Thread(this);
		t.setName("SwipeCard");
		t.start();
	}
	
	@Override
	public void run() {
		while(true){
			try{
				MsrEvent evt=new MsrEvent(this);
				if(isWindows()){
					if(winreader.read()){
						//got a card swipe, fire event
						evt.track1=winreader.hidResult.getTk1Data();
						evt.track2=winreader.hidResult.getTk2Data();
						evt.track3=winreader.hidResult.getTk3Data();
						fireMsrEvent(evt);
					}
				}else{
					//LINUX, got a card swipe, fire event
					if(linreader.read()){
						evt.track1=linreader.fields[1];
						evt.track2=linreader.fields[2];
						evt.track3=linreader.fields[3];
						fireMsrEvent(evt);
					}
				}
				//delay to prevent cpu hogging in case of errors
				Thread.sleep(1000);
			}catch(Exception e){
				logger.error("swipe card error",e);
			}
		}
		
	}
	
	public static void main(String[] args){
		Logging.consoleOnly();
		SwipeCard sc=new SwipeCard();
		sc.addMsrEventListener(new MsrEventListener(){

			@Override
			public void msrEventOccurred(MsrEvent evt) {
				logger.debug(evt.toString());
			}});
	}
	
	public static boolean isWindows(){
		String os = System.getProperty("os.name").toLowerCase();
	    return (os.indexOf( "win" ) >= 0); 
	}
 
 
	public static boolean isUnix(){
		String os = System.getProperty("os.name").toLowerCase();
	    return (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0);
	}

	// This methods allows classes to register for MyEvents
    public void addMsrEventListener(MsrEventListener listener) {
        listenerList.add(MsrEventListener.class, listener);
    }

    // This methods allows classes to unregister for MyEvents
    public void removeMsrEventListener(MsrEventListener listener) {
        listenerList.remove(MsrEventListener.class, listener);
    }

    // This private class is used to fire MyEvents
    void fireMsrEvent(MsrEvent evt) {
        Object[] listeners = listenerList.getListenerList();

        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==MsrEventListener.class) {
                ((MsrEventListener)listeners[i+1]).msrEventOccurred(evt);
            }
        }
    }
}
