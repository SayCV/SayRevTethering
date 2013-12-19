/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.4
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.saydroid.tinyWRAP;

public class SipEvent {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  public SipEvent(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(SipEvent obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        tinyWRAPJNI.delete_SipEvent(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public short getCode() {
    return tinyWRAPJNI.SipEvent_getCode(swigCPtr, this);
  }

  public String getPhrase() {
    return tinyWRAPJNI.SipEvent_getPhrase(swigCPtr, this);
  }

  public SipSession getBaseSession() {
    long cPtr = tinyWRAPJNI.SipEvent_getBaseSession(swigCPtr, this);
    return (cPtr == 0) ? null : new SipSession(cPtr, false);
  }

  public SipMessage getSipMessage() {
    long cPtr = tinyWRAPJNI.SipEvent_getSipMessage(swigCPtr, this);
    return (cPtr == 0) ? null : new SipMessage(cPtr, false);
  }

}
