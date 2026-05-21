package gg.nierqiaodov.handler;

import gg.nierqiaodov.util.ConstantPool;
import gg.nierqiaodov.util.Logger;
import lombok.Data;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Data
//资源文件处理类
public class FileHandler {

    //工作目录对象
    @Getter
    static File workDir = new File(ConstantPool.getWork_Directory());

    @Getter
    static File background = new File(ConstantPool.getBackground_Directory());

    @Getter
    static File character = new File(ConstantPool.getCharacter_Directory());

    @Getter
    static File music = new File(ConstantPool.getMusic_Directory());

    @Getter
    static File lines = new File(ConstantPool.getLines_Directory());

    //工作目录初始化方法
    public static void dirInit(File dir){
        if(!dir.exists()){
            if(dir.mkdir()){
                Logger.logger(dir.getAbsolutePath() + ConstantPool.getInfo_6());
                Logger.logger(dir.getAbsolutePath() + ConstantPool.getInfo_7());
            }
        } else {
            Logger.logger(dir.getAbsolutePath() + ConstantPool.getInfo_8());
        }
    }

    //处理资源文件实现方法(实现将资源复制到工作目录并且重命名编号)
    public static void fileHandler(File file, File dir, String fileName) throws IOException {

        String filePath = file.getName();
        int lastDotIndex = filePath.lastIndexOf(ConstantPool.getLast_Dot_Index());
        String suffix = ConstantPool.getFile_Name_Suffix();
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            suffix = filePath.substring(lastDotIndex);
        }
        String newFileName = fileName + suffix;
        File targetFile = new File(dir, newFileName);
        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

    }

}
