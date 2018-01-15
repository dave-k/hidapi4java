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

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import static net.dksy.msr.jna.hid.LinuxHid.*;

import net.dksy.msr.jna.win32.CLibrary;
import net.dksy.msr.jna.win32.CLibrary.fd_set;
import net.dksy.msr.jna.win32.CLibrary.timeval;
import net.dksy.msr.swipecard.MSR.STATE;

import com.sun.jna.ptr.IntByReference;

public class LinuxMSR {
	static Logger logger = Logger.getLogger(LinuxMSR.class.getName());
	int fd = -1, rd, i;
	fd_set fdset;
	private static int VENDOR_ID = 0x0801; // HID device's vendor ID
	private static int PRODUCT_ID = 0x0002; // HID device's product ID.

	enum STATE {
		STATE_DETERMINE_DEVICE, STATE_HID_READ;
	};

	private STATE iState = STATE.STATE_DETERMINE_DEVICE;
	String[] fields;

	public static void main(String[] args) {
		BasicConfigurator.configure();
		logger.info("Logging enabled!");
		LinuxMSR lt=new LinuxMSR();
		while(true){
			if(lt.read()){
				logger.debug(lt.toString());
			}
		}
	}

	public boolean read() {
		CLibrary clib = CLibrary.INSTANCE;
		CLibrary.timeval tv = new CLibrary.timeval();
		switch (iState) {
			case STATE_DETERMINE_DEVICE:
				// try to find our magstripe reader
				hiddev_devinfo dinfo = new hiddev_devinfo();
				for (int i = 0; i < 10; i++) {
					fd = clib.open("/dev/usb/hiddev" + i, CLibrary.O_RDONLY);
					if (fd == -1) {
						// unable to open device
						continue;
					}
					clib.ioctl(fd, HIDIOCGDEVINFO, dinfo);
					if (dinfo.vendor == VENDOR_ID && dinfo.product == PRODUCT_ID) {
						break;
					}
					// couldn't find device
					clib.close(fd);
					fd = 0;
				}
				if (fd == 0 || fd==-1) {
					// no device found, sleep and retry
					try {
						// delay enough so that we don't thrash the computer looking for the card reader
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
					return false;
				}
				IntByReference version = new IntByReference();
				clib.ioctl(fd, HIDIOCGVERSION, version.getPointer());
				logger.debug("hiddev kernel version is " + (version.getValue() >> 16) + "." + ((version.getValue() >> 8) & 0xff) + "."
						+ (version.getValue() & 0xff));
				logger.debug("Found HID device. Vendor " + dinfo.vendor + " Product " + dinfo.product + " Version " + dinfo.version);
				// get read for reading data
				fdset = new fd_set();
				clib.ioctl(fd, HIDIOCINITREPORT, 0);
				fdset.zero();
				fdset.set(fd);
				iState = STATE.STATE_HID_READ;
				break;
			case STATE_HID_READ:
				hiddev_usage_ref uref = new hiddev_usage_ref();
				//code blocks here until valid data occurs or reader is unplugged
				rd = clib.select(fd + 1, fdset, null, null, tv);
				if (rd >= 0) {
					// hiddev_event[] event=new hiddev_event[64];
					Buffer event = ByteBuffer.allocate(64 * 8);
					rd = clib.read(fd, event, event.capacity());
					if (rd < 0) {
						iState = STATE.STATE_DETERMINE_DEVICE;
						logger.debug("Swipe Card read failed, closing device.");
						clib.close(fd);
						return false;
					}
					//retrieve data, adapted from ups.c example
					hiddev_report_info rinfo = new hiddev_report_info();
					rinfo.report_type = HID_REPORT_TYPE_INPUT;
					rinfo.report_id = HID_REPORT_ID_FIRST;
					clib.ioctl(fd, HIDIOCGREPORTINFO, rinfo);
					//logger.debug("  Report id: " + rinfo.report_id + " (" + rinfo.num_fields + "fields)\n");
					fields = new String[rinfo.num_fields];
					for (int i = 0; i < rinfo.num_fields; i++) {
						hiddev_field_info finfo = new hiddev_field_info();
						finfo.report_type = rinfo.report_type;
						finfo.report_id = rinfo.report_id;
						finfo.field_index = i;
						clib.ioctl(fd, HIDIOCGFIELDINFO, finfo);
						//logger.debug("Field: " + i);
						uref = new hiddev_usage_ref();
						fields[i] = "";
						for (int j = 0; j < finfo.maxusage; j++) {
							uref.report_type = finfo.report_type;
							uref.report_id = finfo.report_id;
							uref.field_index = i;
							uref.usage_index = j;
							clib.ioctl(fd, HIDIOCGUCODE, uref);
							clib.ioctl(fd, HIDIOCGUSAGE, uref);
							fields[i] += ((char) uref.value);
						}
						//logger.debug(fields[i] + "\n");
					}
					return true;
				}
				break;
		}
		return false;
	}
	
	public String toString(){
		String str="";
		if(fields!=null){
			str+="last data count:"+fields.length+"\n";
			for (int i = 0; i < fields.length; i++) {
				str+="field "+i+": "+fields[i]+"\n";
			}
		}
		return str;
	}
	
}// class
