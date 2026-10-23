package top.nierqiaodov.ui;

import javax.swing.*;
import java.awt.*;

//圆角卡片容器:统一面板底色与描边
public class RoundedPanel extends JPanel {

    //填充色与描边色
    Color fill;
    Color border;

    public RoundedPanel(LayoutManager layout) {
        this(layout, Theme.SURFACE, Theme.BORDER);
    }

    public RoundedPanel(LayoutManager layout, Color fill, Color border) {
        super(layout);
        this.fill = fill;
        this.border = border;
        setOpaque(false);
    }

    //更换填充色
    public void setFill(Color fill) {
        this.fill = fill;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g.setColor(fill);
        g.fillRoundRect(0, 0, width, height, Theme.RADIUS, Theme.RADIUS);

        if (border != null) {
            g.setColor(border);
            g.drawRoundRect(0, 0, width - 1, height - 1, Theme.RADIUS, Theme.RADIUS);
        }

        g.dispose();
    }

}
