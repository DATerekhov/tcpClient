package com.example.tcpclient;

/**
 * Created by Dmitry Terekhov on 16.12.2016.
 */

public class Splitter {
	static byte[] takeCode(byte[] commonMessage) {
		byte[] temp = new byte[4];
		for (int i = 0; i < 4; i++) {
			temp[i] = commonMessage[i];
		}
		return temp;
	}

	static byte[] takeMessage(byte[] commonMessage) {
		byte[] temp = new byte[commonMessage.length - 4];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = commonMessage[i + 4];
		}
		return temp;
	}
}
