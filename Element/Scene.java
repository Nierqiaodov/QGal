package gg.nierqiaodov.Element;

import lombok.Data;

@Data
//场景类
public class Scene {
    Scene(Background background, Music music, Character character, Lines lines) {}
    Background background;
    Music music;
    Character character;
    Lines lines;

}
