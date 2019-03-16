package com.cheguza.facilitycenter;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.cardemulation.OffHostApduService;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MusicPlayerActivity extends AppCompatActivity {

    ListView musiclistView;
    TextView textView;
    String[] strings;
    Button nextBtn, prevBtn, pauseBtn;
    int prevPosition, defaultPosition;
    int count = 0;
    static MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        nextBtn = findViewById(R.id.skip_next);
        prevBtn = findViewById(R.id.skip_back);
        pauseBtn = findViewById(R.id.play_selected_song);
        textView = findViewById(R.id.playing_song_name);
        musiclistView = findViewById(R.id.songs_list);

        mediaPlayer = new MediaPlayer();

        runTimePermission();


    }
    /*
          Method For Ask User For Music,Video and Photos Permissions
     */

    public void runTimePermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        displaySongs();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {


                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        token.continuePermissionRequest();
                    }
                }).check();
    }
/*
    Method to Fatch All Audio Files From Your Offline Storage
 */
    public ArrayList<File> findSongs(File file) {
        ArrayList<File> songList = new ArrayList<>();
        File[] files = file.listFiles();
        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                if (file.getAbsolutePath() != null) {
                    songList.addAll(findSongs(singleFile));
                }

            } else {
                if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".MP3")) {
                    songList.add(singleFile);
                }
            }
        }

        return songList;
    }

    /*
        Method For Displaying Songs
     */
    void displaySongs() {
        final ArrayList<File> allSongs = findSongs(Environment.getExternalStorageDirectory());

        strings = new String[allSongs.size()];
        for (int i = 0; i < allSongs.size(); i++) {
            String s1 = allSongs.get(i).getName();
            int maxLength = s1.length();
            if (maxLength > 30) {
                strings[i] = s1.substring(0, 30).concat("...").replace(".mp3", " ");
            } else {
                strings[i] = s1;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, strings);
        musiclistView.setAdapter(adapter);
        musiclistView.setDividerHeight(40);

        musiclistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                defaultPosition = position;
                if (count == 0) {
                    Uri uri = Uri.fromFile(allSongs.get(position));
                    getSongDetails(strings[position], uri);
                    mediaPlayer.start();
                    pauseBtn.setBackgroundResource(R.drawable.ic_pause);
                    count++;
                    prevPosition = position;
                }
                if (count > 0) {
                    if (mediaPlayer.isPlaying() && (prevPosition != position)) {
                        mediaPlayer.reset();
                        Uri uri = Uri.fromFile(allSongs.get(position));
                        getSongDetails(strings[position], uri);
                        mediaPlayer.start();
                        pauseBtn.setBackgroundResource(R.drawable.ic_pause);
                    } else {
                        mediaPlayer.reset();
                        Uri uri = Uri.fromFile(allSongs.get(position));
                        getSongDetails(strings[position], uri);
                        mediaPlayer.start();
                        pauseBtn.setBackgroundResource(R.drawable.ic_pause);
                    }
                }
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    pauseBtn.setBackgroundResource(R.drawable.ic_play);
                } else {
                    mediaPlayer.start();
                    pauseBtn.setBackgroundResource(R.drawable.ic_pause);
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();
                defaultPosition = (defaultPosition + 1);
                Uri uri = Uri.fromFile(allSongs.get(defaultPosition));
                getSongDetails(strings[defaultPosition], uri);
                mediaPlayer.start();
                pauseBtn.setBackgroundResource(R.drawable.ic_pause);
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();
                defaultPosition = (defaultPosition - 1);
                Uri uri = Uri.fromFile(allSongs.get(defaultPosition));
                getSongDetails(strings[defaultPosition], uri);
                mediaPlayer.start();
                pauseBtn.setBackgroundResource(R.drawable.ic_pause);
            }
        });
    }

    public void getSongDetails(String songNameText, Uri uri) {
        textView.setText(songNameText);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Audio Player");
        builder.setMessage("Want to exit ):");
        mediaPlayer.pause();
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.release();
                finish();
            }
        });
        builder.setNegativeButton("Music (:", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.start();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }
}
