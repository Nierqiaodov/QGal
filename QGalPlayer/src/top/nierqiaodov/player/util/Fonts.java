package top.nierqiaodov.player.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//字体池:优先挑选能显示汉字的字体,保证中文界面与中文台词不会变成方块
//Java 直接指定物理字体时不会自动回退中文,所以这里先按候选表挑选可用的中文字体,
//都不可用时退回逻辑字体 Dialog(逻辑字体本身带中文回退链)
public class Fonts {

    //候选中文字体(Windows / macOS / Linux 常见字体,按观感排序)
    static final String[] CJK_FAMILIES = {
            "Microsoft YaHei UI",
            "Microsoft YaHei",
            "微软雅黑",
            "DengXian",
            "等线",
            "SimHei",
            "黑体",
            "PingFang SC",
            "Hiragino Sans GB",
            "Noto Sans CJK SC",
            "Source Han Sans SC",
            "WenQuanYi Micro Hei",
            "SimSun",
            "宋体"
    };

    //没有中文字体时的兜底逻辑字体
    static final String FALLBACK_FAMILY = "Dialog";

    //用于测试的汉字
    static final char TEST_CHAR = '中';

    //最终选用的字体名
    static final String family = resolveFamily();

    //解析可用的中文字体名
    static String resolveFamily() {

        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));

        for (String candidate : CJK_FAMILIES) {
            if (!available.contains(candidate)) {
                continue;
            }
            //double check:该族确实能显示汉字
            Font font = new Font(candidate, Font.PLAIN, 16);
            if (font.canDisplay(TEST_CHAR) && font.canDisplay('文')) {
                return candidate;
            }
        }

        return FALLBACK_FAMILY;
    }

    //当前选用的字体名
    public static String getFamily() {
        return family;
    }

    //普通字体
    public static Font plain(int size) {
        return new Font(family, Font.PLAIN, size);
    }

    //加粗字体
    public static Font bold(int size) {
        return new Font(family, Font.BOLD, size);
    }

    //指定字号与字重的字体
    public static Font font(int style, int size) {
        return new Font(family, style, size);
    }

}
