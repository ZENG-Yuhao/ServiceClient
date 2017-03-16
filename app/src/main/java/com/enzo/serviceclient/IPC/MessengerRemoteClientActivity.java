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
                if (msg.obj != null) {
                    String data = (String) msg.obj;
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

        Intent intent = new Intent("com.zeng.servicedemo.IPC.MessengerService");
        intent.setPackage("com.zeng.servicedemo.IPC.");
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

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
                    msg.obj = strIn;
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    append(strIn + " uploaded.");
                    Log.d("RemoteClientActivity", " data upload:" + strIn);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
            mService = null;
        }
    }
}
