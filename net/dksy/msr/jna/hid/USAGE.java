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

/**
 * HID USB swipe reader usages
 */
public enum USAGE {
  UPG_MSR(0xFF00),
  UID_DECODING_RDR(0x1),

  UID_TRACK_1_DECODE_STATUS(0x20),
  UID_TRACK_2_DECODE_STATUS(0x21),
  UID_TRACK_3_DECODE_STATUS(0x22),
  UID_MAGNEPRINT_STATUS(0x23),
  UID_TRACK_1_LEN(0x28),
  UID_TRACK_2_LEN(0x29),
  UID_TRACK_3_LEN(0x2A),
  UID_MAGNEPRINT_LEN(0x2B),
  UID_TRACK_1_DATA(0x30),
  UID_TRACK_2_DATA(0x31),
  UID_TRACK_3_DATA(0x32),
  UID_MAGNEPRINT_DATA(0x33),
  UID_CARD_ENCODE_TYPE(0x38),
  UID_CARD_STATUS(0x39),
  UID_DEVICE_SERIAL_NUMBER(0x40),
  UID_SEQUENCE_COUNTER(0x41),
  USB_UID_ENCRYPT_STATUS(0x42),
  USB_UID_DUKPT_KSN(0x46),
  USB_UID_MTK1_LEN(0x47),
  USB_UID_MTK2_LEN(0x48),
  USB_UID_MTK3_LEN(0x49),
  USB_UID_MTK1_DATA(0x4A),
  USB_UID_MTK2_DATA(0x4B),
  USB_UID_MTK3_DATA(0x4C),
  USB_UID_ESESSID(0x50);

  private final int usage;

  public int getUsage() {
    return this.usage;
  }

  // constructor
  USAGE(int usage) {
    this.usage = usage;
  }
}
