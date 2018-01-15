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

import java.io.IOException;

import net.dksy.msr.jna.win32.Kernel32;
import net.dksy.msr.jna.win32.SetupAPI;
import net.dksy.msr.jna.win32.W32API;
import com.sun.jna.Pointer;

import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Memory;

/**
 */
public class HID_IO {
	public HIDLibrary hidLibrary = HIDLibrary.INSTANCE;
	public SetupAPI setupAPI = SetupAPI.INSTANCE;
	public Kernel32 kernel32 = Kernel32.INSTANCE;
	// Three tracks
	public byte[] tk1Data = new byte[0];
	public byte[] tk2Data = new byte[0];
	public byte[] tk3Data = new byte[0];
	public int tk1RptDataLen = 0;
	public int tk2RptDataLen = 0;
	public int tk3RptDataLen = 0;
	public IntByReference preparsedData = new IntByReference();
	public Memory readBuffer = null;
	public HIDLibrary._HIDP_CAPS capabilities = new HIDLibrary._HIDP_CAPS();
	public W32API.HANDLE hidDevice = W32API.INVALID_HANDLE_VALUE;
	private HIDLibrary._GUID hidGuid = new HIDLibrary._GUID();
	private SetupAPI.SP_DEVICE_INTERFACE_DATA deviceInterfaceData = new SetupAPI.SP_DEVICE_INTERFACE_DATA();
	private SetupAPI.SP_DEVICE_INTERFACE_DETAIL_DATA deviceInterfaceDetailData = new SetupAPI.SP_DEVICE_INTERFACE_DETAIL_DATA();
	private boolean readPending = false;
	private Pointer hEventObject = null;
	private Kernel32.OVERLAPPED overlapped = new Kernel32.OVERLAPPED();
	private IntByReference numberOfBytesRead = new IntByReference();

	// Constructor
	public HID_IO() {
	}

	// Accessors/Mutators
	public void setTk1Data(byte[] data) {
		this.tk1Data = data;
	}

	public void setTk2Data(byte[] data) {
		this.tk2Data = data;
	}

	public void setTk3Data(byte[] data) {
		this.tk3Data = data;
	}

	public String getTk1Data() {
		return (Native.toString(this.tk1Data));
	}

	public String getTk2Data() {
		return (Native.toString(this.tk2Data));
	}

	public String getTk3Data() {
		return (Native.toString(this.tk3Data));
	}

	/**
	 * IsAttached If no HID devices attached return False
	 */
	public boolean IsAttached() throws Exception {
		W32API.HANDLE hDeviceInfoSet;
		boolean result = false;
		int memberIndex = 0;
		/*
		 * HidD_GetHidGuid Get the GUID for all system HIDs. Returns: the GUID in HidGuid.
		 */
		hidLibrary.HidD_GetHidGuid(hidGuid);
		// System.out.println("hidGuid = " + hidGuid.toString());
		/*
		 * SetupDiGetClassDevs Returns: a handle to a device information set for all installed devices. Requires: the HidGuid returned in
		 * GetHidGuid.
		 */
		int flags = setupAPI.DIGCF_DEVICEINTERFACE | setupAPI.DIGCF_PRESENT;
		hDeviceInfoSet = setupAPI.SetupDiGetClassDevsA(hidGuid, // pointer
				(Pointer) null, (Pointer) null, flags);
		if (Kernel32.INVALID_HANDLE_VALUE.equals(hDeviceInfoSet)) {
			int err = kernel32.GetLastError();
			throw new Exception("setupAPI.SetupDiGetClassDevsA() failed " + hidGuid.toString() + ": '" + getSystemError(err) + "' (" + err + ")");
		}
		// The cbSize element of the MyDeviceInterfaceData structure must be set to
		// the structure's size in bytes.
		deviceInterfaceData.cbSize = deviceInterfaceData.size();
		result = setupAPI.SetupDiEnumDeviceInterfaces(hDeviceInfoSet, (Pointer) null, hidGuid, // pointer
				memberIndex, deviceInterfaceData);
		// destroy the driver list to prevent memory leaks
		setupAPI.SetupDiDestroyDeviceInfoList(hDeviceInfoSet);
		if (!result) {
			hidDevice = W32API.INVALID_HANDLE_VALUE;
			int err = kernel32.GetLastError();
			throw new Exception("setupAPI.SetupDiEnumDeviceInterfaces() failed " + hidGuid.toString() + ": '" + getSystemError(err) + "' ("
					+ err + ")");
		}
		return result;
	}

	/**
	 * DetectDevice Makes a series of API calls to locate the desired HID-class device. Tries to CreateFile Read/Write first - if that fails
	 * creates file with NoAccess to deal with Keyboard Emulation under XP and 2K. Windows XP and 2K opens all keyboards and mice
	 * exclusively with a CreateFile call of its own so all subsequent CreateFile calls for read or write access fail. Feature reports still
	 * work with NoAccess because they are sent through HID API function. Makes a series of API calls to locate the desired HID-class
	 * device.
	 * 
	 * @param As
	 *            char vendorID - HID device's vendor ID
	 * @param As
	 *            char productID - HID device's product ID.
	 * @return True on success, False on failure
	 */
	public boolean DetectDevice(int vendorID, int productID) throws IOException, Exception {
		boolean open = false;
		boolean bResult = false;
		int iResult = 0;
		boolean lastDevice = false;
		Kernel32.SECURITY_ATTRIBUTES security = new Kernel32.SECURITY_ATTRIBUTES();
		// Get the GUID of the HID interface
		hidLibrary.HidD_GetHidGuid(hidGuid); // pointer
		// Return devices that support device interfaces
		// for the specified device interface classes.
		int flags = setupAPI.DIGCF_DEVICEINTERFACE | setupAPI.DIGCF_PRESENT;
		// Get handle to relevant device information set
		W32API.HANDLE hDeviceInfoSet = setupAPI.SetupDiGetClassDevsA(hidGuid, // pointer
				(Pointer) null, (Pointer) null, flags);
		if (Kernel32.INVALID_HANDLE_VALUE.equals(hDeviceInfoSet)) {
			int err = kernel32.GetLastError();
			throw new Exception("setupAPI.SetupDiGetClassDevsA() failed " + hidGuid + ": '" + getSystemError(err) + "' (" + err + ")");
		}
		// Get interface data for the requested instance
		// deviceInterfaceDetailData.cbSize = 5;
		// index into the list of interfaces in the device information set.
		int memberIndex = 0;
		do {
			deviceInterfaceData.cbSize = deviceInterfaceData.size();
			bResult = setupAPI.SetupDiEnumDeviceInterfaces(hDeviceInfoSet, (Pointer) null, hidGuid, // pointer
					memberIndex, deviceInterfaceData);
			if (!bResult) {
				lastDevice = true;
				hidDevice = Kernel32.INVALID_HANDLE_VALUE;
				return false;
			} else {
				// if device already open then just exit
				if (!(hidDevice == W32API.INVALID_HANDLE_VALUE)) {
					open = true;
					break;
				}
				// Get size of symbolic link name
				int[] requiredSize = new int[1];
				requiredSize[0] = 0;
				bResult = setupAPI.SetupDiGetDeviceInterfaceDetailA(hDeviceInfoSet, deviceInterfaceData,
						(SetupAPI.SP_DEVICE_INTERFACE_DETAIL_DATA) null, 0, requiredSize, (Pointer) null);
				int err = kernel32.GetLastError();
				if (!bResult && (err == SetupAPI.ERROR_INSUFFICIENT_BUFFER)) {
					deviceInterfaceDetailData.cbSize = 5; // deviceInterfaceDetailData.size();
					// Get symbolic link name
					bResult = setupAPI.SetupDiGetDeviceInterfaceDetailA(hDeviceInfoSet, deviceInterfaceData, deviceInterfaceDetailData,
							requiredSize[0], null, (Pointer) null);
					if (!bResult) {
						System.out.println("SetupDiGetDeviceInterfaceDetailA: " + bResult);
						setupAPI.SetupDiDestroyDeviceInfoList(hDeviceInfoSet);
						break;
					}
					// The requested access to the file or device,
					// which can be summarized as read, write, both or neither (zero).
					int desiredAccess = Kernel32.GENERIC_WRITE | Kernel32.GENERIC_READ;
					// The requested sharing mode of the file or device,
					// which can be read, write, both, delete, all of these, or none
					int shareMode = Kernel32.FILE_SHARE_READ | Kernel32.FILE_SHARE_WRITE;
					// int shareMode = Kernel32.FILE_SHARE_READ;
					int dwFlagsAndAttributes = Kernel32.FILE_FLAG_OVERLAPPED; // FILE_ATTRIBUTE_NORMAL;
					// int dwFlagsAndAttributes = Kernel32.FILE_ATTRIBUTE_NORMAL;
					String devicePath = new String(deviceInterfaceDetailData.devicePath);
					// Values for SECURITY_ATTRIBUTES structure:
					security.lpSecurityDescriptor = null;
					security.bInheritHandle = true;
					security.nLength = security.size();
					// give time for Windows to fully install keyboard device hooks before trying to open it
					// Thread.sleep(1000);
					// Open file
					hidDevice = kernel32.CreateFile(devicePath, desiredAccess, shareMode, security, Kernel32.OPEN_EXISTING,
							dwFlagsAndAttributes, (W32API.HANDLE) null);
					if (!Kernel32.INVALID_HANDLE_VALUE.equals(hidDevice)) {
						// Now we can find out if it's the device we're looking for.
						//System.out.println("CreateFile() The operation completed successfully.");
						// Read Attributes from the current device
						HIDLibrary._HIDD_ATTRIBUTES attributes = new HIDLibrary._HIDD_ATTRIBUTES();
						// Set the Size property to the number of bytes in the structure.
						attributes.size = attributes.size();
						bResult = hidLibrary.HidD_GetAttributes(hidDevice.getPointer(), attributes);
						if (bResult == true) {
							//System.out.println("HidD_GetAttributes() The operation completed successfully.");
							if (attributes.vendorID == vendorID && attributes.productID == productID) {
								/*
								 * System.out.println("attributes.size: " + attributes.size); System.out.println("attributes.vendorID: " +
								 * (int)attributes.vendorID + " 0x" + Integer.toHexString(attributes.vendorID));
								 * System.out.println("attributes.productID: " + (int)attributes.productID + " 0x" +
								 * Integer.toHexString(attributes.productID)); System.out.println("attributes.versionNumber: " +
								 * (int)attributes.versionNumber + " 0x" + Integer.toHexString(attributes.versionNumber));
								 */
								bResult = hidLibrary.HidD_GetPreparsedData(hidDevice, preparsedData);
								if (bResult) {
									System.out.println("HidD_GetPreparsedData() The operation completed successfully.");
									iResult = hidLibrary.HidP_GetCaps(preparsedData.getValue(), capabilities);
									System.out.println("HidP_GetCaps() The operation completed successfully.");
									if (iResult == HIDLibrary.HIDP_STATUS_SUCCESS) {
										// System.out.println("DetectDevice() hidLibrary.HidP_GetCaps: " + capabilities.toString());
										// If Capabilities.Usage <> UID_DECODING_RDR Or Capabilities.UsagePage <> UPG_MSR Then
										// CloseDevice
										// End If
										open = true;
									} else {
										CloseDevice();
									}
								} else {
									CloseDevice();
								}
							} else {
								CloseDevice();
							}
						} else {
							CloseDevice();
						}
					}
				}
			}
			memberIndex++;
		} while (hidDevice == Kernel32.INVALID_HANDLE_VALUE);
		setupAPI.SetupDiDestroyDeviceInfoList(hDeviceInfoSet);
		return open;
	}

	/**
	 * InitRead Gets the lengths of the individual track data from the HID report using HID API calls. If these lengths are known they could
	 * be hard coded but it is not recommended because then a different device with different track data lengths would not work with the
	 * application.
	 */
	public void InitRead() throws Exception {
		HIDLibrary._HIDP_VALUE_CAPS valueCaps = new HIDLibrary._HIDP_VALUE_CAPS();
		IntByReference numberInputValueCaps = new IntByReference();
		int result = 0;
		// System.out.println("InitRead() InputReportByteLength= " + capabilities.InputReportByteLength);
		readBuffer = new Memory(capabilities.InputReportByteLength);
		short linkCollection = 0; // top-level usage
		numberInputValueCaps.setValue(1);
		/** UID_TRACK_1_DATA **/
		result = hidLibrary.HidP_GetSpecificValueCaps(HIDLibrary.HidP_Input, HIDLibrary.UPG_MSR, linkCollection, HIDLibrary.UID_TRACK_1_DATA,
				valueCaps, numberInputValueCaps, preparsedData.getValue());
		if (!(result == HIDLibrary.HIDP_STATUS_SUCCESS)) {
			String fatalError = "HidP_GetSpecificValueCaps Failed on Track 1 data usage";
			int err = kernel32.GetLastError();
			throw new Exception("HID_IO::InitRead() " + fatalError + ": '" + getSystemError(err) + "' (" + result + ")");
		}
		if (numberInputValueCaps.getValue() == 1) {
			tk1RptDataLen = valueCaps.reportCount;
		} else {
			String fatalError = "HidP_GetSpecificValueCaps Failed to find Track 1 data usage";
			int err = kernel32.GetLastError();
			throw new Exception("HID_IO::InitRead() " + fatalError + ": '" + getSystemError(err) + "' (" + err + ")");
		}
		/** **/
		/** UID_TRACK_2_DATA **/
		result = hidLibrary.HidP_GetSpecificValueCaps(HIDLibrary.HidP_Input, HIDLibrary.UPG_MSR, linkCollection, HIDLibrary.UID_TRACK_2_DATA,
				valueCaps, numberInputValueCaps, preparsedData.getValue());
		if (!(result == HIDLibrary.HIDP_STATUS_SUCCESS)) {
			String fatalError = "HidP_GetSpecificValueCaps Failed on Track 2 data usage";
			int err = kernel32.GetLastError();
			throw new Exception("HID_IO::InitRead() " + fatalError + ": '" + getSystemError(err) + "' (" + result + ")");
		}
		if (numberInputValueCaps.getValue() == 1) {
			tk2RptDataLen = valueCaps.reportCount;
		} else {
			String fatalError = "HidP_GetSpecificValueCaps Failed to find Track 2 data usage";
			int err = kernel32.GetLastError();
			throw new Exception("HID_IO::InitRead() " + fatalError + ": '" + getSystemError(err) + "' (" + err + ")");
		}
		/** **/
		/** UID_TRACK_3_DATA **/
		result = hidLibrary.HidP_GetSpecificValueCaps(HIDLibrary.HidP_Input, HIDLibrary.UPG_MSR, linkCollection, HIDLibrary.UID_TRACK_3_DATA,
				valueCaps, numberInputValueCaps, preparsedData.getValue());
		if (!(result == HIDLibrary.HIDP_STATUS_SUCCESS)) {
			String fatalError = "HidP_GetSpecificValueCaps Failed on Track 3 data usage";
			int err = kernel32.GetLastError();
			throw new Exception("HID_IO::InitRead() " + fatalError + ": '" + getSystemError(err) + "' (" + result + ")");
		}
		if (numberInputValueCaps.getValue() == 1) {
			tk3RptDataLen = valueCaps.reportCount;
		} else {
			String fatalError = "HidP_GetSpecificValueCaps Failed to find Track 3 data usage";
			int err = kernel32.GetLastError();
			throw new Exception("HID_IO::InitRead() " + fatalError + ": '" + getSystemError(err) + "' (" + err + ")");
		}
		/** **/
		tk1Data = new byte[tk1RptDataLen - 1];
		tk2Data = new byte[tk2RptDataLen - 1];
		tk3Data = new byte[tk3RptDataLen - 1];
	}

	/**
	 * ReadInputRpt Reads the input report if one is available. The device only sends an input report when a card is swiped. Therefore, to
	 * avoid having the ReadFile() call lock up the application while waiting for a card to be swiped, overlapped IO should be used as
	 * shown. This routine should be called periodically. A good way to do this is using a timer.
	 * 
	 * @author Dave
	 */
	public boolean ReadInputRpt() throws Exception {
		boolean bWait = true;
		boolean result = false;
		if (readPending) {
			// Waits until the specified object is in the signaled state or the time-out interval elapses.
			int iResult = kernel32.WaitForSingleObject(hEventObject, 500);
			switch (iResult) {
				case Kernel32.WAIT_OBJECT_0: // The state of the specified object is signaled.
					System.out.println("WAIT_OBJECT_0");
					// A read was already started so check to see if it has completed yet.
					result = kernel32.GetOverlappedResult(hidDevice, overlapped, numberOfBytesRead, bWait);
					if (!result) {
						// The read hasn't completed yet.
						int err = kernel32.GetLastError();
						if (!(err == Kernel32.ERROR_IO_INCOMPLETE)) {
							// Something went wrong with the read
							// Maybe the device was unplugged
							CloseDevice();
						}
					} else {
						// The read has completed.
						readPending = false;
						result = true;
					}
					break;
				case Kernel32.WAIT_TIMEOUT:
					// System.out.println("Timed out");
					break;
				default:
					System.out.println("ReadInputRpt WaitForSingleObject() None of the above...");
			}
		} else {
			// No read is pending; start a new one.
			// Do overlapped IO setup
			if (!kernel32.ResetEvent(hEventObject)) {
				String fatalError = "ResetEvent Failed";
				int err = kernel32.GetLastError();
				throw new Exception("HID_IO::ReadInputRpt() " + fatalError + ": '" + getSystemError(err) + "' (" + err + ")");
			}
			overlapped.hEvent = hEventObject;
			overlapped.Internal = 0;
			overlapped.InternalHigh = 0;
			overlapped.Offset = 0;
			overlapped.OffsetHigh = 0;
			// System.out.println("overlapped = " + overlapped.toString());
			// Start the read
			if (!kernel32.ReadFile(hidDevice, readBuffer, capabilities.InputReportByteLength, numberOfBytesRead, overlapped)) {
				// The read did not complete yet
				int err = kernel32.GetLastError();
				if (err == Kernel32.ERROR_IO_PENDING) {
					// The read is pending
					// We are waiting on an input report to be sent from the device
					// A report will only be sent when a card is swiped
					readPending = true;
				} else {
					// Something went wrong with the read
					// Maybe the device was unplugged
					CloseDevice();
				}
			} else {
				// The read completed allready.
				result = true;
			}
		}
		return result;
	} // ReadInputRpt

	/**
	 * InitUSB Initializes USB data
	 */
	public void InitUSB() throws Exception {
		// creates a manual-reset event object
		boolean bManualReset = true;
		// the initial state of the event object
		boolean bInitialState = false;
		hidDevice = Kernel32.INVALID_HANDLE_VALUE;
		preparsedData.setValue(0);
		hEventObject = kernel32.CreateEventA((Kernel32.SECURITY_ATTRIBUTES) null, bManualReset, bInitialState, (Pointer) null);
		if (hEventObject == null) {
			String fatalError = "CreateEvent Failed";
			int err = kernel32.GetLastError();
			throw new Exception("HID_IO::InitUSB() " + fatalError + ": '" + getSystemError(err) + "' (" + err + ")");
		}
		System.out.println("HID_IO::InitUSB() CreateEvent: The operation completed successfully.");
		readPending = false;
	} // InitUSB

	/**
	 * CloseDevice closes the device properly
	 */
	public void CloseDevice() {
		boolean result = false;
		if (!Kernel32.INVALID_HANDLE_VALUE.equals(hidDevice)) {
			if (readPending) {
				readPending = false;
				result = kernel32.CancelIo(hidDevice);
			}
			result = kernel32.CloseHandle(hidDevice.getPointer());
			hidDevice = Kernel32.INVALID_HANDLE_VALUE;
		}
		if (!(preparsedData.getPointer() == null)) {
			result = hidLibrary.HidD_FreePreparsedData(preparsedData.getPointer());
			preparsedData.setValue(0);
		}
	} // CloseDevice

	public boolean ReadUSB(String sData) {
		boolean result = false;
		return result;
	}

	public boolean WriteUSB(byte bData) {
		boolean result = false;
		return result;
	}

	public void CloseUSB() {
		if (!(hEventObject == null)) {
			boolean result = kernel32.CloseHandle(hEventObject);
		}
	}

	public String getSystemError(int code) {
		PointerByReference pref = new PointerByReference();
		kernel32.FormatMessage(Kernel32.FORMAT_MESSAGE_ALLOCATE_BUFFER | Kernel32.FORMAT_MESSAGE_FROM_SYSTEM
				| Kernel32.FORMAT_MESSAGE_IGNORE_INSERTS, null, code, 0, pref, 0, null);
		String s = pref.getValue().getString(0, !Boolean.getBoolean("w32.ascii"));
		s = s.replace(".\r", ".").replace(".\n", ".");
		kernel32.LocalFree(pref.getValue());
		return s;
	}

	public String getHIDpError(int code) {
		// see hidpi.h for codes like 0xC0110001
		String s = null;
		return s;
	}
	/**
	 * HIDP_ERROR_CODES #define HIDP_ERROR_CODES(SEV, CODE) \ ((NTSTATUS) (((SEV) << 28) | (FACILITY_HID_ERROR_CODE << 16) | (CODE)))
	 * 
	 * #define HIDP_STATUS_SUCCESS (HIDP_ERROR_CODES(0x0,0)) #define HIDP_STATUS_NULL (HIDP_ERROR_CODES(0x8,1)) #define
	 * HIDP_STATUS_INVALID_PREPARSED_DATA (HIDP_ERROR_CODES(0xC,1)) #define HIDP_STATUS_INVALID_REPORT_TYPE (HIDP_ERROR_CODES(0xC,2))
	 * #define HIDP_STATUS_INVALID_REPORT_LENGTH (HIDP_ERROR_CODES(0xC,3)) #define HIDP_STATUS_USAGE_NOT_FOUND (HIDP_ERROR_CODES(0xC,4))
	 * #define HIDP_STATUS_VALUE_OUT_OF_RANGE (HIDP_ERROR_CODES(0xC,5)) #define HIDP_STATUS_BAD_LOG_PHY_VALUES (HIDP_ERROR_CODES(0xC,6))
	 * #define HIDP_STATUS_BUFFER_TOO_SMALL (HIDP_ERROR_CODES(0xC,7)) #define HIDP_STATUS_INTERNAL_ERROR (HIDP_ERROR_CODES(0xC,8)) #define
	 * HIDP_STATUS_I8242_TRANS_UNKNOWN (HIDP_ERROR_CODES(0xC,9)) #define HIDP_STATUS_INCOMPATIBLE_REPORT_ID (HIDP_ERROR_CODES(0xC,0xA))
	 * #define HIDP_STATUS_NOT_VALUE_ARRAY (HIDP_ERROR_CODES(0xC,0xB)) #define HIDP_STATUS_IS_VALUE_ARRAY (HIDP_ERROR_CODES(0xC,0xC))
	 * #define HIDP_STATUS_DATA_INDEX_NOT_FOUND (HIDP_ERROR_CODES(0xC,0xD)) #define HIDP_STATUS_DATA_INDEX_OUT_OF_RANGE
	 * (HIDP_ERROR_CODES(0xC,0xE)) #define HIDP_STATUS_BUTTON_NOT_PRESSED (HIDP_ERROR_CODES(0xC,0xF)) #define
	 * HIDP_STATUS_REPORT_DOES_NOT_EXIST (HIDP_ERROR_CODES(0xC,0x10)) #define HIDP_STATUS_NOT_IMPLEMENTED (HIDP_ERROR_CODES(0xC,0x20))
	 */
}