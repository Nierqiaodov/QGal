package top.nierqiaodov.player.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
//背景图类
public class Background {

    //背景代号(b1)
    String code;

    //拆包后的背景图片文件
    File file;

}
