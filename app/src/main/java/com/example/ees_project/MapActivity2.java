package com.example.ees_project;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.icu.text.Edits;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MapActivity2 extends AppCompatActivity implements View.OnClickListener, BeaconConsumer, SensorEventListener {

    private String UUID = "AAAAAAAA-BBBB-BBBB-CCCC-CCCCAAAAAAAA";       // 비콘 1 UUID
    private String UUID_2 = "AAAAAAAA-BBBB-BBBB-CCCC-CCCCAAAAAABB";     // 비콘 2 UUID
    private String UUID_3 = "AAAAAAAA-BBBB-BBBB-CCCC-CCCCAAAAAACC";     // 비콘 3 UUID
    private String UUID_4 = "AAAAAAAA-BBBB-BBBB-CCCC-CCCCAAAAAAFF";     // 비콘 4 UUID

    private String TAG = MainActivity.class.getSimpleName();
    private BeaconManager beaconManager;
    // 감지된 비콘들을 임시로 담을 리스트
    private List<Beacon> beaconList = new ArrayList<>();

    //비콘 저장할 index;
    private int beacon_1_index = 0;     // 현재 위치 <-> 비콘 거리 평균을 구하기 위한 배열 index
    private int beacon_2_index = 0;
    private int beacon_3_index = 0;
    private int beacon_4_index = 0;

    //비콘 좌표
    HashMap<Integer, double[]> beacon_location = new HashMap<>();

    // 비콘 좌표 객체 (x, y, distance)
    private Beacon_Location[] beacon_class = new Beacon_Location[3];

    // 핑거 프린틩을 하기 위한 비콘 좌표
    private double[] beacon_1_location = {0.0, 0.0};            // change
    private double[] beacon_2_location = {20.0, 20.0};          // change
    private double[] beacon_3_location = {40.0, 0.0};           // change
    private double[] beacon_4_location = {60.0, 20.0};          // change

    // 불 좌표
    private double[] fire_location = {0.0, 20.0};

    // 소화기 좌표
    private  double[] water_location = {60.0, 20.0};

    // 출구 좌표
    private double[] exit_location = {60.0, 20.0};

    // 사용자 현재 위치
    private Beacon_Location user_Position;

    // 사용자 <-> 비콘 각각의 거리, 평균 필터링 하기 위한 배열
    private double[] beacon_1 = new double[5];
    private double[] beacon_2 = new double[5];
    private double[] beacon_3 = new double[5];
    private double[] beacon_4 = new double[5];

    private Button lightBtn;
    private boolean mFlashOn;

    private CameraManager mCameraManager;
    private String mCameraId;

    // 나침반
    private ImageView mPointer;                         // 탈출구
    private ImageView mfire;                            // 화재
    private ImageView mfire_ex;                         // 소화기

    // 센서
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;                      // 가속도 센서
    private Sensor mMagnetometer;                       // 자기장 센서
    private float[] mLastAccelerometer = new float[3];  // 가속도를 평균 필터링 하기 위한 배열
    private float[] mLastMagnetometer = new float[3];   // 자기장을 평균 필터링 하기 위한 배열
    private boolean mLastAccelerometerSet = false;      // 가속도 센서의 동작 여부를 체크하는 변수
    private boolean mLastMagnetometerSet = false;       // 자기장 센서의 동작 여부를 체크하는 변수
    private float[] mR = new float[9];                  // 회전 매트릭스
    private float[] mOrientation = new float[3];        // 방위각
    private float mCurrentDegree = 0f;                  // '탈출구' 나침반(화살표) 각도
    private float mCurrentDegree_fi = 0f;               // '화재' 나침반(화살표) 각도
    private float mCurrentDegree_ex = 0f;               // '소화기' 나침반(화살표) 각도

    // 출구 각도 (0 ~ 359)
    private double exit_angle = 0.0;

    // 불 각도 (0 ~ 359)
    private double fire_angle = 0.0;

    // 소화기 각도 (0 ~ 359)
    private double water_angle = 0.0;

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);

        // 센서 설정 -> 센서 값 할당
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // 객체 할당
        mPointer = (ImageView)findViewById(R.id.compass);
        mfire = (ImageView)findViewById(R.id.fire);
        mfire_ex = (ImageView)findViewById(R.id.fire_ex);
        lightBtn = (Button) findViewById(R.id.map2_light_btn);

        // 리스너 설정
        lightBtn.setOnClickListener(this);

        // 카메라 플래시
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(getApplicationContext(), "There is no camera flash.\n The app will finish!", Toast.LENGTH_LONG).show();

            delayedFinish();
            return;
        }

        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        // 실제로 비콘을 탐지하기 위한 비콘매니저 객체를 초기화
        beaconManager = BeaconManager.getInstanceForApplication(this);

        // 여기가 중요한데, 기기에 따라서 setBeaconLayout 안의 내용을 바꿔줘야 하는듯 싶다.
        // 필자의 경우에는 아래처럼 하니 잘 동작했음.
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        // 비콘 탐지를 시작한다. 실제로는 서비스를 시작하는것.
        beaconManager.bind(this);

        handler.sendEmptyMessage(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        String str = "";

        switch (view.getId()) {
            case R.id.map2_light_btn:
                str = "맵2 손전등 버튼";
                flashlight();
                break;
        }

        Toast.makeText(getApplicationContext(), str+"을 클릭함", Toast.LENGTH_SHORT).show();
    }


    private void delayedFinish() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3500);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void flashlight() {
        if (mCameraId == null) {
            try {
                for (String id : mCameraManager.getCameraIdList()) {
                    CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
                    Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                    if (flashAvailable != null && flashAvailable
                            && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        mCameraId = id;
                        break;
                    }
                }
            } catch (CameraAccessException e) {
                mCameraId = null;
                e.printStackTrace();
                return;
            }
        }

        mFlashOn = !mFlashOn;

        try {
            mCameraManager.setTorchMode(mCameraId, mFlashOn);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        }
        catch (RemoteException e) {

        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            //비콘 확인용 해쉬맵
            beacon_location.put(0, beacon_1_location);
            beacon_location.put(1, beacon_2_location);
            beacon_location.put(2, beacon_3_location);
            beacon_location.put(3, beacon_4_location);

            // 비콘의 아이디와 거리를 측정하여 textView에 넣는다
            for(Beacon beacon : beaconList)
            {

                Log.i("beacon", String.valueOf(beacon.getId1()));
                Log.i("beacon", String.valueOf(beacon.getDistance()));

                // 적어놓은 UUID에 해당하는 비콘이 존재할 경우 : 현재 위치 <-> 비콘 거리를 구함
                if (beacon.getId1().toString().equals(UUID.toLowerCase()))
                {
                    if (beacon_1_index < 5)
                        beacon_1[beacon_1_index] = (double) beacon.getDistance();
                    else {
                        beacon_1_index = 0;
                        beacon_1[beacon_1_index] = (double) beacon.getDistance();
//                        Log.d("test____________test", String.valueOf(beacon.getDistance()));
                    }
                    beacon_1_index++;
                }
                if (beacon.getId1().toString().equals(UUID_2.toLowerCase()))
                {
                    if (beacon_2_index < 5)
                        beacon_2[beacon_2_index] = (double) beacon.getDistance();
                    else {
                        beacon_2_index = 0;
                        beacon_2[beacon_2_index] = (double) beacon.getDistance();
                    }
                    beacon_2_index++;
                }
                if (beacon.getId1().toString().equals(UUID_3.toLowerCase()))
                {
                    if (beacon_3_index < 5)
                        beacon_3[beacon_3_index] = (double) beacon.getDistance();
                    else {
                        beacon_3_index = 0;
                        beacon_3[beacon_3_index] = (double) beacon.getDistance();
                    }
                    beacon_3_index++;
                }
                if (beacon.getId1().toString().equals(UUID_4.toLowerCase()))
                {
                    if (beacon_4_index < 5)
                        beacon_4[beacon_4_index] = (double) beacon.getDistance();
                    else {
                        beacon_4_index = 0;
                        beacon_4[beacon_4_index] = (double) beacon.getDistance();
                    }
                    beacon_4_index++;
                }
            }
            // 현재 위치 <-> 각 비콘 거리의 평균을 구하기 위한 배열
            double avg[] = new double[4];

            for (int i = 0; i < 5; i++)
            {
                if (beacon_1[i] == 0 && beacon_2[i] == 0 && beacon_3[i] == 0 && beacon_4[i] == 0)
                    continue;
//                Log.d(":beacon1::::::", String.valueOf(beacon_1[i]));
//                Log.d("::::beacon2::::::", String.valueOf(beacon_2[i]));
//                Log.d(":::::::beacon3::::::", String.valueOf(beacon_3[i]));
                avg[0] += beacon_1[i];
                avg[1] += beacon_2[i];
                avg[2] += beacon_3[i];
                avg[3] += beacon_4[i];
                Log.i("0", String.valueOf(avg[0]));
                Log.i("1", String.valueOf(avg[1]));
                Log.i("2", String.valueOf(avg[2]));
                Log.i("3", String.valueOf(avg[3]));
            }

            // 평균 구하기
            for (int i = 0; i < 4; i++)
                avg[i] /= 5;

            // 비콘이 4개 있을 경우 가장 가까운 3개만 인식하도록 함.
            double max = 0;
            int in = 0;
            int zero = 99;
            for (int i = 0; i < 4; i++)
            {
                if (avg[i] == 0)
                    zero = i;
                if (avg[i] > max) {
                    max = avg[i];
                    in = i;
                    if (zero != 99)
                        in = zero;
                }
            }
            int j = 0;
            for (int i = 0; i < 4; i++)
            {
                if (in != i) {
                    beacon_class[j++] = new Beacon_Location(avg[i] * 10 * 0.7, beacon_location.get(i));     // 비콘 거리 보정하기 (실제 지도와 비콘 축적 맞추기)
                }
            }
            // 여기까지

            // 비콘 위치를 통해 사용자 현재 좌표 구하기
            user_Position = new calculator().get_x_y(beacon_class);

//            user_Position = new calculator().put_x_y();
            Log.i("finish", "ha2");
            //비콘 사이 거리
//            double map_y = Math.sqrt(Math.pow(beacon_3_location[0] > beacon_1_location[0]?beacon_3_location[0] - beacon_1_location[0]:beacon_1_location[0] - beacon_3_location[0], 2) +
//                    Math.pow(beacon_3_location[1] > beacon_1_location[1]?beacon_3_location[1] - beacon_1_location[1]:beacon_1_location[1] - beacon_3_location[1], 2));
//            double map_x = Math.sqrt(Math.pow(beacon_1_location[0] > beacon_2_location[0]?beacon_1_location[0] - beacon_2_location[0]:beacon_2_location[0] - beacon_1_location[0], 2) +
//                    Math.pow(beacon_1_location[1] > beacon_2_location[1]?beacon_1_location[1] - beacon_2_location[1]:beacon_2_location[1] - beacon_1_location[1], 2));
//
//            user_Position.getX() = (map_x + avg[0] + avg[2])/map_x;
//            my_location[1] = 3 - (map_y + avg[1] + avg[0])/map_y;

            //atan2(y, x)
            double up_x = user_Position.getX();         // 사용자 x 좌표 할당
            double up_y = user_Position.getY();         // 사용자 y 좌표 할당
            Log.i("user_position_x", String.valueOf(up_x));
            Log.i("user_position_y", String.valueOf(up_y));
//            double test_x = user_Position.getY() - exit_location[1];    // 사용자 y 좌표 - 출구 y 좌표
////            double test_y = user_Position.getX() - exit_location[0];    // 사용자 x 좌표 - 출구 x 좌표
////            Log.i("exit_location", String.valueOf(test_y));
////            Log.i("exit_location", String.valueOf(test_x));

            // 각도 구하기 : https://unity-programmer.tistory.com/30
            // 라디안 : https://ko.wikipedia.org/wiki/%EB%9D%BC%EB%94%94%EC%95%88
            // 사용자 x, y값 - 출구(or화재or소화기) x, y값 -> 라디안(각도) -> degree(각도)
            exit_angle = Math.toDegrees(Math.atan2(user_Position.getY() - exit_location[1], user_Position.getX() - exit_location[0]));
            fire_angle = Math.toDegrees(Math.atan2(user_Position.getY() - fire_location[1], user_Position.getX() - fire_location[0]));
            water_angle = Math.toDegrees(Math.atan2(user_Position.getY() - water_location[1], user_Position.getX() - water_location[0]));

            Log.i("exit_angle", String.valueOf(exit_angle));
            Log.i("fire_angle", String.valueOf(fire_angle));
            Log.i("water_angle", String.valueOf(water_angle));
            handler.sendEmptyMessageDelayed(0, 100);
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if(event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if(mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);

            // 탈출구 나침반 애니메이션 적용
            float azimuthinDegress  = (float) ((int) ( Math.toDegrees( SensorManager.getOrientation( mR, mOrientation)[0] ) + 360 + exit_angle+70) % 360);          // change  70
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthinDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );

            // 화재 나침반 애니메이션 적용
            float azimuthinDegress_fi  = (float) ((int) ( Math.toDegrees( SensorManager.getOrientation( mR, mOrientation)[0] ) + 360 + fire_angle+70) % 360);       // change  70
            RotateAnimation ra_fi = new RotateAnimation(
                    mCurrentDegree_fi,
                    -azimuthinDegress_fi,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );

            // 소화기 나침반 애니메이션 적용
            float azimuthinDegress_ex  = (float) ((int) ( Math.toDegrees( SensorManager.getOrientation( mR, mOrientation)[0] ) + 360 + water_angle+70) % 360);      // change 70
            RotateAnimation ra_ex = new RotateAnimation(
                    mCurrentDegree_ex,
                    -azimuthinDegress_ex,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            ra.setDuration(250);                        //적용
            ra.setFillAfter(true);
            ra_fi.setDuration(250);
            ra_fi.setFillAfter(true);
            ra_ex.setDuration(250);
            ra_ex.setFillAfter(true);
            mPointer.startAnimation(ra);
            mfire.startAnimation(ra_fi);
            mfire_ex.startAnimation(ra_ex);
            mCurrentDegree = -azimuthinDegress;
            mCurrentDegree_fi = -azimuthinDegress_fi;
            mCurrentDegree_ex = -azimuthinDegress_ex;
        }
    }
    public class Beacon_Location {
        private double x;           // 비콘의 x좌표 값
        private double y;           // 비콘의 y좌표 값
        private double distance;    // 비콘과 현재 위치의 거리

        public Beacon_Location() {}
        public Beacon_Location(Double distance, double[] local) {      // 생성자
            this.x = local[0];
            this.y = local[1];
            this.distance = distance;
        }

        public Beacon_Location(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }
        public double getY() {
            return y;
        }
        public double getDistance() {
            return distance;
        }
    }

    public class calculator {

        public Beacon_Location get_x_y(Beacon_Location[] local)
        {
            // 세개의 기준점 x, y 좌표
            double x1 = local[0].getX();
            double y1 = local[0].getY();
            double x2 = local[1].getX();
            double y2 = local[1].getY();
            double x3 = local[2].getX();
            double y3 = local[2].getY();

            // 세 개 기준점으로 직선거리
            double r1 = local[0].getDistance();
            double r2 = local[1].getDistance();
            double r3 = local[2].getDistance();
            Log.i("r1", String.valueOf(r1));
            Log.i("r2", String.valueOf(r2));
            Log.i("r3", String.valueOf(r3));

            double S = (Math.pow(x3, 2) - Math.pow(x2, 2) + Math.pow(y3, 2) - Math.pow(y2, 2)
                    + Math.pow(r2, 2) - Math.pow(r3, 2)) / 2.0;
            double T = (Math.pow(x1, 2) - Math.pow(x2, 2) + Math.pow(y1, 2) - Math.pow(y2, 2)
                    + Math.pow(r2, 2) - Math.pow(r1, 2)) / 2.0;

            double y = ((T * (x2 - x3)) - (S * (x2 - x1))) / (((y1 - y2) * (x2 - x3))
                    - ((y3 - y2) * (x2 - x1)));

            double x = ((y * (y1 - y2)) - T) / (x2 - x1);

            Beacon_Location userLocation = new Beacon_Location(x, y);

            return userLocation;
        }

        public Beacon_Location put_x_y()
        {
            Beacon_Location userLocation = new Beacon_Location(40, 15);
            return userLocation;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}