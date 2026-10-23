package top.nierqiaodov.ui;

import top.nierqiaodov.Element.Actor;
import top.nierqiaodov.Element.Background;
import top.nierqiaodov.Element.Line;
import top.nierqiaodov.Element.Lines;
import top.nierqiaodov.Element.Scene;
import top.nierqiaodov.packager.ResourceGenerator;
import top.nierqiaodov.util.ConstantPool;
import top.nierqiaodov.util.Fonts;
import top.nierqiaodov.util.Logger;
import top.nierqiaodov.util.TextWrap;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

//场景实时预览面板:绘制背景、角色立绘与台词框
public class ScenePreviewPanel extends JPanel {

    //当前预览的场景
    Scene scene = new Scene();

    //当前显示的台词序号
    int lineIndex = 0;

    //图片缓存(按文件绝对路径缓存,避免每次重绘都读盘)
    Map<String, BufferedImage> imageCache = new HashMap<>();

    //台词序号变化时的回调(点击画面推进台词时通知界面)
    IntConsumer lineChangedListener;

    //台词框高度
    static final int DIALOGUE_HEIGHT = 150;

    //台词字体与信息条字体(字体池保证中文显示)
    static final Font DIALOGUE_FONT = Fonts.plain(22);
    static final Font INFO_FONT = Fonts.plain(13);

    //语音提示标记字体
    static final Font TAG_FONT = Fonts.bold(13);

    public ScenePreviewPanel() {

        setBackground(Color.BLACK);
        setOpaque(true);

        //点击画面推进到下一句台词,模拟游戏内的推进方式
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                nextLine();
                if (lineChangedListener != null) {
                    lineChangedListener.accept(lineIndex);
                }
            }
        });
    }

    //设置待预览的场景
    public void setScene(Scene scene) {
        this.scene = (scene == null) ? new Scene() : scene;
        repaint();
    }

    //设置当前台词序号(不触发回调,避免与界面互相刷新)
    public void setLineIndex(int lineIndex) {
        this.lineIndex = Math.max(0, lineIndex);
        repaint();
    }

    //读取当前台词序号
    public int getLineIndex() {
        return lineIndex;
    }

    //注册台词序号变化回调
    public void setLineChangedListener(IntConsumer lineChangedListener) {
        this.lineChangedListener = lineChangedListener;
    }

    //清空图片缓存(资源被重新导入后调用)
    public void clearImageCache() {
        imageCache.clear();
    }

    //台词条数
    public int getLineCount() {
        Lines lines = (scene == null) ? null : scene.getLines();
        return (lines == null || lines.getLines() == null) ? 0 : lines.getLines().size();
    }

    //推进到下一句(循环)
    public void nextLine() {
        int count = getLineCount();
        if (count > 0) {
            lineIndex = (lineIndex + 1) % count;
            repaint();
        }
    }

    //回退到上一句(循环)
    public void previousLine() {
        int count = getLineCount();
        if (count > 0) {
            lineIndex = (lineIndex - 1 + count) % count;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics;
        //开启抗锯齿与高质量插值,保证缩放后的立绘不发虚
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        //1.背景(覆盖整个画面)
        paintBackground(g, width, height);
        //2.角色立绘(按从左到右的顺序排列)
        paintActors(g, width, height);
        //3.台词框
        paintDialogue(g, width, height);
        //4.顶部信息条
        paintInfo(g, width);
    }

    //绘制背景:等比缩放并居中裁切,不留黑边
    void paintBackground(Graphics2D g, int width, int height) {

        Background background = (scene == null) ? null : scene.getBackground();
        BufferedImage image = (background == null) ? null : loadImage(background.getFile());

        if (image == null) {
            paintPlaceholder(g, 0, 0, width, height, ConstantPool.getLabel_Background());
            return;
        }

        double scale = Math.max((double) width / image.getWidth(), (double) height / image.getHeight());
        int drawWidth = (int) Math.round(image.getWidth() * scale);
        int drawHeight = (int) Math.round(image.getHeight() * scale);
        g.drawImage(image, (width - drawWidth) / 2, (height - drawHeight) / 2, drawWidth, drawHeight, null);
    }

    //绘制角色立绘:按列表顺序从左到右等分槽位,立绘中心点居中
    void paintActors(Graphics2D g, int width, int height) {

        List<Actor> actors = (scene == null) ? null : scene.getActors();
        if (actors == null || actors.isEmpty()) {
            return;
        }

        //留出台词框占用的高度
        int areaHeight = Math.max(1, height - DIALOGUE_HEIGHT);
        int slotWidth = Math.max(1, width / actors.size());

        for (int i = 0; i < actors.size(); i++) {

            Actor actor = actors.get(i);
            int slotLeft = slotWidth * i;
            File file = (actor == null || actor.getEmotion() == null) ? null : actor.getEmotion().getFile();
            BufferedImage image = loadImage(file);

            if (image == null) {
                //缺图时画虚线占位框,并标出缺失的代号
                int boxWidth = Math.min(slotWidth - 20, 240);
                int boxHeight = Math.min((int) (areaHeight * 0.6), 360);
                paintPlaceholder(g, slotLeft + (slotWidth - boxWidth) / 2, (areaHeight - boxHeight) / 2,
                        boxWidth, boxHeight, ResourceGenerator.describeActor(actor));
                continue;
            }

            //立绘最多占槽位高度的 92%、槽位宽度的 96%,并保持原始比例
            double scale = Math.min((areaHeight * 0.92) / image.getHeight(), (slotWidth * 0.96) / image.getWidth());
            int drawWidth = (int) Math.round(image.getWidth() * scale);
            int drawHeight = (int) Math.round(image.getHeight() * scale);

            //水平方向在槽位内居中(整体即从左到右排列),竖直方向中心点居中
            int drawX = slotLeft + (slotWidth - drawWidth) / 2;
            int drawY = (areaHeight - drawHeight) / 2;

            g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
        }
    }

    //占位框:深色底 + 虚线框 + 居中说明文字
    void paintPlaceholder(Graphics2D g, int x, int y, int width, int height, String text) {

        g.setColor(new Color(24, 24, 24));
        g.fillRect(x, y, width, height);

        g.setColor(new Color(110, 110, 110));
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{8.0f, 8.0f}, 0.0f));
        g.drawRect(x, y, Math.max(1, width - 1), Math.max(1, height - 1));
        g.setStroke(new BasicStroke(1.0f));

        g.setFont(INFO_FONT);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, x + (width - metrics.stringWidth(text)) / 2, y + height / 2 + metrics.getAscent() / 2);
    }

    //当前台词(越界返回 null)
    Line getCurrentLine() {

        Lines lines = (scene == null) ? null : scene.getLines();
        if (lines == null || lines.getLines() == null || lines.getLines().isEmpty()) {
            return null;
        }

        return lines.getLines().get(Math.min(lineIndex, lines.getLines().size() - 1));
    }

    //绘制台词框
    void paintDialogue(Graphics2D g, int width, int height) {

        int top = Math.max(0, height - DIALOGUE_HEIGHT);

        //半透明黑底与分隔线
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, top, width, DIALOGUE_HEIGHT);
        g.setColor(new Color(255, 255, 255, 60));
        g.drawLine(0, top, width, top);

        //台词内容(中英混排自动换行)
        Line currentLine = getCurrentLine();
        String text = (currentLine == null || currentLine.getText() == null || currentLine.getText().isEmpty())
                ? ConstantPool.getMsg_No_Line()
                : currentLine.getText();

        g.setFont(DIALOGUE_FONT);
        g.setColor(Color.WHITE);
        //台词只画在台词框内,避免长台词压到立绘上
        Shape oldClip = g.getClip();
        g.setClip(0, top, width, DIALOGUE_HEIGHT);
        TextWrap.draw(g, text, 30, top + 46, width - 60, DIALOGUE_FONT.getSize() + 12);
        g.setClip(oldClip);

        //这一句配了音时在右上角标出语音代号
        if (currentLine != null && currentLine.getVoice() != null
                && currentLine.getVoice().getCode() != null && !currentLine.getVoice().getCode().isEmpty()) {
            paintVoiceTag(g, width, top, currentLine.getVoice().getCode());
        }

        //右下角显示台词进度
        int count = (scene == null) ? 0 : getLineCount();
        if (count > 0) {
            String counter = (Math.min(lineIndex, count - 1) + 1) + ConstantPool.getSlash() + count;
            g.setFont(INFO_FONT);
            g.setColor(new Color(230, 230, 230));
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(counter, width - 30 - metrics.stringWidth(counter), height - 20);
        }
    }

    //绘制语音标记(台词框右上角)
    void paintVoiceTag(Graphics2D g, int width, int top, String voiceCode) {

        String tag = ConstantPool.getLabel_Voice() + ConstantPool.getColon() + voiceCode;

        g.setFont(TAG_FONT);
        FontMetrics metrics = g.getFontMetrics();
        int tagWidth = metrics.stringWidth(tag) + 16;
        int tagHeight = metrics.getHeight() + 4;

        g.setColor(new Color(90, 30, 30, 190));
        g.fillRoundRect(width - tagWidth - 24, top + 10, tagWidth, tagHeight, 10, 10);
        g.setColor(new Color(255, 220, 220));
        g.drawString(tag, width - tagWidth - 24 + 8, top + 10 + 2 + metrics.getAscent());
    }

    //绘制顶部信息条:场景代号、背景、角色顺序、音乐
    void paintInfo(Graphics2D g, int width) {

        if (scene == null) {
            return;
        }

        String backgroundCode = (scene.getBackground() == null) ? ConstantPool.getNone_Option() : scene.getBackground().getCode();
        String musicCode = (scene.getMusic() == null) ? ConstantPool.getNone_Option() : scene.getMusic().getCode();

        StringBuilder castText = new StringBuilder();
        for (Actor actor : scene.getActors()) {
            if (castText.length() > 0) {
                castText.append(ConstantPool.getComma());
            }
            castText.append(ResourceGenerator.describeActor(actor));
        }
        if (castText.length() == 0) {
            castText.append(ConstantPool.getNone_Option());
        }

        String info = scene.getCode()
                + "   " + ConstantPool.getLabel_Background() + ConstantPool.getColon() + backgroundCode
                + "   " + ConstantPool.getLabel_Cast() + ConstantPool.getColon() + castText
                + "   " + ConstantPool.getLabel_Music() + ConstantPool.getColon() + musicCode;

        g.setFont(INFO_FONT);
        FontMetrics metrics = g.getFontMetrics();
        int barHeight = metrics.getHeight() + 10;

        g.setColor(new Color(0, 0, 0, 130));
        g.fillRect(0, 0, width, barHeight);
        g.setColor(new Color(235, 235, 235));
        g.drawString(info, 12, barHeight - 8);
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
