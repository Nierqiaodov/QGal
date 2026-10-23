package top.nierqiaodov.player;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

//带音量的 JLayer 音频输出设备
//JLayer 自带的 JavaSoundAudioDevice 不暴露音量控制,这里自己实现:
//  输出到 SourceDataLine,音量通过 MASTER_GAIN 控制(0 静音,1 原始音量)
class GainAudioDevice implements AudioDevice {

    //输出线路
    SourceDataLine line;

    //当前音量(0..1)
    volatile float volume = 1f;

    //是否已经打开
    volatile boolean open = false;

    //已写入的采样数
    volatile int position = 0;

    @Override
    public void open(Decoder decoder) throws JavaLayerException {

        //16 位、小端,与 write 里的字节顺序保持一致
        AudioFormat format = new AudioFormat(decoder.getOutputFrequency(), 16,
                decoder.getOutputChannels(), true, false);

        try {
            line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));
            line.open(format);
            line.start();
            applyVolume();
            open = true;
        } catch (LineUnavailableException exception) {
            throw new JavaLayerException("can not open audio line", exception);
        }
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void write(short[] samples, int offs, int len) throws JavaLayerException {

        if (line == null) {
            return;
        }

        //JLayer 给的是 16 位采样,这里转成小端字节流
        byte[] bytes = new byte[len * 2];
        for (int i = 0; i < len; i++) {
            short sample = samples[offs + i];
            bytes[i * 2] = (byte) (sample & 0xFF);
            bytes[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        line.write(bytes, 0, bytes.length);
        position += len;
    }

    @Override
    public void close() {

        open = false;

        if (line != null) {
            line.flush();
            line.stop();
            line.close();
            line = null;
        }
    }

    @Override
    public void flush() {
        if (line != null) {
            line.flush();
        }
    }

    @Override
    public int getPosition() {
        return position;
    }

    //设置音量(0..1)
    void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
        applyVolume();
    }

    //把音量映射到 MASTER_GAIN
    void applyVolume() {

        if (line == null || !line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        float minimum = gain.getMinimum();

        //volume = 1 时增益为 0dB,volume = 0 时增益取最小值
        gain.setValue(minimum * (1f - volume));
    }

}
