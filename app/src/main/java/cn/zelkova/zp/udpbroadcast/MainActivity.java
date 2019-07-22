package cn.zelkova.zp.udpbroadcast;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.etMsg = (TextView) findViewById(R.id.etMsg);

        Button btn = (Button) findViewById(R.id.btnSend);
        btn.setOnClickListener(myClickHandle);

        btn = (Button) findViewById(R.id.btnReceive);
        btn.setOnClickListener(myClickHandle);

        findViewById(R.id.btnClean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMsg.setText("");
            }
        });

        showMsgLine("app started");

    }


    Thread thdReceive;
    TextView etMsg;

    private View.OnClickListener myClickHandle = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.btnSend) {
                Thread thd = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendUDP();
                    }
                });
                thd.start();
            }

            if (v.getId() == R.id.btnReceive) {
                receiveUDP();
            }
        }


        private void receiveUDP() {
            if (thdReceive != null) {
                showMsgLine("UDP receiver is running");
                return;
            }

            thdReceive = new Thread(new Runnable() {
                @Override
                public void run() {
                    DatagramSocket rSocket = null;

                    try {
                        rSocket = new DatagramSocket(1661,Inet4Address.getByName(getWifiBroadcastIP()));
                        rSocket.setBroadcast(true);

                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }


                    while (true) {

                        try {

                            byte[] buffer = new byte[512];
                            final DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                            rSocket.receive(dp);

                            showMsgLine("[接收]" + new String(dp.getData(), 0, dp.getLength()));

                        } catch (SocketException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

            thdReceive.start();

            showMsgLine("UDP receiver started");
        }

        private void sendUDP() {
            try {
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                assert manager != null;
                WifiManager.MulticastLock wifiLock = manager.createMulticastLock("localWifi");
                wifiLock.acquire();

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String content = sdf.format(new Date());
                content = "[" + Build.MODEL + "]" + content;
                byte[] sendBuffer = content.getBytes();

                InetAddress bcIP = Inet4Address.getByName(getWifiBroadcastIP());

                DatagramSocket udp = new DatagramSocket();
                udp.setBroadcast(true);
                udp.setTrafficClass(4);
                DatagramPacket dp = new DatagramPacket(sendBuffer, sendBuffer.length, bcIP, 1661);
                udp.send(dp);

                showMsgLine("[发送]" + content);
                Log.d("UDP", "[发送]" + content);

                wifiLock.release();//必须调用

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private String getWifiBroadcastIP() {
        return "255.255.255.255";
    }

    private void showMsgLine(final String msg) {
        showMsg(msg + "\n");
    }

    private void showMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etMsg.append(msg);
            }
        });
    }
}
