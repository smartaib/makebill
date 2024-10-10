package com.kwic.makebill;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.kwic.makebill.utils.BiilToPDF;
import com.kwic.makebill.utils.CustomLog;
import com.kwic.makebill.utils.NotiUtils;
import com.kwic.makebill.utils.PDFUtil;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("unchecked")
public class RemoteMakeBillService extends Service {
    public static final String TAG = "RemoteMakeBillService";

    public static final int MSG_CLIENT_CONNECT = 1;
    public static final int MSG_CLIENT_DISCONNECT = 2;
    public static final int MSG_MAKE_BILL = 3;
    public static final int MSG_RESULT_BILL = 4;
    public static final int MSG_MAKE_COMPLTE_BILL = 6;
    private static final String ACTION_STOP = "com.kwic.makebill.service.action.SCRAPPING_STOP";

    private Messenger mClientCallback = null;
    final Messenger mMessenger = new Messenger(new CallbackHandler());

    private NotificationManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        CustomLog.d("onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        CustomLog.d("onCreate");
        super.onCreate();
        manager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        CustomLog.d("onDestroy");
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        CustomLog.d("onStartCommand");

        if (intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_STOP)) {
            Log.e("YS", "stopService!!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                stopForeground(true);
            }
            stopSelf();
            stopService(intent);
            System.gc();
            return Service.START_NOT_STICKY;
        }

        startNotification();

        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void startNotification() {

        String CHANNEL_ID = getResources().getString(R.string.app_notification_channal);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "바로청구 알림",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("바로청구 NOTIFICATION");
            channel.setShowBadge(false);
            channel.setVibrationPattern(new long[]{100, 200, 100, 200});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableLights(true);
            channel.enableVibration(true);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

        Intent notificationIntent = new Intent(getApplicationContext(), RemoteMakeBillService.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        builder.setContentTitle("바로청구")
                .setContentText("청구서를 만들기 위한 준비중입니다.")
                .setSmallIcon(R.drawable.baro_app_icon_144_white)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            startForeground(1, builder.build());
            CustomLog.d("service startForeground");
        }
    }

    private void sendResultBill(boolean isResult) {
        try{
            CustomLog.d("Send MSG_RESULT_BILL message to client");
            Message added_msg = Message.obtain(null, MSG_RESULT_BILL);
            added_msg.arg1 = isResult ? 1 : 0;;
            mClientCallback.send(added_msg);
        }
        catch( RemoteException e){
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    private class CallbackHandler  extends Handler {

        @Override
        public void handleMessage( Message msg ){
            switch( msg.what ){
                case MSG_CLIENT_CONNECT:
                    CustomLog.d("Received MSG_CLIENT_CONNECT message from client");
                    mClientCallback = msg.replyTo;

                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        path = getExternalFilesDir(null).getAbsolutePath();
                    }
                    CustomLog.d("service path : " + path);

                    break;
                case MSG_CLIENT_DISCONNECT:
                    CustomLog.d("Received MSG_CLIENT_DISCONNECT message from client");
                    mClientCallback = null;
                    try {
                        Intent intent = new Intent(getBaseContext(), RemoteMakeBillService.class);
                        intent.setAction(ACTION_STOP);
                        getBaseContext().startService(intent);
                    }
                    catch( Exception e){
                        e.printStackTrace();
                    }

                    break;
                case MSG_MAKE_COMPLTE_BILL:
                    CustomLog.d("Received message from client: MSG_MAKE_COMPLTE_BILL");
                    if (mClientCallback == null)
                        return;

                    try {
                        boolean isRet = true;

                        Bundle args = msg.getData();
                        String pdf_path = args.getString("base_pdf_path");
                        ArrayList<String> base_data = (ArrayList<String>)args.getSerializable("base_data");
                        String pdf_path4fax = args.getString("base_pdf_path_fax");
                        ArrayList<String> base_data4fax = (ArrayList<String>)args.getSerializable("base_data_fax");

                        BiilToPDF biilToPDF = new BiilToPDF(getBaseContext());
                        if (!biilToPDF.makeCompletePDF(pdf_path, base_data)) {
                            isRet = false;
                        }
                        biilToPDF.makeCompletePDF(pdf_path4fax, base_data4fax);
                        sendResultBill(isRet);

                    } catch (Exception e) {
                        e.printStackTrace();
                        sendResultBill(false);
                    }
                    break;
                case MSG_MAKE_BILL:
                    CustomLog.d("Received message from client: MSG_MAKE_BILL");

                    if (mClientCallback == null)
                        return;

                    try {
                        boolean isRet = true;

                        Bundle args = msg.getData();
                        String base_pdf_path = args.getString("base_pdf_path");
                        String claim_pdf_path = args.getString("claim_pdf_path");
                        String compression_pdf_path = args.getString("compression_pdf_path");
                        Map<String, String> claim_data = (Map<String, String>)args.getSerializable("claim_data");
                        JSONArray rule_data = new JSONArray(args.getString("rule_data"));

                        String complete_pdf_path = args.getString("complete_pdf_path");
                        ArrayList<String> attach_data = (ArrayList<String>)args.getSerializable("attach_data");

                        BiilToPDF biilToPDF = new BiilToPDF(claim_data, getBaseContext());
                        if (biilToPDF.makeBillPDF(base_pdf_path, claim_pdf_path, rule_data)) {
                            PDFUtil pdfUtil = new PDFUtil(getBaseContext());
                            pdfUtil.attachImage(claim_pdf_path, complete_pdf_path, attach_data);

                            if (!biilToPDF.compressionBill(claim_pdf_path, compression_pdf_path)) {
                                isRet = false;
                            }
                        }
                        else {
                            isRet = false;
                        }
                        sendResultBill(isRet);

                    } catch (Exception e) {
                        e.printStackTrace();
                        sendResultBill(false);
                    }
                    break;
            }
        }
    }
}