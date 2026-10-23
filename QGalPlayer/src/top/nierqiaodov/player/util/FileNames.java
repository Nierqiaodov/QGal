package top.nierqiaodov.player.util;

//文件名工具:取后缀与去后缀
public class FileNames {

    //取文件名后缀(含点),没有后缀时返回空串
    public static String getSuffix(String fileName) {

        if (fileName == null) {
            return ConstantPool.getFile_Name_Suffix();
        }

        int lastDotIndex = fileName.lastIndexOf(ConstantPool.getLast_Dot_Index());
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }

        return ConstantPool.getFile_Name_Suffix();
    }

    //去掉文件名后缀(得到资源代号)
    public static String stripSuffix(String fileName) {

        if (fileName == null) {
            return ConstantPool.getVoid_String();
        }

        int lastDotIndex = fileName.lastIndexOf(ConstantPool.getLast_Dot_Index());
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName;
    }

}
