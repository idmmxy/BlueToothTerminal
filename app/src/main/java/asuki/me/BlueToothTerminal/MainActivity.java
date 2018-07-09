package asuki.me.BlueToothTerminal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{
  private static final int REQUEST_ENABLE_BT = 1;

  private TextView text_isConnected;
  private EditText edit_messageReceive;
  private EditText edit_messageTransmit;
  private EditText edit_sendingMessageBuffer;
  private Button button_send;
  private Button button_pairedDevices;

  private BluetoothAdapter bluetoothAdapter;
  private ConnectedThread connectedThread;

  /**
   * @brief:
   * @param: None
   * @retval: None
   */
  MainActivity() {
    text_isConnected = findViewById(R.id.TXT_IS_CONNECTED);
    edit_messageReceive = findViewById(R.id.EDT_RECEIVED_MESSAGE);
    edit_messageTransmit = findViewById(R.id.EDT_SENT_MESSAGE);
    edit_sendingMessageBuffer = findViewById(R.id.EDT_SEND_MESSAGE);
    button_send = findViewById(R.id.BTN_SEND);
    button_pairedDevices = findViewById(R.id.BTN_PAIRED_DEVICES);
  }

  /**
   * @brief:
   * @param:
   * @retval: None
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    button_pairedDevices.setOnClickListener(new View.OnClickListener() {
      /**
       * @brief:
       * @param:
       * @retval: None
       */
      @Override
      public void onClick(View v) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
          Toast.makeText(getApplicationContext(), "该设备不支持蓝牙.", Toast.LENGTH_SHORT).show();
        }

        if (!bluetoothAdapter.isEnabled()) {
          Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Intent intent = new Intent().setClass(getApplicationContext(), DevicesListActivity.class);
        startActivity(intent);
      }
    });

    button_send.setOnClickListener(new View.OnClickListener() {
      /**
       * @brief:
       * @param:
       * @retval: None
       */
      @Override
      public void onClick(View v) {
        if (edit_sendingMessageBuffer.getText().toString().isEmpty()) {
          return;
        }
        String sendStr = edit_sendingMessageBuffer.getText().toString();
        edit_messageTransmit.append(sendStr);
        connectedThread.write(sendStr.getBytes());
      }
    });
  }

  /**
   * @brief:
   * @param:
   * @retval: None
   */
  @Override
  protected void onResume() {
    super.onResume();

    if (null == BluetoothUtils.getBluetoothSocket() || null != connectedThread) {
      text_isConnected.setText("未连接");
    } else {
      text_isConnected.setText("已连接");
      connectedThread = new ConnectedThread(BluetoothUtils.getBluetoothSocket(), new Handler() {
        /**
         * @brief:
         * @param:
         * @retval: None
         */
        @Override
        public void handleMessage(Message msg) {
          super.handleMessage(msg);
          switch (msg.what) {
            case ConnectedThread.MESSAGE_READ:
              byte[] buffer = (byte[]) msg.obj;
              int length = msg.arg1;
              for (int i = 0; i < length; i++) {
                char c = (char) buffer[i];
                edit_messageReceive.getText().append(c);
              }
              break;
          }
        }
      });
      connectedThread.start();
    }
  }
}
