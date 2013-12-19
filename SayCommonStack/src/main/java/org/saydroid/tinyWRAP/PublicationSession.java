/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.4
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.saydroid.tinyWRAP;

public class PublicationSession extends SipSession {
  private long swigCPtr;

  public PublicationSession(long cPtr, boolean cMemoryOwn) {
    super(tinyWRAPJNI.PublicationSession_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(PublicationSession obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        tinyWRAPJNI.delete_PublicationSession(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public boolean Publish(byte[] bytes) {
    if(bytes != null){
		final java.nio.ByteBuffer byteBuffer = this.getByteBuffer(bytes);
        return this.publish(byteBuffer, bytes.length);
    }
    return false;
  }

  public PublicationSession(SipStack pStack) {
    this(tinyWRAPJNI.new_PublicationSession(SipStack.getCPtr(pStack), pStack), true);
  }

  public boolean publish(java.nio.ByteBuffer payload, long len, ActionConfig config) {
    return tinyWRAPJNI.PublicationSession_publish__SWIG_0(swigCPtr, this, payload, len, ActionConfig.getCPtr(config), config);
  }

  public boolean publish(java.nio.ByteBuffer payload, long len) {
    return tinyWRAPJNI.PublicationSession_publish__SWIG_1(swigCPtr, this, payload, len);
  }

  public boolean unPublish(ActionConfig config) {
    return tinyWRAPJNI.PublicationSession_unPublish__SWIG_0(swigCPtr, this, ActionConfig.getCPtr(config), config);
  }

  public boolean unPublish() {
    return tinyWRAPJNI.PublicationSession_unPublish__SWIG_1(swigCPtr, this);
  }

}
