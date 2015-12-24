package com.trungpt.videoplus.sync.dailymotion;

import com.trungpt.videoplus.base.StringUtils;
import com.trungpt.videoplus.sync.RequestDTO;
import com.trungpt.videoplus.sync.RestfulService;
import com.trungpt.videoplus.ui.model.Item;
import com.trungpt.videoplus.ui.model.PageModel;
import com.trungpt.videoplus.ui.model.UserModel;
import com.trungpt.videoplus.ui.model.VideoModel;
import com.trungpt.videoplus.utils.Constant;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by trung on 12/17/2015.
 */
public class DailymotionConnector
{
    public PageModel search(RequestDTO requestDTO)
    {
        DailymotionRequestDTO dailymotionRequestDTO = (DailymotionRequestDTO) requestDTO;
        if (dailymotionRequestDTO.getType().equals(Constant.DAILYMOTION_VIDEOS))
        {
            return seachVideo(dailymotionRequestDTO);

        }
        else if (dailymotionRequestDTO.getType().equals(Constant.DAILYMOTION_USERS))
        {
            return searchUser(dailymotionRequestDTO);
        }
        else
        {
            return null;
        }
    }

    public PageModel searchUser(DailymotionRequestDTO dailymotionRequestDTO)
    {
        String keyword = null;
        if (StringUtils.isNotEmpty(dailymotionRequestDTO.getKeyWord()))
        {
            keyword = dailymotionRequestDTO.getKeyWord();
        }
        DailymotionUserDTO dailymotionUserDTO = RestfulService.getInstance(Constant.HOST_NAME.DAILYMOTION).searchUser(
                keyword
                , Constant.DAILYMOTION_USER_FIELDS
                , "popular"
                , dailymotionRequestDTO.getPage()
                , dailymotionRequestDTO.getLimit());
        return converToUser(dailymotionUserDTO);
    }

    private PageModel converToUser(DailymotionUserDTO dailymotionUserDTO)
    {
        List<Item> items = new ArrayList<>();
        if (dailymotionUserDTO != null)
        {
            List<DailymotionUserDetailDTO> dailymotionUserDetailDTOs = dailymotionUserDTO.getDailymotionUserDetailDTOs();
            for (DailymotionUserDetailDTO dailymotionUserDetailDTO : dailymotionUserDetailDTOs)
            {
                UserModel userModel = new UserModel();
                userModel.setId(dailymotionUserDetailDTO.getId());
                userModel.setName(dailymotionUserDetailDTO.getScreenname());
                userModel.setUrlAvatar(dailymotionUserDetailDTO.getAvatar_720_url());
                userModel.setUrlCover(dailymotionUserDetailDTO.getCover_250_url());
                userModel.setSubscrible(dailymotionUserDetailDTO.getFans_total());
                userModel.setVideoCounts(dailymotionUserDetailDTO.getVideos_total());
                items.add(userModel);
            }
        }
        PageModel videoPage = new PageModel();
        videoPage.setItems(items);
        videoPage.setNextPage(dailymotionUserDTO.getPage());
        return videoPage;
    }

    //
    public PageModel seachVideo(DailymotionRequestDTO dailymotionRequestDTO)
    {
        String keyword = null;
        if (StringUtils.isNotEmpty(dailymotionRequestDTO.getKeyWord()))
        {
            keyword = dailymotionRequestDTO.getKeyWord();
        }
        DailymotionDTO dailymotionDTO = RestfulService.getInstance(Constant.HOST_NAME.DAILYMOTION).search(
                keyword
                , dailymotionRequestDTO.getFields()
                , dailymotionRequestDTO.getFlags()
                , dailymotionRequestDTO.getCreatedAfter()
                , dailymotionRequestDTO.getCreatedBefore()
                , dailymotionRequestDTO.getSort()
                , dailymotionRequestDTO.getCountry()
                , dailymotionRequestDTO.getLongerThan()
                , dailymotionRequestDTO.getShorterThan()
                , dailymotionRequestDTO.getPage()
                , dailymotionRequestDTO.getLimit());
        return convertToVideo(dailymotionDTO);
    }

//    public PageModel seachPlayList(DailymotionRequestDTO dailymotionRequestDTO)
//    {
//        DailymotionPlayListDTO dailymotionPlayListDTO
//                = RestfulService.getInstance(Constant.HOST_NAME.DAILYMOTION).searchDailymotionPlaylist(
//                dailymotionRequestDTO.getKeyWord()
//                , dailymotionRequestDTO.getFields()
//                , dailymotionRequestDTO.getSort()
//                , dailymotionRequestDTO.getPage()
//                , dailymotionRequestDTO.getLimit());
//        List<Item> playlist = new ArrayList<>();
//        if (dailymotionPlayListDTO != null)
//        {
//            List<DailymotionPlayistDetailDTO> dailymotionPlayistDetailDTOs = dailymotionPlayListDTO.getDailymotionPlaylistDetailDTO();
//            for (DailymotionPlayistDetailDTO dailymotionPlayistDetailDTO : dailymotionPlayistDetailDTOs)
//            {
//                Channel channel = new Channel();
//                channel.setId(dailymotionPlayistDetailDTO.getId());
//                channel.setName(dailymotionPlayistDetailDTO.getName());
//                channel.setDescription(dailymotionPlayistDetailDTO.getDescription());
//                channel.setUrlUserAvatar(dailymotionPlayistDetailDTO.getAvatarUrl());
//                channel.setUrlCover(dailymotionPlayistDetailDTO.getCoverUrl());
//                channel.setFollows(dailymotionPlayistDetailDTO.getFollows());
//                channel.setVideos(dailymotionPlayistDetailDTO.getVideos());
//                channel.setUserOffollows(dailymotionPlayistDetailDTO.getFollows());
//                playlist.add(channel);
//            }
//        }
//        VideoPage videoPage = new VideoPage(playlist);
//        videoPage.setNextPage(dailymotionPlayListDTO.getPage());
//        return videoPage;
//    }

    private PageModel convertToVideo(DailymotionDTO dailymotionDTO)
    {
        List<Item> videos = new ArrayList<>();
        if (dailymotionDTO != null)
        {
            List<DailymotionDetailDTO> dailymotionDetailDTOs = dailymotionDTO.getDailymotionDetailDTOs();
            if (dailymotionDetailDTOs != null && dailymotionDetailDTOs.size() > 0)
            {
                for (DailymotionDetailDTO dailymotionDetailDTO : dailymotionDetailDTOs)
                {
                    VideoModel video = new VideoModel();
                    video.setId(dailymotionDetailDTO.getId());
                    video.setName(dailymotionDetailDTO.getTitle());
                    video.setUrl(dailymotionDetailDTO.getUrl());
                    video.setUrlThumbnail(dailymotionDetailDTO.getThumbnail_720_url());
                    video.setDescription(dailymotionDetailDTO.getDescription());
                    video.setViews(dailymotionDetailDTO.getViews_total());
                    int duration = dailymotionDetailDTO.getDuration();
                    int minutes = duration / 60;
                    int seconds = duration % 60;
                    String str = String.format("%d:%02d", minutes, seconds);
                    video.setDuration(str);
                    video.setLikes(new BigInteger("0"));
                    video.setDisLikes(new BigInteger("0"));
                    video.setPublishedAt(dailymotionDetailDTO.getCreated_time() * 1000);
                    UserModel userModel = new UserModel();
                    userModel.setId(dailymotionDetailDTO.getUserId());
                    userModel.setName(dailymotionDetailDTO.getAuthor());
                    userModel.setUrlAvatar(dailymotionDetailDTO.getAuthorAvatar());
                    userModel.setVideoCounts(dailymotionDetailDTO.getUserVideoCount());
                    userModel.setDescription(dailymotionDetailDTO.getUserDescription());
                    userModel.setSubscrible(dailymotionDetailDTO.getSubscribe());
                    userModel.setUrlCover(dailymotionDetailDTO.getUserCoverUrl());
                    video.setUserModel(userModel);
                    videos.add(video);
                }
            }
        }
        PageModel videoPage = new PageModel();
        videoPage.setItems(videos);
        videoPage.setNextPage(dailymotionDTO.getPage());
        return videoPage;
    }

    public PageModel mostPopular(RequestDTO requestDTO)
    {
        DailymotionRequestDTO dailymotionRequestDTO = (DailymotionRequestDTO) requestDTO;
        DailymotionDTO dailymotionDTO = RestfulService.getInstance(Constant.HOST_NAME.DAILYMOTION).mostPopular(
                dailymotionRequestDTO.getFields()
                , dailymotionRequestDTO.getCountry()
                , dailymotionRequestDTO.getFlags()
                , dailymotionRequestDTO.getSort()
                , dailymotionRequestDTO.getPage()
                , dailymotionRequestDTO.getLimit()
        );
        return convertToVideo(dailymotionDTO);
    }

    public PageModel getVideoRelated(RequestDTO requestDTO)
    {
        DailymotionRequestDTO dailymotionRequestDTO = (DailymotionRequestDTO) requestDTO;
        DailymotionDTO dailymotionDTO = RestfulService.getInstance(Constant.HOST_NAME.DAILYMOTION).mostPopular(
                dailymotionRequestDTO.getFields()
                , dailymotionRequestDTO.getCountry()
                , dailymotionRequestDTO.getFlags()
                , dailymotionRequestDTO.getSort()
                , dailymotionRequestDTO.getPage()
                , dailymotionRequestDTO.getLimit()
        );
        return convertToVideo(dailymotionDTO);
    }

    public PageModel getVideosByUser(RequestDTO requestDTO)
    {
        DailymotionRequestDTO dailymotionRequestDTO = (DailymotionRequestDTO) requestDTO;
        DailymotionDTO dailymotionDTO = RestfulService.getInstance(Constant.HOST_NAME.DAILYMOTION)
                .getVideoByUserId(
                        dailymotionRequestDTO.getId()
                        , dailymotionRequestDTO.getFields()
                        , dailymotionRequestDTO.getPage()
                        , dailymotionRequestDTO.getLimit());
        return convertToVideo(dailymotionDTO);
    }
}
