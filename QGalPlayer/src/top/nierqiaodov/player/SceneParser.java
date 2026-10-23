package top.nierqiaodov.player;

import top.nierqiaodov.player.Element.Actor;
import top.nierqiaodov.player.Element.Background;
import top.nierqiaodov.player.Element.Character;
import top.nierqiaodov.player.Element.CharacterEmotion;
import top.nierqiaodov.player.Element.Line;
import top.nierqiaodov.player.Element.Lines;
import top.nierqiaodov.player.Element.Music;
import top.nierqiaodov.player.Element.Scene;
import top.nierqiaodov.player.Element.Voice;
import top.nierqiaodov.player.util.ConstantPool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

//qsc 场景文本解析器(播放器侧独立实现,只负责解码)
//格式: s1{"b1";"c1:e1,c2:e1";"m1";["l1","l2","l3"]}
//  s1            场景代号
//  "b1"          背景代号,一个场景仅可出现一次,可为空串
//  "c1:e1,..."   角色及其表情,可出现多个,顺序即从左到右
//  "m1"          背景音乐代号,可为空串
//  ["l1","l2"]   台词,顺序即播放顺序
public class SceneParser {

    //qsc 语法符号
    static final char BRACE_LEFT = ConstantPool.getBrace_Left().charAt(0);
    static final char BRACE_RIGHT = ConstantPool.getBrace_Right().charAt(0);
    static final char BRACKET_LEFT = ConstantPool.getBracket_Left().charAt(0);
    static final char BRACKET_RIGHT = ConstantPool.getBracket_Right().charAt(0);
    static final char QUOTE = ConstantPool.getQuote().charAt(0);
    static final char SEMICOLON = ConstantPool.getSemicolon().charAt(0);
    static final char COMMA = ConstantPool.getComma().charAt(0);
    static final char COLON = ConstantPool.getColon().charAt(0);
    static final char ESCAPE = ConstantPool.getEscape().charAt(0);

    //读取工程文件中的全部场景文本(跳过空行与 # 注释行)
    public static List<String> readSceneTexts(File projectFile) throws IOException {

        List<String> texts = new ArrayList<>();
        if (projectFile == null || !projectFile.isFile()) {
            return texts;
        }

        for (String line : Files.readAllLines(projectFile.toPath(), StandardCharsets.UTF_8)) {
            String text = line.trim();
            if (text.isEmpty() || text.startsWith(ConstantPool.getComment_Sign())) {
                continue;
            }
            texts.add(text);
        }

        return texts;
    }

    //把单场景 qsc 文本解析成 Scene 实例(只解析代号与台词,文件引用由 PackageReader 补充)
    public static Scene parse(String text) {

        String source = (text == null) ? ConstantPool.getVoid_String() : text.trim();
        if (source.isEmpty()) {
            throw new IllegalArgumentException(ConstantPool.getMsg_Qsc_Invalid() + text);
        }

        QscReader reader = new QscReader(source);
        Scene scene = new Scene();

        //场景代号位于第一个左花括号之前
        scene.setCode(reader.readCode());
        reader.expect(BRACE_LEFT);

        //背景
        String backgroundCode = reader.readQuoted();
        reader.expect(SEMICOLON);

        //角色及表情
        String castText = reader.readQuoted();
        reader.expect(SEMICOLON);

        //背景音乐
        String musicCode = reader.readQuoted();
        reader.expect(SEMICOLON);

        //台词
        List<String> lineTexts = reader.readArray();

        //可选的第五段:每句台词的语音代号(旧版 qsc 没有这一段,直接跟在台词数组后面就是右花括号)
        List<String> voiceCodes = new ArrayList<>();
        if (reader.peek() == SEMICOLON) {
            reader.expect(SEMICOLON);
            voiceCodes = reader.readArray();
        }

        reader.expect(BRACE_RIGHT);

        //组装成 Scene 实例(空代号代表该元素不存在)
        if (!backgroundCode.isEmpty()) {
            Background background = new Background();
            background.setCode(backgroundCode);
            scene.setBackground(background);
        }
        if (!musicCode.isEmpty()) {
            Music music = new Music();
            music.setCode(musicCode);
            scene.setMusic(music);
        }
        scene.setActors(parseActors(castText));
        scene.setLines(parseLines(lineTexts, voiceCodes));

        return scene;
    }

    //把台词文本与语音代号按序号配对
    static Lines parseLines(List<String> lineTexts, List<String> voiceCodes) {

        Lines lines = new Lines();

        for (int i = 0; i < lineTexts.size(); i++) {
            Line line = new Line();
            line.setText(lineTexts.get(i));

            String voiceCode = (i < voiceCodes.size()) ? voiceCodes.get(i) : ConstantPool.getVoid_String();
            if (!voiceCode.isEmpty()) {
                Voice voice = new Voice();
                voice.setCode(voiceCode);
                line.setVoice(voice);
            }

            lines.getLines().add(line);
        }

        return lines;
    }

    //解析角色串 "c1:e1,c2:e1"(顺序即从左到右的排列顺序)
    static List<Actor> parseActors(String castText) {

        List<Actor> actors = new ArrayList<>();
        if (castText == null || castText.isEmpty()) {
            return actors;
        }

        for (String item : castText.split(ConstantPool.getComma())) {
            String one = item.trim();
            if (one.isEmpty()) {
                continue;
            }
            int colonIndex = one.indexOf(COLON);
            String characterCode = (colonIndex < 0) ? one : one.substring(0, colonIndex);
            String emotionCode = (colonIndex < 0) ? ConstantPool.getVoid_String() : one.substring(colonIndex + 1);

            Character character = new Character();
            character.setCode(characterCode.trim());
            CharacterEmotion emotion = new CharacterEmotion();
            emotion.setCode(emotionCode.trim());

            actors.add(new Actor(character, emotion));
        }

        return actors;
    }

    //把场景角色写成 c1:e1 的形式
    public static String describeActor(Actor actor) {

        String characterCode = (actor == null || actor.getCharacter() == null)
                ? ConstantPool.getVoid_String() : actor.getCharacter().getCode();
        String emotionCode = (actor == null || actor.getEmotion() == null)
                ? ConstantPool.getVoid_String() : actor.getEmotion().getCode();

        return characterCode + ConstantPool.getColon() + emotionCode;
    }

    //判断是否为空白字符(避免与 Element.Character 同名类冲突,不用 java.lang.Character)
    static boolean isBlank(char character) {
        return character == ' ' || character == '\t' || character == '\n' || character == '\r';
    }

    //qsc 文本读取器:按游标顺序读取,台词里的逗号冒号不会干扰分段
    static class QscReader {

        //待解析文本
        String source;
        //当前游标位置
        int cursor = 0;

        QscReader(String source) {
            this.source = source;
        }

        //跳过空白字符
        void skipBlank() {
            while (cursor < source.length() && isBlank(source.charAt(cursor))) {
                cursor++;
            }
        }

        //查看当前字符(越界返回 0)
        char peek() {
            return (cursor < source.length()) ? source.charAt(cursor) : (char) 0;
        }

        //读取场景代号(第一个左花括号之前的全部内容)
        String readCode() {

            int start = cursor;
            while (cursor < source.length() && source.charAt(cursor) != BRACE_LEFT) {
                cursor++;
            }
            if (cursor >= source.length()) {
                throw new IllegalArgumentException(ConstantPool.getMsg_Qsc_Invalid() + source);
            }

            String code = source.substring(start, cursor).trim();
            if (code.isEmpty()) {
                throw new IllegalArgumentException(ConstantPool.getMsg_Qsc_Invalid() + source);
            }

            return code;
        }

        //读取一个带引号的字符串(处理反斜杠转义)
        String readQuoted() {

            expect(QUOTE);

            StringBuilder builder = new StringBuilder();
            while (true) {
                if (cursor >= source.length()) {
                    throw new IllegalArgumentException(ConstantPool.getMsg_Qsc_Invalid() + source);
                }
                char character = source.charAt(cursor++);
                if (character == QUOTE) {
                    break;
                }
                if (character == ESCAPE) {
                    if (cursor >= source.length()) {
                        throw new IllegalArgumentException(ConstantPool.getMsg_Qsc_Invalid() + source);
                    }
                    char next = source.charAt(cursor++);
                    if (next == 'n') {
                        builder.append('\n');
                    } else if (next == 't') {
                        builder.append('\t');
                    } else {
                        //\\ 与 \" 直接还原为原字符
                        builder.append(next);
                    }
                    continue;
                }
                builder.append(character);
            }
            skipBlank();

            return builder.toString();
        }

        //读取台词数组 [ "l1","l2" ]
        List<String> readArray() {

            List<String> texts = new ArrayList<>();

            expect(BRACKET_LEFT);
            if (peek() == BRACKET_RIGHT) {
                cursor++;
                skipBlank();
                return texts;
            }

            while (true) {
                texts.add(readQuoted());
                if (peek() == COMMA) {
                    cursor++;
                    continue;
                }
                expect(BRACKET_RIGHT);
                break;
            }

            return texts;
        }

        //匹配并消耗一个指定符号
        void expect(char expected) {

            skipBlank();
            if (cursor >= source.length() || source.charAt(cursor) != expected) {
                throw new IllegalArgumentException(ConstantPool.getMsg_Qsc_Invalid() + source);
            }
            cursor++;
            skipBlank();
        }

    }

}
