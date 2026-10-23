package top.nierqiaodov.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

//多语言文案层:默认中文,支持运行时热切换(-Dqgal.lang=en 或界面上的语言按钮)
//用法:常量池里的界面文案字段用 @I18n 标记并写英文原文;中文词条集中在本类的词条表里
public class Lang {

    //语言代码
    public static final String ZH = "zh";
    public static final String EN = "en";

    //当前语言(可热切换)
    static volatile String current = resolveLanguage(System.getProperty("qgal.lang", ZH));

    //中文词条表(键 = 常量池里的常量名)
    static final Map<String, String> ZH_TEXT = Collections.synchronizedMap(new LinkedHashMap<>());

    //缺少中文词条的键(自检用,空表表示界面文案已全部中文化)
    static final Set<String> MISSING_KEYS = Collections.synchronizedSet(new LinkedHashSet<>());

    static {
        //启动与日志
        put("Info_1", "QGal 启动中...");
        put("Info_2", "初始化完成!");
        put("Info_3", "界面已启动!");
        put("Info_4", "现在可以开始编排了!");
        put("Info_5", "退出!");
        put("Info_6", " 已创建!");
        put("Info_7", " 已就绪!");
        put("Info_8", " 已就绪!");
        put("Info_9", " 无法创建!");
        put("Info_10", " 已更新!");

        //应用信息与首页
        put("Description", "视觉小说的制作与打包");
        put("Slogan", "制作 · 编排 · 打包");
        put("Add_Resource_Desc", "导入背景、角色、表情、音乐与台词配音");
        put("Create_Scene_Desc", "编排背景、角色站位、台词与配音");
        put("Do_Package_Desc", "按 qsc 工程生成 qgl 成品包");
        put("Home_Status", "工作目录:");
        put("Dialog_Language_Title", "界面语言");

        //主界面
        put("Add_Resource", "添加资源");
        put("Create_Scene", "创建场景");
        put("Do_Package", "打包");

        //资源添加
        put("Add_Background", "添加背景");
        put("Add_Character", "添加角色");
        put("Add_Music", "添加音乐");
        put("Add_Emotion", "添加表情");
        put("Add_Voice", "添加语音");
        put("Add_Background_Desc", "一张场景底图,对应背景代号");
        put("Add_Character_Desc", "角色立绘 + 它的第一个表情");
        put("Add_Emotion_Desc", "给已有角色增加一个新表情");
        put("Add_Music_Desc", "背景音乐,支持 wav / ogg / mp3");
        put("Add_Voice_Desc", "一句台词的配音,支持 wav / ogg / mp3");
        put("Choose_File", "选择文件");
        put("Submit", "提交");
        put("Warning", "警告");
        put("Choose_Background", "请选择背景");
        put("Choose_Music", "请选择音乐");
        put("Choose_Character", "请选择角色");
        put("Choose_Emotion", "请选择表情");
        put("Choose_Voice", "请选择语音");

        //场景编辑器
        put("Scene_Editor", "场景编辑器");
        put("Preview", "预览");
        put("Typing_Lines", "输入台词");
        put("Scene_Save", "保存场景");
        put("Add_A_Character", "添加角色");
        put("Label_Project", "工程");
        put("Label_Scene_Code", "场景代号");
        put("Label_Background", "背景");
        put("Label_Character", "角色");
        put("Label_Emotion", "表情");
        put("Label_Music", "音乐");
        put("Label_Cast", "角色编排");
        put("Label_Lines", "台词");
        put("Label_Voice", "语音");
        put("New_Project", "新建工程");
        put("New_Scene", "新建场景");
        put("Load_Scene", "读取场景");
        put("Do_Add", "添加");
        put("Do_Modify", "修改");
        put("Do_Remove", "删除");
        put("Do_Up", "上移");
        put("Do_Down", "下移");
        put("Do_Previous", "上一句");
        put("Do_Next", "下一句");
        put("Do_Preview_Voice", "试听语音");
        put("Do_Preview_Music", "试听音乐");
        put("None_Option", "无");
        put("Back", "返回");

        //弹窗标题
        put("Dialog_Warning", "警告");
        put("Dialog_Error", "错误");
        put("Dialog_Info", "提示");

        //提示信息
        put("Msg_Choose_File", "未选择文件,资源没有添加!");
        put("Msg_Code_Empty", "代号不能为空!");
        put("Msg_Code_Illegal", "代号只能包含字母、数字、下划线或汉字!");
        put("Msg_Source_Invalid", "源文件无效!");
        put("Msg_Target_Invalid", "目标目录无效!");
        put("Msg_Dir_Invalid", "目录无法创建!");
        put("Msg_Import_Done", "资源已添加:");
        put("Msg_No_Resource", "没有找到资源,请先添加资源!");
        put("Msg_No_Emotion", "该角色还没有表情!");
        put("Msg_No_Voice", "还没有添加语音资源!");
        put("Msg_Scene_Saved", "场景已保存:");
        put("Msg_Scene_Not_Found", "工程中找不到该场景!");
        put("Msg_Scene_Empty", "这个场景还没有添加任何内容!");
        put("Msg_Project_Not_Found", "工程文件不存在!");
        put("Msg_Project_Empty", "工程里还没有任何场景!");
        put("Msg_Project_Name", "请输入新工程代号:");
        put("Msg_Choose_Project", "请先选择一个工程!");
        put("Msg_No_Project", "没有找到工程,请先创建一个场景!");
        put("Msg_Line_Empty", "台词不能为空!");
        put("Msg_No_Line", "( 暂无台词 )");
        put("Msg_Cast_Empty", "请先选择角色和表情!");
        put("Msg_Package_Done", "打包完成:");
        put("Msg_Duplicated_Code", " 重复了,已使用第一个:");
        put("Msg_Missing_Resource", "资源缺失:");
        put("Msg_Qsc_Invalid", "场景文本格式有误:");
        put("Msg_Audio_Unsupported", "Java 原生不支持该音频格式,已跳过:");
        put("Msg_Audio_Error", "音频无法播放:");
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
    //调用方切换之后需要执行 ConstantPool.reload() 并重建界面
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

    //另一种语言的显示名(给语言切换按钮用)
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
