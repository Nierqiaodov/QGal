package top.nierqiaodov.player;

import top.nierqiaodov.player.Element.Actor;
import top.nierqiaodov.player.Element.Background;
import top.nierqiaodov.player.Element.Line;
import top.nierqiaodov.player.Element.Music;
import top.nierqiaodov.player.Element.Scene;
import top.nierqiaodov.player.util.ConstantPool;
import top.nierqiaodov.player.util.FileNames;
import top.nierqiaodov.player.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//拆包读取:把 qgl 成品解到临时目录,并按 project.qsc 的顺序还原全部场景
//包内结构见 README:
//  project.qsc / manifest.txt
//  <场景代号>/scene.qsc | scene.txt | Background/* | Character/<序号>_<角色>_<表情>.* | Music/*
public class PackageReader {

    //打开成品并完成拆包,返回可播放的工程
    public static QGalPackage open(File packageFile) throws IOException {

        if (packageFile == null || !packageFile.isFile()) {
            throw new IOException(ConstantPool.getMsg_Package_Not_Found());
        }

        //解包到临时目录
        File workDir = Files.createTempDirectory("qgal-play-").toFile();

        try {
            extract(packageFile, workDir);

            //工程文件决定场景顺序
            File projectFile = new File(workDir, ConstantPool.getPackage_Project_File());
            if (!projectFile.isFile()) {
                throw new IOException(ConstantPool.getMsg_Package_Invalid() + ConstantPool.getPackage_Project_File());
            }

            List<Scene> scenes = new ArrayList<>();
            for (String text : SceneParser.readSceneTexts(projectFile)) {
                Scene scene = SceneParser.parse(text);
                File sceneDir = new File(workDir, scene.getCode());
                if (!sceneDir.isDirectory()) {
                    Logger.error(ConstantPool.getMsg_Scene_Folder_Missing() + scene.getCode());
                    continue;
                }
                resolveScene(scene, sceneDir);
                scenes.add(scene);
            }

            if (scenes.isEmpty()) {
                throw new IOException(ConstantPool.getMsg_No_Scene());
            }

            Logger.logger(ConstantPool.getPackage_Header() + ConstantPool.getColon()
                    + FileNames.stripSuffix(packageFile.getName()) + " [" + scenes.size() + "]"
                    + ConstantPool.getColon() + workDir.getAbsolutePath());

            return new QGalPackage(packageFile, workDir, scenes);

        } catch (IOException | RuntimeException exception) {
            //失败时不留临时目录
            QGalPackage.deleteDirectory(workDir);
            throw exception;
        }
    }

    //把压缩包完整解到目标目录(带目录穿越防护)
    static void extract(File packageFile, File targetDir) throws IOException {

        String targetPath = targetDir.getCanonicalPath();

        try (ZipFile zipFile = new ZipFile(packageFile, StandardCharsets.UTF_8)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File targetFile = new File(targetDir, entry.getName());
                //拒绝 ../ 之类的越界条目
                if (!targetFile.getCanonicalPath().startsWith(targetPath)) {
                    throw new IOException(ConstantPool.getMsg_Package_Invalid() + entry.getName());
                }
                if (entry.isDirectory()) {
                    if (!targetFile.exists() && !targetFile.mkdirs()) {
                        throw new IOException(ConstantPool.getMsg_Dir_Invalid() + targetFile.getAbsolutePath());
                    }
                    continue;
                }
                File parent = targetFile.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    throw new IOException(ConstantPool.getMsg_Dir_Invalid() + parent.getAbsolutePath());
                }
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    //在场景文件夹里找到每个元素对应的文件
    static void resolveScene(Scene scene, File sceneDir) {

        //背景:Background/<背景代号>.<后缀>
        Background background = scene.getBackground();
        if (background != null) {
            File file = findCodeFile(new File(sceneDir, ConstantPool.getPackage_Background_Dir()), background.getCode());
            background.setFile(file);
            if (file == null) {
                Logger.error(ConstantPool.getMsg_Missing_Resource() + background.getCode());
            }
        }

        //背景音乐:Music/<音乐代号>.<后缀>
        Music music = scene.getMusic();
        if (music != null) {
            File file = findCodeFile(new File(sceneDir, ConstantPool.getPackage_Music_Dir()), music.getCode());
            music.setFile(file);
            if (file == null) {
                Logger.error(ConstantPool.getMsg_Missing_Resource() + music.getCode());
            }
        }

        //角色:Character/<序号>_<角色代号>_<表情代号>.<后缀>,序号即从左到右的顺序
        File characterDir = new File(sceneDir, ConstantPool.getPackage_Character_Dir());
        List<Actor> actors = scene.getActors();
        for (int i = 0; i < actors.size(); i++) {
            Actor actor = actors.get(i);
            if (actor == null || actor.getEmotion() == null) {
                continue;
            }
            String characterCode = (actor.getCharacter() == null) ? ConstantPool.getVoid_String() : actor.getCharacter().getCode();
            String emotionCode = actor.getEmotion().getCode();
            File file = findActorFile(characterDir, i + 1, characterCode, emotionCode);
            actor.getEmotion().setFile(file);
            if (file == null) {
                Logger.error(ConstantPool.getMsg_Missing_Resource() + SceneParser.describeActor(actor));
            }
        }

        //台词语音:Voice/<台词序号>_<语音代号>.<后缀>
        if (scene.getLines() != null) {
            File voiceDir = new File(sceneDir, ConstantPool.getPackage_Voice_Dir());
            List<Line> lines = scene.getLines().getLines();
            for (int i = 0; i < lines.size(); i++) {
                Line line = lines.get(i);
                if (line == null || line.getVoice() == null) {
                    continue;
                }
                String voiceCode = line.getVoice().getCode();
                File file = findVoiceFile(voiceDir, i + 1, voiceCode);
                line.getVoice().setFile(file);
                if (file == null) {
                    Logger.error(ConstantPool.getMsg_Missing_Resource()
                            + ConstantPool.getLabel_Voice() + ConstantPool.getColon() + voiceCode);
                }
            }
        }
    }

    //按台词序号查找语音文件,找不到时退化为按语音代号匹配
    static File findVoiceFile(File voiceDir, int order, String voiceCode) {

        File[] files = voiceDir.listFiles();
        if (files == null) {
            return null;
        }

        String orderPrefix = order + ConstantPool.getPackage_Name_Sign();
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith(orderPrefix)) {
                return file;
            }
        }

        String codeSuffix = ConstantPool.getPackage_Name_Sign() + voiceCode;
        for (File file : files) {
            if (file.isFile() && FileNames.stripSuffix(file.getName()).endsWith(codeSuffix)) {
                return file;
            }
        }

        return null;
    }

    //按代号查找文件(文件名去掉后缀等于代号)
    static File findCodeFile(File dir, String code) {

        File[] files = dir.listFiles();
        if (files == null || code == null) {
            return null;
        }

        for (File file : files) {
            if (file.isFile() && FileNames.stripSuffix(file.getName()).equals(code)) {
                return file;
            }
        }

        return null;
    }

    //按顺序号查找角色文件,找不到时退化为按 角色代号_表情代号 匹配
    static File findActorFile(File characterDir, int order, String characterCode, String emotionCode) {

        File[] files = characterDir.listFiles();
        if (files == null) {
            return null;
        }

        String orderPrefix = order + ConstantPool.getPackage_Name_Sign();
        String codeSuffix = characterCode + ConstantPool.getPackage_Name_Sign() + emotionCode;

        for (File file : files) {
            if (file.isFile() && file.getName().startsWith(orderPrefix)) {
                return file;
            }
        }

        for (File file : files) {
            if (file.isFile() && FileNames.stripSuffix(file.getName()).endsWith(codeSuffix)) {
                return file;
            }
        }

        return null;
    }

}
