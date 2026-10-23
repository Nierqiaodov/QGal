package top.nierqiaodov.util;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

//中英混排的自动换行工具
//中文按字断行,英文单词整体不拆断,行首不出现逗号句号等标点(挂在上行末尾)
public class TextWrap {

    //不能出现在行首的标点(中英文)
    static final String NO_LINE_START = "，。、；：？！）】》」』’”·,.;:?!)]}%";

    //把文本按最大宽度切成多行(保留原文里的手工换行)
    public static List<String> wrap(String text, FontMetrics metrics, int maxWidth) {

        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        for (String paragraph : text.split("\n", -1)) {
            wrapParagraph(paragraph, metrics, maxWidth, result);
        }

        return result;
    }

    //按最大宽度绘制自动换行文本,返回绘制结束后的基线 y
    public static int draw(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {

        int drawY = y;

        for (String line : wrap(text, g.getFontMetrics(), maxWidth)) {
            g.drawString(line, x, drawY);
            drawY += lineHeight;
        }

        return drawY;
    }

    //处理单个段落(不含手工换行)
    static void wrapParagraph(String paragraph, FontMetrics metrics, int maxWidth, List<String> result) {

        if (paragraph.isEmpty()) {
            result.add(paragraph);
            return;
        }

        StringBuilder line = new StringBuilder();
        int cursor = 0;

        while (cursor < paragraph.length()) {

            //切出一个不可分块:连续的西文/数字算一个词,其余按单字处理
            int end = cursor + 1;
            if (isLatin(paragraph.charAt(cursor))) {
                while (end < paragraph.length() && isLatin(paragraph.charAt(end))) {
                    end++;
                }
            }
            String token = paragraph.substring(cursor, end);

            //放不下时先结算当前行
            if (line.length() > 0 && metrics.stringWidth(line + token) > maxWidth) {
                //标点不能出现在行首,直接挂在本行末尾
                if (isNoLineStart(token.charAt(0))) {
                    line.append(token);
                    result.add(trimEnd(line));
                    line.setLength(0);
                    cursor = end;
                    continue;
                }
                result.add(trimEnd(line));
                line.setLength(0);
                //新行不以空白开头
                if (isBlank(token.charAt(0))) {
                    cursor = end;
                    continue;
                }
            }

            //单个词本身就超宽时按字符硬断
            while (metrics.stringWidth(token) > maxWidth && token.length() > 1) {
                int cut = 1;
                while (cut < token.length() && metrics.stringWidth(token.substring(0, cut + 1)) <= maxWidth) {
                    cut++;
                }
                result.add(token.substring(0, cut));
                token = token.substring(cut);
            }

            line.append(token);
            cursor = end;
        }

        if (line.length() > 0) {
            result.add(trimEnd(line));
        }
    }

    //去掉行尾空白
    static String trimEnd(StringBuilder line) {

        int end = line.length();
        while (end > 0 && isBlank(line.charAt(end - 1))) {
            end--;
        }

        return line.substring(0, end);
    }

    //是否为空白字符
    static boolean isBlank(char character) {
        return character == ' ' || character == '\t';
    }

    //是否为西文字母或数字(这些字符组成的词不拆断)
    static boolean isLatin(char character) {
        return (character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z')
                || (character >= '0' && character <= '9')
                || character == '\'' || character == '-';
    }

    //是否为不能出现在行首的标点
    static boolean isNoLineStart(char character) {
        return NO_LINE_START.indexOf(character) >= 0;
    }

}
