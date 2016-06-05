package go.will.wifkonect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

import static com.android.volley.Request.Method.POST;


public class MainActivity extends AppCompatActivity {
    BluetoothSPP bt;
    TextView textRecived;
    EditText etSend;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textRecived=(TextView)findViewById(R.id.txtsend) ;
        etSend=(EditText)findViewById(R.id.etSend);
        bt = new BluetoothSPP(this);

        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.i("Check", "Length : " + data.length);
                Log.i("Check", "Message : " + message);
                Toast.makeText(getApplicationContext()
                        , message
                        , Toast.LENGTH_SHORT).show();
                textRecived.setText(message);

            }
        });

        Button btnConnect = (Button)findViewById(R.id.btnConnect);
        assert btnConnect != null;
        btnConnect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(MainActivity.this, DeviceList.class);
                    intent.putExtra("bluetooth_devices", "Bluetooth devices");
                    intent.putExtra("no_devices_found", "No device");
                    intent.putExtra("scanning", "Scanning");
                    intent.putExtra("scan_for_devices", "Search");
                    intent.putExtra("select_device", "Select");
                    intent.putExtra("layout_list", R.layout.devices_layout_list);
                    intent.putExtra("layout_text", R.layout.devices_layout_text);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);


                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if(!bt.isBluetoothEnabled()) {
            bt.enable();
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK){
                bt.connect(data);
            }
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void setup() {
        Button btnSend = (Button)findViewById(R.id.btnSend);
        assert btnSend != null;
        btnSend.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                bt.send(etSend.getText().toString(), true);
                Log.d("status",String.valueOf(bt.getServiceState()));
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                StringRequest myReq = new StringRequest(POST, "https://api.telegram.org/bot"+getResources().getString(R.string.id_bot_telegram)+"/sendMessage?chat_id="+getResources().getString(R.string.chat_id)+"&text="+etSend.getText().toString(), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                            Log.d("volleyResponse",s);
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                                    Log.d("volleyResponse","bad");
                            }
                        });

                        queue.add(myReq);

            }
        });
    }


}
