package mochineko.android.socket_program;

import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectManager {

    private static ConnectManager instance;
    private MainActivity mainActivity;
    private String hostName;
    private int port;
    private volatile Socket socket;

    private ConnectManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public static ConnectManager getInstance(MainActivity mainActivity) {
        if (instance == null) {
            instance = new ConnectManager(mainActivity);
        }
        return instance;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }

    public void connect() {
        new Thread(() -> {
            try {
                this.socket = new Socket();
                socket.connect(new InetSocketAddress(hostName, port));
                do {
                    byte[] buffer = new byte[1024];
                    int count = socket.getInputStream().read(buffer);
                    if (count != -1) {
                        String message = new String(buffer, 0, count);
                        System.out.println("[Server]" + message);
                    }
                } while (!socket.isClosed());
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void send(String message) {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    return;
                }
                socket.getOutputStream().write((message + "\n").getBytes());
                socket.getOutputStream().flush();
                System.out.println("[Client]" + message);
                mainActivity.runOnUiThread(() -> {
                    TextView textView = mainActivity.findViewById(R.id.textView);
                    textView.setText(textView.getText() + "\n" + message);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } ).start();
    }
}
