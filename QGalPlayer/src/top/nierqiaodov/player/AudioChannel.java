package top.nierqiaodov.player;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import top.nierqiaodov.player.util.ConstantPool;
import top.nierqiaodov.player.util.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

//单个音频通道:同一时刻只播放一个文件,支持循环、单次、音量与播放结束回调
//格式支持:
//  wav / aiff 走 Java 原生 AudioSystem + Clip
//  ogg 走 vorbisspi(java-vorbis-support)解码,先转成 PCM 再交给 Clip
//  mp3 走 JLayer + 自带音量的输出设备(GainAudioDevice)
class AudioChannel {

    //通道名(背景音乐 / 语音),只用于日志
    final String label;

    //当前播放中的 Clip(原生路径)
    volatile Clip clip;

    //当前播放中的 MP3 播放器与输出设备(JLayer 路径)
    volatile Player mp3Player;
    volatile GainAudioDevice mp3Device;

    //播放序号,用于丢弃过期的播放请求(快速切换时避免声音叠在一起)
    volatile long generation = 0;

    //是否循环播放
    volatile boolean looping = false;

    //当前播放的文件
    volatile File currentFile;

    //当前音量(0..1)
    volatile float volume = 0.8f;

    AudioChannel(String label) {
        this.label = label;
    }

    //播放(不带结束回调)
    public void play(File file, boolean looping) {
        play(file, looping, null);
    }

    //播放:onFinished 只在"单次播放自然结束"时回调,被停止或切换不会回调
    public void play(File file, boolean looping, Runnable onFinished) {

        stop();

        if (file == null || !file.isFile()) {
            return;
        }

        this.looping = looping;
        this.currentFile = file;
        long currentGeneration = generation;

        if (isMp3(file)) {
            playMp3(file, currentGeneration, onFinished);
            return;
        }

        playClip(file, currentGeneration, onFinished);
    }

    //停止并释放当前音频
    public void stop() {

        //先递增序号,让还在解码的线程与结束回调都失效
        generation++;
        looping = false;
        currentFile = null;

        Player currentMp3 = mp3Player;
        mp3Player = null;
        if (currentMp3 != null) {
            currentMp3.close();
        }

        GainAudioDevice device = mp3Device;
        mp3Device = null;
        if (device != null) {
            device.close();
        }

        Clip currentClip = clip;
        clip = null;
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
        }
    }

    //当前播放的文件
    public File getCurrentFile() {
        return currentFile;
    }

    //设置音量(0..1),立即作用到正在播放的音频
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
        applyVolume();
    }

    //当前音量
    public float getVolume() {
        return volume;
    }

    //把音量作用到当前正在播放的 Clip 或 JLayer 设备上
    void applyVolume() {

        Clip currentClip = clip;
        if (currentClip != null && currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(gain.getMinimum() * (1f - volume));
        }

        GainAudioDevice device = mp3Device;
        if (device != null) {
            device.setVolume(volume);
        }
    }

    //是否为 mp3(Java 原生不支持,JLayer 走单独路径)
    static boolean isMp3(File file) {
        return file.getName().toLowerCase().endsWith(".mp3");
    }

    //把编码音频流(如 ogg 的 VORBISENC)转成 16 位 PCM,否则 Clip 打不开
    static AudioInputStream toPcmStream(AudioInputStream source) throws IOException {

        AudioFormat format = source.getFormat();
        if (AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())) {
            return source;
        }

        AudioFormat target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(), 16, format.getChannels(),
                format.getChannels() * 2, format.getSampleRate(), false);

        return AudioSystem.getAudioInputStream(target, source);
    }

    //原生路径:wav / aiff / ogg
    void playClip(File file, long currentGeneration, Runnable onFinished) {

        Thread thread = new Thread(() -> {
            try (AudioInputStream rawStream = AudioSystem.getAudioInputStream(file);
                 AudioInputStream stream = toPcmStream(rawStream)) {

                Clip newClip = AudioSystem.getClip();
                newClip.open(stream);

                //解码期间可能已经切到别的音频,这种情况下直接丢弃
                if (currentGeneration != generation) {
                    newClip.close();
                    return;
                }

                newClip.addLineListener(event -> {
                    //自然播完才会带着相同的 generation 触发
                    if (event.getType() == LineEvent.Type.STOP
                            && onFinished != null && !looping && currentGeneration == generation) {
                        onFinished.run();
                    }
                });

                if (looping) {
                    newClip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    newClip.start();
                }

                clip = newClip;
                applyVolume();
                Logger.logger(label + ConstantPool.getColon() + file.getName());

            } catch (UnsupportedAudioFileException | IllegalArgumentException exception) {
                Logger.logger(ConstantPool.getMsg_Audio_Unsupported() + file.getName());
            } catch (IOException | LineUnavailableException exception) {
                Logger.error(ConstantPool.getMsg_Audio_Error() + exception.getMessage());
            }
        }, "QGalAudio");
        thread.setDaemon(true);
        thread.start();
    }

    //mp3 路径:JLayer 阻塞式播放,循环时播完重开
    void playMp3(File file, long currentGeneration, Runnable onFinished) {

        Thread thread = new Thread(() -> {
            do {
                try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {

                    GainAudioDevice device = new GainAudioDevice();
                    device.setVolume(volume);

                    Player player = new Player(stream, device);
                    mp3Player = player;
                    mp3Device = device;

                    Logger.logger(label + ConstantPool.getColon() + file.getName());
                    player.play();

                    //自然播完,且序号没变才回调
                    if (!looping && onFinished != null && currentGeneration == generation) {
                        onFinished.run();
                    }

                } catch (FileNotFoundException exception) {
                    Logger.error(ConstantPool.getMsg_Audio_Error() + exception.getMessage());
                    return;
                } catch (JavaLayerException exception) {
                    Logger.logger(ConstantPool.getMsg_Audio_Unsupported() + file.getName());
                    return;
                } catch (IOException exception) {
                    Logger.error(ConstantPool.getMsg_Audio_Error() + exception.getMessage());
                    return;
                }
            } while (looping && currentGeneration == generation);

            mp3Player = null;
            mp3Device = null;
        }, "QGalMp3");
        thread.setDaemon(true);
        thread.start();
    }

}
