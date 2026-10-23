package top.nierqiaodov.handler;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import top.nierqiaodov.util.ConstantPool;
import top.nierqiaodov.util.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

//单个音频通道:同一时刻只播放一个文件,支持循环与单次
//格式支持:
//  wav / aiff 走 Java 原生 AudioSystem + Clip
//  ogg 走 vorbisspi(java-vorbis-support)解码,先转成 PCM 再交给 Clip
//  mp3 走 JLayer 解码线程(JLayer 为阻塞式播放,循环靠播完后重新打开实现)
class AudioChannel {

    //通道名(背景音乐 / 语音),只用于日志
    final String label;

    //当前播放中的 Clip(原生路径)
    volatile Clip clip;

    //当前播放中的 MP3 播放器(JLayer 路径)
    volatile Player mp3Player;

    //播放序号,用于丢弃过期的播放请求(快速切换时避免声音叠在一起)
    volatile long generation = 0;

    //是否循环播放
    volatile boolean looping = false;

    //当前播放的文件
    volatile File currentFile;

    AudioChannel(String label) {
        this.label = label;
    }

    //播放:looping 为 true 时循环,为 false 时只播一次
    public void play(File file, boolean looping) {

        stop();

        if (file == null || !file.isFile()) {
            return;
        }

        this.looping = looping;
        this.currentFile = file;
        long currentGeneration = generation;

        if (isMp3(file)) {
            playMp3(file, currentGeneration);
            return;
        }

        playClip(file, currentGeneration);
    }

    //停止并释放当前音频
    public void stop() {

        //递增序号,让还在解码的线程放弃播放
        generation++;
        looping = false;
        currentFile = null;

        Player currentMp3 = mp3Player;
        mp3Player = null;
        if (currentMp3 != null) {
            currentMp3.close();
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
    void playClip(File file, long currentGeneration) {

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

                if (looping) {
                    newClip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    newClip.start();
                }
                clip = newClip;
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
    void playMp3(File file, long currentGeneration) {

        Thread thread = new Thread(() -> {
            do {
                try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
                    Player player = new Player(stream);
                    mp3Player = player;
                    Logger.logger(label + ConstantPool.getColon() + file.getName());
                    player.play();
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
        }, "QGalMp3");
        thread.setDaemon(true);
        thread.start();
    }

}
