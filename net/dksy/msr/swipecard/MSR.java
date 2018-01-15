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

import java.io.IOException;

import net.dksy.msr.jna.hid.HIDResult;
import net.dksy.msr.jna.win32.W32API;

public class MSR { //
	enum STATE {
		STATE_IDLE, STATE_DETERMINE_DEVICE, STATE_HID_START, STATE_KEYBD_START, STATE_HID_READ, STATE_KEYBD_READ;
	};

	private static int VENDOR_ID = 0x0801; // HID device's vendor ID
	private static int PRODUCT_ID = 0x0002; // HID device's product ID.
	private STATE iState = STATE.STATE_IDLE;
	private W32API.HANDLE hidDevice = W32API.INVALID_HANDLE_VALUE;
	public HIDResult hidResult = new HIDResult();

	// Constructor
	public MSR() {
		try {
			hidResult.InitUSB();
		} catch (Exception e) {
			hidResult.CloseUSB();
			System.out.println(e.getMessage());
		}
	}

	/**
	 * read Read the card;
	 */
	public boolean read() {
		try {
			// if no device attached, reset the state to idle
			if (!hidResult.IsAttached()) {
				System.out.println("HID Device NOT detected");
				iState = STATE.STATE_IDLE;
				hidDevice = W32API.INVALID_HANDLE_VALUE;
			} else {
				if (hidResult.DetectDevice(VENDOR_ID, PRODUCT_ID)) {
					if (iState == STATE.STATE_IDLE) {
						System.out.println("Device detected");
						iState = STATE.STATE_DETERMINE_DEVICE;
					}
				}
			}
			switch (iState) {
				// Program is in IDLE (this is the initial condition or start)
				case STATE_IDLE:
					if (hidDevice == W32API.INVALID_HANDLE_VALUE) {
					} else
						iState = STATE.STATE_DETERMINE_DEVICE;
					break;
				// program has done a DetectDevice but doesn't know what device (if any) it is
				case STATE_DETERMINE_DEVICE:
					iState = STATE.STATE_HID_START;
					break;
				// set up for HID mode read
				case STATE_HID_START:
					hidResult.InitRead();
					iState = STATE.STATE_HID_READ;
					break;
				case STATE_KEYBD_START:
					break;
				// do an HID read
				case STATE_HID_READ:
					// System.out.println("hidResult.ReadInputRpt()");
					// Thread.sleep(2000);
					if (hidResult.ReadInputRpt() == true) {
						// System.out.println("hidResult.GetMSRResults");
						if (GetMSRResults()) {
							// setTrack1(hidResult.getTk1Data());
							//DisplayMSRResults();
							return true;
							 //DisplayMSRResults();
						}
					}
					break;
				case STATE_KEYBD_READ: // do nothing
					break;
			}
		} catch (IOException ioe) {
			iState = STATE.STATE_IDLE;
			System.out.println(ioe.getMessage());
		} catch (Exception e) {
			iState = STATE.STATE_IDLE;
			System.out.println(e.getMessage());
		}
		return false;
	}

	/**
	 * GetMSRResults Extracts the magnetic stripe read results from the HID report and places the data into specific variables. This routine
	 * uses the HID API to extract the data from the HID report. This is the recommended way of extracting the data. Using the HID API to
	 * extract the data allows the HID report data to be reordered with no affect on the application.
	 * 
	 * @return
	 */
	private boolean GetMSRResults() {
		boolean result = true;
		for (int track = 1; track <= 3; ++track) {
			try {
				if (hidResult.GetMSRResults(track)) {
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return result;
	}

	/**
	 * DisplayMSRResults Displays magnetic stripe read results
	 */
	public void DisplayMSRResults() {
		System.out.println("Track1: " + hidResult.getTk1Data());
		System.out.println("Track2: " + hidResult.getTk2Data());
		System.out.println("Track3: " + hidResult.getTk3Data());
	}

	public static void main(String[] args) {
		MSR msr = new MSR();
		do {
			 msr.read();
			 msr.DisplayMSRResults();
		} while (true);
	}
}
