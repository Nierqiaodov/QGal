package top.nierqiaodov.player;

import top.nierqiaodov.player.Element.Actor;
import top.nierqiaodov.player.Element.Background;
import top.nierqiaodov.player.Element.Line;
import top.nierqiaodov.player.Element.Lines;
import top.nierqiaodov.player.Element.Scene;
import top.nierqiaodov.player.util.ConstantPool;
import top.nierqiaodov.player.util.Fonts;
import top.nierqiaodov.player.util.Lang;
import top.nierqiaodov.player.util.Logger;
import top.nierqiaodov.player.util.TextWrap;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//播放画面:绘制背景、角色立绘与台词框,并负责台词、语音与场景的推进
public class PlayerPanel extends JPanel {

    //场景列表(按工程文件中的顺序)
    List<Scene> scenes = new ArrayList<>();

    //当前场景序号与台词序号
    int sceneIndex = 0;
    int lineIndex = 0;

    //是否已播放到结尾
    boolean finished = false;

    //自动播放开关与定时器
    boolean autoPlay = false;
    Timer autoTimer;

    //语音连播:一句配音播完自动进入下一句
    boolean voiceAdvance = true;

    //语音令牌:切换台词后旧语音的回调自动失效
    int voiceToken = 0;

    //音频播放器(背景音乐循环 + 台词语音单次 + 音量/静音)
    AudioPlayer audioPlayer = new AudioPlayer();

    //当前场景音乐文件
    File currentMusicFile;

    //图片缓存(按文件路径缓存,避免每帧重绘都读盘)
    Map<String, BufferedImage> imageCache = new HashMap<>();

    //进度变化回调(用于刷新窗口标题)
    Runnable progressListener;

    public PlayerPanel() {

        setBackground(Theme.BG);
        setOpaque(true);

        //自动播放:每 2.5 秒推进一句
        autoTimer = new Timer(2500, event -> {
            if (!finished) {
                next();
            }
        });
        autoTimer.setRepeats(true);

        //点击画面推进台词,播放结束后点击重新开始
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (finished) {
                    restart();
                } else {
                    next();
                }
            }
        });
    }

    //载入拆包结果
    public void load(QGalPackage qGalPackage) {

        scenes = new ArrayList<>(qGalPackage.getScenes());
        sceneIndex = 0;
        lineIndex = 0;
        finished = scenes.isEmpty();
        currentMusicFile = null;
        playSceneMusic();
        playLineVoice();
        notifyProgress();
        repaint();
    }

    //注册进度回调
    public void setProgressListener(Runnable progressListener) {
        this.progressListener = progressListener;
        notifyProgress();
    }

    //当前场景
    Scene getCurrentScene() {
        return (sceneIndex < 0 || sceneIndex >= scenes.size()) ? null : scenes.get(sceneIndex);
    }

    //当前场景的台词列表
    List<Line> getCurrentLines() {
        Scene scene = getCurrentScene();
        Lines lines = (scene == null) ? null : scene.getLines();
        return (lines == null || lines.getLines() == null) ? new ArrayList<>() : lines.getLines();
    }

    //当前台词(越界返回 null)
    Line getCurrentLine() {

        List<Line> lines = getCurrentLines();
        if (lines.isEmpty()) {
            return null;
        }

        return lines.get(Math.min(lineIndex, lines.size() - 1));
    }

    //当前台词文本
    public String getCurrentLineText() {

        Line line = getCurrentLine();

        return (line == null || line.getText() == null) ? ConstantPool.getVoid_String() : line.getText();
    }

    //推进:当前场景台词未播完就下一句,否则进入下一场景
    public void next() {

        if (scenes.isEmpty()) {
            return;
        }

        List<Line> lines = getCurrentLines();
        if (lineIndex + 1 < lines.size()) {
            lineIndex++;
        } else if (sceneIndex + 1 < scenes.size()) {
            sceneIndex++;
            lineIndex = 0;
            playSceneMusic();
        } else {
            finished = true;
            if (autoTimer.isRunning()) {
                autoTimer.stop();
            }
        }

        if (finished) {
            audioPlayer.stopVoice();
        } else {
            playLineVoice();
        }

        notifyProgress();
        repaint();
    }

    //回退一句或回到上一个场景
    public void previous() {

        if (scenes.isEmpty()) {
            return;
        }

        finished = false;

        if (lineIndex > 0) {
            lineIndex--;
        } else if (sceneIndex > 0) {
            sceneIndex--;
            //回到上一个场景的最后一句
            lineIndex = Math.max(0, getCurrentLines().size() - 1);
            playSceneMusic();
        }

        playLineVoice();
        notifyProgress();
        repaint();
    }

    //从头重新播放
    public void restart() {

        sceneIndex = 0;
        lineIndex = 0;
        finished = false;
        currentMusicFile = null;
        playSceneMusic();
        playLineVoice();
        notifyProgress();
        repaint();
    }

    //切换自动播放,返回切换后的状态
    public boolean toggleAutoPlay() {

        autoPlay = !autoPlay;
        if (autoPlay && !finished) {
            autoTimer.start();
        } else {
            autoTimer.stop();
        }
        repaint();

        return autoPlay;
    }

    //切换语音连播,返回切换后的状态
    public boolean toggleVoiceAdvance() {

        voiceAdvance = !voiceAdvance;
        repaint();

        return voiceAdvance;
    }

    //加减音量(供快捷键使用),返回调整后的音量
    public float changeVolume(float delta) {
        float volume = audioPlayer.changeVolume(delta);
        repaint();
        return volume;
    }

    //切换静音,返回切换后的静音状态
    public boolean toggleMute() {
        boolean muted = audioPlayer.toggleMute();
        repaint();
        return muted;
    }

    //切换界面语言(文字每帧现取,重载常量池后重绘即可)
    public void toggleLanguage() {

        Lang.toggle();
        ConstantPool.reload();
        notifyProgress();
        repaint();
    }

    //停止定时器与音频(关闭窗口时调用)
    public void stopAudio() {
        autoTimer.stop();
        audioPlayer.stopAll();
    }

    //播放当前场景的背景音乐(与当前一致时不重播)
    void playSceneMusic() {

        Scene scene = getCurrentScene();
        File musicFile = (scene == null || scene.getMusic() == null) ? null : scene.getMusic().getFile();

        if (musicFile == null) {
            if (currentMusicFile != null) {
                audioPlayer.stopBackground();
                currentMusicFile = null;
            }
            return;
        }

        if (musicFile.equals(currentMusicFile)) {
            return;
        }

        currentMusicFile = musicFile;
        audioPlayer.playBackground(musicFile);
    }

    //播放当前台词的语音;播完(且没有切到别的句子)时按开关自动推进
    void playLineVoice() {

        Line line = getCurrentLine();
        File voiceFile = (line == null || line.getVoice() == null) ? null : line.getVoice().getFile();

        //每次换台词都换令牌,旧语音的回调自动作废
        final int token = ++voiceToken;

        if (voiceFile == null) {
            audioPlayer.stopVoice();
            return;
        }

        audioPlayer.playVoice(voiceFile, () -> onVoiceFinished(token));
    }

    //语音播放结束:仍在同一句且开启了连播时,稍作停顿后推进
    void onVoiceFinished(int token) {

        if (!voiceAdvance || finished) {
            return;
        }

        SwingUtilities.invokeLater(() -> {

            if (token != voiceToken || finished) {
                return;
            }

            Timer delay = new Timer(380, event -> {
                if (token == voiceToken && !finished) {
                    next();
                }
            });
            delay.setRepeats(false);
            delay.start();
        });
    }

    //通知进度变化
    void notifyProgress() {
        if (progressListener != null) {
            progressListener.run();
        }
    }

    //当前进度描述(窗口标题用)
    public String describeProgress() {

        Scene scene = getCurrentScene();
        if (scene == null) {
            return ConstantPool.getPlayer_End();
        }

        int count = getCurrentLines().size();
        int current = Math.min(lineIndex + 1, Math.max(1, count));

        return scene.getCode() + "  " + current + ConstantPool.getSlash() + count;
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        int dialogueHeight = Math.max(120, height / 4 + 10);
        int stageHeight = Math.max(1, height - dialogueHeight);

        //1.背景铺满整个画面
        paintBackground(g, width, height);
        //2.角色立绘(站台区域)
        paintActors(g, width, stageHeight);
        //3.台词框
        paintDialogue(g, width, height, dialogueHeight);
        //4.右上角状态标签
        paintHud(g, width);
        //5.结束画面
        if (finished) {
            paintFinished(g, width, height);
        }
    }

    //绘制背景:等比缩放居中裁切,铺满画面
    void paintBackground(Graphics2D g, int width, int height) {

        Background background = (getCurrentScene() == null) ? null : getCurrentScene().getBackground();
        BufferedImage image = (background == null) ? null : loadImage(background.getFile());

        if (image == null) {
            g.setColor(Theme.BG);
            g.fillRect(0, 0, width, height);
            return;
        }

        double scale = Math.max((double) width / image.getWidth(), (double) height / image.getHeight());
        int drawWidth = (int) Math.round(image.getWidth() * scale);
        int drawHeight = (int) Math.round(image.getHeight() * scale);
        g.drawImage(image, (width - drawWidth) / 2, (height - drawHeight) / 2, drawWidth, drawHeight, null);
    }

    //绘制角色立绘:按顺序从左到右等分槽位,立绘中心点居中
    void paintActors(Graphics2D g, int width, int stageHeight) {

        Scene scene = getCurrentScene();
        List<Actor> actors = (scene == null) ? null : scene.getActors();
        if (actors == null || actors.isEmpty()) {
            return;
        }

        int slotWidth = Math.max(1, width / actors.size());

        for (int i = 0; i < actors.size(); i++) {

            Actor actor = actors.get(i);
            File file = (actor == null || actor.getEmotion() == null) ? null : actor.getEmotion().getFile();
            BufferedImage image = loadImage(file);
            int slotLeft = slotWidth * i;

            if (image == null) {
                //缺图时画一个暗色占位,保证播放不中断
                g.setColor(new Color(36, 36, 44));
                int boxWidth = (int) (slotWidth * 0.6);
                int boxHeight = (int) (stageHeight * 0.7);
                g.fillRoundRect(slotLeft + (slotWidth - boxWidth) / 2, (stageHeight - boxHeight) / 2,
                        boxWidth, boxHeight, 16, 16);
                continue;
            }

            double scale = Math.min((stageHeight * 0.95) / image.getHeight(), (slotWidth * 0.96) / image.getWidth());
            int drawWidth = (int) Math.round(image.getWidth() * scale);
            int drawHeight = (int) Math.round(image.getHeight() * scale);

            int drawX = slotLeft + (slotWidth - drawWidth) / 2;
            int drawY = (stageHeight - drawHeight) / 2;

            g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
        }
    }

    //绘制台词框:圆角半透明卡片 + 顶部强调线
    void paintDialogue(Graphics2D g, int width, int height, int dialogueHeight) {

        int margin = Math.max(18, width / 40);
        int boxWidth = width - margin * 2;
        int boxHeight = dialogueHeight - margin / 2;
        int boxX = margin;
        int boxY = height - boxHeight - margin / 2;

        //底部渐变,让立绘与台词框自然过渡
        g.setPaint(new GradientPaint(0, Math.max(0, boxY - 60), new Color(0, 0, 0, 0),
                0, boxY + 40, new Color(0, 0, 0, 150)));
        g.fillRect(0, Math.max(0, boxY - 60), width, Math.min(height, 100));
        g.setPaint(null);

        //卡片底与描边
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, Theme.RADIUS + 4, Theme.RADIUS + 4);
        g.setColor(new Color(255, 255, 255, 28));
        g.drawRoundRect(boxX, boxY, boxWidth - 1, boxHeight - 1, Theme.RADIUS + 4, Theme.RADIUS + 4);

        //顶部强调线
        g.setColor(Theme.ACCENT);
        g.fillRoundRect(boxX + 18, boxY, Math.max(48, boxWidth / 6), 3, 3, 3);

        //文字
        int fontSize = Math.max(15, width / 48);
        int textX = boxX + 26;
        int textY = boxY + fontSize + 26;

        g.setFont(Fonts.plain(fontSize));
        g.setColor(Theme.TEXT);

        String text = getCurrentLineText();
        if (text.isEmpty()) {
            text = ConstantPool.getMsg_No_Line();
        }

        Shape oldClip = g.getClip();
        g.setClip(boxX, boxY, boxWidth, boxHeight);
        TextWrap.draw(g, text, textX, textY, boxWidth - 52, (int) (fontSize * 1.6));
        g.setClip(oldClip);

        //这句配了音时,在台词框右上角标出语音代号
        Line currentLine = getCurrentLine();
        if (currentLine != null && currentLine.getVoice() != null
                && currentLine.getVoice().getCode() != null && !currentLine.getVoice().getCode().isEmpty()) {
            String tag = ConstantPool.getLabel_Voice() + ConstantPool.getColon() + currentLine.getVoice().getCode();
            paintPillRight(g, tag, boxX + boxWidth - 16, boxY + 16,
                    new Color(0x8A, 0x2F, 0x4E, 210), new Color(0xFF, 0xDF, 0xE6));
        }

        //左下角操作提示,右下角进度
        Font infoFont = Fonts.plain(Math.max(10, width / 105));
        g.setFont(infoFont);
        FontMetrics metrics = g.getFontMetrics();

        g.setColor(Theme.TEXT_DIM);
        g.drawString(ConstantPool.getPlayer_Hint(), textX, boxY + boxHeight - 14);

        String counter = describeProgress();
        g.setColor(Theme.TEXT);
        g.drawString(counter, boxX + boxWidth - 24 - metrics.stringWidth(counter), boxY + boxHeight - 14);
    }

    //右上角状态标签:自动 / 语音连播 / 音量
    void paintHud(Graphics2D g, int width) {

        int right = width - 18;
        int top = 16;

        //音量(静音时显示 MUTE)
        String volumeText = audioPlayer.isMuted()
                ? ConstantPool.getLabel_Mute()
                : ConstantPool.getLabel_Volume() + " " + Math.round(audioPlayer.effectiveVolume() * 100) + ConstantPool.getPercent_Sign();
        Color volumeFill = audioPlayer.isMuted() ? new Color(0x8A, 0x2F, 0x2F, 210) : new Color(0, 0, 0, 150);
        right -= paintPillRight(g, volumeText, right, top, volumeFill, Theme.TEXT);

        //语音连播
        if (voiceAdvance) {
            right -= paintPillRight(g, ConstantPool.getLabel_Voice_Auto(), right, top,
                    new Color(Theme.ACCENT_ALT.getRed(), Theme.ACCENT_ALT.getGreen(), Theme.ACCENT_ALT.getBlue(), 200), Color.WHITE);
        }

        //自动播放
        if (autoPlay) {
            right -= paintPillRight(g, ConstantPool.getPlayer_Auto(), right, top,
                    new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 210), Color.WHITE);
        }
    }

    //画一个右对齐的胶囊标签,返回它占用的宽度(含间距)
    int paintPillRight(Graphics2D g, String text, int right, int top, Color fill, Color foreground) {

        g.setFont(Fonts.plain(Math.max(10, getWidth() / 105)));
        FontMetrics metrics = g.getFontMetrics();

        int textWidth = metrics.stringWidth(text);
        int pillWidth = textWidth + 18;
        int pillHeight = metrics.getHeight() + 6;
        int x = right - pillWidth;

        g.setColor(fill);
        g.fillRoundRect(x, top, pillWidth, pillHeight, pillHeight, pillHeight);
        g.setColor(foreground);
        g.drawString(text, x + 9, top + 3 + metrics.getAscent());

        return pillWidth + 8;
    }

    //绘制结束画面
    void paintFinished(Graphics2D g, int width, int height) {

        g.setColor(new Color(0, 0, 0, 185));
        g.fillRect(0, 0, width, height);

        int fontSize = Math.max(22, width / 20);
        g.setFont(Fonts.bold(fontSize));
        g.setColor(Theme.TEXT);

        FontMetrics metrics = g.getFontMetrics();
        String text = ConstantPool.getPlayer_End();
        g.drawString(text, (width - metrics.stringWidth(text)) / 2, height / 2 + metrics.getAscent() / 2);

        //强调线
        int lineWidth = Math.max(60, fontSize * 2);
        g.setColor(Theme.ACCENT);
        g.fillRoundRect((width - lineWidth) / 2, height / 2 + 12, lineWidth, 3, 3, 3);

        g.setFont(Fonts.plain(Math.max(11, width / 85)));
        g.setColor(Theme.TEXT_DIM);
        FontMetrics hintMetrics = g.getFontMetrics();
        String hint = ConstantPool.getPlayer_Hint();
        g.drawString(hint, (width - hintMetrics.stringWidth(hint)) / 2, height / 2 + fontSize + 34);
    }

    //按文件读取图片并缓存,读取失败返回 null
    BufferedImage loadImage(File file) {

        if (file == null || !file.isFile()) {
            return null;
        }

        String path = file.getAbsolutePath();
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException exception) {
            Logger.error(exception.getMessage());
        }
        imageCache.put(path, image);

        return image;
    }

}
