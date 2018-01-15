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
package net.dksy.msr.jna.win32;
import java.nio.Buffer;

import net.dksy.msr.jna.hid.LinuxHid;
import net.dksy.msr.jna.hid.LinuxHid.hiddev_devinfo;
import net.dksy.msr.jna.hid.LinuxHid.hiddev_field_info;
import net.dksy.msr.jna.hid.LinuxHid.hiddev_report_info;
import net.dksy.msr.jna.hid.LinuxHid.hiddev_usage_ref;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure; 

public interface CLibrary extends Library {
	public static final CLibrary INSTANCE = (CLibrary)Native.loadLibrary("c", CLibrary.class);
	public static final int FD_SETSIZE = 1024;
	public static final int NFDBITS = 32 * 8;
	public static final int FD_NFDBITS = NFDBITS;
	public static final int howmany = (((FD_SETSIZE) + ((FD_NFDBITS) - 1)) / (FD_NFDBITS));

	public static final int 
	O_RDONLY	= 0x0000,		// open for reading only
	O_WRONLY	= 0x0001,		// open for writing only
	O_RDWR		= 0x0002,		// open for reading and writing
	O_EVTONLY   = 0x8000;		// descriptor requested for event notifications only
	
	public static class fd_set extends Structure {
		public int[] fds_bits = new int[howmany];

		public void set(int fd) {
			int index = fd / FD_NFDBITS;
			int offset = fd % FD_NFDBITS;
			fds_bits[index] |= (1 << offset);
		}

		public void clr(int fd) {
			int index = fd / FD_NFDBITS;
			int offset = fd % FD_NFDBITS;
			fds_bits[index] &= ~(1 << offset);
		}

		public boolean isSet(int fd) {
			int index = fd / FD_NFDBITS;
			int offset = fd % FD_NFDBITS;
			return (fds_bits[index] & (1 << offset)) != 0;
		}

		public void zero() {
			for (int i = 0; i < fds_bits.length; i++) {
				fds_bits[i] = 0;
			}
		}
	}

	public static class timeval extends Structure {
		public NativeLong tv_sec = new NativeLong(0);
		public NativeLong tv_usec = new NativeLong(0);

		public boolean isSet() {
			return tv_sec.longValue() != 0 || tv_usec.longValue() != 0;
		}

		public void clear() {
			tv_sec.setValue(0);
			tv_usec.setValue(0);
		}

		public void setTimeInMillis(long millis) {
			tv_sec.setValue(millis / 1000L);
			tv_usec.setValue((millis % 1000L) * 1000L);
		}

		public long getTimeInMillis() {
			return (tv_sec.longValue() * 1000L) + ((tv_usec.longValue() + 500L) / 1000L);
		}
	}

	public int open(String device, int flags);

	public int close(int filedes);

	public int fcntl(int filedes, int command, int others);
	
	public int ioctl(int filedes, int command, LinuxHid.hiddev_usage_ref q);
	public int ioctl(int filedes, int command, LinuxHid.hiddev_report_info q);
	public int ioctl(int filedes, int command, LinuxHid.hiddev_field_info q);
	public int ioctl(int filedes, int command, LinuxHid.hiddev_devinfo q);
	public int ioctl(int filedes, int command, Pointer q);
	public int ioctl(int filedes, int command, int ptr);

	public int read(int filedes, Buffer buf, int counts);
	public int read(int filedes, byte[] buffer, int counts);
	public int read(int filedes, Pointer p, int counts);

	public int select(int nfds, fd_set readfds, fd_set writefds, fd_set errorfds, timeval timeout);
	
	

	public int atol(String s);
	public void printf(String format, Object... args);
}
