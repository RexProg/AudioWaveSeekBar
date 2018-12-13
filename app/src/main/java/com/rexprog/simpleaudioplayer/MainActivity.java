package com.rexprog.simpleaudioplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.rexprog.audiowaveseekbar.AudioWaveSeekBar;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button play;
    Button stop;
    Button pause;
    MediaPlayer mp;
    SeekBar seekbar;
    Handler handler;
    Runnable runnable;
    AudioWaveSeekBar waveSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play = findViewById(R.id.play);
        stop = findViewById(R.id.stop);
        pause = findViewById(R.id.pause);
        seekbar = findViewById(R.id.seekBar);
        waveSeekBar = findViewById(R.id.audioWaveSeekBar);

        waveSeekBar.setWaveform(UUID.randomUUID().toString().getBytes());
        handler = new Handler();

        mp = MediaPlayer.create(this, R.raw.song);
        stop.setVisibility(View.INVISIBLE);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop.setVisibility(View.VISIBLE);
                play();
                playCycle();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.pause();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop.setVisibility(View.INVISIBLE);
                if (mp.isPlaying()) {
                    mp.pause();
                    mp.seekTo(0);
                    seekbar.setProgress(0);
                    waveSeekBar.setProgress(0);
                    handler.removeCallbacks(runnable);
                }
            }
        });
    }

    public void play() {
        mp.start();

        waveSeekBar.setDuration(mp.getDuration());
        waveSeekBar.setProgress(0);
        waveSeekBar.setOnSeekBarChangeListener(new AudioWaveSeekBar.SeekBarChangeListener() {
            @Override
            public void OnSeekBarChangeListener(int progress) {
                mp.seekTo(progress);
            }
        });
        seekbar.setMax(mp.getDuration());
        seekbar.setProgress(0);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mp.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void playCycle() {
        waveSeekBar.setProgress(mp.getCurrentPosition());
        seekbar.setProgress(mp.getCurrentPosition());

        if (mp.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }
}
