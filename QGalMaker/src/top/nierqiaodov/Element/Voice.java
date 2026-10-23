package top.nierqiaodov.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
//角色语音类(一句台词对应的一段配音)
public class Voice {

    //语音代号(v1),对应 Voice 目录下的文件名
    String code;

    //语音文件
    File file;

}
