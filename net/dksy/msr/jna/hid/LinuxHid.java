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
package net.dksy.msr.jna.hid;

import com.sun.jna.Structure;

public class LinuxHid {

	public static final int HIDIOCGVERSION = 0x80044801;
	public static final int HIDIOCGDEVINFO = 0x801C4803;
	public static final int HIDIOCINITREPORT = 0x4805;
	public static final int HIDIOCGFIELDINFO = 0xC038480A;
	public static final int HIDIOCGUCODE = 0xC018480D;
	public static final int HIDIOCGUSAGE = 0xC018480B;
	public static final int HIDIOCGREPORTINFO = 0xC00C4809;
	public static final int HID_REPORT_TYPE_INPUT = 1;
	public static final int HID_REPORT_ID_FIRST = 0x00000100;
	public static final int HID_REPORT_ID_NEXT = 0x00000200;
	
	public static class hiddev_field_info extends Structure {
		public int report_type;
		public int report_id;
		public int field_index;
		public int maxusage;
		public int flags;
		public int physical; /* physical usage for this field */
		public int logical; /* logical usage for this field */
		public int application; /* application usage for this field */
		public int logical_minimum;
		public int logical_maximum;
		public int physical_minimum;
		public int physical_maximum;
		public int unit_exponent;
		public int unit;
	}

	public static class hiddev_report_info extends Structure {
		public int report_type;
		public int report_id;
		public int num_fields;
	}

	public static class hiddev_usage_ref extends Structure {
		public int report_type;
		public int report_id;
		public int field_index;
		public int usage_index;
		public int usage_code;
		public int value;
	}

	public static class hiddev_devinfo extends Structure {
		public int bustype;
		public int busnum;
		public int devnum;
		public int ifnum;
		public short vendor;
		public short product;
		public short version;
		public int num_applications;
	}

	public static class hiddev_event extends Structure {
		public int hid;
		public int value;
	}

}
