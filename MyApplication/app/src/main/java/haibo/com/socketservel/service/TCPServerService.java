package haibo.com.socketservel.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class TCPServerService extends Service {
    private boolean mIsServiceDestoryed = false;
    private final String TAG = "TCPServerService";
    private String[] mDefinedMsg = new String[]{
            "你好啊，哈哈",
            "请问你叫什么名字呀？",
            "你是哥哥还是妹妹？",
            "给你讲个笑话好不好"
    };

    public TCPServerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate");
        new Thread(new TcpServer()).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed = true;
        super.onDestroy();
    }

    private class TcpServer implements Runnable{

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                Log.e(TAG,"servel failed,port:8688");
                e.printStackTrace();
                return;
            }
            while (!mIsServiceDestoryed){
                try {
                    final Socket client = serverSocket.accept();
                    Log.e(TAG,"accept");
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //这个true很重要啊
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())),true);
        out.println("欢迎来到聊天室");

        while (!mIsServiceDestoryed){
            String str = in.readLine();
            Log.e(TAG,"msg from client:"+str);
            if (str == null){
                break;
            }
            int i = new Random().nextInt(mDefinedMsg.length);
            String msg = mDefinedMsg[i];
            out.println(msg);
            Log.e(TAG,"send:"+msg);
        }
        Log.e(TAG,"client quit.");
        out.close();
        in.close();
        client.close();
    }


}
