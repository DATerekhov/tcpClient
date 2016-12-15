package com.example.tcpclient;

/**
 * Created by Dmitry Terekhov on 15.12.2016.
 */

public class Merger {
	static byte[] ByteArrays(byte[] first, byte[] second)
	{
		byte[] res;
		if (first != null && second != null)
		{
			res = new byte[first.length + second.length];
			for (int i = 0; i < first.length; i++)
			{
				res[i] = first[i];
			}

			int j = first.length;
			for (int i = 0; i < second.length; i++)
			{
				res[j++] = second[i];
			}

			return res;
		}
		else
		{
			return null;
		}

	}
}
