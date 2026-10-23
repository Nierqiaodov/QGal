package top.nierqiaodov.ui;

import top.nierqiaodov.util.ConstantPool;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

//界面控件工厂:统一按钮、标签、卡片、窗口与弹窗的样式
public class UiBuilder {

    //常用字体快捷入口(场景编辑器等地方会用到)
    static Font buttonFont = Theme.bodyFont();
    static Font labelFont = Theme.bodyFont();

    //强调色实心按钮
    public static JButton primaryButton(String text) {
        return style(new JButton(text), Theme.ACCENT, Color.WHITE);
    }

    //描边次要按钮
    public static JButton secondaryButton(String text) {
        return style(new JButton(text), Theme.SURFACE_HOVER, Theme.TEXT);
    }

    //无背景幽灵按钮
    public static JButton ghostButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.smallFont());
        button.setForeground(Theme.TEXT_DIM);
        button.putClientProperty("JButton.buttonType", "borderless");
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusable(false);
        return button;
    }

    //小号次要按钮(场景编辑器里的操作按钮)
    public static JButton smallButton(String text) {
        return smallButton(text, 96);
    }

    //指定宽度的小号次要按钮
    public static JButton smallButton(String text, int width) {

        JButton button = secondaryButton(text);
        button.setFont(Theme.smallFont());

        Dimension size = new Dimension(width, 30);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);

        return button;
    }

    //统一的按钮外观
    static JButton style(JButton button, Color background, Color foreground) {

        button.setFont(Theme.bodyFont());
        button.setBackground(background);
        button.setForeground(foreground);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(6, 16, 6, 16));

        return button;
    }

    //首页 / 资源页的大号卡片按钮
    public static MenuCard menuCard(String title, String subtitle, Color accent) {
        return new MenuCard(title, subtitle, accent);
    }

    //正文标签
    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.bodyFont());
        label.setForeground(Theme.TEXT);
        return label;
    }

    //次要标签
    public static JLabel dimLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.smallFont());
        label.setForeground(Theme.TEXT_DIM);
        return label;
    }

    //分组标题(带主色竖条)
    public static JLabel sectionTitle(String text) {

        JLabel label = new JLabel("  " + text);
        label.setFont(Theme.headingFont());
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setIcon(new AccentBar());

        return label;
    }

    //主标题
    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.titleFont());
        label.setForeground(Theme.TEXT);
        return label;
    }

    //圆角卡片容器
    public static RoundedPanel card(LayoutManager layout) {
        return new RoundedPanel(layout);
    }

    //统一风格的输入框
    public static JTextField textField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(Theme.bodyFont());
        field.putClientProperty("JComponent.roundRect", Boolean.TRUE);
        return field;
    }

    //统一下拉框
    public static JComboBox<String> comboBox(String[] items) {

        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(Theme.bodyFont());

        Dimension size = new Dimension(210, 30);
        comboBox.setPreferredSize(size);
        comboBox.setMinimumSize(size);
        comboBox.setMaximumSize(size);
        comboBox.putClientProperty("JComponent.roundRect", Boolean.TRUE);

        return comboBox;
    }

    //把若干控件左对齐排成一行
    public static JPanel row(Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        panel.setOpaque(false);
        for (Component component : components) {
            panel.add(component);
        }
        return panel;
    }

    //把控件水平居中
    public static JPanel center(Component component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        panel.add(component);
        return panel;
    }

    //顶栏:左侧标题 + 右侧若干控件
    public static JPanel header(String titleText, JComponent... rightSide) {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(new EmptyBorder(12, 20, 12, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(title(titleText));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        for (JComponent component : rightSide) {
            right.add(component);
        }

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    //统一风格窗口(居中、不可缩放)
    public static JFrame frame(String titleText, int width, int height) {

        JFrame frame = new JFrame(titleText);
        frame.setSize(width, height);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setBackground(Theme.BG);

        return frame;
    }

    //警告弹窗
    public static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, ConstantPool.getDialog_Warning(), JOptionPane.WARNING_MESSAGE);
    }

    //错误弹窗
    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, ConstantPool.getDialog_Error(), JOptionPane.ERROR_MESSAGE);
    }

    //提示弹窗
    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, ConstantPool.getDialog_Info(), JOptionPane.INFORMATION_MESSAGE);
    }

    //确认弹窗
    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, ConstantPool.getDialog_Info(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    //文本输入弹窗(取消时返回 null)
    public static String input(Component parent, String message, String defaultValue) {
        return (String) JOptionPane.showInputDialog(parent, message, ConstantPool.getDialog_Info(),
                JOptionPane.PLAIN_MESSAGE, null, null, defaultValue);
    }

    //分组标题左侧的强调竖条
    static class AccentBar implements Icon {

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(Theme.ACCENT);
            g.fillRoundRect(x, y + 2, 3, getIconHeight() - 4, 3, 3);

            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return 6;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

}
