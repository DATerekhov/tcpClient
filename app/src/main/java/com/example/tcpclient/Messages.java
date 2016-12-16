package com.example.tcpclient;

import java.io.InputStream;

/**
 * Created by Dmitry Terekhov on 15.12.2016.
 */

public class Messages {
	static byte[] GetMessage(InputStream inputStream) {
		byte[] data = new byte[1024*1024];
		int bytes = 0;
		try {
			bytes = inputStream.read(data, 0, data.length);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		if (bytes < 1024*1024)
		{
			byte[] result = new byte[bytes];
			for (int i = 0; i < bytes; i++)
			{
				result[i] = data[i];
			}
			return result;
		}

		return data;
	}
}
