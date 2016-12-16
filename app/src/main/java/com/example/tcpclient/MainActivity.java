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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
	private final String TAG = "MainActivity";
	final String DIR_SD = "MyFiles";
	final String FILENAME_SD = "fileSD";
	private final int REQUEST_CODE_GALLERY = 1;
	private final int REQUEST_CODE_LOGIN = 2;

	SharedPreferences sPref;
	private static Uri imageUri = null;
	private EditText etLoginName;

	private EditText edit_ip = null;
	private EditText edit_port = null;
	private Button btn_connect = null;
	private Button bGallery = null;
	private static EditText edit_receive = null;
	private EditText edit_send = null;
	private Button btn_send = null;
	private TextView tvIP;
	private TextView tvPort;
	private Button bSendImage = null;
	TextView tvGalleryChoice;
	ImageView imageView;

	//About the socket
	Handler handler;
	static ClientThread clientThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		edit_ip = (EditText) findViewById(R.id.edit_ip);
		edit_port = (EditText) findViewById(R.id.edit_port);
		edit_receive = (EditText) findViewById(R.id.edit_receive);
		btn_connect = (Button) findViewById(R.id.btn_connect);
		btn_send = (Button) findViewById(R.id.btn_send);
		edit_send = (EditText) findViewById(R.id.edit_send);
		tvIP = (TextView) findViewById(R.id.txt_ip);
		tvPort = (TextView) findViewById(R.id.txt_port);
		tvGalleryChoice = (TextView) findViewById(R.id.tvGalleryChoice);
		imageView = (ImageView) findViewById(R.id.imageView);
		bSendImage = (Button)findViewById(R.id.bSendImage);
		etLoginName = (EditText)findViewById(R.id.etLoginName);

		init();
		Animat();

		if(clientThread != null){
			if(clientThread.isConnect){
				btn_connect.setText("Disconnect");
				btn_connect.setEnabled(false);
			}
		}
	}

	@Override
	protected void onDestroy() {
		saveIpAndPort();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
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

		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		photoPickerIntent.setType("image/* video/*");
		startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data == null)return;
		switch (requestCode) {
			case REQUEST_CODE_GALLERY:
				if (resultCode == RESULT_OK) {
					imageUri = null;
					imageUri = data.getData();
					//imageView.setImageURI(imageUri);
					String KEK = getRealPathFromURI(this, imageUri);
					tvGalleryChoice.setText(KEK);

					Log.d(TAG, "onActivityResult: " + KEK);
					File file = new File(KEK);
					if (file.exists()) {
						Log.d(TAG, "onActivityResult: File Exist");
					} else
						Log.d(TAG, "onActivityResult: File Not Exist");
				}
			case REQUEST_CODE_LOGIN:
				if (resultCode == RESULT_OK){

				}
				break;
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

	public void bConnect_Click(View view) {
		if(clientThread != null){
			if(clientThread.isConnect){
				btn_connect.setText(R.string.btn_disconnect);
				btn_connect.setEnabled(false);
				return;
			}
		}
		String ip = edit_ip.getText().toString();
		String port = edit_port.getText().toString();

		saveIpAndPort();

		Log.d(TAG, ip + port);

		if(etLoginName.getText().toString().isEmpty())return;

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
						msg.obj = etLoginName.getText().toString();
						clientThread.sendHandler.sendMessage(msg);
						Log.d(TAG, "Message nickname send!");

						btn_connect.setText(R.string.btn_disconnect);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1000);
	}

	public void bSend_Click(View view) {
		try {
			Message msg = new Message();
			msg.what = 0x852;
			msg.obj = edit_send.getText().toString();
			clientThread.sendHandler.sendMessage(msg);
			edit_send.setText("");
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
			e.printStackTrace();

		}
	}

	public void bSendImage_Click(View view) {
		if (clientThread == null) {
			Toast.makeText(MainActivity.this, "is not Connected", Toast.LENGTH_SHORT).show();
			return;
		}
		if(imageUri == null) {
			Toast.makeText(this, "Картинка не выбрана", Toast.LENGTH_SHORT).show();
			return;
		}

		String imagePath = getRealPathFromURI(this, imageUri);

		File file = new File(imagePath);
		if (file.exists()) {
			Log.d(TAG, "onActivityResult: File Exist");
		} else{
			Log.d(TAG, "onActivityResult: File Not Exist");
		}

		Message msg = new Message();
		msg.what = 0x800;
		msg.obj = imagePath;
		clientThread.sendHandler.sendMessage(msg);

		imageUri = null;
		imageView.setImageResource(0);
		tvGalleryChoice.clearComposingText();
		Toast.makeText(this, "Image sending!", Toast.LENGTH_SHORT).show();
	}
}