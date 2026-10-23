package top.nierqiaodov.packager;

import top.nierqiaodov.Element.Actor;
import top.nierqiaodov.Element.Background;
import top.nierqiaodov.Element.Line;
import top.nierqiaodov.Element.Music;
import top.nierqiaodov.Element.Scene;
import top.nierqiaodov.Element.Voice;
import top.nierqiaodov.handler.FileHandler;
import top.nierqiaodov.handler.SceneFileHandler;
import top.nierqiaodov.util.ConstantPool;
import top.nierqiaodov.util.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//打包部署:根据 qsc 工程文件编排,输出 qgl 成品(实际是 zip 压缩包,后缀改为 qgl 以注明文件特征)
//压缩包内结构:
//  project.qsc                     工程文件原样副本
//  manifest.txt                    总清单(场景顺序与各场景元素概况)
//  <场景代号>/scene.qsc             该场景的单场景 qsc 文本
//  <场景代号>/scene.txt             该场景的元素文本说明(分类与出现顺序)
//  <场景代号>/Background/<代号>.<后缀>
//  <场景代号>/Character/<序号>_<角色代号>_<表情代号>.<后缀>
//  <场景代号>/Music/<代号>.<后缀>
//  <场景代号>/Voice/<序号>_<语音代号>.<后缀>    序号 = 该语音所属台词的播放顺序
public class Packager {

    //执行打包,返回生成的 qgl 压缩包
    public static File doPackager(File projectFile) throws IOException {

        if (projectFile == null || !projectFile.isFile()) {
            throw new IOException(ConstantPool.getMsg_Project_Not_Found());
        }

        //按工程文件中的顺序读取全部场景
        List<Scene> scenes = SceneFileHandler.readProject(projectFile);
        if (scenes.isEmpty()) {
            throw new IOException(ConstantPool.getMsg_Project_Empty());
        }

        String projectCode = FileHandler.stripSuffix(projectFile.getName());

        //准备输出目录与输出文件
        File outputDir = FileHandler.getOutputDir();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException(ConstantPool.getMsg_Dir_Invalid() + outputDir.getAbsolutePath());
        }
        File outputFile = new File(outputDir, projectCode + ConstantPool.getQgl_Suffix());

        //第一遍:解析资源并生成总清单(同时收集缺失资源)
        List<String> manifestLines = new ArrayList<>();
        List<String> missingResources = new ArrayList<>();

        manifestLines.add(ConstantPool.getPackage_Header());
        manifestLines.add("Project: " + projectCode);
        manifestLines.add("Scene Count: " + scenes.size());
        manifestLines.add(ConstantPool.getVoid_String());

        for (Scene scene : scenes) {
            missingResources.addAll(SceneFileHandler.resolve(scene));
            manifestLines.addAll(describeScene(scene));
            manifestLines.add(ConstantPool.getVoid_String());
        }

        //第二遍:写入压缩包
        try (ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {

            zipStream.setLevel(Deflater.DEFAULT_COMPRESSION);

            //工程文件原样放入包内,便于播放器与编辑器互相校验
            putText(zipStream, ConstantPool.getPackage_Project_File(), Files.readString(projectFile.toPath(), StandardCharsets.UTF_8));

            //每个场景单独打包成一个文件夹
            for (Scene scene : scenes) {
                putScene(zipStream, scene);
            }

            //总清单最后写入
            putText(zipStream, ConstantPool.getPackage_Manifest_File(), String.join("\n", manifestLines));
        }

        if (!missingResources.isEmpty()) {
            Logger.error(ConstantPool.getMsg_Missing_Resource() + missingResources);
        }
        Logger.logger(ConstantPool.getPackage_Header() + ConstantPool.getColon() + projectCode
                + " [" + scenes.size() + "] -> " + outputFile.getAbsolutePath());

        return outputFile;
    }

    //写入一个场景文件夹:元素文件 + qsc 文本 + 元素说明
    static void putScene(ZipOutputStream zipStream, Scene scene) throws IOException {

        String basePath = scene.getCode() + ConstantPool.getSlash();

        //场景的 qsc 文本(播放器据此还原元素与顺序)
        putText(zipStream, basePath + ConstantPool.getPackage_Scene_File(), ResourceGenerator.scenePackager(scene));

        //元素的文本说明(分类与出现顺序)
        putText(zipStream, basePath + ConstantPool.getPackage_Scene_Text(), String.join("\n", describeScene(scene)));

        //背景(一个场景仅可出现一次)
        Background background = scene.getBackground();
        if (background != null && background.getFile() != null) {
            String fileName = background.getCode() + FileHandler.getSuffix(background.getFile().getName());
            putFile(zipStream, basePath + ConstantPool.getPackage_Background_Dir() + fileName, background.getFile());
        }

        //角色立绘(文件名带序号,解包后仍可还原从左到右的顺序)
        List<Actor> actors = scene.getActors();
        for (int i = 0; i < actors.size(); i++) {
            Actor actor = actors.get(i);
            if (actor == null || actor.getCharacter() == null || actor.getEmotion() == null || actor.getEmotion().getFile() == null) {
                continue;
            }
            String fileName = (i + 1) + ConstantPool.getPackage_Name_Sign()
                    + actor.getCharacter().getCode() + ConstantPool.getPackage_Name_Sign()
                    + actor.getEmotion().getCode()
                    + FileHandler.getSuffix(actor.getEmotion().getFile().getName());
            putFile(zipStream, basePath + ConstantPool.getPackage_Character_Dir() + fileName, actor.getEmotion().getFile());
        }

        //背景音乐
        Music music = scene.getMusic();
        if (music != null && music.getFile() != null) {
            String fileName = music.getCode() + FileHandler.getSuffix(music.getFile().getName());
            putFile(zipStream, basePath + ConstantPool.getPackage_Music_Dir() + fileName, music.getFile());
        }

        //台词语音(文件名带台词序号,播放器按序号对上每一句)
        List<Line> lines = (scene.getLines() == null) ? new ArrayList<>() : scene.getLines().getLines();
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            Voice voice = (line == null) ? null : line.getVoice();
            if (voice == null || voice.getFile() == null) {
                continue;
            }
            String fileName = (i + 1) + ConstantPool.getPackage_Name_Sign()
                    + voice.getCode()
                    + FileHandler.getSuffix(voice.getFile().getName());
            putFile(zipStream, basePath + ConstantPool.getPackage_Voice_Dir() + fileName, voice.getFile());
        }
    }

    //场景的元素文本说明:分类、代号与出现顺序
    static List<String> describeScene(Scene scene) {

        List<String> lines = new ArrayList<>();

        lines.add("[" + scene.getCode() + "]");

        //背景
        Background background = scene.getBackground();
        if (background == null) {
            lines.add("Background: (none)");
        } else {
            lines.add("Background: " + background.getCode() + describeFile(background.getFile()));
        }

        //角色(按从左到右的排列顺序)
        List<Actor> actors = scene.getActors();
        lines.add("Character Count: " + actors.size());
        for (int i = 0; i < actors.size(); i++) {
            Actor actor = actors.get(i);
            String characterCode = (actor == null || actor.getCharacter() == null) ? ConstantPool.getVoid_String() : actor.getCharacter().getCode();
            String emotionCode = (actor == null || actor.getEmotion() == null) ? ConstantPool.getVoid_String() : actor.getEmotion().getCode();
            lines.add("  " + (i + 1) + ". " + characterCode + " : " + emotionCode
                    + describeFile((actor == null || actor.getEmotion() == null) ? null : actor.getEmotion().getFile()));
        }

        //背景音乐
        Music music = scene.getMusic();
        if (music == null) {
            lines.add("Music: (none)");
        } else {
            lines.add("Music: " + music.getCode() + describeFile(music.getFile()));
        }

        //台词(按播放顺序,带配音的句子标出语音代号与文件)
        List<Line> dialogues = (scene.getLines() == null) ? new ArrayList<>() : scene.getLines().getLines();
        lines.add("Line Count: " + dialogues.size());
        for (int i = 0; i < dialogues.size(); i++) {
            Line line = dialogues.get(i);
            String text = (line == null || line.getText() == null) ? ConstantPool.getVoid_String() : line.getText();
            Voice voice = (line == null) ? null : line.getVoice();
            String voiceText = (voice == null)
                    ? ConstantPool.getVoid_String()
                    : "  <voice: " + voice.getCode() + describeFile(voice.getFile()) + ">";
            //手工换行在清单里用 \n 显示,保持编号整齐
            lines.add("  " + (i + 1) + ". " + text.replace("\n", "\\n") + voiceText);
        }

        return lines;
    }

    //描述资源文件(缺失时明确标出)
    static String describeFile(File file) {

        if (file == null) {
            return " (MISSING)";
        }

        return " (" + file.getName() + ")";
    }

    //写入文本条目(统一使用 UTF-8)
    static void putText(ZipOutputStream zipStream, String name, String text) throws IOException {

        zipStream.putNextEntry(new ZipEntry(name));
        zipStream.write(text.getBytes(StandardCharsets.UTF_8));
        zipStream.closeEntry();
    }

    //写入文件条目(按字节搬运,不改动资源内容)
    static void putFile(ZipOutputStream zipStream, String name, File file) throws IOException {

        zipStream.putNextEntry(new ZipEntry(name));
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                zipStream.write(buffer, 0, read);
            }
        }
        zipStream.closeEntry();
    }

}
