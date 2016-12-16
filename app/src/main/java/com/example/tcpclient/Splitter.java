package com.example.tcpclient;

/**
 * Created by Dmitry Terekhov on 16.12.2016.
 */

public class Splitter {
	 static byte[] takeCode(byte[] commonMessage)
	{
		byte[] temp = new byte[4];
		for (int i = 0; i < 4; i++)
		{
			temp[i] = commonMessage[i];
		}
		return temp;
	}
	 static byte[] takeFileNameLength(byte[] commonMessage)
	{
		byte[] temp = new byte[4];
		for (int i = 0; i < 4; i++)
		{
			temp[i] = commonMessage[i + 4];
		}
		return temp;
	}

	 static byte[] takeFileName(int length, byte[] commonMessage)
	{
		byte[] temp = new byte[length];
		for (int i = 0; i < length; i++)
		{
			temp[i] = commonMessage[i + 8];
		}
		return temp;
	}

	 static byte[] takeMessage(int length, byte[] commonMessage)
	{
		byte[] temp = new byte[commonMessage.length - 8 - length];
		for (int i = 0; i < temp.length; i++)
		{
			temp[i] = commonMessage[i + 8 + length];
		}
		return temp;
	}
}
