package top.nierqiaodov.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scene {

    //场景代号
    String code;

    //背景,一个场景中仅可出现一次,可为空
    Background background;

    //背景音乐,可为空
    Music music;

    //出场角色,列表顺序即从左到右的排列顺序
    List<Actor> actors = new ArrayList<>();

    //台词
    Lines lines = new Lines();

}
