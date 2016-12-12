package com.example.tcpclient;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ChatActivity extends AppCompatActivity {
	final String TAG = "myLogs";
	final String DIR_SD = "MyFiles";
	final String FILENAME_SD = "fileSD";
	private final int REQUEST_CODE_GALLERY = 1;

	TextView tvGalleryChoice;
	ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		tvGalleryChoice = (TextView)findViewById(R.id.tvGalleryChoice);
		imageView = (ImageView)findViewById(R.id.imageView);
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

					File sdPath = Environment.getExternalStorageDirectory();
					Log.d(TAG, "onActivityResult: " + sdPath.getPath());
					Log.d(TAG, "onActivityResult: " + getRealPathFromURI(this,imageUri));
				}
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
}