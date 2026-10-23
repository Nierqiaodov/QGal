package top.nierqiaodov.util;

import java.text.SimpleDateFormat;
import java.util.Date;

//调试生成器
public class Logger {

    //日志时间格式
    static SimpleDateFormat formatter = new SimpleDateFormat(ConstantPool.getDate_Format());

    //日志输出(修缮:时间戳在每次调用时重新生成,不再冻结在程序启动时刻)
    public static void logger(String info) {

        System.out.println(ConstantPool.getLog_Prefix() + "[" + formatter.format(new Date()) + "] " + info);
    }

    //错误日志(输出到标准错误,便于与普通日志区分)
    public static void error(String info) {

        System.err.println(ConstantPool.getLog_Prefix() + "[" + formatter.format(new Date()) + "] " + info);
    }

}
