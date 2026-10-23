package top.nierqiaodov.handler;

import top.nierqiaodov.util.ConstantPool;

import java.io.File;

//音频播放器:两条通道,背景音乐循环、角色语音单次,可以同时出声
//支持格式见 AudioChannel(wav/aiff/ogg/mp3)
public class AudioPlayer {

    //背景音乐通道(循环)
    AudioChannel backgroundChannel = new AudioChannel(ConstantPool.getLabel_Music());

    //角色语音通道(单次)
    AudioChannel voiceChannel = new AudioChannel(ConstantPool.getLabel_Voice());

    //循环播放背景音乐
    public void playBackground(File file) {
        backgroundChannel.play(file, true);
    }

    //停止背景音乐
    public void stopBackground() {
        backgroundChannel.stop();
    }

    //播放一次语音(再次调用会打断上一句)
    public void playVoice(File file) {
        voiceChannel.play(file, false);
    }

    //停止语音
    public void stopVoice() {
        voiceChannel.stop();
    }

    //当前背景音乐文件
    public File getBackgroundFile() {
        return backgroundChannel.getCurrentFile();
    }

    //停止全部音频
    public void stopAll() {
        backgroundChannel.stop();
        voiceChannel.stop();
    }

}
