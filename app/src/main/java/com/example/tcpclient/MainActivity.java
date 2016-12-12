package com.example.tcpclient;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
	private final String TAG = "MainActivity";
	final String DIR_SD = "MyFiles";
	final String FILENAME_SD = "fileSD";
	private final int REQUEST_CODE_GALLERY = 1;

	SharedPreferences sPref;

	private EditText edit_ip = null;
	private EditText edit_port = null;
	private Button btn_connect = null;
	private Button bGallery = null;
	private EditText edit_receive = null;
	private EditText edit_send = null;
	private Button btn_send = null;
	private TextView tvIP;
	private TextView tvPort;
	TextView tvGalleryChoice;
	ImageView imageView;

	//About the socket
	Handler handler;
	ClientThread clientThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		edit_ip = (EditText) findViewById(R.id.edit_ip);
		edit_port = (EditText) findViewById(R.id.edit_port);
		edit_receive = (EditText) findViewById(R.id.edit_receive);
		btn_connect = (Button) findViewById(R.id.btn_connect);
		btn_send = (Button) findViewById(R.id.btn_send);
		edit_send = (EditText)findViewById(R.id.edit_send);
		tvIP = (TextView) findViewById(R.id.txt_ip);
		tvPort = (TextView) findViewById(R.id.txt_port);
		tvGalleryChoice = (TextView) findViewById(R.id.tvGalleryChoice);
		imageView = (ImageView) findViewById(R.id.imageView);

		init();
		Animat();

		btn_send.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (clientThread.isConnect) {
					Toast.makeText(MainActivity.this, "isConnected", Toast.LENGTH_SHORT).show();
					btn_connect.setText(R.string.btn_disconnect);
				}

				try {
					Message msg = new Message();
					msg.what = 0x852;
					msg.obj = edit_send.getText().toString();
					clientThread.sendHandler.sendMessage(msg);
					edit_send.setText("");
					/*String imagePath = "/storage/emulated/0/DCIM/Camera/P_20161210_121416.jpg";
					Message msg = new Message();
					msg.what = 0x800;
					msg.obj = imagePath;
					clientThread.sendHandler.sendMessage(msg);*/
				} catch (Exception e) {
					Log.d(TAG, e.getMessage());
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		saveIpAndPort();
		super.onDestroy();
	}

	void writeFileSD() {
		// проверяем доступность SD
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Log.d(TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
			return;
		}
		// получаем путь к SD
		File sdPath = Environment.getExternalStorageDirectory();
		// добавляем свой каталог к пути
		sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
		// создаем каталог
		sdPath.mkdirs();
		// формируем объект File, который содержит путь к файлу
		File sdFile = new File(sdPath, FILENAME_SD);
		try {
			// открываем поток для записи
			BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
			// пишем данные
			bw.write("Содержимое файла на SD");
			// закрываем поток
			bw.close();
			Log.d(TAG, "Файл записан на SD: " + sdFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readFileSD() {
		// проверяем доступность SD
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Log.d(TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
			return;
		}
		// получаем путь к SD
		File sdPath = Environment.getExternalStorageDirectory();
		// добавляем свой каталог к пути
		sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
		// формируем объект File, который содержит путь к файлу
		File sdFile = new File(sdPath, FILENAME_SD);
		try {
			// открываем поток для чтения
			BufferedReader br = new BufferedReader(new FileReader(sdFile));
			String str = "";
			// читаем содержимое
			while ((str = br.readLine()) != null) {
				Log.d(TAG, str);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = {MediaStore.Images.Media.DATA};
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void test_Click(View view) {
		Intent intent = new Intent(this, MediaRecorderActivity.class);
		startActivity(intent);
	}

	public void galleryChoiceClick(View view) {

		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
			case REQUEST_CODE_GALLERY:
				if (resultCode == RESULT_OK) {
					Uri imageUri = imageReturnedIntent.getData();
					tvGalleryChoice.setText(imageUri.getEncodedPath());
					imageView.setImageURI(imageUri);

					String KEK = getRealPathFromURI(this, imageUri);
					File sdPath = Environment.getExternalStorageDirectory();
					Log.d(TAG, "onActivityResult: " + sdPath.getPath());
					Log.d(TAG, "onActivityResult: " + KEK);
					File file = new File(KEK);
					if (file.exists()) {
						Log.d(TAG, "onActivityResult: File Exist");
					} else
						Log.d(TAG, "onActivityResult: File Not Exist");
				}
		}
	}

	private void saveIpAndPort() {
		sPref = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = sPref.edit();
		editor.putString("IP", edit_ip.getText().toString());
		editor.putString("PORT", edit_port.getText().toString());
		editor.apply();
	}

	private void init() {
		sPref = getPreferences(MODE_PRIVATE);
		String ip = sPref.getString("IP", "192.168.1.35");
		String port = sPref.getString("PORT", "8888");
		edit_ip.setText(ip);
		edit_port.setText(port);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0x123) {
					edit_receive.setText(msg.obj.toString());
				}
			}
		};
	}

	private void Animat() {
		Animation anim1 = null;
		Animation anim2 = null;
		Animation anim5 = null;
		Animation anim6 = null;
		Animation bAnim = null;

		anim1 = AnimationUtils.loadAnimation(this, R.anim.translate);
		anim2 = AnimationUtils.loadAnimation(this, R.anim.translate2);
		anim5 = AnimationUtils.loadAnimation(this, R.anim.translate5);
		anim6 = AnimationUtils.loadAnimation(this, R.anim.translate6);

		bAnim = AnimationUtils.loadAnimation(this, R.anim.myscale);
		btn_connect.startAnimation(bAnim);

		tvIP.startAnimation(anim1);
		tvPort.startAnimation(anim2);
		edit_ip.startAnimation(anim5);
		edit_port.startAnimation(anim6);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	void Sleep(int i) {
		try {
			TimeUnit.MILLISECONDS.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void bConnect_Click(View view) {
		String ip = edit_ip.getText().toString();
		String port = edit_port.getText().toString();

		saveIpAndPort();

		Log.d(TAG, ip + port);

		clientThread = new ClientThread(handler, ip, port);
		new Thread(clientThread).start();
		Log.d(TAG, "clientThread is start!!");

		Handler handler = new Handler();    //отложенный вызов
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					if (clientThread.isConnect) {
						Message msg = new Message();
						msg.what = 0x840;
						msg.obj = "Nickname";
						clientThread.sendHandler.sendMessage(msg);
						Log.d(TAG, "Message nickname send!");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1500);

		//Intent intent = new Intent(this, ChatActivity.class);
		//startActivity(intent);
	}
}