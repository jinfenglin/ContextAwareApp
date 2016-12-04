package com.example.danielzhang.accelerometertest;

public class AudioData {
    private short[] audioData;
    private long timestamp;

    public AudioData(short[] data, long timeStamp) {
        this.audioData = data;
        this.timestamp = timeStamp;
    }

    public short[] getAudioData() {
        return audioData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timeStamp) {
        this.timestamp = timeStamp;
    }

    public void setAudioData(short[] audioData) {
        this.audioData = audioData;
    }
}
