package top.nierqiaodov.player;

import top.nierqiaodov.player.util.ConstantPool;

import java.io.File;

//音频播放器:两条通道,背景音乐循环、角色语音单次,可以同时出声
//支持格式见 AudioChannel(wav/aiff/ogg/mp3),并提供音量与静音控制
public class AudioPlayer {

    //默认音量
    static final float DEFAULT_VOLUME = 0.8f;

    //每次调整的步长
    static final float VOLUME_STEP = 0.1f;

    //背景音乐通道(循环)
    AudioChannel backgroundChannel = new AudioChannel(ConstantPool.getLabel_Music());

    //角色语音通道(单次)
    AudioChannel voiceChannel = new AudioChannel(ConstantPool.getLabel_Voice());

    //当前音量(0..1)与静音状态
    volatile float volume = DEFAULT_VOLUME;
    volatile boolean muted = false;

    //循环播放背景音乐
    public void playBackground(File file) {
        backgroundChannel.setVolume(effectiveVolume());
        backgroundChannel.play(file, true);
    }

    //停止背景音乐
    public void stopBackground() {
        backgroundChannel.stop();
    }

    //播放一次语音(再次调用会打断上一句)
    public void playVoice(File file) {
        playVoice(file, null);
    }

    //播放一次语音,播完后回调 onFinished(被停止或切换时不会回调)
    public void playVoice(File file, Runnable onFinished) {
        voiceChannel.setVolume(effectiveVolume());
        voiceChannel.play(file, false, onFinished);
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

    //设置音量(0..1)
    public void setVolume(float volume) {

        this.volume = Math.max(0f, Math.min(1f, volume));
        if (this.volume > 0f) {
            muted = false;
        }
        applyVolume();
    }

    //当前音量(不含静音)
    public float getVolume() {
        return volume;
    }

    //加减音量,返回调整后的音量
    public float changeVolume(float delta) {
        setVolume(volume + delta);
        return volume;
    }

    //切换静音,返回切换后的静音状态
    public boolean toggleMute() {
        muted = !muted;
        applyVolume();
        return muted;
    }

    //当前是否静音
    public boolean isMuted() {
        return muted;
    }

    //实际生效的音量(静音时为 0)
    public float effectiveVolume() {
        return muted ? 0f : volume;
    }

    //把音量下发到两条通道
    void applyVolume() {
        backgroundChannel.setVolume(effectiveVolume());
        voiceChannel.setVolume(effectiveVolume());
    }

}
