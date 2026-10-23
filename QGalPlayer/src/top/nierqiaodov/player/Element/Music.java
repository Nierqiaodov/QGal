package top.nierqiaodov.player.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
//背景音乐类
public class Music {

    //音乐代号(m1)
    String code;

    //拆包后的音乐文件
    File file;

}
