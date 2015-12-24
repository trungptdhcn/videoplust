package com.trungpt.videoplus.download;

import android.app.NotificationManager;
import android.support.v7.app.NotificationCompat;
import com.trungpt.videoplus.ui.model.DirectLink;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by trung on 12/21/2015.
 */
public class Downloader2 extends RealmObject implements Runnable
{

    protected URL url;
    protected String fileName;
    public String folder;
    protected String videoType;
    protected int fileSize;
    protected int state;
    @Ignore
    protected int numberConnections;
    @Ignore
    protected DirectLink directLink;
    @Ignore
    private int downloaded;
    @Ignore
    protected List<DownloadThread> listDownloadThread;
    @Ignore
    protected static final int BLOCK_SIZE = 4096;
    @Ignore
    protected static final int BUFFER_SIZE = 4096;
    @Ignore
    protected static final int MIN_DOWNLOAD_SIZE = BLOCK_SIZE * 100;
    @Ignore
    public static final String STATUSES[] = {"Downloading",
            "Paused", "Complete", "Cancelled", "Error"};

    @Ignore
    private UpdateProgress updateProgress;
    @Ignore
    public static final int DOWNLOADING = 0;
    @Ignore
    public static final int PAUSED = 1;
    @Ignore
    public static final int COMPLETED = 2;
    @Ignore
    public static final int CANCELLED = 3;
    @Ignore
    public static final int ERROR = 4;

    public Downloader2(String folder, int numberConnections, DirectLink directLink)
    {
        try
        {
            this.url = new URL(directLink.getUri());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        this.folder = folder;
        this.fileName = directLink.getName();
        this.videoType = directLink.getType();
        this.directLink = directLink;
        this.numberConnections = numberConnections;
        this.fileSize = -1;
        this.state = DOWNLOADING;
        downloaded = 0;
        listDownloadThread = new ArrayList<>();
    }

    public URL getUrl()
    {
        return url;
    }

    public void setUrl(URL url)
    {
        this.url = url;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFolder()
    {
        return folder;
    }

    public void setFolder(String folder)
    {
        this.folder = folder;
    }

    public String getVideoType()
    {
        return videoType;
    }

    public void setVideoType(String videoType)
    {
        this.videoType = videoType;
    }

    public void setFileSize(int fileSize)
    {
        this.fileSize = fileSize;
    }

    public void pause()
    {
        setState(PAUSED);
    }

    public void resume()
    {
        setState(DOWNLOADING);
        download();
    }

    public void cancel()
    {
        setState(CANCELLED);
    }

    public String getURL()
    {
        return url.toString();
    }

    public int getFileSize()
    {
        return fileSize;
    }

    public float getProgress()
    {
        return ((float) downloaded / fileSize) * 100;
    }

    public int getState()
    {
        return state;
    }

    public DirectLink getDirectLink()
    {
        return directLink;
    }

    public void setDirectLink(DirectLink directLink)
    {
        this.directLink = directLink;
    }

    protected void setState(int value)
    {
        state = value;
        stateChanged();
    }

    protected void download()
    {
        Thread t = new Thread(this);
        t.start();
    }

    protected synchronized void downloaded(int value)
    {
        downloaded += value;
        stateChanged();
    }

    protected void stateChanged()
    {
        if (updateProgress != null)
        {
            updateProgress.update((int) (getProgress()));
        }
    }

    public void setUpdateProgress(UpdateProgress updateProgress)
    {
        this.updateProgress = updateProgress;
    }

    @Override
    public void run()
    {
        HttpURLConnection conn = null;
        try
        {
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.connect();
            if (conn.getResponseCode() / 100 != 2)
            {
                error();
            }
            int contentLength = conn.getContentLength();
            if (contentLength < 1)
            {
                error();
            }
            if (fileSize == -1)
            {
                fileSize = contentLength;
                stateChanged();
            }
            if (state == DOWNLOADING)
            {
                if (listDownloadThread.size() == 0)
                {
                    if (fileSize > MIN_DOWNLOAD_SIZE)
                    {
                        int partSize = Math.round(((float) fileSize / numberConnections) / BLOCK_SIZE) * BLOCK_SIZE;
                        int startByte = 0;
                        int endByte = partSize - 1;
                        DownloadThread thread = new DownloadThread(1, url, folder + fileName + videoType, startByte, endByte);
                        listDownloadThread.add(thread);
                        int i = 2;
                        while (endByte < fileSize)
                        {
                            startByte = endByte + 1;
                            endByte += partSize;
                            thread = new DownloadThread(i, url, folder + fileName + videoType, startByte, endByte);
                            listDownloadThread.add(thread);
                            ++i;
                        }
                    }
                    else
                    {
                        DownloadThread thread = new DownloadThread(1, url, folder + fileName + videoType, 0, fileSize);
                        listDownloadThread.add(thread);
                    }
                }
                else
                {
                    for (int i = 0; i < listDownloadThread.size(); ++i)
                    {
                        if (!listDownloadThread.get(i).isFinished())
                        {
                            listDownloadThread.get(i).download();
                        }
                    }
                }
                for (int i = 0; i < listDownloadThread.size(); i++)
                {
                    listDownloadThread.get(i).waitFinish();
                }
                if (state == DOWNLOADING)
                {
                    setState(COMPLETED);
                }
            }
        }
        catch (Exception e)
        {
            error();
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }

    private void error()
    {
        System.out.println("ERROR");
        setState(ERROR);
    }

    protected class DownloadThread implements Runnable
    {
        protected int threadId;
        protected URL url;
        protected String fileName;
        protected int startByte;
        protected int endByte;
        protected boolean isFinish;
        protected Thread thread;

        public DownloadThread(int threadId, URL url, String fileName, int startByte, int endByte)
        {
            this.threadId = threadId;
            this.url = url;
            this.fileName = fileName;
            this.startByte = startByte;
            this.endByte = endByte;
            this.isFinish = false;

            download();
        }

        /**
         * Get whether the thread is finished download the part of file
         */
        public boolean isFinished()
        {
            return isFinish;
        }

        /**
         * Start or resume the download
         */
        public void download()
        {
            thread = new Thread(this);
            thread.start();
        }

        /**
         * Waiting for the thread to finish
         *
         * @throws InterruptedException
         */
        public void waitFinish() throws InterruptedException
        {
            thread.join();
        }

        @Override
        public void run()
        {
            BufferedInputStream in = null;
            RandomAccessFile raf = null;
            try
            {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                String byteRange = startByte + "-" + endByte;
                conn.setRequestProperty("Range", "bytes=" + byteRange);
                conn.connect();
                if (conn.getResponseCode() / 100 != 2)
                {
                    error();
                }
                in = new BufferedInputStream(conn.getInputStream());
                raf = new RandomAccessFile(fileName, "rw");
                raf.seek(startByte);
                byte data[] = new byte[BUFFER_SIZE];
                int numRead;
                while ((state == DOWNLOADING) && (numRead = in.read(data, 0, BUFFER_SIZE)) != -1)
                {
                    raf.write(data, 0, numRead);
                    startByte += numRead;
                    downloaded(numRead);
                }
                if (state == DOWNLOADING)
                {
                    isFinish = true;
                }
            }
            catch (Exception e)
            {
                error();
            }
            finally
            {
                if (raf != null)
                {
                    try
                    {
                        raf.close();
                    }
                    catch (IOException e)
                    {
                    }
                }

                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
        }
    }
}
