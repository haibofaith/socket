package haibo.com.socketservel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import haibo.com.socketservel.service.TCPServerService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, TCPServerService.class));
    }
}
