package asuki.me.BlueToothTerminal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;

public class MainActivity extends Activity{
  private static final int REQUEST_ENABLE_BT = 1;

  private TextView text_isConnected;
  private TextView text_messageReceive;
  private EditText edit_messageTransmit;
  private EditText edit_sendingBuffer;
  private Button button_onSend;
  private Button button_onPairedDevices;

  private BlueToothHandler blueToothHandler;
  private static BluetoothSocket socket;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    text_isConnected = findViewById(R.id.TXT_IS_CONNECTED);
    text_messageReceive = findViewById(R.id.EDT_RECEIVED_MESSAGE);
    edit_messageTransmit = findViewById(R.id.EDT_SENT_MESSAGE);
    edit_sendingBuffer = findViewById(R.id.EDT_SEND_MESSAGE);
    button_onSend = findViewById(R.id.BTN_SEND);
    button_onPairedDevices = findViewById(R.id.BTN_PAIRED_DEVICES);

    text_messageReceive.setFocusable(false);
    edit_messageTransmit.setFocusable(false);
    edit_sendingBuffer.setHint("在此填写发送内容");
    edit_sendingBuffer.setOnClickListener(_TextBuffer_Listener);
    button_onSend.setText("发送");
    button_onSend.setClickable(false);
    button_onSend.setOnClickListener(_BLESendMessage_Listener);
  }

  /**
   * @brief:
   * @param:
   * @retval: None
   */
  @Override
  protected void onResume() {
    super.onResume();

    if (null == socket || !socket.isConnected()) {
      button_onSend.setClickable(false);
      text_isConnected.setText("未连接");
      button_onPairedDevices.setText("连接蓝牙设备");
      button_onPairedDevices.setOnClickListener(_BLELink_Listener);
    } else {
      button_onSend.setClickable(true);
      text_isConnected.setText("已连接");
      button_onPairedDevices.setText("断开蓝牙设备");
      button_onPairedDevices.setOnClickListener(_BLEDisconnect_Listener);
      blueToothHandler = new BlueToothHandler(socket, new Handler(_Message_Handler));
      blueToothHandler.start();
    }
  }

  private Handler.Callback _Message_Handler = new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case BlueToothHandler.MESSAGE_READ:
          text_messageReceive.append(new String((byte[]) msg.obj));
          break;
      }
      return true;
    }
  };
  private View.OnClickListener _TextBuffer_Listener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      edit_sendingBuffer.setHint(null);
      edit_sendingBuffer.setOnClickListener(null);
    }
  };
  private View.OnClickListener _BLESendMessage_Listener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if (!edit_sendingBuffer.getText().toString().isEmpty()) {
        String sendStr = edit_sendingBuffer.getText().toString();
        edit_messageTransmit.append(sendStr);
        blueToothHandler.write(sendStr.getBytes());
      }
    }
  };
  private View.OnClickListener _BLEDisconnect_Listener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      try {
        blueToothHandler.cancel();
        socket.close();
        text_messageReceive.setText("");
        onResume();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  };
  private View.OnClickListener _BLELink_Listener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (bluetoothAdapter == null) {
        Toast.makeText(getApplicationContext(), "该设备不支持蓝牙.", Toast.LENGTH_SHORT).show();
      }

      if (null == bluetoothAdapter || !bluetoothAdapter.isEnabled()) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      }

      Intent intent = new Intent().setClass(getApplicationContext(), DevicesListActivity.class);
      startActivity(intent);
    }
  };
  public static void setSocket(BluetoothSocket socket) {
    MainActivity.socket = socket;
  }
}
