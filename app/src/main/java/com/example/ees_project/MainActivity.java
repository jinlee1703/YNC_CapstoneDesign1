package com.example.ees_project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.SystemRequirementsChecker;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    private String TAG = MainActivity.class.getSimpleName();
    private BeaconManager beaconManager;
    // 감지된 비콘들을 임시로 담을 리스트
    private List<Beacon> beaconList = new ArrayList<>();
    private String UUID = "AAAAAAAA-BBBB-BBBB-CCCC-CCCCAAAAAAAA";
    private String UUID_2 = "AAAAAAAA-BBBB-BBBB-CCCC-CCCCAAAAAABB";
    private String UUID_3 = "AAAAAAAA-BBBB-BBBB-CCCC-CCCCAAAAAACC";
    private String UUID_4 = "AAAAAAAA-BBBB-BBBB-CCCC-CCCCAAAAAADD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        // 실제로 비콘을 탐지하기 위한 비콘매니저 객체를 초기화
        beaconManager = BeaconManager.getInstanceForApplication(this);

        // 여기가 중요한데, 기기에 따라서 setBeaconLayout 안의 내용을 바꿔줘야 하는듯 싶다.
        // 필자의 경우에는 아래처럼 하니 잘 동작했음.
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        // 비콘 탐지를 시작한다. 실제로는 서비스를 시작하는것.
        beaconManager.bind(this);

        handler.sendEmptyMessage(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) { //이 엔터를 타야만 비콘검색가능
                // textView.setText("진입 \n");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d(TAG,"Exit : " + region.getId1());
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            if (beacons.size() > 0) {
                beaconList.clear();
                for (Beacon beacon : beacons) {
                    beaconList.add(beacon);
                }
            }
            }
        });
        try {
            //beaconManager.startMonitoringBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        }
        catch (RemoteException e) {

        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Intent intent = new Intent(getApplicationContext(), MapActivity2.class);
            startActivity(intent);
            MainActivity.this.finish();
            // 비콘의 아이디와 거리를 측정하여 textView에 넣는다
//            for(Beacon beacon : beaconList)
//            {
//                // textView.append("했음 \n");+
////                textView.setText("ID : " + beacon.getId2() + " / " + "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n");
////                Toast.makeText(getApplicationContext(), "ID : " + beacon.getId2() + " / " + "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())), Toast.LENGTH_SHORT).show();
////                Toast.makeText(getApplicationContext(), "UUID : " + beacon.getId1(), Toast.LENGTH_LONG).show();
//            }
//            if (beaconList.size() > 0) {
//                activityChange();
//            } else {
//                // 자기 자신을 1초마다 호출
//                handler.sendEmptyMessageDelayed(0, 1000);
//            }
        }
    };

    public void activityChange() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for(Beacon beacon : beaconList)
                {

                    Log.i("beacon", String.valueOf(beacon.getId1()));
                    Log.i("beacon", String.valueOf(beacon.getDistance()));
                    if (beacon.getId1().toString().equals(UUID.toLowerCase()) ||
                        beacon.getId1().toString().equals(UUID_2.toLowerCase()) ||
                        beacon.getId1().toString().equals(UUID_3.toLowerCase()) ||
                            beacon.getId1().toString().equals(UUID_4.toLowerCase()))
                    {
                        Intent intent = new Intent(getApplicationContext(), MapActivity2.class);
                        startActivity(intent);
                        MainActivity.this.finish();
                    }
                }

                handler.removeMessages(0);
            }
        }, 2000);
    }
}
