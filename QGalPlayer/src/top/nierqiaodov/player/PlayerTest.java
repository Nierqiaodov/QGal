package top.nierqiaodov.player;

import top.nierqiaodov.player.util.ConstantPool;
import top.nierqiaodov.player.util.FileNames;
import top.nierqiaodov.player.util.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

//播放器测试入口:直接播放 GalWork 文件夹里的成品(联调用,不参与正常启动流程)
//用法:
//  java -cp QGalPlayer-1.0.jar top.nierqiaodov.player.PlayerTest            播放最新的成品
//  java -cp QGalPlayer-1.0.jar top.nierqiaodov.player.PlayerTest demo       播放 demo.qgl
//  java -cp QGalPlayer-1.0.jar top.nierqiaodov.player.PlayerTest --list     只列出成品,不播放
//  java -cp QGalPlayer-1.0.jar top.nierqiaodov.player.PlayerTest --check    对选中的成品做拆包自检
//  ... --workdir D:\某某\GalWork                                            指定 GalWork 位置
//也可以从正式入口转进来: java -jar QGalPlayer-1.0.jar --test [同上参数]
public class PlayerTest {

    //成品时间显示格式
    static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {

        //先处理语言参数:必须在常量池(以及 Lang)初始化之前设置系统属性
        for (int i = 0; i < args.length - 1; i++) {
            if ("--lang".equals(args[i])) {
                System.setProperty("qgal.lang", args[i + 1]);
            }
        }

        Logger.logger(ConstantPool.getInfo_1());
        Logger.logger(ConstantPool.getInfo_5());

        //1.解析参数
        boolean listOnly = false;
        boolean checkOnly = false;
        File workDir = null;
        String targetName = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (isListFlag(arg)) {
                listOnly = true;
            } else if (ConstantPool.getSelf_Check_Flag().equals(arg)) {
                checkOnly = true;
            } else if (ConstantPool.getWorkdir_Flag().equals(arg) && i + 1 < args.length) {
                workDir = new File(args[++i]);
            } else if (ConstantPool.getLang_Flag().equals(arg)) {
                //语言参数已经在入口处理过,这里跳过它的值
                i++;
            } else if (!arg.startsWith(ConstantPool.getFlag_Prefix())) {
                targetName = arg;
            } else {
                Logger.error(ConstantPool.getMsg_Package_NotFound() + arg);
            }
        }

        //2.定位 GalWork/Output
        File outputDir = resolveOutputDir(workDir);
        if (outputDir == null) {
            return;
        }

        //3.列出成品
        List<File> packages = listPackages(outputDir);
        if (packages.isEmpty()) {
            Logger.error(ConstantPool.getMsg_Package_Empty() + outputDir.getAbsolutePath());
            return;
        }
        printPackages(packages, outputDir);

        if (listOnly) {
            return;
        }

        //4.选择成品(不指定名字时取最新的一个)
        File targetFile = selectPackage(packages, targetName);
        if (targetFile == null) {
            return;
        }
        Logger.logger(ConstantPool.getInfo_6() + targetFile.getAbsolutePath()
                + " (" + targetFile.length() + " bytes)");
        Logger.logger(ConstantPool.getMsg_Test_Keys());

        //5.自检模式:只拆包不开窗口
        if (checkOnly) {
            boolean passed = SelfCheck.run(targetFile);
            System.exit(passed ? 0 : 1);
        }

        //6.播放(窗口关闭前进程不会退出)
        try {
            QGalPlayer.play(targetFile);
        } catch (IOException exception) {
            Logger.error(exception.getMessage());
            System.exit(1);
        }
    }

    //判断是否为 --list
    static boolean isListFlag(String arg) {
        return ConstantPool.getList_Flag().equals(arg);
    }

    //定位成品目录:显式指定的 GalWork > 当前目录下的 GalWork > 当前盘符根目录下的 GalWork
    static File resolveOutputDir(File workDir) {

        File baseDir = workDir;

        if (baseDir == null) {
            baseDir = new File(ConstantPool.getWork_Dir_Name());
            if (!baseDir.isDirectory()) {
                //编辑器把工作目录建在盘符根目录下(/GalWork)
                baseDir = new File(ConstantPool.getRoot_Sign() + ConstantPool.getWork_Dir_Name());
            }
        }

        if (!baseDir.isDirectory()) {
            Logger.error(ConstantPool.getMsg_Work_Dir_Missing() + baseDir.getAbsolutePath());
            return null;
        }

        File outputDir = new File(baseDir, ConstantPool.getOutput_Dir_Name());
        if (!outputDir.isDirectory()) {
            Logger.error(ConstantPool.getMsg_Output_Dir_Missing() + outputDir.getAbsolutePath());
            return null;
        }

        return outputDir;
    }

    //列出成品(按修改时间倒序,最新的排在最前)
    static List<File> listPackages(File outputDir) {

        File[] files = outputDir.listFiles();
        List<File> packages = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(ConstantPool.getQgl_Suffix())) {
                    packages.add(file);
                }
            }
        }

        packages.sort(Comparator.comparingLong(File::lastModified).reversed().thenComparing(File::getName));

        return packages;
    }

    //打印成品清单
    static void printPackages(List<File> packages, File outputDir) {

        Logger.logger("Packages in " + outputDir.getAbsolutePath() + ConstantPool.getColon() + packages.size());

        for (int i = 0; i < packages.size(); i++) {
            File file = packages.get(i);
            Logger.logger("  " + (i + 1) + ". " + file.getName()
                    + "  " + formatter.format(new Date(file.lastModified()))
                    + "  " + file.length() + " bytes");
        }
    }

    //选择成品:指定名字(可省略 .qgl 后缀)时按名字匹配,否则取最新的一个
    static File selectPackage(List<File> packages, String targetName) {

        if (targetName == null || targetName.isEmpty()) {
            return packages.get(0);
        }

        String wantedName = targetName.toLowerCase();
        String wantedCode = FileNames.stripSuffix(wantedName);

        for (File file : packages) {
            String fileName = file.getName().toLowerCase();
            if (fileName.equals(wantedName) || FileNames.stripSuffix(fileName).equals(wantedCode)) {
                return file;
            }
        }

        Logger.error(ConstantPool.getMsg_Package_NotFound() + targetName);

        return null;
    }

}
