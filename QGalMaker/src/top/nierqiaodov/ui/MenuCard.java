package top.nierqiaodov.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

//大号卡片按钮:左侧强调条 + 标题 + 说明,首页与资源页用
public class MenuCard extends JButton {

    //说明文字(为空时只显示标题,高度也会变矮)
    String subtitle;

    //强调条颜色
    Color accentBar = Theme.ACCENT;

    //悬停 / 按下状态(自己维护,便于整卡重绘)
    boolean hover = false;
    boolean press = false;

    public MenuCard(String title, String subtitle, Color accentBar) {

        super(title);

        this.subtitle = (subtitle == null) ? "" : subtitle;
        this.accentBar = (accentBar == null) ? Theme.ACCENT : accentBar;

        setFont(Theme.headingFont());
        setForeground(Theme.TEXT);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hover = false;
                press = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent event) {
                press = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                press = false;
                repaint();
            }
        });
    }

    public MenuCard(String title, String subtitle) {
        this(title, subtitle, Theme.ACCENT);
    }

    //改说明文字(语言热切换后重建卡片时用)
    public void setSubtitle(String subtitle) {
        this.subtitle = (subtitle == null) ? "" : subtitle;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        int height = subtitle.isEmpty() ? 56 : 80;
        return new Dimension(560, height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(560, getPreferredSize().height);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        //卡片底与描边
        Color fill = press ? Theme.SURFACE_ACTIVE : (hover ? Theme.SURFACE_HOVER : Theme.SURFACE);
        g.setColor(fill);
        g.fillRoundRect(0, 0, width, height, Theme.RADIUS, Theme.RADIUS);
        g.setColor(hover ? accentBar.darker() : Theme.BORDER);
        g.drawRoundRect(0, 0, width - 1, height - 1, Theme.RADIUS, Theme.RADIUS);

        //左侧强调条
        g.setColor(press ? accentBar.brighter() : accentBar);
        g.fillRoundRect(0, 10, 5, height - 20, 5, 5);

        int textX = Theme.PAD + 6;

        //标题
        g.setFont(Theme.headingFont());
        g.setColor(Theme.TEXT);
        FontMetrics titleMetrics = g.getFontMetrics();
        int titleBaseline;
        if (subtitle.isEmpty()) {
            titleBaseline = (height + titleMetrics.getAscent() - titleMetrics.getDescent()) / 2;
        } else {
            titleBaseline = 12 + titleMetrics.getAscent();
        }
        g.drawString(getText(), textX, titleBaseline);

        //说明
        if (!subtitle.isEmpty()) {
            g.setFont(Theme.smallFont());
            g.setColor(Theme.TEXT_DIM);
            g.drawString(subtitle, textX, titleBaseline + 21);
        }

        g.dispose();
    }

}
