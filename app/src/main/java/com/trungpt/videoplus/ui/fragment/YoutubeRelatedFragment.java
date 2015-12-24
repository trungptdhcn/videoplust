package com.trungpt.videoplus.ui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.Bind;
import butterknife.OnItemClick;
import com.bumptech.glide.Glide;
import com.trungpt.videoplus.R;
import com.trungpt.videoplus.asyntask.AsyncTaskSearchData;
import com.trungpt.videoplus.asyntask.listener.AsyncTaskListener;
import com.trungpt.videoplus.base.BaseFragment;
import com.trungpt.videoplus.base.StringUtils;
import com.trungpt.videoplus.sync.youtube.YoutubeConnector;
import com.trungpt.videoplus.sync.youtube.request.YoutubeRequestDTO;
import com.trungpt.videoplus.ui.activity.UserDetailActivity;
import com.trungpt.videoplus.ui.activity.YoutubeVideoDetailActivity;
import com.trungpt.videoplus.ui.adapter.CommonAdapter;
import com.trungpt.videoplus.ui.listener.EndlessScrollListener;
import com.trungpt.videoplus.ui.model.Item;
import com.trungpt.videoplus.ui.model.PageModel;
import com.trungpt.videoplus.ui.model.UserModel;
import com.trungpt.videoplus.ui.model.VideoModel;
import com.trungpt.videoplus.utils.Constant;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by trung on 12/14/2015.
 */
public class YoutubeRelatedFragment extends BaseFragment implements AsyncTaskListener
{
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.list_view_related)
    ListView listView;
    CommonAdapter adapter;
    String nextPage;
    YoutubeRequestDTO requestDTO;
    VideoModel videoModel;

    @Override
    public int getLayout()
    {
        return R.layout.related_videos_fragment;
    }

    @Override
    public void setDataToView(Bundle savedInstanceState)
    {
        videoModel = getArguments().getParcelable("video");
        if (videoModel != null)
        {
            addHeader();
            requestDTO = new YoutubeRequestDTO
                    .YoutubeRequestBuilder("")
                    .id(videoModel.getId())
                    .build();
            AsyncTaskSearchData asyncTask = new AsyncTaskSearchData(getActivity(), Constant.HOST_NAME.YOUTUBE_RELATED, requestDTO);
            asyncTask.setListener(YoutubeRelatedFragment.this);
            asyncTask.execute();
            EndlessScrollListener endlessScrollListener = new EndlessScrollListener()
            {
                @Override
                public boolean onLoadMore(int page, int totalItemsCount)
                {
                    if (StringUtils.isNotEmpty(nextPage))
                    {
                        requestDTO.setPageToken(nextPage);
                        AsyncTaskSearchData asyncTask = new AsyncTaskSearchData(getActivity(), Constant.HOST_NAME.YOUTUBE_RELATED, requestDTO);
                        asyncTask.setListener(YoutubeRelatedFragment.this);
                        asyncTask.execute();
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
        Intent intent = new Intent(getActivity(), YoutubeVideoDetailActivity.class);
        intent.putExtra("video", (Parcelable) adapter.getItem(position - 1));
        startActivity(intent);
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
        final TextView tvUserName = (TextView) userInfo.findViewById(R.id.layout_user_information_tvUserName);
        final TextView tvUserSubscrible = (TextView) userInfo.findViewById(R.id.layout_user_information_tvSub);
        final ImageView ivUserAvatar = (ImageView) userInfo.findViewById(R.id.layout_user_information_ivAvatar);

        final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        new AsyncTask<Void, Void, UserModel>()
        {
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected UserModel doInBackground(Void... params)
            {
                YoutubeConnector youtubeConnector = new YoutubeConnector(YoutubeRelatedFragment.this.getActivity());
                return youtubeConnector.getAuthorInfo(videoModel.getUserModel().getId());
            }

            @Override
            protected void onPostExecute(UserModel userModel)
            {
                super.onPostExecute(userModel);
                tvUserSubscrible.setText(userModel.getSubscrible() != null ? (nf.format(userModel.getSubscrible()) + " subscribes") : "0 subscribes");
                Glide.with(YoutubeRelatedFragment.this)
                        .load(userModel.getUrlAvatar())
                        .placeholder(R.drawable.ic_default)
                        .centerCrop()
                        .crossFade()
                        .into(ivUserAvatar);
                videoModel.setUserModel(userModel);
                tvUserName.setText(userModel.getName());
                progressBar.setVisibility(View.GONE);
            }
        }.execute();

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
                intent.putExtra("host_name", Constant.HOST_NAME.YOUTUBE);
                startActivity(intent);
            }
        });
        tvName.setText(videoModel.getName());
        tvViews.setText(videoModel.getViews() != null ? nf.format(videoModel.getViews()) : "0");
        tvLikes.setText(videoModel.getLikes() != null ? nf.format(videoModel.getLikes()) : "0");
        tvDisLikes.setText(videoModel.getDisLikes() != null ? nf.format(videoModel.getDisLikes()) : "0");
        tvDescription.setText(videoModel.getDescription());
        tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
        listView.addHeaderView(userInfo, null, false);

    }
}
