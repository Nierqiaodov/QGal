package top.nierqiaodov.player.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
//角色表情类(qsc 中 c1:e1 的 e1)
public class CharacterEmotion {

    //表情代号(e1)
    String code;

    //拆包后的表情图片文件
    File file;

}
