package top.nierqiaodov.player.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

//多语言文案层:默认中文,支持运行时热切换(-Dqgal.lang=en 或播放界面按下 L)
//用法:常量池里的界面文案字段用 @I18n 标记并写英文原文;中文词条集中在本类的词条表里
public class Lang {

    //语言代码
    public static final String ZH = "zh";
    public static final String EN = "en";

    //当前语言(可热切换)
    static volatile String current = resolveLanguage(System.getProperty("qgal.lang", ZH));

    //中文词条表(键 = 常量池里的常量名)
    static final Map<String, String> ZH_TEXT = Collections.synchronizedMap(new LinkedHashMap<>());

    //缺少中文词条的键(自检用)
    static final Set<String> MISSING_KEYS = Collections.synchronizedSet(new LinkedHashSet<>());

    static {
        //运行信息
        put("Info_1", "QGalPlayer 启动中...");
        put("Info_2", "成品已打开!");
        put("Info_3", " 已清理!");
        put("Info_4", "退出!");
        put("Info_5", "测试入口:播放 GalWork 里的成品!");
        put("Info_6", "播放:");

        //应用信息
        put("Description", "成品播放器");
        put("File_Filter_Qgl", "QGal 成品 (*.qgl)");

        //播放界面
        put("Player_Title", "QGal 播放器");
        put("Player_Auto", "自动");
        put("Player_End", "全剧终");
        put("Player_Hint", "( 点击 / 空格 / → 继续,← 回退,R 重播,A 自动,V 语音连播,L 语言,M 静音,esc 退出 )");
        put("Label_Music", "音乐");
        put("Label_Voice", "语音");
        put("Label_Volume", "音量");
        put("Label_Mute", "静音");
        put("Label_Voice_Auto", "语音连播");

        //弹窗标题
        put("Dialog_Error", "错误");
        put("Dialog_Info", "提示");

        //提示信息
        put("Msg_Package_Not_Found", "成品文件不存在!");
        put("Msg_Package_Invalid", "成品包已损坏:");
        put("Msg_Scene_Folder_Missing", "成品中缺少场景文件夹:");
        put("Msg_Missing_Resource", "资源缺失:");
        put("Msg_Dir_Invalid", "目录无法创建:");
        put("Msg_Qsc_Invalid", "场景文本格式有误:");
        put("Msg_No_Line", "( 暂无台词 )");
        put("Msg_No_Scene", "成品里没有任何场景!");
        put("Msg_Audio_Unsupported", "Java 原生不支持该音频格式,已跳过:");
        put("Msg_Audio_Error", "音频无法播放:");
        put("Msg_Work_Dir_Missing", "找不到 GalWork 目录:");
        put("Msg_Output_Dir_Missing", "找不到 Output 目录:");
        put("Msg_Package_Empty", "没有找到成品,请先在 QGalMaker 里打包:");
        put("Msg_Package_NotFound", "没有找到该成品:");
        put("Msg_Test_Keys", "按键:点击 / 空格 / → 下一句,← 上一句,R 重播,A 自动,V 语音连播,L 语言,M 静音,esc 退出");
    }

    //取当前语言的文案:英文或中文缺词时直接使用传入的英文原文
    public static String text(String key, String english) {

        if (EN.equals(current)) {
            return english;
        }

        String text = ZH_TEXT.get(key);
        if (text == null) {
            if (english != null && !english.isEmpty()) {
                MISSING_KEYS.add(key);
            }
            return english;
        }

        return text;
    }

    //当前语言
    public static String getCurrent() {
        return current;
    }

    //是否中文界面
    public static boolean isChinese() {
        return ZH.equals(current);
    }

    //热切换语言:返回是否真的发生了变化
    //调用方切换之后需要执行 ConstantPool.reload()
    public static boolean setLanguage(String language) {

        String resolved = resolveLanguage(language);
        if (resolved.equals(current)) {
            return false;
        }

        current = resolved;
        return true;
    }

    //在中文与英文之间来回切换,返回切换后的语言
    public static String toggle() {
        setLanguage(isChinese() ? EN : ZH);
        return current;
    }

    //另一种语言的显示名
    public static String otherLanguageName() {
        return isChinese() ? "English" : "中文";
    }

    //缺少中文词条的键(自检用)
    public static Set<String> missingKeys() {
        return MISSING_KEYS;
    }

    //记录一条中文词条
    static void put(String key, String text) {
        ZH_TEXT.put(key, text);
    }

    //把语言参数归一化:以 en 开头视为英文,其余一律中文
    public static String resolveLanguage(String language) {

        if (language == null) {
            return ZH;
        }

        return language.trim().toLowerCase().startsWith("en") ? EN : ZH;
    }

}
