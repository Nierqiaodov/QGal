package top.nierqiaodov.player;

import com.formdev.flatlaf.FlatDarkLaf;
import top.nierqiaodov.player.util.Fonts;

import javax.swing.*;
import java.awt.*;

//播放器视觉规范:深色 + 玫红强调,与编辑器保持一致
//播放画面是自绘的,这里的配色同时给 HUD 与弹窗使用
public class Theme {

    //配色
    public static final Color BG = new Color(0x0E, 0x0C, 0x16);
    public static final Color SURFACE = new Color(0x21, 0x1E, 0x2E);
    public static final Color SURFACE_HOVER = new Color(0x2B, 0x27, 0x40);
    public static final Color BORDER = new Color(0x3B, 0x35, 0x52);
    public static final Color ACCENT = new Color(0xE0, 0x5F, 0x8C);
    public static final Color ACCENT_ALT = new Color(0x7C, 0x6B, 0xF0);
    public static final Color ACCENT_COOL = new Color(0x4F, 0xB6, 0xC9);
    public static final Color TEXT = new Color(0xF3, 0xEF, 0xF8);
    public static final Color TEXT_DIM = new Color(0xAC, 0xA4, 0xBE);
    public static final Color ERROR = new Color(0xE8, 0x6B, 0x6B);

    //圆角
    public static final int RADIUS = 16;

    //是否已初始化
    static boolean applied = false;

    //初始化外观(弹窗、下拉框等原生控件统一风格)
    public static void apply() {

        if (applied) {
            return;
        }
        applied = true;

        FlatDarkLaf.setup();

        UIManager.put("defaultFont", Fonts.plain(13));
        UIManager.put("Component.accentColor", ACCENT);
        UIManager.put("Component.arc", RADIUS);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("ToolTip.background", SURFACE_HOVER);
        UIManager.put("ToolTip.foreground", TEXT);
    }

}
