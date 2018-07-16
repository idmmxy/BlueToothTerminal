package asuki.me.BlueToothTerminal;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by wjg on 2017/6/11.
 */

public class BlueToothHandler extends Thread {
  private final InputStream mmInStream;
  private final OutputStream mmOutStream;
  private Handler mHandler;

  final static int MESSAGE_READ = 1;

  BlueToothHandler(BluetoothSocket socket, Handler handler) {
    mHandler = handler;
    InputStream tmpIn = null;
    OutputStream tmpOut = null;

    // Get the input and output streams, using temp objects because
    // member streams are final
    try {
      tmpIn = socket.getInputStream();
      tmpOut = socket.getOutputStream();
    } catch (IOException e) { }

    mmInStream = tmpIn;
    mmOutStream = tmpOut;
  }

  public void run() {

    int bytes; // bytes returned from read()

    // Keep listening to the InputStream until an exception occurs
    while (true) {
      byte[] buffer = new byte[100];
      try {
        // Read from the InputStream
        bytes = mmInStream.read(buffer);
        // Send the obtained bytes to the UI activity
        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                .sendToTarget();
      } catch (IOException e) {
        break;
      }
    }
  }

  /* Call this from the main activity to send data to the remote device */
  void write(byte[] bytes) {
    try {
      mmOutStream.write(bytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /* Call this from the main activity to shutdown the connection */
  public void cancel() throws IOException {
    mmInStream.close();
  }

}
