package top.nierqiaodov.player.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//标记常量池里的界面文案字段
//被标记的字段:初始值是英文原文,启动时按当前语言填充,语言热切换时由 ConstantPool.reload() 重新填充
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface I18n {
}
