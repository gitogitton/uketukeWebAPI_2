package com.example.uketukewebapi_2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().toString();
    private Button httpRequest;
    private EditText searchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        httpRequest = (Button)findViewById( R.id.button );
        searchId = (EditText) findViewById( R.id.edit_searchId );

        searchId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d( TAG, "httpRequest beforeTextChanged() s / start / count / after :" + s.toString() + " / " + start + " / " + count + " / " + after  );
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d( TAG, "httpRequest onTextChanged() s / start / before / count : " + s.toString() + " / " + start + " / " + before + " / " + count  );
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d( TAG, "httpRequest afterTextChanged() : " + s.toString() );
                if ( s.toString().equals( "0" ) ) { //0始まりは許可しない
                    s.clear();
                }
            }
        });

        httpRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d( TAG, "httpRequest onClick()" );
                searchId.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d( TAG, "Thread run." );
                        requestHTTP_json();
                    }
                }).start();
            }
        });
    }

    private void requestHTTP_json() {
        Log.d( TAG, "requestHTTP_json()  run." );
        try {
//          String urlString = "http://192.168.11.4:8080/project/xxxx?user_id=35&password=12345";
            String urlString = "http://192.168.11.10:8080/project/userServlet?user_id=";
            urlString = urlString + searchId.getText().toString();
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//          URLConnection connection = url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); //msec : コネクションタイムアウト：10秒
            connection.setReadTimeout( 10000 ); //msec : inputStreamのリードタイムアウト：10秒（value specifies the timeout when reading from Input stream when a connection is established to a resource.）
//            connection.setDoInput(true); //true:URLに対してRead dataを設定（デフォルト：true）
//            connection.setDoOutput(true);//true:URLに対してwrite dataをする（デフォルト：false）
            connection.connect();
            int connectStatus = connection.getResponseCode();
            if (connectStatus == HttpsURLConnection.HTTP_OK) {
                Log.d(TAG, "HttpURLConnection.HTTP_OK");
                StringBuilder result = new StringBuilder();
                result.setLength(0);
                //response読み込み
                final InputStream inputStream = connection.getInputStream();
//              final String encoding = connection.getContentEncoding();
                final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }

                bufferedReader.close();
                inputStreamReader.close();
                inputStream.close();

                JSONObject jsonObject = new JSONObject(result.toString());
                TextView loginName = (TextView) findViewById(R.id.receive_data);
                loginName.setText(jsonObject.getString("name"));
            } else {
                Log.d(TAG, "HttpURLConnection NG connectStatus = " + connectStatus);
            }
        } catch ( SocketTimeoutException e ) { //connection timeout
            Log.d( TAG, "connection timeout !" );
        } catch ( IllegalArgumentException e ) { //input stream read timeout
            Log.d( TAG, "read timeout !" );
        } catch (IOException | JSONException e) {
            Log.d( TAG, "なんでかな～" );
            e.printStackTrace();
        }
    }
}