package com.example.tcpclient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Dmitry Terekhov on 15.12.2016.
 */

public class Converter {

	static byte[] FileToBytes(String path)
	{
		ByteArrayOutputStream out = null;
		InputStream input = null;
		try
		{
			out = new ByteArrayOutputStream();
			input = new BufferedInputStream(new FileInputStream(path));
			int data = 0;
			while ((data = input.read()) != -1)
			{
				out.write(data);
			}
		}
		catch (FileNotFoundException ex)
		{
			ex.printStackTrace();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if(input != null)
				{
					input.close();
				}

				if(out != null)
				{
					out.close();
				}
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}

		return out.toByteArray();
	}
	static byte[] getBytes(int v)
	{
		byte[] writeBuffer = new byte[4];
		writeBuffer[3] = (byte) ((v >> 24) & 0xFF);
		writeBuffer[2] = (byte) ((v >> 16) & 0xFF);
		writeBuffer[1] = (byte) ((v >> 8) & 0xFF);
		writeBuffer[0] = (byte) ((v >> 0) & 0xFF);
		return writeBuffer;
	}
	static int toInt32(byte[] data, int offset)
	{
		return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)
				| ((data[offset + 2] & 0xFF) << 16)
				| ((data[offset + 3] & 0xFF) << 24);
	}
}
