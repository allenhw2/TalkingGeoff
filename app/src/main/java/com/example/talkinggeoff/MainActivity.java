package com.example.talkinggeoff;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    /** Audio file where recording will be saved and read. */
    File mediaFile = new File(Environment.getExternalStorageDirectory(), "audio.pcm");

    /** Denotes if the app is recording. */
    public static boolean recording;

    /** Audio Handler. */
    private AudioTrack audioTrack;

    /** Handles Audio Recording. */
    private AudioRecord recorder;

    /** Writes audio data to media file. */
    DataOutputStream dataOutputStream;

    /** Record button. */
    Button record;

    /** Play button. */
    Button play;

    /** ImageView where faces are rendered. */
    ImageView faceView;

    /** Drawable face to render to faceView. */
    int faceToDraw = R.drawable.geoff_copy;

    /** Drawable gif to render to faceView. */
    int faceToAni = R.drawable.geoff;

    /** Recording duration. */
    long duration = 5000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button createGame = findViewById(R.id.createGame);
        createGame.setVisibility(View.VISIBLE);
        createGame.setOnClickListener(unused -> {
            setContentView(R.layout.new_game_activity);
            faceView = findViewById(R.id.imageView2);
            faceView.setVisibility(View.VISIBLE);


            record = findViewById(R.id.record);
            record.setOnClickListener(args -> {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        recording = true;
                        try {
                            startRecord();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            });

            play = findViewById(R.id.play);
            play.setOnClickListener(args -> {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        recording = false;
                        if (mediaFile.exists()) {
                            try {
                                playFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Nothing recorded yet!", 2).show();
                        }
                    }
                }).start();
            });

            ImageButton nord = findViewById(R.id.FaceFour);
            ImageButton chal = findViewById(R.id.FaceThree);


            nord.setOnClickListener(args -> {
                Glide.with(MainActivity.this).load(R.drawable.nordick).into(faceView);
                faceToDraw = R.drawable.nordick;
                faceToAni = R.drawable.nordickani;
            });

            chal.setOnClickListener(args -> {
                Glide.with(MainActivity.this).load(R.drawable.geoff_copy).into(faceView);
                faceToDraw = R.drawable.geoff_copy;
                faceToAni = R.drawable.geoff;
            });

        });
    }

    private void playFile() throws IOException {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                record.setEnabled(false);
                play.setEnabled(false);
                Glide.with(MainActivity.this).load(faceToAni).into(faceView);
            }
        });
        int i;
        RadioGroup pitches = findViewById(R.id.PitchOptions);
        switch (pitches.getCheckedRadioButtonId()) {
            case R.id.LowPitch:
                i = 9000;
                break;
            case R.id.NormalPitch:
                i = 11025;
                break;
            case R.id.HighPitch:
                i = 13000;
                break;
            case R.id.chipmunk:
                i = 20000;
                break;
            default:
                i = 11025;
        }

        int shortSizeInBytes = Short.SIZE / Byte.SIZE;
        int bufferSizeInBytes = (int) (mediaFile.length() / shortSizeInBytes);
        short[] audio = new short[bufferSizeInBytes];

        InputStream input = new FileInputStream(mediaFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(input);
        DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

        int j = 0;
        while (dataInputStream.available() > 0) {
            audio[j] = dataInputStream.readShort();
            j++;
        }

        dataInputStream.close();

        audioTrack = new AudioTrack(3, i, 2, 2, bufferSizeInBytes, 1);
        audioTrack.play();
        audioTrack.write(audio, 0, bufferSizeInBytes);
        long t= System.currentTimeMillis();
        long end = t + 1000;
        while(System.currentTimeMillis() < end) {
            //wait
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                record.setEnabled(true);
                play.setEnabled(true);
                Glide.with(MainActivity.this).load(faceToDraw).into(faceView);
            }
        });

    }

    private void startRecord() throws IOException {
        EditText time = findViewById(R.id.durationSeconds2);
        String s = String.valueOf(time.getText());
        if (s.equals("")) {
            duration = 5000;
        } else {
            duration = Long.parseLong(s) * 1000;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                record.setEnabled(false);
                play.setEnabled(false);
            }
        });


        File newAudio = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "audio.pcm");

        newAudio.createNewFile();

        OutputStream audioOutput = new FileOutputStream(newAudio);
        BufferedOutputStream bufferedAudioOutput = new BufferedOutputStream(audioOutput);
        dataOutputStream = new DataOutputStream(bufferedAudioOutput);

        int bufferSize = AudioRecord.getMinBufferSize(11205, 2, 2);

        short[] audio = new short[bufferSize];
        recorder = new AudioRecord(1, 11025, 2, 2, bufferSize);
        recorder.startRecording();

        long t = System.currentTimeMillis();
        long end = t + duration;
        while(System.currentTimeMillis() < end) {
            int numberOfShorts = recorder.read(audio, 0, bufferSize);
            for (int i = 0; i < numberOfShorts; i++) {
                dataOutputStream.writeShort(audio[i]);

            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                record.setEnabled(true);
                play.setEnabled(true);
            }
        });
        System.out.println("escaped");
        recording = false;
        recorder.stop();
        dataOutputStream.close();

    }

    protected void onDestroy() {
        super.onDestroy();
        recording = false;
        if (audioTrack != null)
        {
            audioTrack.release();
        }
    }
}
