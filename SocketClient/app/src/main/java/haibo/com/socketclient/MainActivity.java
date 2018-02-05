package haibo.com.socketclient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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

    private TextView content_tv;

    private EditText edit_query;

    private static boolean isFlag = false;

    private String content_msg;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    content_msg = "success connect";
                    Log.e(TAG,content_msg);
                    content_tv.setText(content_msg);
                    break;
                case 2:
                    Log.e(TAG, "receive:"+msg.obj);
                    content_msg=content_msg+"\n"+msg.obj;
                    content_tv.setText(content_msg);
                    break;
                case 3:
                    content_msg ="connect failed,retry...";
                    Log.e(TAG,content_msg);
                    content_tv.setText(content_msg);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edit_query = (EditText) findViewById(R.id.edit_query);
        content_tv = (TextView) findViewById(R.id.content_tv);
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
            content_msg=content_msg+"\n"+"client:"+msg;
            content_tv.setText(content_msg);
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
                handler.sendEmptyMessage(3);
            }

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (!MainActivity.isFlag){
                    //每一秒钟从服务端读取一次数据
                    SystemClock.sleep(1000);
                    String msg = br.readLine();
                    if (msg!=null){
                        String time = null;
                        SimpleDateFormat sdf= new SimpleDateFormat("HH:mm:ss");
                        time = sdf.format(new Date(System.currentTimeMillis()));
                        final String showedMsg = "server" +time +":"+msg+"\n";
                        handler.obtainMessage(2,showedMsg).sendToTarget();
                    }
                }
                Log.e(TAG, "client:"+"quit...");
                printWriter.close();
                br.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
