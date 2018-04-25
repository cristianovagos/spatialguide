package com.paydayme.spatialguide.core.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.storage.InternalStorage;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.download.Download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

import static com.paydayme.spatialguide.core.Constant.BROADCAST_DOWNLOAD_COMPLETED;
import static com.paydayme.spatialguide.core.Constant.BROADCAST_ERROR_DOWNLOAD;
import static com.paydayme.spatialguide.core.Constant.BROADCAST_MESSAGE_PROGRESS;
import static com.paydayme.spatialguide.core.Constant.BROADCAST_NO_POINTS;
import static com.paydayme.spatialguide.core.Constant.FILES_BASE_URL;
import static com.paydayme.spatialguide.core.Constant.POINT_STORAGE_SEPARATOR;

public class DownloadService extends IntentService {

    private static final String TAG = "DownloadService";

    private int totalFileSize;
    private int routeID;
    private Route route;
    private ArrayList<String> downloadList = new ArrayList<>();
    private ArrayList<Integer> pointsID = new ArrayList<>();
    private int counter = 1;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        routeID = intent.getIntExtra("route", -1);

        if(routeID != -1) {
            route = getRoute();

            if(route != null)
                initDownload();
        } else {
            sendErrorIntent();
        }
    }

    private Route getRoute() {
        try {
            return (Route) InternalStorage.readObject(getApplicationContext(), Constant.ROUTE_STORAGE_SEPARATOR + routeID);
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException: " + e.getMessage());
        }
        return null;
    }

    private void initDownload() {
        if(route.getRoutePoints().size() < 1) {
            sendNoPointsIntent();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FILES_BASE_URL)
                .build();

        DownloadInterface downloadInterface = retrofit.create(DownloadInterface.class);

        for(Point p : route.getRoutePoints()) {
            downloadList.add(p.getPointAudioURL());
            pointsID.add(p.getPointID());
        }

        int i = 0;
        for(String file : downloadList) {
            Call<ResponseBody> request = downloadInterface.downloadFile(file);
            try {
                downloadFile(request.execute().body(), pointsID.get(i));
            } catch (IOException e) {
                sendErrorIntent();
                return;
            }
            i++;
        }
        sendCompletionIntent();
    }

    private void downloadFile(ResponseBody body, Integer pointID) throws IOException {
        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile = new File(getApplicationContext().getFilesDir(),
                POINT_STORAGE_SEPARATOR + pointID + ".wav");
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {
                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                timeCount++;
            }

            output.write(data, 0, count);
        }
        onDownloadComplete(counter);
        output.flush();
        output.close();
        bis.close();

        counter++;
    }

    private void onDownloadComplete(int index) {
        Download download = new Download();
        download.setProgress(100);
        sendIntent(download, index);
    }

    private void sendIntent(Download download, int index) {
        Intent intent = new Intent(BROADCAST_MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        intent.putExtra("index", index);
        intent.putExtra("total", downloadList.size());
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void sendErrorIntent() {
        Intent intent = new Intent(BROADCAST_ERROR_DOWNLOAD);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void sendCompletionIntent() {
        Intent intent = new Intent(BROADCAST_DOWNLOAD_COMPLETED);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void sendNoPointsIntent() {
        Intent intent = new Intent(BROADCAST_NO_POINTS);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }
}
