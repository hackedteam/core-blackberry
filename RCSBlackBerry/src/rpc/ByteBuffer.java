//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
package rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import rpc.codec.base64.Base64Decoder;
import rpc.codec.base64.Base64Encoder;
import rpc.codec.base64.Base64FormatException;
import rpc.json.me.JSONException;
import rpc.json.me.JSONObject;

/**
 * ByteBuffer
 *
 * @author Nazmul Idris
 * @version 1.0
 * @since Nov 3, 2008, 3:42:21 PM
 */
public class ByteBuffer {

//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
// constants
//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

public static final int BUFFER_SIZE = 512;
//
// jvm constants for string encoding
//
public static final String ASCII = "US-ASCII";
public static final String UTF8 = "UTF-8";
public static final String DEFAULT_CHAR_ENCODING = UTF8;

//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
// data
//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

protected byte[] byteRay = null;
protected String enc = DEFAULT_CHAR_ENCODING;

//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
// constructors
//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

public ByteBuffer() {
}

public ByteBuffer(byte[] srcBuf) {
  append(srcBuf);
}

public ByteBuffer(ByteBuffer bb) {
  append(bb);
}

/**
 * this method does not close the InputStream. Simply reads all data on the stream into a byte
 * buffer.
 */
public ByteBuffer(InputStream is) throws IOException {
  byte[] readBuf = new byte[BUFFER_SIZE];
  while (true) {
    int read = is.read(readBuf);
    if (read == -1) break;
    append(readBuf, 0, read);
  }
}
public ByteBuffer(String s) throws IllegalArgumentException {
  //BBUtils.assertNotNullOrEmpty(s, "ByteBuffer constructor - string can not be null or empty.");
  append(s.getBytes());
}

//
// pure functionality of the class
//
public ByteBuffer append(
    byte[] srcBuf, int srcStartIndex, int srcLength)
{
  if (byteRay == null) {
    //create a new array
    byteRay = new byte[srcLength];
    //copy the src array to the new one
    /*
    System.out.println(
      "byteRay.length="+byteRay.length +
      ",srcBuf.length="+srcBuf.length +
      ",srcStartIndex="+srcStartIndex +
      ",srcLength="+srcLength );
    */
    arrayCopy(
        srcBuf, srcStartIndex, byteRay, 0, srcLength);
  }
  else {
    int currentSize = byteRay.length;
    //create a new array (which is bigger than existing one)
    byte[] newByteRay = new byte[currentSize + srcLength];
    //copy the old (internal) array into the new one
    arrayCopy(byteRay, 0, newByteRay, 0, currentSize);
    //copy the src array into the new one
    int newByteRayStartIndex = currentSize;
    arrayCopy(
        srcBuf, srcStartIndex,
        newByteRay, newByteRayStartIndex,
        srcLength);
    //now blow away the old internal byte array with the bigger one
    byteRay = newByteRay;
  }

  return this;
}

public byte[] toByteArray() {
  return getBytes();
}

/**
 * This method simply returns the size of this object in KB.
 * This is <b>not</b> the same as {@link #getString()}, which should be used if you're trying to encode the bytes to
 * string form.
 */
public String toString() {
  if ((byteRay != null) && (byteRay.length > 0)) {
    float sizeInKB = byteRay.length / 1000f;
    return sizeInKB + " KB";
  }
  else {
    return "0 KB";
  }
}

/*
public String toString() {
  if (byteRay == null) {
    return null;
  }

  try {
    return new String(byteRay, enc);
  }
  catch (UnsupportedEncodingException e) {
    //this will never happen, unless the DEFAULT_CHAR_ENCODING
    //is invalid
    return "support.ConstantsIF.DEFAULT_CHAR_ENCODING=" +
           DEFAULT_CHAR_ENCODING + " is invalid!";
  }
}
*/

public void setEncoding(String enc) {
  if (enc == null) {
    return;
  }
  else {
    //test this encoding string to be valid
    try {
      byte[] bytes = {(byte) '0', (byte) '1'};
      new String(bytes, enc);
      this.enc = enc;
    }
    catch (UnsupportedEncodingException e) {
      //don't override the default encoding
      System.out.println("unsupported encoding");
    }
  }
}

/** might return null if encoding fails; unlike {@link #getString()} this does not use system default encoding */
public String getEncodedString() {
  try {
    return new String(byteRay, enc);
  }
  catch (Exception e) {
    return null;
  }
}

/** returns a string representation of the byte[] using system default encoding */
public String getString() {
  if (byteRay != null) return new String(byteRay);
  else return "";
}

//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
// base64 codec
//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

/** might return null if encoding fails */
public String getBase64EncodedString() throws IOException {

  if (byteRay == null) return new Base64Encoder("").processString();

  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  ByteArrayInputStream is = getInputStream();

  try {
    Base64Encoder enc = new Base64Encoder(is, baos);
    enc.process();
    return baos.toString();
  }
  catch (IOException e) {
    throw e;
  }
  finally {
    if (baos != null) try {
      baos.close();
    }
    catch (IOException e) {}

    if (is != null) try {
      is.close();
    }
    catch (IOException e) {}
  }

}

/** construct a bytebuffer from the given base64 encoded string */
public static ByteBuffer fromBase64EncodedString(String serform) throws Base64FormatException, IOException {

  //BBUtils.assertNotNullOrEmpty(serform, "Base64Encoded String can not be null or empty.");

  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  ByteArrayInputStream bais = new ByteArrayInputStream(serform.getBytes());

  try {
    Base64Decoder dec = new Base64Decoder(bais, baos);
    dec.process();
    return new ByteBuffer(baos.toByteArray());
  }
  finally {
    try {
      baos.close();
    }
    catch (Exception e) {}
    try {
      bais.close();
    }
    catch (Exception e) {}
  }

}

//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
// internal impl methods
//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

protected final void arrayCopy(byte[] srcBuf, int srcStartIndex,
                               byte[] destBuf, int destStartIndex,
                               int numberOfBytesToCopy)
{
  System.arraycopy(srcBuf, srcStartIndex,
                   destBuf, destStartIndex,
                   numberOfBytesToCopy);
  /*
  System.out.println( "arrayCopy start" );
  for( int i=0; i<numberOfBytesToCopy; i++) {
    destBuf[ destStartIndex + i ] = srcBuf[ srcStartIndex + i ];
    System.out.println( "\tindex="+i );
  }
  System.out.println( "arrayCopy end" );
  */
}

//
// accessors for internal state
//
public byte[] getBytes() {
  if (byteRay == null) {
    return new byte[0];
  }
  return byteRay;
}

public ByteArrayInputStream getInputStream() {
  return new ByteArrayInputStream(getBytes());
}

public int getSize() {
  if (byteRay != null) {
    return byteRay.length;
  }
  else {
    return 0;
  }
}

//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
// convenience methods
//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

public void append(byte[] srcBuf) {
  append(srcBuf, 0, srcBuf.length);
}

public void append(ByteBuffer buf) {
  append(buf.getBytes(), 0, buf.getSize());
}

public void clear() {
  if (byteRay != null) {
    byteRay = null;
  }
}

//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
// JSON support
//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

/** this method adds a base 64 encoded version of this bytebuffer to the given JSON object's key */
public void addToJSON(JSONObject object, String key) throws IOException, JSONException {
  //BBUtils.assertNotNullOrEmpty(key, "ByteBuffer - key for json object can't be empty");
  //BBUtils.assertNotNull(object, "ByteBuffer - json object can't be empty");

  object.put(key, this.getBase64EncodedString());
}

/** this method extracts a value (for the key) from a JSON object, and base 64 decodes it */
public static ByteBuffer getFromJSON(JSONObject object, String key)
    throws JSONException, Base64FormatException, IOException
{
  //BBUtils.assertNotNullOrEmpty(key, "ByteBuffer - key for json object can't be empty");
  //BBUtils.assertNotNull(object, "ByteBuffer - json object can't be empty");

  String value = object.getString(key);
  return ByteBuffer.fromBase64EncodedString(value);
}

//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
// self test method
//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

public static void selftest() {
  byte[] br1 = {(byte) '0', (byte) '1'};
  byte[] br2 = {(byte) '<', (byte) 'T', (byte) '>'};

  System.out.println("::bb1.append( br1 )");
  ByteBuffer bb1 = new ByteBuffer().append(br1, 0, 2);
  bb1.setEncoding(UTF8);
  System.out.println();

  System.out.println("::bb1.toString():" + bb1.toString());
  System.out.println();

  System.out.println("::bb1.append( br2 )");
  bb1.append(br2, 0, 3);
  System.out.println();

  System.out.println("::bb1.toString():" + bb1.toString());
  System.out.println();
}

}//end of ByteB
// uffer class
