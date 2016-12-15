package com.example.tcpclient;

import java.util.concurrent.TimeUnit;

/**
 * Created by Dmitry Terekhov on 15.12.2016.
 */

public class Sleeper {
	static void milliseconds(int i) {
		try {
			TimeUnit.MILLISECONDS.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	static void seconds(int i){
		try {
			TimeUnit.SECONDS.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
