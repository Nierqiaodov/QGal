package top.nierqiaodov.player.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
//台词对话类(方括号内内容,按播放顺序保存)
public class Lines {

    //台词列表(每句可带一段语音)
    List<Line> lines = new ArrayList<>();

}
