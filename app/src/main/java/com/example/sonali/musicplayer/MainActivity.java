package com.example.sonali.musicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    ImageButton btnPlay,btnForward,btnBackward,btnNext,btnPrevious,btnPlaylist,btnRepeat,btnShuffle;
    ImageView AlbumArt;
    SeekBar songProgressBar;
    TextView songTitleLabel,CurrentDurationLabel,TotalDurationLabel;
    MediaPlayer mediaPlayer;
    Handler mHandler=new Handler();
    Typeface customFonts;
    private static final int MY_PERMISSION_REQUEST=1;
    ArrayList<String>  songList;
    ListView listView;
    ArrayAdapter<String> adapter;
    String currentLocation;
    String currentTitle;
    int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
//test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        customFonts = Typeface.createFromAsset(getAssets(),  "fonts/leaguespartan-bold.ttf");

        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        AlbumArt= (ImageView) findViewById(R.id.songAlbumCover);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        CurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        TotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        mediaPlayer = new MediaPlayer();

        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mediaPlayer.setOnCompletionListener(this);

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)!=getPackageManager().PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }
        }else{
            getSongList();
        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    if(mediaPlayer!=null){
                        mediaPlayer.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.play_btn);
                    }
                }else{
                    // Resume song
                    if(mediaPlayer!=null){
                        mediaPlayer.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.pause_btn);
                    }
                }
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    int currentPosition=mediaPlayer.getCurrentPosition();
                    if(currentPosition+5000<=mediaPlayer.getDuration())
                    {
                        mediaPlayer.seekTo(currentPosition+5000);
                    }
                    else
                    {
                        mediaPlayer.seekTo(mediaPlayer.getDuration());
                    }
            }
        });

        btnBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition=mediaPlayer.getCurrentPosition();
                if(currentPosition+5000<=mediaPlayer.getDuration())
                {
                    mediaPlayer.seekTo(currentPosition-5000);
                }
                else
                {
                    mediaPlayer.seekTo(0);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentSongIndex< songList.size()-1)
                {
                    currentSongIndex++;
                    playSong(currentSongIndex);
                }
                else{
                    currentSongIndex=0;
                    playSong(currentSongIndex);
                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentSongIndex!=0)
                {
                    currentSongIndex=(songList.size()-1);
                    playSong(currentSongIndex);
                }
                else
                {
                    currentSongIndex=-1;
                    playSong(currentSongIndex);
                }
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRepeat){
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.ic_no_replay);
                }else {
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.ic_replay);
                    btnShuffle.setImageResource(R.drawable.ic_no_shuffle);
                }
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShuffle){
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.ic_no_shuffle);
                }else{
                    // make repeat to true
                    isShuffle= true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.ic_shuffle);
                    btnRepeat.setImageResource(R.drawable.ic_no_replay);
                }
            }
        });
    }

    public void playSong(int songIndex){

        try {
            ContentResolver contentResolver=getContentResolver();
            Uri songUri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor songCursor=contentResolver.query(songUri,null,null,null,MediaStore.Audio.Media.TITLE);
            if(songCursor!=null && songCursor.moveToPosition(songIndex)) {
                int SongTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int SongLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                currentTitle = songCursor.getString(SongTitle);
                currentLocation = songCursor.getString(SongLocation);
                currentSongIndex = songIndex;
                Cursor cursorAlbum;
                Long albumId = Long.valueOf(songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                cursorAlbum = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART}, MediaStore.Audio.Albums._ID + "=" + albumId, null, null);

                if (cursorAlbum != null && cursorAlbum.moveToFirst()) {
                    String albumCoverPath = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    Bitmap bm = BitmapFactory.decodeFile(albumCoverPath);
                    AlbumArt.setImageBitmap(bm);
                }
            }
            mediaPlayer.reset();
            String location=currentLocation;
            mediaPlayer.setDataSource(getApplicationContext(),Uri.parse(location));
            mediaPlayer.prepare();
            mediaPlayer.start();

            songTitleLabel.setText(currentTitle);
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            updateProgressBar();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getSongList(){
        listView=(ListView)findViewById(R.id.listView);
        songList=new ArrayList<>();
        getMusic();
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,songList){

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            // Cast the list view each item as text view
            TextView item = (TextView) super.getView(position,convertView,parent);

            // Set the typeface/font for the current item
            item.setTypeface(customFonts);

            // Set the list view item's text color
            item.setTextColor(Color.parseColor("#ffffff"));

            // Set the item text style to bold
           // item.setTypeface(item.getTypeface(), Typeface.BOLD);

            // Change the item text size
            //item.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);

            // return the view
            return item;
        }
    };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                for (int j = 0; j < adapterView.getChildCount(); j++)
                    adapterView.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.item));

                // change the background color of the selected element
                view.setBackgroundColor(getResources().getColor(R.color.selected_item));
                playSong(position);
            }
        });
    }

    public void getMusic(){
        ContentResolver contentResolver=getContentResolver();
        Uri songUri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor=contentResolver.query(songUri,null,null,null,MediaStore.Audio.Media.TITLE);
        if(songCursor!=null && songCursor.moveToFirst()){
            int SongTitle=songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int SongArtist=songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int SongLocation=songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do{
                String songTitle=songCursor.getString(SongTitle);
                String songArtist=songCursor.getString(SongArtist);
                //String songLocation=songCursor.getString(SongLocation);
                songList.add(songTitle + "\n"+ songArtist);
            }while(songCursor.moveToNext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSION_REQUEST:{
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

                        getSongList();

                    }else{
                        Toast.makeText(this, "permission not Granted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }
    }

    public void updateProgressBar(){
        mHandler.postDelayed(mUpdateTimeTask,100);
    }

    private Runnable mUpdateTimeTask=new Runnable(){
        public void run(){
            long totalDuration=mediaPlayer.getDuration();
            long currentDuration=mediaPlayer.getCurrentPosition();


            TotalDurationLabel.setText(Timer(totalDuration));
            CurrentDurationLabel.setText(Timer(currentDuration));

            long totalSeconds=(int)(totalDuration/1000);
            long currentSeconds=(int)(currentDuration/1000);
            double percentage=((double)currentSeconds/totalSeconds)*100;
            int progress = (int)(percentage);
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);

        }
    };

    public String Timer(long milliseconds){
        String finalTimerString = "";
        String secondsString;

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);

        int totalDuration = (int) (mediaPlayer.getDuration()/ 1000);
        int currentDuration = (int) ((((double)seekBar.getProgress()) / 100) * totalDuration);
        int new_currentDuration=currentDuration*1000;
        mediaPlayer.seekTo(new_currentDuration);
        updateProgressBar();

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(isRepeat) {
            playSong(currentSongIndex);
        }
        else if(isShuffle){
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songList.size()-1)+1) ;
            playSong(currentSongIndex);
        }
        else{
            if(currentSongIndex < (songList.size() - 1)) {
                currentSongIndex = currentSongIndex + 1;
                playSong(currentSongIndex);
            }
            else{
            // play first song
            currentSongIndex = 0;
            playSong(currentSongIndex);
        }
        }
    }

}
