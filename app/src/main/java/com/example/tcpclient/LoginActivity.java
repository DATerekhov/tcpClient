package com.example.tcpclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

	private EditText etLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		etLogin = (EditText)findViewById(R.id.etLogin);
	}

	public void bLoginOK_Click(View view) {
		Intent intent = new Intent();
		intent.putExtra("name", etLogin.getText().toString());
		setResult(RESULT_OK, intent);
		finish();
	}
}
