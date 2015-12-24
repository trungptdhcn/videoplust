package com.trungpt.videoplus.download;

import android.app.*;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.trungpt.videoplus.MainActivity;
import com.trungpt.videoplus.R;
import com.trungpt.videoplus.ui.model.DirectLink;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by trung on 12/22/2015.
 */
public class DownloadService extends Service
{
    private static final String TAG = DownloadService.class.getSimpleName();
    public static final String PENDING_RESULT_EXTRA = "direct_link";
    private NotificationManager notify;
    public static boolean serviceState = false;
    public DirectLink directLink;

    @Override
    public void onCreate()
    {
        serviceState = true;
        notify = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        DirectLink directLink = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
        this.directLink = directLink;
        try
        {
            DownloadManager.getInstance().createDownload(new URL(directLink.getUri()),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/", directLink.getName(), ".mp4", directLink);
//            if (DownloadManager.getInstance().getDownloadList().size() > 0)
//            {
//                this.showNotification("Downloading ...!", "Video Plus");
//            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {

        Log.d("SERVICE-DESTROY", "DESTORY");
        serviceState = false;
    }

    void showNotification(String message, String title)
    {
        // In this sample, we'll use the same text for the ticker and the expanded notification
//        CharSequence text = message;
//
//        // Set the icon, scrolling text and timestamp
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.ic_default)
//                        .setContentTitle("Video Plus")
//                        .setContentText("Downloading ...!");
        CharSequence text = message;

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_default, "vvs",
                System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this.getBaseContext(), 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, title,
                text, contentIntent);
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notify.notify(R.string.app_name, notification);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
