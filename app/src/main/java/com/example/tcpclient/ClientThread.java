package com.example.tcpclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ClientThread implements Runnable
{
	private final String TAG = "ClientThread";
	
	private Socket socket;
	private String ip;
	private int port;
	private Handler receiveHandler;
	public Handler sendHandler;
	private InputStream inputStream;
	private OutputStream outputStream;
	public boolean isConnect = false;
	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
			Environment.DIRECTORY_PICTURES), "CameraSample");

	public ClientThread(Handler handler, String ip, String port) {
		this.receiveHandler = handler;
		this.ip = ip;
		this.port = Integer.parseInt(port);
		Log.d(TAG, "ClientThread's construct is OK!!");
	}

	public ClientThread()
	{
		Log.d(TAG, "It is may be construct's problem...");
	}

	public void run()
	{
		try 
		{
			Log.d(TAG, "Into the run()");
			socket = new Socket(ip, port);

			isConnect = socket.isConnected();
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			
			//To monitor if receive Msg from Server
			new Thread()
			{
				@Override
				public void run()
				{

					byte[] sizeArray;
					int fileSize;
					byte[] commonMessage;
					byte[] mesCode;
					int code;
					byte[] temp;
					byte[] fileNameLength = new byte[4];
					int length;
					byte[] fileName;

					StringBuilder stringBuilder = new StringBuilder();

					try
					{
						while(socket.isConnected())
						{
							/*
							mesCode = Messages.GetMessage(inputStream);
							code = Converter.toInt32(mesCode, 0);
							Log.d(TAG, "run: Код сщщбщения " + code);

							sizeArray = Messages.GetMessage(inputStream);
							fileSize = Converter.toInt32(sizeArray, 0);
							Log.d(TAG, "Размер сообщения: " + fileSize);

							if(fileSize == -1)
							{
								inputStream.close();
								outputStream.close();
							}
							if(fileSize == 0)continue;

							temp = null;
							do
							{
								if (temp == null)
								{
									temp = Messages.GetMessage(inputStream);
								}
								else
								{
									temp = Merger.ByteArrays(temp, Messages.GetMessage(inputStream));
								}
							} while (temp.length < fileSize);

							Log.d(TAG, "принято: " + temp.length + " байт");

							switch (code){
								case 852:
									stringBuilder.append(new String(temp, 0, fileSize) + "\n");
									Message msg = new Message();
									msg.what = 0x123;
									msg.obj = stringBuilder.toString();
									receiveHandler.sendMessage(msg);
									break;
							}
							*/
							sizeArray = Messages.GetMessage(inputStream);
							fileSize = Converter.toInt32(sizeArray, 0);

							Log.d(TAG, "run: Размер файла от клиента: " + fileSize);

							temp = null;
							commonMessage = null;

							do
							{
								if (commonMessage == null)
								{
									commonMessage = Messages.GetMessage(inputStream);
								}
								else
								{
									commonMessage = Merger.ByteArrays(commonMessage, Messages.GetMessage(inputStream));
								}
							} while (commonMessage.length < fileSize);


							Log.d(TAG, "run: пришло " + commonMessage.length + " байт");

							mesCode = Splitter.takeCode(commonMessage);
							fileNameLength = Splitter.takeFileNameLength(commonMessage);
							length = Converter.toInt32(fileNameLength, 0);
							fileName = Splitter.takeFileName(length, commonMessage);
							temp = Splitter.takeMessage(length, commonMessage);

							code = Converter.toInt32(mesCode, 0);

							Log.d(TAG, "run: Код сообщения: " + code);

							switch (code)
							{
								case 800:
									Converter.BytesToFile(temp, mediaStorageDir.getPath()+ File.separator + new String(fileName, 0, fileName.length));
									break;
								case 852:
									stringBuilder.append(new String(temp, 0, temp.length) + "\n");
									Message msg = new Message();
									msg.what = 0x123;
									msg.obj = stringBuilder.toString();
									receiveHandler.sendMessage(msg);
									break;
							}
						}
					}
					catch(Exception e)
					{
						Log.d(TAG, e.getMessage());
						e.printStackTrace();
					}
				}
				
			}.start();
			
			//To Send Msg to Server
			Looper.prepare();
			sendHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (msg.what == 0x852) { //сообщение чата
						try {
							byte[] temp;
							byte[] size;
							byte[] mesCode;
							byte[] byteFileName;
							byte[] fileNameLength;
							byte[] commonMessage;

							temp = msg.obj.toString().getBytes();
							mesCode = Converter.getBytes(852);
							byteFileName = Converter.getBytes(0);
							fileNameLength = Converter.getBytes(0);

							commonMessage = Merger.ByteArrays(mesCode, fileNameLength);
							commonMessage = Merger.ByteArrays(commonMessage, byteFileName);
							commonMessage = Merger.ByteArrays(commonMessage, temp);

							size = Converter.getBytes(commonMessage.length);

							outputStream.write(size, 0, size.length);

							Sleeper.milliseconds(100);

							outputStream.write(commonMessage, 0, commonMessage.length);
							outputStream.flush();
						} catch (Exception e) {
							Log.d(TAG, e.getMessage());
							e.printStackTrace();
						}
					}
					if (msg.what == 0x840) {
						try { //сообщение отправки ника на сервер
							/*byte[] mes = msg.obj.toString().getBytes();
							byte[] mesSize = Converter.getBytes(mes.length);

							outputStream.write(mesSize, 0, mesSize.length);
							outputStream.write(mes, 0, mes.length);
							outputStream.flush();*/
							byte[] temp;
							byte[] size;
							byte[] mesCode;
							byte[] byteFileName;
							byte[] fileNameLength;
							byte[] commonMessage;

							temp = msg.obj.toString().getBytes();
							mesCode = Converter.getBytes(852);
							byteFileName = Converter.getBytes(0);
							fileNameLength = Converter.getBytes(0);

							commonMessage = Merger.ByteArrays(mesCode, fileNameLength);
							commonMessage = Merger.ByteArrays(commonMessage, byteFileName);
							commonMessage = Merger.ByteArrays(commonMessage, temp);

							size = Converter.getBytes(commonMessage.length);

							outputStream.write(size, 0, size.length);

							Sleeper.milliseconds(100);

							outputStream.write(commonMessage, 0, commonMessage.length);
							outputStream.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (msg.what == 0x800) {
						try {	//отправка выбранного из галереи
							String imagePath = msg.obj.toString();
							String fileName = "test.jpg";
							File file = new File(imagePath);

							if (file.exists()) {
								Log.d(TAG, "handleMessage: File exists");
								fileName = file.getName();
							} else {
								Log.d(TAG, "handleMessage: File not exists");
							}

							byte[] temp;
							byte[] size;
							byte[] mesCode;
							byte[] byteFileName;
							byte[] fileNameLength;
							byte[] commonMessage;

							Log.d(TAG, "handleMessage: sending Image");

							try {
								temp = Converter.FileToBytes(imagePath);
								mesCode = Converter.getBytes(800);
								byteFileName = fileName.getBytes();
								fileNameLength = Converter.getBytes(byteFileName.length);

								commonMessage = Merger.ByteArrays(mesCode, fileNameLength);
								commonMessage = Merger.ByteArrays(commonMessage, byteFileName);
								commonMessage = Merger.ByteArrays(commonMessage, temp);

								size = Converter.getBytes(commonMessage.length);
								outputStream.write(size, 0, size.length);

								Sleeper.milliseconds(100);

								outputStream.write(commonMessage, 0, commonMessage.length);
								outputStream.flush();
							} catch (IOException ex) {
								ex.printStackTrace();
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			Looper.loop();
			
		} catch (SocketTimeoutException e) 
		{
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}catch (UnknownHostException e) 
		{
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
}
