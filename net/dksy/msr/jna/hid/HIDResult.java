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

import com.sun.jna.ptr.IntByReference;

public class HIDResult extends HID_IO {

  private int status_code = 0;
  private int len_code = 0;
  private int data_code = 0;
  private byte[] tkData = null;
  private int tkRptDataLen = 0;
  private int track = 0;
  
  // Constructor
  public HIDResult() {

  }
  
  public void init() {
    switch (this.track) {
      case 1:
        this.status_code = HIDLibrary.UID_TRACK_1_DECODE_STATUS;
        this.len_code = HIDLibrary.UID_TRACK_1_LEN;
        this.data_code = HIDLibrary.UID_TRACK_1_DATA;
        this.tkData = tk1Data;
        this.tkRptDataLen = tk1RptDataLen;
        break;
      case 2:
        this.status_code = HIDLibrary.UID_TRACK_2_DECODE_STATUS;
        this.len_code = HIDLibrary.UID_TRACK_2_LEN;
        this.data_code = HIDLibrary.UID_TRACK_2_DATA;
        this.tkData = tk2Data;
        this.tkRptDataLen = tk2RptDataLen;
        break;
      case 3:
        this.status_code = HIDLibrary.UID_TRACK_3_DECODE_STATUS;
        this.len_code = HIDLibrary.UID_TRACK_3_LEN;
        this.data_code = HIDLibrary.UID_TRACK_3_DATA;
        this.tkData = tk3Data;
        this.tkRptDataLen = tk3RptDataLen;
        break;
    }
  }

  public void setTrack(int track) {
    this.track = track;
  }

  /**
   * GetMSRResults
   * Extracts the magnetic stripe read results from the HID report and places the
   * data into specific variables. This routine uses the HID API to extract the
   * data from the HID report. This is the recommended way of extracting the data.
   * Using the HID API to extract the data allows the HID report data to
   * be reordered with no affect on the application.
   * @return
   */
  public boolean GetMSRResults(int track) throws Exception {
    IntByReference crdEcdTyp = new IntByReference();
    IntByReference cardSts = new IntByReference();
    IntByReference tk1DcdSts = new IntByReference();
    IntByReference tk1Len = new IntByReference();

    int result = 0;
    short linkCollection = 0;  // top-level usage
    setTrack(track);
    init();

    result = hidLibrary.HidP_GetUsageValue( 
        HIDLibrary.HidP_Input,
        HIDLibrary.UPG_MSR,
        linkCollection,
        this.status_code,
        tk1DcdSts, // out
        preparsedData.getValue(),
        readBuffer,
        capabilities.InputReportByteLength);
    if (!(result == HIDLibrary.HIDP_STATUS_SUCCESS)) {
      String fatalError = "HidP_GetUsageValue Failed on Tk1DcdSts";
      int err = kernel32.GetLastError();
      throw new Exception("HID_IO::GetMSRResults() "
            + fatalError + ": '"
            + getSystemError(err)
            + "' (" + Integer.toHexString(result) + ")");
    }
    result = hidLibrary.HidP_GetUsageValue(
        HIDLibrary.HidP_Input,
        HIDLibrary.UPG_MSR,
        linkCollection,
        this.len_code,
        tk1Len, // out
        preparsedData.getValue(),
        readBuffer,
        capabilities.InputReportByteLength);
    if (!(result == hidLibrary.HIDP_STATUS_SUCCESS)) {
      String fatalError = "HidP_GetUsageValue Failed on Tk1Len";
      int err = kernel32.GetLastError();
      throw new Exception("HID_IO::GetMSRResults() "
            + fatalError + ": '"
            + getSystemError(err)
            + "' (" + Integer.toHexString(result) + ")");
    }

    result = hidLibrary.HidP_GetUsageValueArray(
        HIDLibrary.HidP_Input,
        HIDLibrary.UPG_MSR,
        0,
        this.data_code,
        tkData, // out
        tkRptDataLen,
        preparsedData.getValue(),
        readBuffer,
        capabilities.InputReportByteLength);
    if (!(result == hidLibrary.HIDP_STATUS_SUCCESS)) {
      String fatalError = "HidP_GetUsageValueArray Failed on Tk1Data";
      int err = kernel32.GetLastError();
      throw new Exception("HID_IO::GetMSRResults() "
            + fatalError + ": '"
            + getSystemError(err)
            + "' (" + Integer.toHexString(result) + ")");
    }

    result = hidLibrary.HidP_GetUsageValue(
        HIDLibrary.HidP_Input, 
        HIDLibrary.UPG_MSR,
        linkCollection,
        HIDLibrary.UID_CARD_ENCODE_TYPE,
        crdEcdTyp, // out
        preparsedData.getValue(),
        readBuffer,
        capabilities.InputReportByteLength);
    if (!(result == hidLibrary.HIDP_STATUS_SUCCESS)) {
      String fatalError = "HidP_GetUsageValue Failed on CrdEcdTyp";
      int err = kernel32.GetLastError();
      throw new Exception("HID_IO::GetMSRResults() "
            + fatalError + ": '"
            + getSystemError(err)
            + "' (" + Integer.toHexString(result) + ")");
    }
/*
 * HIDP_STATUS_USAGE_NOT_FOUND
    result = hidLibrary.HidP_GetUsageValue(
        HIDLibrary.HidP_Input,
        HIDLibrary.UPG_MSR,
        linkCollection,
        HIDLibrary.UID_CARD_STATUS,
        cardSts, // out
        preparsedData.getValue(),
        readBuffer,
        capabilities.InputReportByteLength);
    if (!(result == hidLibrary.HIDP_STATUS_SUCCESS)) {
      String fatalError = "HidP_GetUsageValue Failed on CardSts";
      int err = kernel32.GetLastError();
      throw new Exception("HID_IO::GetMSRResults() "
            + fatalError + ": '"
            + getSystemError(err)
            + "' (" + Integer.toHexString(result) + ")");
    }
*/
    return true;
  }
}
