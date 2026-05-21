package gg.nierqiaodov.util;

import lombok.Data;
import lombok.Getter;

//常量池
@Data
public class ConstantPool {

    @Getter
    final static String Info_1 = "Starting QGal!";
    @Getter
    final static String Info_2 = "Init finished!";
    @Getter
    final static String Info_3 = "UI started!";
    @Getter
    final static String Info_4 = "Now you can compose!";
    @Getter
    final static String Info_5 = "EXIT!";
    @Getter
    final static String Info_6 = " created!";
    @Getter
    final static String Info_7 = " ready!";
    @Getter
    final static String Info_8 = " ready!";

    @Getter
    final static String Application = "Q GalMaker";
    @Getter
    final static String Description = "Making and Packaging";
    @Getter
    final static String Version = "1.0.0";
    @Getter
    final static String Font = "Segoe UI";
    @Getter
    final static String Add_Resource = "ADD RESOURCE";
    @Getter
    final static String Create_Scene = "CREATE SCENE";
    @Getter
    final static String Do_Package = "PACKAGE";
    @Getter
    final static String Date_Format = "HH:mm:ss";
    @Getter
    final static String Logout_Format = "[QGalMaker Log Out][" + Logger.formatter.format(Logger.date) + "]";
    @Getter
    final static String Combine_Sign = "-";
    @Getter
    final static String Work_Directory = "/GalWork";
    @Getter
    final static String Background_Directory = "/GalWork/Background";
    @Getter
    final static String Character_Directory = "/GalWork/Character";
    @Getter
    final static String Music_Directory = "/GalWork/Music";
    @Getter
    final static String Lines_Directory = "/GalWork/Lines";
    @Getter
    final static String Last_Dot_Index = ".";
    @Getter
    final static String File_Name_Suffix = "";
    @Getter
    final static String Add_Background = "ADD BACKGROUND";
    @Getter
    final static String Add_Character = "ADD CHARACTER";
    @Getter
    final static String Add_Music = "ADD MUSIC ";
    @Getter
    final static String Add_Resource_To_Work_Directory = "Choose the resource you want to add";
    @Getter
    final static String Choose_File = "CHOOSE FILE";
    @Getter
    final static String Submit = "SUBMIT";

}
