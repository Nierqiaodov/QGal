package top.nierqiaodov.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//场景角色类(角色 + 该角色在本场景使用的表情)
public class Actor {

    //出场角色
    Character character;

    //本场景使用的表情
    CharacterEmotion emotion;

}
