package top.nierqiaodov.util;

import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//常量池
//界面文案字段用 @I18n 标记,字段名就是 Lang 里的词条键:
//  字段初始值是英文原文,类加载时按当前语言填充一次,语言热切换时由 reload() 重新填充
//  纯语法/路径/格式类常量不标记,避免被翻译
@Data
public class ConstantPool {

    //启动与日志信息
    @Getter
    @I18n
    static String Info_1 = "Starting QGal!";
    @Getter
    @I18n
    static String Info_2 = "Init finished!";
    @Getter
    @I18n
    static String Info_3 = "UI started!";
    @Getter
    @I18n
    static String Info_4 = "Now you can compose!";
    @Getter
    @I18n
    static String Info_5 = "EXIT!";
    @Getter
    @I18n
    static String Info_6 = " created!";
    @Getter
    @I18n
    static String Info_7 = " ready!";
    @Getter
    @I18n
    static String Info_8 = " ready!";
    @Getter
    @I18n
    static String Info_9 = " can not be created!";
    @Getter
    @I18n
    static String Info_10 = " replaced!";
    //自检结果的机器可读标记(不翻译,便于脚本识别)
    @Getter
    final static String Info_11 = "SELFTEST PASSED";
    @Getter
    final static String Info_12 = "SELFTEST FAILED";

    //应用信息
    @Getter
    final static String Application = "Q GalMaker";
    @Getter
    @I18n
    static String Description = "Making and Packaging";
    //版本号来自 Maven 构建信息,不再写死
    @Getter
    final static String Version = BuildInfo.version();
    @Getter
    final static String Date_Format = "HH:mm:ss";
    //日志前缀(时间戳由 Logger 每条重新生成,不再在启动时定型)
    @Getter
    final static String Log_Prefix = "[QGalMaker]";

    //字符串拼接符号
    @Getter
    final static String Combine_Sign = "-";

    //工作目录
    @Getter
    final static String Work_Directory = "/GalWork";
    @Getter
    final static String Background_Directory = "/GalWork/Background";
    @Getter
    final static String Character_Directory = "/GalWork/Character";
    @Getter
    final static String Music_Directory = "/GalWork/Music";
    @Getter
    final static String Voice_Directory = "/GalWork/Voice";
    @Getter
    final static String Lines_Directory = "/GalWork/Lines";
    @Getter
    final static String Scene_Directory = "/GalWork/Scenes";
    @Getter
    final static String Output_Directory = "/GalWork/Output";

    //文件名与后缀
    @Getter
    final static String Last_Dot_Index = ".";
    @Getter
    final static String File_Name_Suffix = "";
    //场景工程文件后缀(类 json 文本)
    @Getter
    final static String Qsc_Suffix = ".qsc";
    //成品压缩包后缀(实际是 zip 压缩包)
    @Getter
    final static String Qgl_Suffix = ".qgl";

    //qsc 语法符号: s1{"b1";"c1:e1,c2:e1";"m1";["l1","l2","l3"];["v1",""]}
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
    //资源代号合法格式(字母、数字、下划线、汉字,避免与语法符号冲突)
    @Getter
    final static String Code_Pattern = "^[\\w\\x{4e00}-\\x{9fa5}]+$";
    //代号自动生成前缀
    @Getter
    final static String Scene_Code_Prefix = "s";
    @Getter
    final static String Project_Code_Prefix = "p";

    //打包结构(压缩包内的目录与文件名)
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
    //压缩包内文件名的连接符(角色: 序号_角色代号_表情代号;语音: 序号_语音代号)
    @Getter
    final static String Package_Name_Sign = "_";

    //主界面文案
    @Getter
    @I18n
    static String Add_Resource = "ADD RESOURCE";
    @Getter
    @I18n
    static String Create_Scene = "CREATE SCENE";
    @Getter
    @I18n
    static String Do_Package = "PACKAGE";

    //资源添加文案
    @Getter
    @I18n
    static String Add_Background = "ADD BACKGROUND";
    @Getter
    @I18n
    static String Add_Character = "ADD CHARACTER";
    @Getter
    @I18n
    static String Add_Music = "ADD MUSIC";
    @Getter
    @I18n
    static String Add_Emotion = "ADD EMOTION";
    @Getter
    @I18n
    static String Add_Voice = "ADD VOICE";
    @Getter
    @I18n
    static String Choose_File = "CHOOSE FILE";
    @Getter
    @I18n
    static String Submit = "SUBMIT";
    @Getter
    @I18n
    static String Warning = "WARNING";
    @Getter
    @I18n
    static String Choose_Background = "CHOOSE BACKGROUND";
    @Getter
    @I18n
    static String Choose_Music = "CHOOSE MUSIC";
    @Getter
    @I18n
    static String Choose_Character = "CHOOSE CHARACTER";
    @Getter
    @I18n
    static String Choose_Emotion = "CHOOSE EMOTION";
    @Getter
    @I18n
    static String Choose_Voice = "CHOOSE VOICE";

    //场景编辑器文案
    @Getter
    @I18n
    static String Scene_Editor = "SCENE EDITOR";
    @Getter
    @I18n
    static String Preview = "PREVIEW";
    @Getter
    @I18n
    static String Typing_Lines = "TYPING LINES";
    @Getter
    @I18n
    static String Scene_Save = "SAVE SCENE";
    @Getter
    @I18n
    static String Add_A_Character = "ADD CHARACTER";
    @Getter
    @I18n
    static String Label_Project = "PROJECT";
    @Getter
    @I18n
    static String Label_Scene_Code = "SCENE CODE";
    @Getter
    @I18n
    static String Label_Background = "BACKGROUND";
    @Getter
    @I18n
    static String Label_Character = "CHARACTER";
    @Getter
    @I18n
    static String Label_Emotion = "EMOTION";
    @Getter
    @I18n
    static String Label_Music = "MUSIC";
    @Getter
    @I18n
    static String Label_Cast = "CHARACTER LAYOUT";
    @Getter
    @I18n
    static String Label_Lines = "LINES";
    @Getter
    @I18n
    static String Label_Voice = "VOICE";
    @Getter
    @I18n
    static String New_Project = "NEW PROJECT";
    @Getter
    @I18n
    static String New_Scene = "NEW SCENE";
    @Getter
    @I18n
    static String Load_Scene = "LOAD SCENE";
    @Getter
    @I18n
    static String Do_Add = "ADD";
    @Getter
    @I18n
    static String Do_Modify = "MODIFY";
    @Getter
    @I18n
    static String Do_Remove = "REMOVE";
    @Getter
    @I18n
    static String Do_Up = "UP";
    @Getter
    @I18n
    static String Do_Down = "DOWN";
    @Getter
    @I18n
    static String Do_Previous = "PREV";
    @Getter
    @I18n
    static String Do_Next = "NEXT";
    @Getter
    @I18n
    static String Do_Preview_Voice = "PLAY VOICE";
    @Getter
    @I18n
    static String Do_Preview_Music = "PLAY MUSIC";
    @Getter
    @I18n
    static String None_Option = "NONE";
    @Getter
    final static String Void_String = "";
    @Getter
    final static String Slash = "/";

    //弹窗标题
    @Getter
    @I18n
    static String Dialog_Warning = "WARNING";
    @Getter
    @I18n
    static String Dialog_Error = "ERROR";
    @Getter
    @I18n
    static String Dialog_Info = "INFO";

    //提示信息
    @Getter
    @I18n
    static String Msg_Choose_File = "No file chosen, the resource was not added!";
    @Getter
    @I18n
    static String Msg_Code_Empty = "Code can not be empty!";
    @Getter
    @I18n
    static String Msg_Code_Illegal = "Code can only contain letters, digits, underline or Chinese!";
    @Getter
    @I18n
    static String Msg_Source_Invalid = "The source file is invalid!";
    @Getter
    @I18n
    static String Msg_Target_Invalid = "The target directory is invalid!";
    @Getter
    @I18n
    static String Msg_Dir_Invalid = "The directory can not be created!";
    @Getter
    @I18n
    static String Msg_Import_Done = "Resource added: ";
    @Getter
    @I18n
    static String Msg_No_Resource = "No resource found, please add some first!";
    @Getter
    @I18n
    static String Msg_No_Emotion = "This character has no emotion yet!";
    @Getter
    @I18n
    static String Msg_No_Voice = "There is no voice resource yet!";
    @Getter
    @I18n
    static String Msg_Scene_Saved = "Scene saved: ";
    @Getter
    @I18n
    static String Msg_Scene_Not_Found = "Scene not found in this project!";
    @Getter
    @I18n
    static String Msg_Scene_Empty = "Nothing has been added to this scene yet!";
    @Getter
    @I18n
    static String Msg_Project_Not_Found = "The project file does not exist!";
    @Getter
    @I18n
    static String Msg_Project_Empty = "There is no scene in this project!";
    @Getter
    @I18n
    static String Msg_Project_Name = "Input the new project code:";
    @Getter
    @I18n
    static String Msg_Choose_Project = "Please choose a project first!";
    @Getter
    @I18n
    static String Msg_No_Project = "No project found, please create a scene first!";
    @Getter
    @I18n
    static String Msg_Line_Empty = "The line can not be empty!";
    @Getter
    @I18n
    static String Msg_No_Line = "( no line )";
    @Getter
    @I18n
    static String Msg_Cast_Empty = "Please choose a character and an emotion first!";
    @Getter
    @I18n
    static String Msg_Package_Done = "Package finished: ";
    @Getter
    @I18n
    static String Msg_Duplicated_Code = " duplicated, the first one is used: ";
    @Getter
    @I18n
    static String Msg_Missing_Resource = "Resource is missing: ";
    @Getter
    @I18n
    static String Msg_Qsc_Invalid = "The scene text is invalid: ";
    @Getter
    @I18n
    static String Msg_Audio_Unsupported = "Audio format is not supported by Java sound, skipped: ";
    @Getter
    @I18n
    static String Msg_Audio_Error = "Audio can not be played: ";

    //命令行参数
    @Getter
    final static String Self_Test_Flag = "--selftest";
    @Getter
    final static String Lang_Flag = "--lang";

    //首页与资源说明文案
    @Getter
    @I18n
    static String Slogan = "Make  ·  Compose  ·  Package";
    @Getter
    @I18n
    static String Add_Resource_Desc = "Import backgrounds, characters, emotions, music and voices";
    @Getter
    @I18n
    static String Create_Scene_Desc = "Compose background, cast layout, lines and voices";
    @Getter
    @I18n
    static String Do_Package_Desc = "Pack the qsc project into a qgl package";
    @Getter
    @I18n
    static String Home_Status = "Work directory:";
    @Getter
    @I18n
    static String Dialog_Language_Title = "Language";
    @Getter
    @I18n
    static String Back = "BACK";
    @Getter
    @I18n
    static String Add_Background_Desc = "A scene backdrop, identified by the background code";
    @Getter
    @I18n
    static String Add_Character_Desc = "A character sprite plus its first emotion";
    @Getter
    @I18n
    static String Add_Emotion_Desc = "Add one more emotion to an existing character";
    @Getter
    @I18n
    static String Add_Music_Desc = "Background music, wav / ogg / mp3";
    @Getter
    @I18n
    static String Add_Voice_Desc = "Voice of one line, wav / ogg / mp3";

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

    //按当前语言重新读取全部界面文案(语言热切换后由界面调用)
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
