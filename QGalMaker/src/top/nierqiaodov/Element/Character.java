package top.nierqiaodov.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
//角色立绘类(一个角色可以有多个表情)
public class Character {

    //角色代号(c1),对应 Character 目录下的子目录名
    String code;

    //该角色拥有的全部表情
    List<CharacterEmotion> emotions = new ArrayList<>();

}
