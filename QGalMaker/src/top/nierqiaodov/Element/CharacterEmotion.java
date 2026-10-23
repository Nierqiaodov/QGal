package top.nierqiaodov.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
//角色表情类(qsc 中的 c1:e1 的 e1)
public class CharacterEmotion {

    //表情代号(e1),对应 Character/角色代号 目录下的文件名
    String code;

    //表情图片文件
    File file;

}
