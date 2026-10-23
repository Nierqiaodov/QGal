package top.nierqiaodov.player.util;

import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//常量池(播放器侧:只包含成品格式与播放界面需要的常量,与编辑器互不依赖)
//界面文案字段用 @I18n 标记,字段名就是 Lang 里的词条键:启动时按当前语言填充一次,
//语言热切换时由 reload() 重新填充
@Data
public class ConstantPool {

    //运行信息
    @Getter
    @I18n
    static String Info_1 = "Starting QGalPlayer!";
    @Getter
    @I18n
    static String Info_2 = "Package opened!";
    @Getter
    @I18n
    static String Info_3 = " removed!";
    @Getter
    @I18n
    static String Info_4 = "EXIT!";

    //应用信息
    @Getter
    final static String Application = "Q GalPlayer";
    @Getter
    @I18n
    static String Description = "Package Player";
    //版本号来自 Maven 构建信息,不再写死
    @Getter
    final static String Version = BuildInfo.version();
    @Getter
    final static String Date_Format = "HH:mm:ss";
    //日志前缀(时间戳由 Logger 每条重新生成)
    @Getter
    final static String Log_Prefix = "[QGalPlayer]";

    //成品格式:包内结构
    @Getter
    final static String Package_Header = "QGal Package";
    @Getter
    final static String Package_Project_File = "project.qsc";
    @Getter
    final static String Package_Manifest_File = "manifest.txt";
    @Getter
    final static String Package_Scene_File = "scene.qsc";
    @Getter
    final static String Package_Scene_Text = "scene.txt";
    @Getter
    final static String Package_Background_Dir = "Background/";
    @Getter
    final static String Package_Character_Dir = "Character/";
    @Getter
    final static String Package_Music_Dir = "Music/";
    @Getter
    final static String Package_Voice_Dir = "Voice/";
    //文件名连接符: 角色=序号_角色代号_表情代号,语音=台词序号_语音代号
    @Getter
    final static String Package_Name_Sign = "_";

    //qsc 语法符号: s1{"b1";"c1:e1,c2:e1";"m1";["l1","l2"];["v1",""]}
    @Getter
    final static String Brace_Left = "{";
    @Getter
    final static String Brace_Right = "}";
    @Getter
    final static String Bracket_Left = "[";
    @Getter
    final static String Bracket_Right = "]";
    @Getter
    final static String Quote = "\"";
    @Getter
    final static String Semicolon = ";";
    @Getter
    final static String Comma = ",";
    @Getter
    final static String Colon = ":";
    @Getter
    final static String Escape = "\\";
    //工程文件中的注释符号
    @Getter
    final static String Comment_Sign = "#";

    //文件名与后缀
    @Getter
    final static String Last_Dot_Index = ".";
    @Getter
    final static String File_Name_Suffix = "";
    @Getter
    final static String Qsc_Suffix = ".qsc";
    @Getter
    final static String Qgl_Suffix = ".qgl";
    //文件选择过滤器用到的后缀(不含点)
    @Getter
    final static String Qgl_Extension = "qgl";
    @Getter
    @I18n
    static String File_Filter_Qgl = "QGal package (*.qgl)";

    //播放界面文案
    @Getter
    @I18n
    static String Player_Title = "QGal Player";
    @Getter
    @I18n
    static String Player_Auto = "AUTO";
    @Getter
    @I18n
    static String Player_End = "END";
    @Getter
    @I18n
    static String Player_Hint = "( click / space / RIGHT to continue, LEFT to back, R to restart, esc to exit )";
    @Getter
    @I18n
    static String Label_Music = "MUSIC";
    @Getter
    @I18n
    static String Label_Voice = "VOICE";
    @Getter
    @I18n
    static String Label_Volume = "VOLUME";
    @Getter
    @I18n
    static String Label_Mute = "MUTE";
    @Getter
    @I18n
    static String Label_Voice_Auto = "VOICE AUTO";
    @Getter
    final static String Void_String = "";
    @Getter
    final static String Slash = "/";
    @Getter
    final static String Percent_Sign = "%";

    //弹窗标题
    @Getter
    @I18n
    static String Dialog_Error = "ERROR";
    @Getter
    @I18n
    static String Dialog_Info = "INFO";

    //提示信息
    @Getter
    @I18n
    static String Msg_Package_Not_Found = "The qgl package does not exist!";
    @Getter
    @I18n
    static String Msg_Package_Invalid = "The qgl package is broken: ";
    @Getter
    @I18n
    static String Msg_Scene_Folder_Missing = "The scene folder is missing in the package: ";
    @Getter
    @I18n
    static String Msg_Missing_Resource = "Resource is missing: ";
    @Getter
    @I18n
    static String Msg_Dir_Invalid = "The directory can not be created: ";
    @Getter
    @I18n
    static String Msg_Qsc_Invalid = "The scene text is invalid: ";
    @Getter
    @I18n
    static String Msg_No_Line = "( no line )";
    @Getter
    @I18n
    static String Msg_No_Scene = "There is no scene in this package!";
    @Getter
    @I18n
    static String Msg_Audio_Unsupported = "Audio format is not supported by Java sound, skipped: ";
    @Getter
    @I18n
    static String Msg_Audio_Error = "Audio can not be played: ";

    //命令行参数
    @Getter
    final static String Self_Check_Flag = "--check";
    @Getter
    final static String Self_Test_Flag = "--selftest";
    @Getter
    final static String Test_Flag = "--test";
    @Getter
    final static String List_Flag = "--list";
    @Getter
    final static String Workdir_Flag = "--workdir";
    @Getter
    final static String Lang_Flag = "--lang";
    @Getter
    final static String Flag_Prefix = "--";

    //GalWork 目录名与成品子目录(与编辑器约定一致)
    @Getter
    final static String Work_Dir_Name = "GalWork";
    @Getter
    final static String Output_Dir_Name = "Output";
    @Getter
    final static String Root_Sign = "/";

    //测试入口日志与提示
    @Getter
    @I18n
    static String Info_5 = "Test entry: play the packages in GalWork!";
    @Getter
    @I18n
    static String Info_6 = "Play: ";
    @Getter
    @I18n
    static String Msg_Work_Dir_Missing = "The GalWork directory can not be found: ";
    @Getter
    @I18n
    static String Msg_Output_Dir_Missing = "The Output directory can not be found: ";
    @Getter
    @I18n
    static String Msg_Package_Empty = "There is no qgl package, please pack one in QGalMaker first: ";
    @Getter
    @I18n
    static String Msg_Package_NotFound = "The package was not found: ";
    @Getter
    @I18n
    static String Msg_Test_Keys = "Keys: click / space / RIGHT next, LEFT back, R restart, A auto, V voice auto, L language, M mute, esc exit";

    //界面文案字段(反射重载用)与英文原文备份
    static final List<Field> TEXT_FIELDS = new ArrayList<>();
    static final Map<String, String> ENGLISH_TEXT = new LinkedHashMap<>();

    static {
        //收集 @I18n 字段:记下英文原文,并按当前语言填充一次
        for (Field field : ConstantPool.class.getDeclaredFields()) {
            if (field.getAnnotation(I18n.class) == null) {
                continue;
            }
            try {
                ENGLISH_TEXT.put(field.getName(), (String) field.get(null));
                TEXT_FIELDS.add(field);
            } catch (IllegalAccessException exception) {
                Logger.error(exception.getMessage());
            }
        }
        reload();
    }

    //按当前语言重新读取全部界面文案(语言热切换后调用)
    public static void reload() {

        for (Field field : TEXT_FIELDS) {
            try {
                field.set(null, Lang.text(field.getName(), ENGLISH_TEXT.get(field.getName())));
            } catch (IllegalAccessException exception) {
                Logger.error(exception.getMessage());
            }
        }
    }

}
