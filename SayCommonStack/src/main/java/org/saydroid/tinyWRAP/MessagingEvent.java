/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.4
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.saydroid.tinyWRAP;

public class MessagingEvent extends SipEvent {
  private long swigCPtr;

  public MessagingEvent(long cPtr, boolean cMemoryOwn) {
    super(tinyWRAPJNI.MessagingEvent_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(MessagingEvent obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        tinyWRAPJNI.delete_MessagingEvent(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public tsip_message_event_type_t getType() {
    return tsip_message_event_type_t.swigToEnum(tinyWRAPJNI.MessagingEvent_getType(swigCPtr, this));
  }

  public MessagingSession getSession() {
    long cPtr = tinyWRAPJNI.MessagingEvent_getSession(swigCPtr, this);
    return (cPtr == 0) ? null : new MessagingSession(cPtr, false);
  }

  public MessagingSession takeSessionOwnership() {
    long cPtr = tinyWRAPJNI.MessagingEvent_takeSessionOwnership(swigCPtr, this);
    return (cPtr == 0) ? null : new MessagingSession(cPtr, true);
  }

}
