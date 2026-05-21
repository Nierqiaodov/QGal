package gg.nierqiaodov.util;

import java.text.SimpleDateFormat;
import java.util.Date;

//调试生成器
public class Logger {

    static SimpleDateFormat formatter = new SimpleDateFormat(ConstantPool.getDate_Format());
    static Date date = new Date();

    public static void logger(String info) {

        System.out.println(ConstantPool.getLogout_Format() + info);
    }

}
