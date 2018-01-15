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

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;


public interface SetupAPI extends UUID {

// Flags controlling what is included in the device information set built
// by SetupDiGetClassDevs
//
    public static int ERROR_INSUFFICIENT_BUFFER = 122;

    public static int DIGCF_DEFAULT = 0x00000001;  // only valid with DIGCF_DEVICEINTERFACE
    public static int DIGCF_PRESENT = 0x00000002;
    public static int DIGCF_ALLCLASSES =0x00000004;
    public static int DIGCF_PROFILE = 0x00000008;
    public static int DIGCF_DEVICEINTERFACE = 0x00000010;

    // An SP_DEVICE_INTERFACE_DATA structure defines a device interface in a device information set.
    //
    // typedef struct _SP_DEVICE_INTERFACE_DATA {
    //  DWORD  cbSize;
    //  GUID  InterfaceClassGuid;
    //  DWORD  Flags;
    //  ULONG_PTR  Reserved;
    // } SP_DEVICE_INTERFACE_DATA, *PSP_DEVICE_INTERFACE_DATA;
    public static class SP_DEVICE_INTERFACE_DATA extends Structure {
         public int cbSize;
         public _GUID interfaceClassGuid;
         public int flags;
         public Pointer reserved;
     }

    // An SP_DEVICE_INTERFACE_DETAIL_DATA structure contains the path for a device interface.
    //
    // typedef struct _SP_DEVICE_INTERFACE_DETAIL_DATA {
    //  DWORD  cbSize; The size, in bytes, of the fixed portion of the
    //                  SP_DEVICE_INTERFACE_DETAIL_DATA structure.
    //  TCHAR  DevicePath[ANYSIZE_ARRAY];
    // } SP_DEVICE_INTERFACE_DETAIL_DATA, *PSP_DEVICE_INTERFACE_DETAIL_DATA;
    public static class SP_DEVICE_INTERFACE_DETAIL_DATA extends Structure {
        public int cbSize;
        public byte[] devicePath = new byte[256];
    }

    SetupAPI INSTANCE = (SetupAPI)Native.loadLibrary("setupapi", SetupAPI.class);

    // The SetupDiGetClassDevs function returns a handle to
    // a device information set that contains requested
    // device information elements for a local machine
    //  HDEVINFO
    //  SetupDiGetClassDevs(
    //      IN LPGUID  ClassGuid,  OPTIONAL
    //      IN PCTSTR  Enumerator,  OPTIONAL
    //      IN HWND  hwndParent,  OPTIONAL
    //      IN DWORD  Flags
    //  );
    public W32API.HANDLE SetupDiGetClassDevsA(
            _GUID classGuid,
            Pointer enumerator,
            Pointer hwndParent,
            int flags);  

    /**
     * The SetupDiEnumDeviceInterfaces function enumerates the device interfaces
     * that are contained in a device information set.
     * On return, MyDeviceInterfaceData contains the handle to a
     * SP_DEVICE_INTERFACE_DATA structure for a detected device.
     * Requires:
     *  the DeviceInfoSet returned in SetupDiGetClassDevs.
     *  the HidGuid returned in GetHidGuid.
     *  An index to specify a device.
     * 
     * WINSETUPAPI BOOL WINAPI
     *  SetupDiEnumDeviceInterfaces(
     *    IN HDEVINFO  DeviceInfoSet,
     *    IN PSP_DEVINFO_DATA  DeviceInfoData,  OPTIONAL
     *    IN LPGUID  InterfaceClassGuid,
     *    IN DWORD  MemberIndex,
     *    OUT PSP_DEVICE_INTERFACE_DATA  DeviceInterfaceData
     *    );
     */
    public boolean SetupDiEnumDeviceInterfaces(
                W32API.HANDLE deviceInfoSet,
                Pointer deviceInfoData,  // OPTIONAL
                _GUID interfaceClassGuid,
                int memberIndex,
                SP_DEVICE_INTERFACE_DATA deviceInterfaceData
            );

    // The SetupDiGetDeviceInterfaceDetail function returns details about a device interface.

    // WINSETUPAPI BOOL WINAPI
    //  SetupDiGetDeviceInterfaceDetail(
    //    IN HDEVINFO  DeviceInfoSet,
    //    IN PSP_DEVICE_INTERFACE_DATA  DeviceInterfaceData,
    //    OUT PSP_DEVICE_INTERFACE_DETAIL_DATA  DeviceInterfaceDetailData,  OPTIONAL
    //    IN DWORD  DeviceInterfaceDetailDataSize,
    //    OUT PDWORD  RequiredSize,  OPTIONAL
    //    OUT PSP_DEVINFO_DATA  DeviceInfoData  OPTIONAL
    //    );
    public boolean SetupDiGetDeviceInterfaceDetailA(
                W32API.HANDLE deviceInfoSet,
                SP_DEVICE_INTERFACE_DATA deviceInterfaceData,
                SP_DEVICE_INTERFACE_DETAIL_DATA deviceInterfaceDetailData,
                int deviceInterfaceDetailDataSize,
                int[] requiredSize,
                Pointer deviceInfoData 
        );
    // The SetupDiDestroyDeviceInfoList function deletes
    // a device information set and frees all associated memory.
    //
    // WINSETUPAPI BOOL WINAPI
    //  SetupDiDestroyDeviceInfoList(
    //    IN HDEVINFO  DeviceInfoSet
    //    );
    public void SetupDiDestroyDeviceInfoList(W32API.HANDLE deviceInfoSet);

}