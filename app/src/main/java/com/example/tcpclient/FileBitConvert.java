package com.example.tcpclient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Dmitry Terekhov on 06.12.2016.
 */

public class FileBitConvert {
	public static byte[] FileToBytes(String path) {
		ByteArrayOutputStream out = null;
		InputStream input = null;
		try {
			out = new ByteArrayOutputStream();
			input = new BufferedInputStream(new FileInputStream(path));
			int data = 0;
			while ((data = input.read()) != -1) {
				out.write(data);
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}

				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return out.toByteArray();
	}

	public static void BytesToFile(byte[] bytes, String path) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(path);
			out.write(bytes, 0, bytes.length);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}