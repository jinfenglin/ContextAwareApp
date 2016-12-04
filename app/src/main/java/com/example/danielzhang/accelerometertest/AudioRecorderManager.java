package com.example.danielzhang.accelerometertest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTimestamp;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AudioRecorderManager {
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    int bufferSize;
    List<AudioData> audioDataList;

    public List<AudioData> getBuffer() {
        return audioDataList;
    }

    public void setBuffer(List<AudioData> audioDataList) {
        this.audioDataList = audioDataList;
    }

    public AudioRecorderManager() {
        audioDataList = new ArrayList<AudioData>();
    }

    private static int[] mSampleRates = new int[]{8000, 11025, 22050, 44100};
    private static short[] aformats = new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT};
    private static short[] chConfigs = new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO};


    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : aformats) {
                for (short channelConfig : chConfigs) {
                    try {
                        Log.d("Log:", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                + channelConfig);
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC, rate, channelConfig, audioFormat, java.lang.Math.max(bufferSize, 1024 * 8));

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                this.bufferSize = java.lang.Math.max(bufferSize, 1024 * 8);
                                return recorder;
                            }

                        }
                    } catch (Exception e) {
                        Log.e("Log:", rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
        return null;
    }

    public void startRecording() {
        if (recorder != null){
            recorder.release();
            recorder = null;
            Log.d("Audio", "Releasing audio recorder");
        }
        recorder = findAudioRecord();
        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                storeData();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    /**
     * Store the sampled date to a list
     */
    private void storeData() {
        while (isRecording) {
            short sData[] = new short[bufferSize];
            AudioTimestamp audioTimestamp = new AudioTimestamp();
            recorder.read(sData, 0, bufferSize);
            //recorder.getTimestamp(audioTimestamp, AudioTimestamp.TIMEBASE_MONOTONIC); //require api 24
            //audioDataList.add(new AudioData(sData, audioTimestamp.nanoTime));
            audioDataList.add(new AudioData(sData, System.currentTimeMillis()));
        }
    }


    /**
     * Write the audio data to a given path
     *
     * @param path     The path of the output destination
     * @param baseTime A timestamp representing the start point of sensor data collecting event
     */
    public void writeDataToPath(String path, long baseTime) {
        StringBuilder sb = new StringBuilder();
        File file = new File(path);
        FileOutputStream outputStream;
        for (AudioData audioData : audioDataList) {
            StringBuilder mergeData = new StringBuilder();
            for (short dataPiece : audioData.getAudioData()) {
                mergeData.append(";");
                mergeData.append(dataPiece);
            }
            sb.append(audioData.getTimestamp() - baseTime + mergeData.toString() + "\n");
        }

        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(sb.toString().getBytes());
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop recording the data
     */
    public void stopRecording() {
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    /**
     * Clear the data that have been stored in memory
     */
    public void clearBuff() {
        audioDataList.clear();
    }
}
