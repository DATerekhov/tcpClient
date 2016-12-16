package com.example.tcpclient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Dmitry Terekhov on 12.12.2016.
 */
public class testConnection {

	static String host = "192.168.0.101";
	static int port = 31010;
	static Socket client;
	static InputStream is;
	static OutputStream os;
/*
	public static void main(String[] args) {

		try
		{
			client = new Socket(host, port); //подключение клиента
			is = client.getInputStream();
			os = client.getOutputStream();// получаем поток

			System.out.println("Все хорошо");

			// запускаем новый поток для получения данных
			Thread receiveThread = new Thread(new ReceiveMessage());
			receiveThread.start(); //старт потока
			SendMessage();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			Disconnect();
		}
	}*/
	// отправка сообщений
	static void SendMessage() {
		File file = new File("buch.djvu");
		byte[] temp;
		byte[] size;

		System.out.println("Ent?");
		try {
			temp = FileToBytes("buch.djvu");
			size = getBytes(temp.length);

			os.write(size, 0, size.length);
			os.write(temp, 0, temp.length);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

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

	static void BytesToFile(byte[] bytes, String path)
	{
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(path);
			out.write(bytes, 0, bytes.length);
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
	}
	// получение сообщений
	static class ReceiveMessage implements Runnable
	{
		public void run() {
			byte[] sizeArray;
			int fileSize = 0;
			byte[] temp;

			while (true)
			{
				try
				{
					sizeArray = GetMessage();
					fileSize = toInt32(sizeArray, 0);
					System.out.println("Размер файла = " + fileSize);

					temp = null;
					do
					{
						if (temp == null)
						{
							temp = GetMessage();
						}
						else
						{
							temp = MergeArrays(temp, GetMessage());
						}


					} while (temp.length < fileSize);

					System.out.println("Temp length = " + temp.length);

					BytesToFile(temp, "buch2.djvu");
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					System.out.println("Подключение прервано!"); //соединение было прервано
					Disconnect();
				}
			}
		}

	}

	static byte[] MergeArrays(byte[] first, byte[] second)
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

	static byte[] GetMessage()
	{
		byte[] data = new byte[1024 * 1024];
		int bytes = 0;
		try {
			bytes = is.read(data, 0, data.length);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		if (bytes < 1024 * 1024)
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

	static void Disconnect()
	{
		try {
			if (is != null)
				is.close();//отключение потока
			if (os != null)
				os.close();//отключение потока
			if (client != null)
				client.close();//отключение клиента
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}
}
