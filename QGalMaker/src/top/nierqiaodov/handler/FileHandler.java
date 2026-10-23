package top.nierqiaodov.handler;

import top.nierqiaodov.util.ConstantPool;
import top.nierqiaodov.util.Logger;
import lombok.Data;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Data
//资源文件处理类
public class FileHandler {

    //资源代号合法格式(字母、数字、下划线、汉字)
    static final Pattern codePattern = Pattern.compile(ConstantPool.getCode_Pattern());

    //工作目录对象
    @Getter
    static File workDir = new File(ConstantPool.getWork_Directory());

    @Getter
    static File backgroundDir = new File(ConstantPool.getBackground_Directory());

    @Getter
    static File characterDir = new File(ConstantPool.getCharacter_Directory());

    @Getter
    static File musicDir = new File(ConstantPool.getMusic_Directory());

    @Getter
    static File voiceDir = new File(ConstantPool.getVoice_Directory());

    @Getter
    static File linesDir = new File(ConstantPool.getLines_Directory());

    @Getter
    static File sceneDir = new File(ConstantPool.getScene_Directory());

    @Getter
    static File outputDir = new File(ConstantPool.getOutput_Directory());

    //全部工作目录(父目录在前,保证可以按顺序建立)
    public static File[] allDirs() {
        return new File[]{workDir, backgroundDir, characterDir, musicDir, voiceDir, linesDir, sceneDir, outputDir};
    }

    //工作目录初始化方法(修缮:改用 mkdirs,多级目录也能一次建好,失败时给出提示)
    public static void dirInit(File dir) {
        if (dir.exists()) {
            Logger.logger(dir.getAbsolutePath() + ConstantPool.getInfo_8());
            return;
        }
        if (dir.mkdirs()) {
            Logger.logger(dir.getAbsolutePath() + ConstantPool.getInfo_6());
            Logger.logger(dir.getAbsolutePath() + ConstantPool.getInfo_7());
        } else {
            Logger.error(dir.getAbsolutePath() + ConstantPool.getInfo_9());
        }
    }

    //一次性初始化全部工作目录
    public static void allDirInit() {
        for (File dir : allDirs()) {
            dirInit(dir);
        }
    }

    //资源导入:把源文件按指定代号复制到目标目录,返回落地后的文件
    public static File importResource(File file, File dir, String code) throws IOException {

        if (file == null || !file.isFile()) {
            throw new IOException(ConstantPool.getMsg_Source_Invalid());
        }
        if (dir == null) {
            throw new IOException(ConstantPool.getMsg_Target_Invalid());
        }
        if (!isLegalCode(code)) {
            throw new IOException(ConstantPool.getMsg_Code_Illegal());
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException(ConstantPool.getMsg_Dir_Invalid());
        }

        //保留源文件后缀,代号即资源标识(b1.png 对应代号 b1)
        String suffix = getSuffix(file.getName());
        String newFileName = code + suffix;
        File targetFile = new File(dir, newFileName);
        if (targetFile.exists()) {
            Logger.logger(targetFile.getAbsolutePath() + ConstantPool.getInfo_10());
        }
        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Logger.logger(targetFile.getAbsolutePath() + ConstantPool.getInfo_6());

        return targetFile;
    }

    //代号合法性检查
    public static boolean isLegalCode(String code) {
        return code != null && codePattern.matcher(code).matches();
    }

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

    //列出目录下的资源代号(去后缀,按名称排序)
    public static String[] listCodes(File dir) {
        return listCodes(dir, null);
    }

    //列出目录下指定后缀的资源代号
    public static String[] listCodes(File dir, String suffix) {
        File[] files = (dir == null) ? null : dir.listFiles();
        List<String> codes = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                if (suffix != null && !file.getName().endsWith(suffix)) {
                    continue;
                }
                codes.add(stripSuffix(file.getName()));
            }
        }
        return codes.toArray(new String[0]);
    }

    //列出目录下所有子目录名(一个角色一个目录)
    public static String[] listDirectoryCodes(File dir) {
        File[] files = (dir == null) ? null : dir.listFiles();
        List<String> codes = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                if (file.isDirectory()) {
                    codes.add(file.getName());
                }
            }
        }
        return codes.toArray(new String[0]);
    }

    //按代号查找资源文件(后缀不限),找不到返回 null
    public static File findResource(File dir, String code) {
        File[] files = (dir == null) ? null : dir.listFiles();
        if (files == null || code == null || code.isEmpty()) {
            return null;
        }
        for (File file : files) {
            if (file.isFile() && stripSuffix(file.getName()).equals(code)) {
                return file;
            }
        }
        return null;
    }

    //角色表情目录:Character/角色代号(角色的每个表情都是该目录下的一个文件)
    public static File getEmotionDir(String characterCode) {
        return new File(characterDir, characterCode);
    }

    //列出某个角色的全部表情代号
    public static String[] listEmotionCodes(String characterCode) {
        return listCodes(getEmotionDir(characterCode));
    }

    //按代号查找某个角色的表情图片
    public static File findEmotion(String characterCode, String emotionCode) {
        return findResource(getEmotionDir(characterCode), emotionCode);
    }

}
