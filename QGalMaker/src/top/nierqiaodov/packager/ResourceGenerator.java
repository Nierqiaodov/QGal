package top.nierqiaodov.packager;

import top.nierqiaodov.Element.Actor;
import top.nierqiaodov.Element.Background;
import top.nierqiaodov.Element.Character;
import top.nierqiaodov.Element.CharacterEmotion;
import top.nierqiaodov.Element.Line;
import top.nierqiaodov.Element.Lines;
import top.nierqiaodov.Element.Music;
import top.nierqiaodov.Element.Scene;
import top.nierqiaodov.Element.Voice;
import top.nierqiaodov.util.ConstantPool;

import java.util.ArrayList;
import java.util.List;

//对Scene实例进行包装实现:负责 Scene 实例与 qsc 文本之间的编解码
//qsc 单场景文本格式:
//  s1{"b1";"c1:e1,c2:e1";"m1";["台词1","台词2"];["v1",""]}
//  s1            场景代号
//  "b1"          背景代号,一个场景仅可出现一次,可为空串
//  "c1:e1,..."   角色及其表情,可出现多个,按从左到右的顺序排列
//  "m1"          背景音乐代号,可为空串
//  ["台词..."]    台词,按播放顺序排列,可为空数组
//  ["v1",""]     可选:每句台词的语音代号,与台词一一对应,空串表示该句没有配音;
//                整个场景都没有配音时这一段整体省略(与旧版 qsc 完全兼容)
public class ResourceGenerator {

    //qsc 语法符号(统一从常量池取,保证与解析逻辑单一来源)
    static final char BRACE_LEFT = ConstantPool.getBrace_Left().charAt(0);
    static final char BRACE_RIGHT = ConstantPool.getBrace_Right().charAt(0);
    static final char BRACKET_LEFT = ConstantPool.getBracket_Left().charAt(0);
    static final char BRACKET_RIGHT = ConstantPool.getBracket_Right().charAt(0);
    static final char QUOTE = ConstantPool.getQuote().charAt(0);
    static final char SEMICOLON = ConstantPool.getSemicolon().charAt(0);
    static final char COMMA = ConstantPool.getComma().charAt(0);
    static final char COLON = ConstantPool.getColon().charAt(0);
    static final char ESCAPE = ConstantPool.getEscape().charAt(0);

    //把 Scene 实例包装成 qsc 单场景文本
    public static String scenePackager(Scene scene) {

        StringBuilder builder = new StringBuilder();

        //场景代号
        builder.append(scene.getCode()).append(BRACE_LEFT);

        //背景代号(一个场景仅一次,没有背景时写空串)
        builder.append(QUOTE);
        if (scene.getBackground() != null) {
            builder.append(scene.getBackground().getCode());
        }
        builder.append(QUOTE).append(SEMICOLON);

        //角色及表情(列表顺序即从左到右的顺序,角色与表情之间用冒号,角色之间用逗号)
        builder.append(QUOTE);
        List<Actor> actors = scene.getActors();
        for (int i = 0; actors != null && i < actors.size(); i++) {
            if (i > 0) {
                builder.append(COMMA);
            }
            Actor actor = actors.get(i);
            builder.append(actor.getCharacter().getCode()).append(COLON).append(actor.getEmotion().getCode());
        }
        builder.append(QUOTE).append(SEMICOLON);

        //背景音乐代号
        builder.append(QUOTE);
        if (scene.getMusic() != null) {
            builder.append(scene.getMusic().getCode());
        }
        builder.append(QUOTE).append(SEMICOLON);

        //台词(方括号内,转义后以逗号分隔)
        builder.append(BRACKET_LEFT);
        List<Line> lines = (scene.getLines() == null) ? new ArrayList<>() : scene.getLines().getLines();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                builder.append(COMMA);
            }
            Line line = lines.get(i);
            builder.append(QUOTE).append(escape((line == null) ? ConstantPool.getVoid_String() : line.getText())).append(QUOTE);
        }
        builder.append(BRACKET_RIGHT);

        //第五段:每句台词的语音代号(全都没有配音时整段省略)
        if (hasAnyVoice(lines)) {
            builder.append(SEMICOLON).append(BRACKET_LEFT);
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0) {
                    builder.append(COMMA);
                }
                Voice voice = (lines.get(i) == null) ? null : lines.get(i).getVoice();
                builder.append(QUOTE);
                if (voice != null && voice.getCode() != null) {
                    builder.append(voice.getCode());
                }
                builder.append(QUOTE);
            }
            builder.append(BRACKET_RIGHT);
        }

        builder.append(BRACE_RIGHT);

        return builder.toString();
    }

    //判断台词里是否存在配音
    static boolean hasAnyVoice(List<Line> lines) {

        for (Line line : lines) {
            if (line != null && line.getVoice() != null
                    && line.getVoice().getCode() != null && !line.getVoice().getCode().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    //把 qsc 单场景文本拆解回 Scene 实例(只解析代号与台词,文件引用由 SceneFileHandler.resolve 补充)
    public static Scene sceneUnpackager(String text) {

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

    //把场景角色写成 c1:e1 的形式(界面列表与信息条共用)
    public static String describeActor(Actor actor) {

        String characterCode = (actor == null || actor.getCharacter() == null)
                ? ConstantPool.getVoid_String() : actor.getCharacter().getCode();
        String emotionCode = (actor == null || actor.getEmotion() == null)
                ? ConstantPool.getVoid_String() : actor.getEmotion().getCode();

        return characterCode + ConstantPool.getColon() + emotionCode;
    }

    //把一句台词写成列表里显示的样子(有配音时前面加 [语音代号])
    public static String describeLine(Line line) {

        if (line == null) {
            return ConstantPool.getVoid_String();
        }

        String text = (line.getText() == null) ? ConstantPool.getVoid_String() : line.getText().replace("\n", " ");

        Voice voice = line.getVoice();
        if (voice == null || voice.getCode() == null || voice.getCode().isEmpty()) {
            return text;
        }

        return ConstantPool.getBracket_Left() + voice.getCode() + ConstantPool.getBracket_Right() + " " + text;
    }

    //解析角色串 "c1:e1,c2:e1"(顺序即从左到右的排列顺序)
    public static List<Actor> parseActors(String castText) {

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

    //转义台词中的特殊字符,保证 qsc 文本可以被完整还原
    static String escape(String text) {

        if (text == null) {
            return ConstantPool.getVoid_String();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == ESCAPE) {
                builder.append(ESCAPE).append(ESCAPE);
            } else if (character == QUOTE) {
                builder.append(ESCAPE).append(QUOTE);
            } else if (character == '\n') {
                builder.append(ESCAPE).append('n');
            } else if (character == '\t') {
                builder.append(ESCAPE).append('t');
            } else if (character != '\r') {
                //回车符直接丢弃,换行交给 \n 表示
                builder.append(character);
            }
        }

        return builder.toString();
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

        //读取字符串数组 [ "a","b" ]
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
