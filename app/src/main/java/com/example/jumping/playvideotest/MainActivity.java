package com.example.jumping.playvideotest;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {
    private VideoView videoView;
    private Button play;
    private Button open;
    private Button pause;
    private Button replay;
    private String videofilepath;
    public static final int CHOOSE_VIDEO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = (VideoView) findViewById(R.id.video_view);
        open = (Button) findViewById(R.id.open);
        play = (Button) findViewById(R.id.play);
        pause = (Button) findViewById(R.id.pause);
        replay = (Button) findViewById(R.id.replay);

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("video/*");
                startActivityForResult(intent, CHOOSE_VIDEO);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoView.isPlaying()) {
                    videoView.start();
                } else if (videoView.isPlaying()) {
                    videoView.pause();
                    initvideopath(videofilepath);
                    videoView.start();
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.pause();
            }
        });

        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    videoView.resume();
                } else if (!videoView.isPlaying()) {
                    videoView.pause();
                    initvideopath(videofilepath);
                    videoView.start();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_VIDEO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        videofilepath = null;
                        Uri uri = data.getData();
                        if (DocumentsContract.isDocumentUri(this, uri)) {
                            String docid = DocumentsContract.getDocumentId(uri);
                            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                                String id = docid.split(":")[1];
                                String selection = MediaStore.Video.Media._ID + "=" + id;
                                videofilepath = getvideopath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, selection);
                            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                                Uri contenturi = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docid));
                                videofilepath = getvideopath(contenturi, null);
                            }
                        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                            videofilepath = getvideopath(uri, null);
                        }
                        initvideopath(videofilepath);
                    } else {
                        videofilepath = null;
                        Uri uri = data.getData();
                        videofilepath = getvideopath(uri, null);
                        initvideopath(videofilepath);

                    }
                }
                break;
            default:
                break;
        }
    }

    private String getvideopath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void initvideopath(String videopath) {
        try {
            videoView.setVideoPath(videopath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.suspend();
        }
    }
}
