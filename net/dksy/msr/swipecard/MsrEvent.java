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

import java.util.*;

public class MsrEvent extends EventObject {
  public MsrEvent(Object source) {
    super(source);
  }

  public String track1;
  public String track2;
  public String track3;
  
  @Override
  public String toString(){
  	String str="T1: "+track1;
  	str+="T2: "+track2;
  	str+="T3: "+track3;
  	return str;
  }
}
