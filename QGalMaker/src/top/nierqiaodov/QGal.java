package top.nierqiaodov;

import top.nierqiaodov.handler.FileHandler;
import top.nierqiaodov.ui.UI;
import top.nierqiaodov.util.ConstantPool;
import top.nierqiaodov.util.Fonts;
import top.nierqiaodov.util.Lang;
import top.nierqiaodov.util.Logger;
import top.nierqiaodov.util.SelfTest;

public class QGal {

    public static void main(String[] args) {

        //先处理语言参数:必须在常量池被读取之前设置系统属性,后设无效
        //用法: java -jar QGalMaker-1.0.jar --lang en
        String language = pickArgument(args, "--lang");
        if (language != null) {
            System.setProperty("qgal.lang", language);
        }

        Logger.logger(ConstantPool.getInfo_1());

        //修缮:一次性初始化全部工作目录(含 Scenes、Output、Voice)
        FileHandler.allDirInit();
        Logger.logger(ConstantPool.getInfo_2());
        //诊断信息:实际选用的中文字体与当前界面语言
        Logger.logger("font=" + Fonts.getFamily() + " / language=" + Lang.getCurrent());

        //命令行带 --selftest 时只跑无界面自检,便于验证编码、中文与打包流程
        if (hasFlag(args, ConstantPool.getSelf_Test_Flag())) {
            boolean passed = SelfTest.run();
            Logger.logger(passed ? ConstantPool.getInfo_11() : ConstantPool.getInfo_12());
            System.exit(passed ? 0 : 1);
        }

        UI.mainUi();
        Logger.logger(ConstantPool.getInfo_3());
        Logger.logger(ConstantPool.getInfo_4());

    }

    //取出 "--参数名 值" 形式的值(没有该参数时返回 null)
    static String pickArgument(String[] args, String flag) {

        for (int i = 0; i < args.length - 1; i++) {
            if (flag.equals(args[i])) {
                return args[i + 1];
            }
        }

        return null;
    }

    //是否包含某个开关参数
    static boolean hasFlag(String[] args, String flag) {

        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }

        return false;
    }

}
