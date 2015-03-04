package com.example.tritonia.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.nio.channels.Channel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tritonia on 2015-03-04.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;
    WifiP2pManager.PeerListListener myPeerListListener;
    private List peersList = new ArrayList();

    public WiFiDirectBroadcastReceiver() {
        super();
    }

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Log.d("WiFiP2P", "Wifi P2P is enabled");
                mActivity.txt.setText("Wifi P2P is enabled");
            } else {
                // Wi-Fi P2P is disabled
                Log.d("WiFiP2P", "Wifi P2P is disabled");
                mActivity.txt.setText("Wifi P2P is disabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers

            //Move to a button click to prevent infinite loop everytime new peer joins
            if (mManager != null) {
                mManager.requestPeers((WifiP2pManager.Channel) mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Log.d("WiFiP2P",String.format("PeerListListener: %d peers available, updating device list", peers.getDeviceList().size()));


                        // Out with the old, in with the new.
                        peersList.clear();
                        peersList.addAll(peers.getDeviceList());

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                        String ts = sdf.format(new java.util.Date());

                        if (peersList.size() == 0) {
                            Log.d("WiFiP2P", "No devices found - " + ts);
                            mActivity.txt.append("\n No devices found - " + ts);
                            return;
                        } else {
                            mActivity.txt.append("\n" + peersList.size() + " peer(s) available - " + ts);
                            Log.d("WiFiP2P" , "============= List of Devices ==================== - " + ts);
                            for (int i = 0 ; i < peersList.size() ; i++)
                            {
                                Log.d("WiFiP2P" , " " + peersList.get(i).toString());
                                Log.d("WiFiP2P" , "=================================");
                            }

                        }

                        WifiP2pDevice device;
                        device = (WifiP2pDevice) peersList.get(0);//choose who to connect to
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;
                        mManager.connect((WifiP2pManager.Channel) mChannel, config, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("WiFiP2P", "Connection Succeeded");
                                mActivity.txt.append("Connection Succeeded ");
                            }

                            @Override
                            public void onFailure(int reason) {
                                mActivity.txt.append("Connection Failed");
                                Log.d("WiFiP2P" , "Connection Failed");
                            }
                        });
                    }

                });
            }


        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

}

