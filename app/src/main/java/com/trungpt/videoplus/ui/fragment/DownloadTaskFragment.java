package com.trungpt.videoplus.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import butterknife.Bind;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.trungpt.videoplus.MainActivity;
import com.trungpt.videoplus.R;
import com.trungpt.videoplus.base.BaseFragment;
import com.trungpt.videoplus.download.DownloadManager;
import com.trungpt.videoplus.download.Downloader;
import com.trungpt.videoplus.download.UpdateProgress;
import com.trungpt.videoplus.event.DownloadEvent;
import com.trungpt.videoplus.ui.adapter.DashboardDownloadAdapter;
import com.trungpt.videoplus.ui.model.VideoModel;
import de.greenrobot.event.EventBus;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by trung on 12/18/2015.
 */
public class DownloadTaskFragment extends BaseFragment
{
    @Bind(R.id.list_dashboard)
    ListView listDashboard;

    @Override
    public int getLayout()
    {
        return R.layout.download_fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        EventBus.getDefault().register(this);
    }

    @Override
    public void setDataToView(Bundle savedInstanceState)
    {
        DashboardDownloadAdapter adapter = new DashboardDownloadAdapter(getActivity());
        listDashboard.setAdapter(adapter);
    }

//    public void onEventMainThread(DownloadEvent event)
//    {
//        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View viewProgress = vi.inflate(R.layout.progress_bar_download, null, false);
//        final TextView tvPercentage = (TextView) viewProgress.findViewById(R.id.progress_bar_tvPercentage);
//        final ImageButton btCancel = (ImageButton) viewProgress.findViewById(R.id.progress_bar_btCancel);
//        final TextView tvName = (TextView) viewProgress.findViewById(R.id.progress_bar_tvName);
//        final TextView tvTitle = (TextView) viewProgress.findViewById(R.id.progress_tvTitle);
//        final ImageView ivAvatar = (ImageView) viewProgress.findViewById(R.id.progress_ivAvatar);
//        final ProgressBar progressBar = (ProgressBar) viewProgress.findViewById(R.id.progress_bar_progressBar);
//        final VideoModel videoModel = event.getVideoModel();
//        tvTitle.setText(videoModel.getName());
//        llContainer.addView(viewProgress);
//        Glide.with(this)
//                .load(videoModel.getUrlThumbnail())
//                .asBitmap()
//                .animate(R.anim.abc_fade_in)
//                .placeholder(R.drawable.ic_default)
//                .fallback(R.drawable.ic_default)
//                .centerCrop()
//                .into(new SimpleTarget<Bitmap>(150, 150)
//                {
//                    @Override
//                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim)
//                    {
//                        ivAvatar.setImageBitmap(bitmap);
//                    }
//                });
//        try
//        {
//            Downloader download = DownloadManager.getInstance().createDownload(new URL(videoModel.getDirectLinks().get(0).getUri()),
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/", videoModel.getName(), ".mp4");
////            barProgressDialog = new ProgressDialog(VideoRelatedFragment.this.getActivity());
////            barProgressDialog.setTitle("Downloading Image ...");
////            barProgressDialog.setMessage("Download in progress ...");
////            barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
////            barProgressDialog.setProgress(0);
////            barProgressDialog.setMax(100);
////            barProgressDialog.show();
//            download.setUpdateProgress(new UpdateProgress()
//            {
//                @Override
//                public void update(final int percentage)
//                {
//                    getActivity().runOnUiThread(new Runnable()
//                    {
//                        @Override
//                        public void run()
//                        {
//                            progressBar.setProgress((int) percentage);
//                            if (percentage != 100)
//                            {
//                                tvPercentage.setText(percentage + " %");
//                            }
//                            else
//                            {
//                                tvPercentage.setText("Download complete!");
//                            }
//                            tvName.setText(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + videoModel.getName());
//                        }
//                    });
////                    progressBar.setProgress(percentage);
//                }
//            });
//
//        }
//        catch (MalformedURLException e)
//        {
//            e.printStackTrace();
//        }
//    }

}
