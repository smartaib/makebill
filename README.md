

# Makebill

보험청구용 PDF 파일 과 청구서 data, 그외 user sign 이미지를 전달받아 보험청구용 PDF파일을 만드는 Android Library입니다.
Remote Service형태로 제작되었고 Messenger를 통해 IPC 통신을 지원합니다.
이 Library는 iText 5 version을 베이스로 제작되었습니다.

## License & Copyright

The project is licensed under the GNU Affero General Public License V3. This is a copyleft license. See [LICENSE.md]

I have selected this license over any other, in order to ensure that any adaptations or improvements to the code base, require to be published under the same license. This will protect any hard work from being adapted into closed sourced projects, without giving back.

# Installation

1. Library source를 Android App 프로젝트에 sub project로 추가합니다.
2. Android App 프로젝트에 아래 내용을 추가합니다.

add setting.gradle
``` gradle
include ':makebill'
```

add build.gradle ( app )
``` gradle
    implementation project(path: ':makebill')

```

add AndroidMenifest.xml ( app )
``` xml
        <service
            android:name="com.kwic.makebill.RemoteMakeBillService"
            android:process=":remote"/>

```

# USE

1. 호출할 Activity에서 ServiceConnection 및 Handler를 생성한다.
``` java
private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            CustomLog.d( "onServiceConnected : " + name);
            mServiceCallback = new Messenger(service);

            // connect to service
            Message connect_msg = Message.obtain( null, RemoteMakeBillService.MSG_CLIENT_CONNECT);
            connect_msg.replyTo = mClientCallback;
            try {
                mServiceCallback.send(connect_msg);
                CustomLog.d("Send MSG_CLIENT_CONNECT message to Service");
                billActivity.setLoading(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            CustomLog.d("onServiceDisconnected: "+name);
            mServiceCallback = null;
        }
    };

    private class CallbackHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RemoteMakeBillService.MSG_RESULT_BILL:

                    int iResult = msg.arg1;
                    CustomLog.d("Recevied MSG_RESULT_BILL message from service ~ value :" + iResult);

                    if (iResult == 1) {
                        sendFile();
                    }
                    else {
                        showErrorToast();
                    }
                    break;
            }
        }
    }
```
2. OnCreate 함수에서 서비스를 생성
``` java
import com.kwic.makebill.RemoteMakeBillService;


        Intent serviceIntent = new Intent(getApplicationContext(), RemoteMakeBillService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
```

3. 청구서 만들기 message 보내기
``` java
        try {
            Message connect_msg = Message.obtain( null, RemoteMakeBillService.MSG_MAKE_BILL);

            Bundle bundle = new Bundle();
            bundle.putSerializable("claim_data", 보험청구 데이타(Map<String, String>));
            bundle.putString("rule_data", pdf용 Rule data(JSON Format));
            bundle.putString("base_pdf_path", 보험청구용 빈 pdf 문서 path);
            bundle.putString("claim_pdf_path", 제작된 보험청구 pdf path);
            bundle.putString("compression_pdf_path", 제작된 보험청구 pdf path(size 압축용));

            connect_msg.setData(bundle);
            
            mServiceCallback.send(connect_msg);
            CustomLog.d("Send MSG_MAKE_BILL message to Service");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
```

