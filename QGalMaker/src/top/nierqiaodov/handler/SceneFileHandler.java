package top.nierqiaodov.handler;

import top.nierqiaodov.Element.Actor;
import top.nierqiaodov.Element.Background;
import top.nierqiaodov.Element.Line;
import top.nierqiaodov.Element.Music;
import top.nierqiaodov.Element.Scene;
import top.nierqiaodov.packager.ResourceGenerator;
import top.nierqiaodov.util.ConstantPool;
import top.nierqiaodov.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//场景工程文件(.qsc)读写:一个工程文件可以包含多个场景,每行一个场景
public class SceneFileHandler {

    //列出所有工程代号(Scenes 目录下的 .qsc 文件名)
    public static String[] listProjectCodes() {
        return FileHandler.listCodes(FileHandler.getSceneDir(), ConstantPool.getQsc_Suffix());
    }

    //读取工程文件中的全部场景(按文件中的顺序,重复代号只保留第一个)
    public static List<Scene> readProject(File projectFile) throws IOException {

        Map<String, Scene> sceneMap = new LinkedHashMap<>();

        for (String text : readSceneTexts(projectFile)) {
            try {
                Scene scene = ResourceGenerator.sceneUnpackager(text);
                if (sceneMap.containsKey(scene.getCode())) {
                    Logger.error(scene.getCode() + ConstantPool.getMsg_Duplicated_Code() + text);
                    continue;
                }
                sceneMap.put(scene.getCode(), scene);
            } catch (IllegalArgumentException exception) {
                Logger.error(exception.getMessage());
            }
        }

        return new ArrayList<>(sceneMap.values());
    }

    //读取工程文件中的全部场景文本(跳过空行与注释行)
    public static List<String> readSceneTexts(File projectFile) throws IOException {

        List<String> texts = new ArrayList<>();

        for (String line : readRawLines(projectFile)) {
            String text = line.trim();
            if (text.isEmpty() || text.startsWith(ConstantPool.getComment_Sign())) {
                continue;
            }
            texts.add(text);
        }

        return texts;
    }

    //读取工程文件的原始行(保留注释与书写顺序,便于人工修改工程文件)
    public static List<String> readRawLines(File projectFile) throws IOException {
        if (projectFile == null || !projectFile.isFile()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Files.readAllLines(projectFile.toPath(), StandardCharsets.UTF_8));
    }

    //把场景写入工程文件:同代号就整行覆盖,否则追加到文件末尾
    public static void saveScene(File projectFile, Scene scene) throws IOException {

        List<String> lines = readRawLines(projectFile);
        String text = ResourceGenerator.scenePackager(scene);
        String head = scene.getCode() + ConstantPool.getBrace_Left();

        int replaceIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().startsWith(head)) {
                replaceIndex = i;
                break;
            }
        }

        if (replaceIndex >= 0) {
            lines.set(replaceIndex, text);
        } else {
            lines.add(text);
        }

        writeLines(projectFile, lines);
        Logger.logger(projectFile.getAbsolutePath() + ConstantPool.getInfo_10() + text);
    }

    //写入工程文件(父目录不存在时自动建立)
    public static void writeLines(File projectFile, List<String> lines) throws IOException {

        File parent = (projectFile == null) ? null : projectFile.getParentFile();
        if (projectFile == null) {
            throw new IOException(ConstantPool.getMsg_Project_Not_Found());
        }
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException(ConstantPool.getMsg_Dir_Invalid() + parent.getAbsolutePath());
        }

        Files.write(projectFile.toPath(), lines, StandardCharsets.UTF_8);
    }

    //按代号取出工程文件中的一个场景,找不到返回 null
    public static Scene findScene(File projectFile, String sceneCode) throws IOException {
        for (Scene scene : readProject(projectFile)) {
            if (scene.getCode().equals(sceneCode)) {
                return scene;
            }
        }
        return null;
    }

    //把场景中的代号解析为真实资源文件,返回缺失资源的说明列表(空表表示全部找到)
    public static List<String> resolve(Scene scene) {

        List<String> missing = new ArrayList<>();
        if (scene == null) {
            return missing;
        }

        //背景(一个场景中仅可出现一次)
        Background background = scene.getBackground();
        if (background != null) {
            File file = FileHandler.findResource(FileHandler.getBackgroundDir(), background.getCode());
            background.setFile(file);
            if (file == null) {
                missing.add(ConstantPool.getLabel_Background() + ConstantPool.getColon() + background.getCode());
            }
        }

        //背景音乐
        Music music = scene.getMusic();
        if (music != null) {
            File file = FileHandler.findResource(FileHandler.getMusicDir(), music.getCode());
            music.setFile(file);
            if (file == null) {
                missing.add(ConstantPool.getLabel_Music() + ConstantPool.getColon() + music.getCode());
            }
        }

        //角色及表情
        for (Actor actor : scene.getActors()) {
            if (actor == null || actor.getCharacter() == null) {
                continue;
            }
            String characterCode = actor.getCharacter().getCode();
            String emotionCode = (actor.getEmotion() == null) ? ConstantPool.getVoid_String() : actor.getEmotion().getCode();
            File file = FileHandler.findEmotion(characterCode, emotionCode);
            if (actor.getEmotion() != null) {
                actor.getEmotion().setFile(file);
            }
            if (file == null) {
                missing.add(ConstantPool.getLabel_Character() + ConstantPool.getColon() + characterCode + ConstantPool.getColon() + emotionCode);
            }
        }

        //台词语音
        if (scene.getLines() != null) {
            for (Line line : scene.getLines().getLines()) {
                if (line == null || line.getVoice() == null) {
                    continue;
                }
                String voiceCode = line.getVoice().getCode();
                File file = FileHandler.findResource(FileHandler.getVoiceDir(), voiceCode);
                line.getVoice().setFile(file);
                if (file == null) {
                    missing.add(ConstantPool.getLabel_Voice() + ConstantPool.getColon() + voiceCode);
                }
            }
        }

        return missing;
    }

    //推荐一个未被占用的场景代号(s1/s2/...)
    public static String suggestSceneCode(File projectFile) {

        List<String> usedCodes = new ArrayList<>();
        try {
            for (Scene scene : readProject(projectFile)) {
                usedCodes.add(scene.getCode());
            }
        } catch (IOException exception) {
            Logger.error(exception.getMessage());
        }

        for (int i = 1; i < 1000; i++) {
            String code = ConstantPool.getScene_Code_Prefix() + i;
            if (!usedCodes.contains(code)) {
                return code;
            }
        }

        return ConstantPool.getScene_Code_Prefix() + usedCodes.size();
    }

    //推荐一个未被占用的工程代号(p1/p2/...)
    public static String suggestProjectCode() {

        String[] usedCodes = listProjectCodes();

        for (int i = 1; i < 1000; i++) {
            String code = ConstantPool.getProject_Code_Prefix() + i;
            if (!java.util.Arrays.asList(usedCodes).contains(code)) {
                return code;
            }
        }

        return ConstantPool.getProject_Code_Prefix() + usedCodes.length;
    }

}
