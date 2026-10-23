package top.nierqiaodov.player;

import top.nierqiaodov.player.Element.Scene;
import top.nierqiaodov.player.util.ConstantPool;
import top.nierqiaodov.player.util.FileNames;
import top.nierqiaodov.player.util.Logger;
import lombok.Data;

import java.io.File;
import java.util.List;

@Data
//拆包结果:原始成品、解包目录与按播放顺序排好的场景
public class QGalPackage {

    //原始 qgl 成品
    File sourceFile;

    //解包目录(临时目录)
    File workDir;

    //按工程文件顺序排好的场景
    List<Scene> scenes;

    public QGalPackage(File sourceFile, File workDir, List<Scene> scenes) {
        this.sourceFile = sourceFile;
        this.workDir = workDir;
        this.scenes = scenes;
    }

    //工程代号(成品文件名去掉后缀)
    public String getProjectCode() {
        return FileNames.stripSuffix((sourceFile == null) ? ConstantPool.getVoid_String() : sourceFile.getName());
    }

    //关闭:清理解包目录
    //音频线程可能在关闭瞬间还占着文件句柄,所以失败时重试几次,最后再登记退出时删除
    public void close() {

        if (workDir == null) {
            return;
        }

        for (int i = 0; i < 5; i++) {
            if (deleteDirectory(workDir)) {
                Logger.logger(workDir.getAbsolutePath() + ConstantPool.getInfo_3());
                return;
            }
            try {
                Thread.sleep(120);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        //仍然删不掉:登记退出时删除,避免留下垃圾目录
        registerDeleteOnExit(workDir);
        Logger.error(workDir.getAbsolutePath() + ConstantPool.getInfo_3());
    }

    //登记整棵目录为退出时删除
    static void registerDeleteOnExit(File dir) {

        if (dir == null) {
            return;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    registerDeleteOnExit(file);
                } else {
                    file.deleteOnExit();
                }
            }
        }

        dir.deleteOnExit();
    }

    //递归删除目录,返回目录是否已经被删掉
    static boolean deleteDirectory(File dir) {

        if (dir == null || !dir.exists()) {
            return true;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

        return dir.delete() || !dir.exists();
    }

}
