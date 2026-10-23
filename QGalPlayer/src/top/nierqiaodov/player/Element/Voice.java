package top.nierqiaodov.player.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
//角色语音类(一句台词对应的一段配音)
public class Voice {

    //语音代号(v1)
    String code;

    //拆包后的语音文件
    File file;

}
