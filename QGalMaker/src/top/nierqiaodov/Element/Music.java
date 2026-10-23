package top.nierqiaodov.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
//背景音乐类
public class Music {

    //音乐代号(m1),对应 Music 目录下的文件名
    String code;

    //音乐文件
    File file;

}
