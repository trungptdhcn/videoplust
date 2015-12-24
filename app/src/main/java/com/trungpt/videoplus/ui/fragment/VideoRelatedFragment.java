package com.trungpt.videoplus.ui.fragment;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.support.v7.app.NotificationCompat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.Bind;
import butterknife.OnItemClick;
import com.bumptech.glide.Glide;
import com.trungpt.videoplus.MainActivity;
import com.trungpt.videoplus.R;
import com.trungpt.videoplus.asyntask.AsyncTaskMostPopular;
import com.trungpt.videoplus.asyntask.AsyncTaskSearchData;
import com.trungpt.videoplus.asyntask.listener.AsyncTaskListener;
import com.trungpt.videoplus.base.BaseFragment;
import com.trungpt.videoplus.base.StringUtils;
import com.trungpt.videoplus.download.DownloadManager;
import com.trungpt.videoplus.download.DownloadService;
import com.trungpt.videoplus.download.Downloader;
import com.trungpt.videoplus.download.UpdateProgress;
import com.trungpt.videoplus.event.DownloadEvent;
import com.trungpt.videoplus.sync.RequestDTO;
import com.trungpt.videoplus.sync.dailymotion.DailymotionRequestDTO;
import com.trungpt.videoplus.sync.vimeo.request.VimeoRequestDTO;
import com.trungpt.videoplus.sync.youtube.YoutubeConnector;
import com.trungpt.videoplus.sync.youtube.request.YoutubeRequestDTO;
import com.trungpt.videoplus.ui.activity.UserDetailActivity;
import com.trungpt.videoplus.ui.activity.VimeoVideoDetailActivity;
import com.trungpt.videoplus.ui.activity.YoutubeVideoDetailActivity;
import com.trungpt.videoplus.ui.adapter.CommonAdapter;
import com.trungpt.videoplus.ui.listener.EndlessScrollListener;
import com.trungpt.videoplus.ui.model.*;
import com.trungpt.videoplus.utils.Constant;
import de.greenrobot.event.EventBus;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by trung on 12/16/2015.
 */
public class VideoRelatedFragment extends BaseFragment implements AsyncTaskListener
{

    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.list_view_related)
    ListView listView;
    CommonAdapter adapter;
    String nextPage;
    RequestDTO requestDTO;
    VideoModel videoModel;
    Constant.HOST_NAME host_name;
    DownloadService downloadService;
    ProgressDialog barProgressDialog;

    @Override
    public int getLayout()
    {
        return R.layout.related_videos_fragment;
    }

    @Override
    public void setDataToView(Bundle savedInstanceState)
    {
        videoModel = getArguments().getParcelable("video");
//        directLinks = (List<DirectLink>) getArguments().getSerializable("direct_list");
        host_name = (Constant.HOST_NAME) getArguments().getSerializable("host_name");
        if (videoModel != null)
        {
            addHeader();
            switch (host_name)
            {
                case VIMEO:
                    requestDTO = new VimeoRequestDTO
                            .VimeoRequestBuilder("")
                            .id(videoModel.getId())
                            .build();
                    AsyncTaskMostPopular asyncTask = new AsyncTaskMostPopular(getActivity()
                            , Constant.HOST_NAME.VIMEO_RELATED, requestDTO);
                    asyncTask.setListener(VideoRelatedFragment.this);
                    asyncTask.execute();
                    break;
                case DAILYMOTION:
                    requestDTO = new DailymotionRequestDTO
                            .DailymotionRequestBuilder("")
                            .id(videoModel.getId())
                            .fields(Constant.DAILYMOTION_VIDEO_FIELDS)
                            .page(1)
                            .limit(10)
                            .build();
                    AsyncTaskMostPopular asyncTaskDailymotion = new AsyncTaskMostPopular(getActivity()
                            , Constant.HOST_NAME.DAILYMOTION_RELATED, requestDTO);
                    asyncTaskDailymotion.setListener(VideoRelatedFragment.this);
                    asyncTaskDailymotion.execute();
                    break;
            }

            EndlessScrollListener endlessScrollListener = new EndlessScrollListener()
            {
                @Override
                public boolean onLoadMore(int page, int totalItemsCount)
                {
                    if (StringUtils.isNotEmpty(nextPage))
                    {
                        switch (host_name)
                        {
                            case VIMEO:
                                ((VimeoRequestDTO) requestDTO).setPageToken(page + "");
                                AsyncTaskMostPopular asyncTask = new AsyncTaskMostPopular(getActivity(), Constant.HOST_NAME.VIMEO_RELATED, requestDTO);
                                asyncTask.setListener(VideoRelatedFragment.this);
                                asyncTask.execute();
                                break;
                            case DAILYMOTION:
                                ((DailymotionRequestDTO) requestDTO).setPage(page);
                                AsyncTaskMostPopular asyncTaskDailyMotion = new AsyncTaskMostPopular(getActivity(), Constant.HOST_NAME.DAILYMOTION, requestDTO);
                                asyncTaskDailyMotion.setListener(VideoRelatedFragment.this);
                                asyncTaskDailyMotion.execute();
                                break;
                        }
                        return true;
                    }
                    else
                    {
                        return false;
                    }

                }
            };
            listView.setOnScrollListener(endlessScrollListener);
        }


    }

    @Override
    public void prepare()
    {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void complete(Object obj)
    {
        PageModel page = (PageModel) obj;
        if (page != null)
        {
            List<Item> videos = page.getItems();
            nextPage = page.getNextPage();
            if (adapter == null)
            {
                adapter = new CommonAdapter(videos, getActivity());
                listView.setAdapter(adapter);
            }
            else
            {
                adapter.getVideos().addAll(videos);
                adapter.notifyDataSetChanged();
            }

        }
        progressBar.setVisibility(View.GONE);
    }

    @OnItemClick(R.id.list_view_related)
    public void itemClick(int position)
    {
        Intent intent = new Intent(getActivity(), VimeoVideoDetailActivity.class);
        intent.putExtra("video", (Parcelable) adapter.getItem(position - 1));
        intent.putExtra("host_name", host_name);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    public void addHeader()
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup userInfo = (ViewGroup) inflater.inflate(R.layout.layout_video_detail_information, listView, false);
        TextView tvName = (TextView) userInfo.findViewById(R.id.tvName);
        CheckBox cbExpand = (CheckBox) userInfo.findViewById(R.id.cbExpand);
        TextView tvViews = (TextView) userInfo.findViewById(R.id.tvViews);
        final TextView tvDescription = (TextView) userInfo.findViewById(R.id.tvDescription);
        RelativeLayout rlUser = (RelativeLayout) userInfo.findViewById(R.id.rlUser);
        TextView tvLikes = (TextView) userInfo.findViewById(R.id.tvLikes);
        TextView tvDisLikes = (TextView) userInfo.findViewById(R.id.tvDisLikes);
        ImageView ivDownload = (ImageView) userInfo.findViewById(R.id.ivDownload);
        TextView tvUserName = (TextView) userInfo.findViewById(R.id.layout_user_information_tvUserName);
        final TextView tvUserSubscrible = (TextView) userInfo.findViewById(R.id.layout_user_information_tvSub);
        final ImageView ivUserAvatar = (ImageView) userInfo.findViewById(R.id.layout_user_information_ivAvatar);
        final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        if (cbExpand.isChecked())
        {
            tvDescription.setVisibility(View.VISIBLE);
        }
        else
        {
            tvDescription.setVisibility(View.GONE);
        }
        cbExpand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    tvDescription.setVisibility(View.VISIBLE);
                }
                else
                {
                    tvDescription.setVisibility(View.GONE);
                }
            }
        });
        rlUser.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                intent.putExtra("video", videoModel.getUserModel());
                intent.putExtra("host_name", host_name);
                startActivity(intent);
            }
        });
        UserModel userModel = videoModel.getUserModel();
        tvUserSubscrible.setText(userModel.getSubscrible() != null ? (nf.format(userModel.getSubscrible()) + " subscribes") : "0 subscribes");
        Glide.with(VideoRelatedFragment.this)
                .load(userModel.getUrlAvatar())
                .placeholder(R.drawable.ic_default)
                .centerCrop()
                .crossFade()
                .into(ivUserAvatar);
        tvName.setText(videoModel.getName());
        tvUserName.setText(videoModel.getUserModel().getName());
        tvViews.setText(videoModel.getViews() != null ? nf.format(videoModel.getViews()) : "0");
        tvLikes.setText(videoModel.getLikes() != null ? nf.format(videoModel.getLikes()) : "0");
        tvDisLikes.setText(videoModel.getDisLikes() != null ? nf.format(videoModel.getDisLikes()) : "0");
        tvDescription.setText(videoModel.getDescription());
        tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
        listView.addHeaderView(userInfo, null, false);

        ivDownload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DirectLink directLink = videoModel.getDirectLinks().get(0);
//                Intent intent = new Intent(getActivity().getApplicationContext(), DownloadService.class);
//                intent.putExtra(DownloadService.PENDING_RESULT_EXTRA, videoModel.getDirectLinks().get(0));
//                getActivity().startService(intent);
                try
                {
                    DownloadManager.getInstance().createDownload(new URL(directLink.getUri()),
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/", directLink.getName(), ".mp4", directLink);
                }
                catch (MalformedURLException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
