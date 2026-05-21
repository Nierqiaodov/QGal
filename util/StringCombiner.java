package gg.nierqiaodov.util;

//字符串拼接器实现
public class StringCombiner {

    public static String combine(String[] strings) {

        StringBuilder s = new StringBuilder();
        for(String string : strings){
            s.append(ConstantPool.getCombine_Sign()).append(string);
       }

        s.append(ConstantPool.getCombine_Sign());

        return s.toString();

    }

}
