package haibo.com.socketclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private PrintWriter printWriter;
    private final String TAG = "MainActivity";

    private EditText edit_query;

    private static boolean isFlag = false;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    Log.e(TAG,"success connect");
                    break;
                case 2:
                    Log.e(TAG, (String) msg.obj);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edit_query = (EditText) findViewById(R.id.edit_query);
        Intent intent = new Intent("haibo.com.socketservel.service.TCPServerService");
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);

        new Thread(){
            @Override
            public void run() {
                connectTCPServer();
            }
        }.start();
    }

    public void send(View view){
        String msg =edit_query.getText().toString().trim();
        if (!TextUtils.isEmpty(msg)&&printWriter!=null){
            printWriter.println(msg);
            Log.e(TAG, "client:"+msg);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFlag = true;
    }

    private void connectTCPServer() {
        Socket socket = null;
        while (socket == null){
            try {
                socket = new Socket("localhost",8688);
                printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                handler.sendEmptyMessage(1);
            } catch (IOException e) {
                SystemClock.sleep(1000);
                e.printStackTrace();
                Log.e(TAG,"connect failed,retry...");
            }

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (!MainActivity.isFlag){
                    SystemClock.sleep(1000);
                    String msg = br.readLine();
                    Log.e(TAG,"receive:"+msg);
                    if (msg!=null){
                        String time = null;
                        SimpleDateFormat sdf= new SimpleDateFormat("HH:mm:ss");
                        time = sdf.format(new Date(System.currentTimeMillis()));
                        final String showedMsg = "server" +time +":"+msg+"\n";
                        handler.obtainMessage(2,showedMsg).sendToTarget();
                    }
                }
                System.out.println("quit...");
                printWriter.close();
                br.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}
