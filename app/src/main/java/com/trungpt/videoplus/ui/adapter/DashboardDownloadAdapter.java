package com.trungpt.videoplus.ui.adapter;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.trungpt.videoplus.MainActivity;
import com.trungpt.videoplus.R;
import com.trungpt.videoplus.download.DownloadManager;
import com.trungpt.videoplus.download.Downloader;
import com.trungpt.videoplus.download.UpdateProgress;
import com.trungpt.videoplus.ui.model.DirectLink;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by trung on 12/22/2015.
 */
public class DashboardDownloadAdapter extends BaseAdapter
{
    private List<Downloader> downloaders = new ArrayList<>();
    private Context context;


    public DashboardDownloadAdapter(Context context)
    {
        if (DownloadManager.getInstance().getDownloadList() != null)
        {
            this.downloaders = DownloadManager.getInstance().getDownloadList();
        }
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return downloaders.size();
    }

    @Override
    public Object getItem(int position)
    {
        return downloaders.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        final ViewHolder holder;
        Downloader downloader = downloaders.get(position);
        final DirectLink directLink = downloaders.get(position).getDirectLink();
        if (convertView != null)
        {
            holder = (ViewHolder) convertView.getTag();
        }
        else
        {
            convertView = inflater.inflate(R.layout.progress_bar_download, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        holder.tvName.setText(directLink.getName());
        holder.tvFolder.setText(downloader.folder);
        Glide.with(context)
                .load(directLink.getUrlThumb())
                .centerCrop()
                .placeholder(R.drawable.ic_default)
                .crossFade()
                .into(holder.ivThumb);
        downloader.setUpdateProgress(new UpdateProgress()
        {
            @Override
            public void update(final int percentage)
            {
                ((Activity) context).runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        holder.progressBar.setProgress(percentage);
                    }
                });

            }
        });
        return convertView;
    }

    static class ViewHolder
    {
        @Bind(R.id.progress_tvName)
        TextView tvName;
        @Bind(R.id.progress_bar_tvFolder)
        TextView tvFolder;
        @Bind(R.id.progress_ivAvatar)
        ImageView ivThumb;
        @Bind(R.id.progress_bar_progressBar)
        NumberProgressBar progressBar;

        public ViewHolder(View view)
        {
            ButterKnife.bind(this, view);
        }
    }
}
