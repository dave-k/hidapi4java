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

import com.sun.jna.Library;
import com.sun.jna.Structure;

public interface UUID extends Library {

    /**
     * Microsoft implementation of the distributed computing environment (DCE)
     * universally unique identifier ( UUID)
     *
     * typedef struct _GUID {
     *  DWORD Data1;
     *  WORD Data2;
     *  WORD Data3;
     *  BYTE Data4[8];
     * } GUID;
     */
    public static class _GUID extends Structure {
        public int data1;
        public short data2;
        public short data3;
        public byte[] data4 = new byte[8];
    }
}