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

import net.dksy.msr.jna.win32.UUID;
import net.dksy.msr.jna.win32.W32API;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Memory;

public interface HIDLibrary extends W32API, UUID {

  /**
   * The HIDP_REPORT_TYPE enumeration type is used to specify a HID report type.
   */
  public enum HIDP_REPORT_TYPE {
    HidP_Input,
    HidP_Output,
    HidP_Feature
  }

  /**
   * The HIDP_CAPS structure contains information about 
   * a top-level collection's capability.
   *
   * typedef struct _HIDP_CAPS {
      USAGE  Usage;
      USAGE  UsagePage;
      USHORT  InputReportByteLength;
      USHORT  OutputReportByteLength;
      USHORT  FeatureReportByteLength;
      USHORT  Reserved[17];
      USHORT  NumberLinkCollectionNodes;
      USHORT  NumberInputButtonCaps;
      USHORT  NumberInputValueCaps;
      USHORT  NumberInputDataIndices;
      USHORT  NumberOutputButtonCaps;
      USHORT  NumberOutputValueCaps;
      USHORT  NumberOutputDataIndices;
      USHORT  NumberFeatureButtonCaps;
      USHORT  NumberFeatureValueCaps;
      USHORT  NumberFeatureDataIndices;
    } HIDP_CAPS, *PHIDP_CAPS;
   */
  public static class _HIDP_CAPS extends Structure {
    public short  Usage; // A usage ID value is an unsigned 16-bit value.
    public short  UsagePage;
    public short  InputReportByteLength;
    public short  OutputReportByteLength;
    public short  FeatureReportByteLength;
    public short[]  Reserved = new short[17];
    public short  NumberLinkCollectionNodes;
    public short  NumberInputButtonCaps;
    public short  NumberInputValueCaps;
    public short  NumberInputDataIndices;
    public short  NumberOutputButtonCaps;
    public short  NumberOutputValueCaps;
    public short  NumberOutputDataIndices;
    public short  NumberFeatureButtonCaps;
    public short  NumberFeatureValueCaps;
    public short  NumberFeatureDataIndices;
  }

  /**
   * The HIDP_VALUE_CAPS structure contains information that describes 
   * the capability of a set of HID control values 
   * (either a single usage or a usage range).
    typedef struct _HIDP_VALUE_CAPS {
      USAGE  UsagePage;
      UCHAR  ReportID;
      BOOLEAN  IsAlias;
      USHORT  BitField;
      USHORT  LinkCollection;
      USAGE  LinkUsage;
      USAGE  LinkUsagePage;
      BOOLEAN  IsRange;
      BOOLEAN  IsStringRange;
      BOOLEAN  IsDesignatorRange;
      BOOLEAN  IsAbsolute;
      BOOLEAN  HasNull;
      UCHAR  Reserved;
      USHORT  BitSize;
      USHORT  ReportCount;
      USHORT  Reserved2[5];
      ULONG  UnitsExp;
      ULONG  Units;
      LONG  LogicalMin, LogicalMax;
      LONG  PhysicalMin, PhysicalMax;
      union {
        struct {
        USAGE  UsageMin, UsageMax;
        USHORT  StringMin, StringMax;
        USHORT  DesignatorMin, DesignatorMax;
        USHORT  DataIndexMin, DataIndexMax;
        } Range;
        struct {
        USAGE  Usage, Reserved1;
        USHORT  StringIndex, Reserved2;
        USHORT  DesignatorIndex, Reserved3;
        USHORT  DataIndex, Reserved4;
        } NotRange;
      };
    } HIDP_VALUE_CAPS, *PHIDP_VALUE_CAPS;  
   */
  public static class _HIDP_VALUE_CAPS extends Structure {
    public short  usagePage;
    public short  reportID;
//    public byte  isAlias;
    public short  bitField;
    public short  linkCollection;
    public short  linkUsage;
    public short  linkUsagePage;
    public byte  isRange;
    public byte  isStringRange;
    public byte  isDesignatorRange;
    public byte  isAbsolute;
    public byte  hasNull;
    public byte  reserved1;
    public short  bitSize;
    public short  reportCount;
    public short[] reserved2 = new short[5];
    public int  unitsExp;
    public int  units;
    public int  logicalMin, logicalMax;
    public int  physicalMin, physicalMax;
    public short  UsageMin, UsageMax;
    public short  StringMin, StringMax;
    public short  DesignatorMin, DesignatorMax;
    public short  DataIndexMin, DataIndexMax;
  }

  public static class BUFFER extends Structure {
    public byte[] data = new byte[255];
  }

  /**
   * The HIDD_ATTRIBUTES structure contains vendor information about a HIDClass device.
   * typedef struct _HIDD_ATTRIBUTES {
   *   ULONG  Size;       Specifies the size, in bytes, of a HIDD_ATTRIBUTES structure.
   *   USHORT  VendorID;    Specifies a HID device's vendor ID.
   *   USHORT  ProductID;   Specifies a HID device's product ID.
   *   USHORT  VersionNumber; Specifies the manufacturer's revision number for a HIDClass device
   * } HIDD_ATTRIBUTES, *PHIDD_ATTRIBUTES;
   */
  public static class _HIDD_ATTRIBUTES extends Structure {
    public int size;
    public char  vendorID;  //ASCII. Java's 'char' also can be used as an unsigned short,
    public char  productID; //  i.e. it represents numbers from 0 up to 2^16
    public char  versionNumber;
  }

  HIDLibrary INSTANCE = (HIDLibrary)Native.loadLibrary("hid", HIDLibrary.class);

  // The HidD_GetHidGuid routine
  // returns the device interface GUID for HIDClass devices.
  //  VOID
  //  HidD_GetHidGuid(
  //    OUT LPGUID  HidGuid
  //  );
  void HidD_GetHidGuid(_GUID hidGuid);

  // The HidD_GetProductString routine returns the embedded string of 
  // a top-level collection that identifies the manufacturer's product.
  //
  // BOOLEAN
  //  HidD_GetProductString(
  //  IN HANDLE  HidDeviceObject,
  //  OUT PVOID  Buffer,
  //  IN ULONG  BufferLength
  //  );
  boolean HidD_GetProductString(
    Pointer hidDeviceObject,
    BUFFER buffer,
    int bufferLength 
  );

  /**
   * The HidD_GetAttributes routine returns the attributes of
   * a specified top-level collection
   * - the HID's Vendor ID, Product ID, and Version Number.
   * BOOLEAN
   *  HidD_GetAttributes(
   *    IN HANDLE  HidDeviceObject,
   *    OUT PHIDD_ATTRIBUTES  Attributes
   *  );
   */  
  boolean HidD_GetAttributes(
      Pointer hidDeviceObject,
      _HIDD_ATTRIBUTES attributes
  );

  // The routine successfully returned the value data.
  public static int HIDP_STATUS_SUCCESS = 0x110000;

  // Typedef enum defines a set of integer constants for HidP_Report_Type
  // Remember to declare these as integers (16 bits)
  public static int HidP_Input = 0;
  // HID USB swipe reader usages
  public static int UPG_MSR = 0xFF00;
  public static int UID_DECODING_RDR = 0x1;

  public static int UID_TRACK_1_DATA = 0x30;
  public static int UID_TRACK_2_DATA = 0x31;
  public static int UID_TRACK_3_DATA = 0x32;

  public static int UID_TRACK_1_DECODE_STATUS = 0x20;
  public static int UID_TRACK_2_DECODE_STATUS = 0x21;
  public static int UID_TRACK_3_DECODE_STATUS = 0x22;
  public static int UID_TRACK_1_LEN = 0x28;
  public static int UID_TRACK_2_LEN = 0x29;
  public static int UID_TRACK_3_LEN = 0x2A;
  public static int UID_CARD_ENCODE_TYPE = 0x38;
  public static int UID_CARD_STATUS = 0x39;
  /**
   * The HidP_GetSpecificValueCaps routine returns a value capability array
   * that describes all HID control values that meet a specified selection criteria.
   * NTSTATUS
   *  HidP_GetSpecificValueCaps(
   *    IN HIDP_REPORT_TYPE  ReportType,
   *    IN USAGE  UsagePage,
   *    IN USHORT  LinkCollection,
   *    IN USAGE  Usage,
   *    OUT PHIDP_VALUE_CAPS  ValueCaps,
   *    IN OUT PULONG  ValueCapsLength,
   *    IN PHIDP_PREPARSED_DATA  PreparsedData
   *  );
   */
  int HidP_GetSpecificValueCaps (
    int reportType,
    int usagePage,
    int linkCollection,
    int usage,
    _HIDP_VALUE_CAPS valueCaps,
    IntByReference valueCapsLength,
    int preparsedData
  );

  /**
   * The HidD_FreePreparsedData routine releases the resources that the HID class driver allocated to hold a top-level collection's preparsed data.
   *  BOOLEAN
   *  HidD_FreePreparsedData(
   *    IN PHIDP_PREPARSED_DATA  PreparsedData
   *    );
   * @param preparsedData
   * @return
   */
   // boolean HidD_FreePreparsedData(int preparsedData);
  boolean HidD_FreePreparsedData(Pointer p);
  /**
   * The HidP_GetUsageValue routine extracts the data associated with a
   * HID control value that matches the selection criteria in a HID report.
   * NTSTATUS
    HidP_GetUsageValue(
      IN HIDP_REPORT_TYPE  ReportType,
      IN USAGE  UsagePage,
      IN USHORT  LinkCollection,
      IN USAGE  Usage,
      OUT PULONG  UsageValue,
      IN PHIDP_PREPARSED_DATA  PreparsedData,
      IN PCHAR  Report,
      IN ULONG  ReportLength
    );
   */
  int HidP_GetUsageValue(
    int  ReportType,  // HIDP_REPORT_TYPE
    int  UsagePage,
    int  LinkCollection,
    int  Usage,
    IntByReference  UsageValue,
    int  PreparsedData,
    Memory  Report,
    int  ReportLength
  );

  /**
   * The HidP_GetUsageValueArray routine extracts the data associated with a
   * HID control usage value array from a HID report.
   *  NTSTATUS
   *  HidP_GetUsageValueArray(
   *    IN HIDP_REPORT_TYPE  ReportType,
   *    IN USAGE  UsagePage,
   *    IN USHORT  LinkCollection  OPTIONAL,
   *    IN USAGE  Usage,
   *    OUT PCHAR  UsageValue, Pointer to a caller-allocated buffer
   *    IN USHORT  UsageValueByteLength,
   *    IN PHIDP_PREPARSED_DATA  PreparsedData,
   *    IN PCHAR  Report,
   *    IN ULONG  ReportLength
   *    );
   */

  int HidP_GetUsageValueArray(
    int ReportType,
    int UsagePage,
    int LinkCollection,  //   OPTIONAL
    int Usage,
    byte[] UsageValue,
    int UsageValueByteLength,
    int PreparsedData,
    Memory Report,
    int ReportLength
  );

  /*
   * HidD_GetPreparsedData
   * The HidD_GetPreparsedData routine returns a top-level collection's preparsed data.
   * BOOLEAN
   *  HidD_GetPreparsedData(
   *  IN HANDLE  HidDeviceObject,
   *  OUT PHIDP_PREPARSED_DATA  *PreparsedData - Pointer to the address of a
          routine-allocated buffer that contains a collection's preparsed data
  );
   */
  boolean HidD_GetPreparsedData(
    HANDLE  HidDeviceObject,
    IntByReference pPreparsedData
  );

  /*
   * HidP_GetCaps
   * The HidP_GetCaps routine returns a top-level collection's HIDP_CAPS structure.
   * NTSTATUS
   *  HidP_GetCaps(
   *   IN PHIDP_PREPARSED_DATA  PreparsedData,
   *   OUT PHIDP_CAPS  Capabilities
   *  );
   */
  int HidP_GetCaps(
    int  PreparsedData,
    _HIDP_CAPS  Capabilities
  );
}
