package top.nierqiaodov.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
//背景图类
public class Background {

    //背景代号(b1),对应 Background 目录下的文件名
    String code;

    //背景图片文件
    File file;

}
