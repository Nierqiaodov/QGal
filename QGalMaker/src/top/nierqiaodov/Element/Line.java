package top.nierqiaodov.Element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//台词行类(一句台词 + 该句可选的语音)
public class Line {

    //台词文本
    String text;

    //该句的语音,没有配音时为 null
    Voice voice;

}
