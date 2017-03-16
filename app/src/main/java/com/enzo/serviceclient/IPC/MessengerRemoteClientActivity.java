package com.enzo.serviceclient.IPC;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.enzo.serviceclient.R;

public class MessengerRemoteClientActivity extends AppCompatActivity {
    public static final int MSG_UPLOAD = 1;
    public static final int MSG_DOWNLOAD = 2;
    public static final int MSG_REPLY = 3;

    public static final String FLAG_DATA = "data";

    private Button btn_upload, btn_download;
    private EditText input;
    private TextView output;

    private boolean isServiceBound = false;
    private ServiceConnection mServiceConnection = new MyServiceConnection();
    private Messenger mService = null;

    private Messenger mMessenger = new Messenger(new ClientMessageHandler());

    private class ClientMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_REPLY) {
                if (msg.getData() != null) {
                    String data = (String) msg.getData().get("content");
                    append(data + " downloaded.");
                    Log.d("ClientMessageHandler", "Data download --> " + data);
                }
            } else
                super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger_remote_client);

        // after API 5.0, implicit intent is forbidden. A package name is necessary.
        // you should add this <intent-filter> in your server's manifests file.
        //  <action android:name="com.zeng.servicedemo.ACTION_BIND"/>
        Intent intent = new Intent("com.zeng.servicedemo.ACTION_BIND");
        intent.setPackage("com.zeng.servicedemo");
        bindService(intent, mServiceConnection, 0);

        Log.d("RemoteClientActivity", "create");

        btn_upload = (Button) findViewById(R.id.btn_upload);
        btn_download = (Button) findViewById(R.id.btn_download);
        input = (EditText) findViewById(R.id.input);
        output = (TextView) findViewById(R.id.output);

        btn_upload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String strIn = input.getText().toString();
                if (isServiceBound) {
                    Message msg = Message.obtain(null, MSG_UPLOAD, 0, 0);
                    Bundle data = new Bundle();
                    data.putString("content", strIn);
                    msg.setData(data);
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    append(strIn + " uploaded.");
                }
            }
        });

        btn_download.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isServiceBound) {
                    Message msg = Message.obtain(null, MSG_DOWNLOAD, 0, 0);
                    msg.replyTo = mMessenger;
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    private void append(String text) {
        String str = output.getText().toString();
        str += "\n" + text;
        output.setText(str);
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = new Messenger(iBinder);
            isServiceBound = true;
            Toast.makeText(MessengerRemoteClientActivity.this, "Service bound.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
            mService = null;
            Toast.makeText(MessengerRemoteClientActivity.this, "Service unbound.", Toast.LENGTH_SHORT).show();
        }
    }
}
