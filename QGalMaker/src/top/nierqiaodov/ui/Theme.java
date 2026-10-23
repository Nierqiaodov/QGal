package top.nierqiaodov.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import top.nierqiaodov.util.Fonts;

import javax.swing.*;
import java.awt.*;

//统一视觉规范:FlatLaf 深色主题 + 玫红强调色
//所有窗口都必须先调用 Theme.apply(),再创建任何 Swing 组件
public class Theme {

    //配色:深底 + 玫红强调,适合视觉小说工具的沉浸感
    public static final Color BG = new Color(0x14, 0x12, 0x20);
    public static final Color HEADER = new Color(0x1A, 0x17, 0x26);
    public static final Color SURFACE = new Color(0x21, 0x1E, 0x2E);
    public static final Color SURFACE_HOVER = new Color(0x2B, 0x27, 0x40);
    public static final Color SURFACE_ACTIVE = new Color(0x35, 0x2F, 0x4C);
    public static final Color FIELD = new Color(0x2A, 0x26, 0x38);
    public static final Color BORDER = new Color(0x3B, 0x35, 0x52);
    public static final Color ACCENT = new Color(0xE0, 0x5F, 0x8C);
    public static final Color ACCENT_ALT = new Color(0x7C, 0x6B, 0xF0);
    public static final Color ACCENT_COOL = new Color(0x4F, 0xB6, 0xC9);
    public static final Color TEXT = new Color(0xF3, 0xEF, 0xF8);
    public static final Color TEXT_DIM = new Color(0xAC, 0xA4, 0xBE);
    public static final Color TEXT_FAINT = new Color(0x77, 0x70, 0x8B);
    public static final Color OK = new Color(0x62, 0xC3, 0x8C);
    public static final Color WARN = new Color(0xE8, 0xB9, 0x5A);
    public static final Color ERROR = new Color(0xE8, 0x6B, 0x6B);

    //圆角与间距
    public static final int RADIUS = 14;
    public static final int PAD = 16;

    //是否已经初始化外观
    static boolean applied = false;

    //各类字号
    public static Font titleFont() {
        return Fonts.bold(21);
    }

    public static Font headingFont() {
        return Fonts.bold(15);
    }

    public static Font bodyFont() {
        return Fonts.plain(13);
    }

    public static Font smallFont() {
        return Fonts.plain(12);
    }

    public static Font tinyFont() {
        return Fonts.plain(11);
    }

    //初始化外观(重复调用安全)
    public static void apply() {

        if (applied) {
            return;
        }
        applied = true;

        FlatDarkLaf.setup();

        //整体字体:必须覆盖,保证中文不会被换回没有汉字的默认字体
        UIManager.put("defaultFont", bodyFont());

        //强调色与圆角
        UIManager.put("Component.accentColor", ACCENT);
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.arc", RADIUS);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.innerFocusWidth", 1);

        //基础容器
        UIManager.put("Panel.background", BG);
        UIManager.put("App.color", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", bodyFont());

        //输入类控件
        UIManager.put("TextField.background", FIELD);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("TextField.selectionBackground", ACCENT);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextArea.background", FIELD);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("TextArea.caretForeground", ACCENT);
        UIManager.put("TextArea.selectionBackground", ACCENT);
        UIManager.put("TextArea.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.background", FIELD);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", ACCENT);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.buttonBackground", FIELD);

        //列表
        UIManager.put("List.background", FIELD);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", ACCENT);
        UIManager.put("List.selectionForeground", Color.WHITE);

        //滚动条(细一些)
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

        //弹窗
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", bodyFont());
        UIManager.put("ToolTip.background", SURFACE_HOVER);
        UIManager.put("ToolTip.foreground", TEXT);
    }

}
