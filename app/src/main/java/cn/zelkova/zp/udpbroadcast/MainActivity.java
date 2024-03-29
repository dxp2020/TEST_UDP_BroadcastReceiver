package cn.zelkova.zp.udpbroadcast;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.zelkova.zp.Book;
import cn.zelkova.zp.BookManager;
import cn.zelkova.zp.service.LocalService;

public class MainActivity extends FragmentActivity {

    private String hostName = "255.255.255.255";
    private BookManager bookManager;
    private Book book;

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

        findViewById(R.id.btn_aidl_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bookManager!=null&&book!=null){
                    try {
                        bookManager.setBookName(book,"水浒传");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        findViewById(R.id.btn_aidl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bindService(new Intent(MainActivity.this, LocalService.class), new ServiceConnection() {

                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        try {
                            bookManager = BookManager.Stub.asInterface(iBinder);
                            book = bookManager.getBook();
                            Toast.makeText(MainActivity.this,"book-->"+book.getName(),Toast.LENGTH_SHORT).show();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {
                        Toast.makeText(MainActivity.this,"断开-->"+componentName.getClassName(),Toast.LENGTH_SHORT).show();
                    }

                }, BIND_AUTO_CREATE);
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
                        while (true) {
                            sendUDP();
                            try {
                                Thread.sleep(500l);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
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
                        rSocket = new DatagramSocket(7001);
                        rSocket.setBroadcast(true);

                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                    while (true) {

                        try {
                            WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            assert manager != null;
                            WifiManager.MulticastLock wifiLock = manager.createMulticastLock("localWifi");
                            wifiLock.acquire();

                            byte[] buffer = new byte[512];
                            final DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                            rSocket.receive(dp);

                            showMsgLine("[接收]" + new String(dp.getData(), 0, dp.getLength()));

                            wifiLock.release();//必须调用

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
                DatagramPacket dp = new DatagramPacket(sendBuffer, sendBuffer.length, bcIP, 7001);
                udp.send(dp);

                showMsgLine("[发送]" + content+"---"+hostName);
                Log.d("UDP", "[发送]" + content );

                wifiLock.release();//必须调用

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private String getWifiBroadcastIP() {
//        hostName = TouchNetUtil.getLocalInetAddress(getApplicationContext()).getHostName();
//        hostName = hostName.substring(0,hostName.lastIndexOf("."))+".255";
        return hostName;
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
