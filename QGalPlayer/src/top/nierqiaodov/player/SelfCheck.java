package top.nierqiaodov.player;

import top.nierqiaodov.player.Element.Actor;
import top.nierqiaodov.player.Element.Background;
import top.nierqiaodov.player.Element.Line;
import top.nierqiaodov.player.Element.Music;
import top.nierqiaodov.player.Element.Scene;
import top.nierqiaodov.player.util.BuildInfo;
import top.nierqiaodov.player.util.ConstantPool;
import top.nierqiaodov.player.util.Fonts;
import top.nierqiaodov.player.util.Lang;
import top.nierqiaodov.player.util.Logger;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

//无界面自检:验证成品能否正确拆包、元素与语音能否还原、画面能否绘制、中文与音频解码是否可用
//用法: java -jar QGalPlayer-1.0.jar --check 成品.qgl
public class SelfCheck {

    //执行自检,返回是否全部通过
    public static boolean run(File packageFile) {

        boolean passed = true;
        QGalPackage qGalPackage = null;

        try {
            //0.运行环境:中文文案、中文字体、mp3/ogg 解码支持
            passed &= checkEnvironment();

            //1.拆包
            qGalPackage = PackageReader.open(packageFile);
            List<Scene> scenes = qGalPackage.getScenes();
            Logger.logger("Scene Count: " + scenes.size());

            //2.逐个场景检查元素与语音是否都还原到位
            for (Scene scene : scenes) {
                Logger.logger("  " + describeScene(scene));
                passed &= checkScene(scene);
            }

            //3.离屏绘制一帧,确认画面链路可用
            PlayerPanel playerPanel = new PlayerPanel();
            playerPanel.setSize(960, 540);
            playerPanel.setDoubleBuffered(false);
            playerPanel.load(qGalPackage);
            passed &= checkRender(playerPanel);

            //4.逐句推进,确认不会越界
            int totalLines = 0;
            for (Scene scene : scenes) {
                totalLines += scene.getLines().getLines().size();
            }
            for (int i = 0; i < totalLines + scenes.size() + 1; i++) {
                playerPanel.next();
            }
            playerPanel.stopAudio();
            Logger.logger("Progress after run: " + playerPanel.describeProgress());

        } catch (IOException exception) {
            Logger.error(exception.getMessage());
            passed = false;
        } finally {
            //5.关闭后解包目录必须被清理
            if (qGalPackage != null) {
                File workDir = qGalPackage.getWorkDir();
                qGalPackage.close();
                passed &= report("cleanup", !workDir.exists());
            }
        }

        Logger.logger(passed ? "SELF CHECK PASSED" : "SELF CHECK FAILED");

        return passed;
    }

    //只做运行环境自检(不需要成品):中文文案、中文字体、mp3/ogg 解码是否就绪
    //发布门禁与 CI 用这个入口,退出码 0 表示环境正常
    public static boolean runEnvironment() {

        boolean passed = checkEnvironment();

        Logger.logger("version: " + BuildInfo.version()
                + " / font: " + Fonts.getFamily() + " / language: " + Lang.getCurrent());
        Logger.logger(passed ? "SELF CHECK PASSED" : "SELF CHECK FAILED");

        return passed;
    }

    //环境三项检查(不打印总结)
    static boolean checkEnvironment() {

        boolean passed = true;

        passed &= checkLanguage();
        passed &= checkFont();
        passed &= checkAudioSupport();

        return passed;
    }

    //中文界面文案是否齐全
    static boolean checkLanguage() {

        String title = ConstantPool.getPlayer_Title();
        boolean noMissing = Lang.missingKeys().isEmpty();
        boolean chineseText = !Lang.isChinese() || containsChinese(title);

        Logger.logger("language: " + Lang.getCurrent() + " / missing: " + Lang.missingKeys() + " / title: " + title);

        return report("lang zh", noMissing && chineseText);
    }

    //中文字体是否可用
    static boolean checkFont() {

        boolean passed = Fonts.plain(20).canDisplay('中') && Fonts.plain(20).canDisplay('文');

        Logger.logger("font family: " + Fonts.getFamily());

        return report("font cjk", passed);
    }

    //mp3 / ogg 解码支持是否就绪(解码器与 SPI 注册都在 classpath 上才算就绪)
    //注意:AudioSystem.getAudioFileTypes() 只反映"能写"的格式,读支持要看 SPI 注册
    static boolean checkAudioSupport() {

        //mp3:JLayer 解码器
        boolean mp3 = isClassPresent("javazoom.jl.player.Player");
        //ogg:vorbis 解码器(java-vorbis-support 或早期 vorbisspi 的类名都认)
        boolean oggReader = isClassPresent("com.github.trilarion.sound.vorbis.sampled.spi.VorbisAudioFileReader")
                || isClassPresent("javazoom.spi.vorbis.sampled.file.VorbisAudioFileReader");
        boolean oggRegistered = SelfCheck.class.getResource("/META-INF/services/javax.sound.sampled.spi.AudioFileReader") != null;

        Logger.logger("audio decoders: mp3(jlayer)=" + mp3
                + " / ogg(vorbisspi)=" + (oggReader && oggRegistered));

        return report("audio mp3+ogg", mp3 && oggReader && oggRegistered);
    }

    //检查一个场景的元素与语音文件是否齐全
    static boolean checkScene(Scene scene) {

        boolean passed = true;

        Background background = scene.getBackground();
        if (background != null) {
            passed &= report("background " + background.getCode(), background.getFile() != null);
        }

        Music music = scene.getMusic();
        if (music != null) {
            passed &= report("music " + music.getCode(), music.getFile() != null);
        }

        for (Actor actor : scene.getActors()) {
            String text = SceneParser.describeActor(actor);
            passed &= report("actor " + text, actor.getEmotion() != null && actor.getEmotion().getFile() != null);
        }

        //台词语音
        List<Line> lines = scene.getLines().getLines();
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            if (line == null || line.getVoice() == null) {
                continue;
            }
            passed &= report("voice of line " + (i + 1) + " (" + line.getVoice().getCode() + ")", line.getVoice().getFile() != null);
        }

        if (lines.isEmpty()) {
            Logger.logger("    warning: no line in " + scene.getCode());
        }

        return passed;
    }

    //离屏绘制一帧,确认画面不是全黑
    static boolean checkRender(PlayerPanel playerPanel) {

        int width = Math.max(1, playerPanel.getWidth());
        int height = Math.max(1, playerPanel.getHeight());

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        playerPanel.paint(graphics);
        graphics.dispose();

        long nonBlack = 0;
        long total = 0;
        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4) {
                total++;
                if ((canvas.getRGB(x, y) & 0xFFFFFF) != 0) {
                    nonBlack++;
                }
            }
        }

        Logger.logger("    painted samples: " + nonBlack + ConstantPool.getSlash() + total);

        return report("render", nonBlack > total / 100);
    }

    //场景概况:代号、背景、角色顺序、音乐、台词与配音数
    static String describeScene(Scene scene) {

        StringBuilder builder = new StringBuilder();
        builder.append(scene.getCode());
        builder.append("  bg=").append((scene.getBackground() == null) ? "-" : scene.getBackground().getCode());

        builder.append("  cast=");
        if (scene.getActors().isEmpty()) {
            builder.append("-");
        }
        for (int i = 0; i < scene.getActors().size(); i++) {
            if (i > 0) {
                builder.append(ConstantPool.getComma());
            }
            builder.append(SceneParser.describeActor(scene.getActors().get(i)));
        }

        builder.append("  music=").append((scene.getMusic() == null) ? "-" : scene.getMusic().getCode());

        int voiceCount = 0;
        for (Line line : scene.getLines().getLines()) {
            if (line != null && line.getVoice() != null) {
                voiceCount++;
            }
        }
        builder.append("  lines=").append(scene.getLines().getLines().size());
        builder.append("  voices=").append(voiceCount);

        return builder.toString();
    }

    //类是否在 classpath 上
    static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    //是否包含汉字
    static boolean containsChinese(String text) {

        if (text == null) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character >= '\u4e00' && character <= '\u9fa5') {
                return true;
            }
        }

        return false;
    }

    //输出单项自检结果
    static boolean report(String item, boolean passed) {
        Logger.logger("  CHECK [" + item + "] " + (passed ? "OK" : "FAILED"));
        return passed;
    }

}
