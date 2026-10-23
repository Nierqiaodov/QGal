package top.nierqiaodov.util;

import top.nierqiaodov.Element.Line;
import top.nierqiaodov.Element.Scene;
import top.nierqiaodov.Element.Voice;
import top.nierqiaodov.handler.FileHandler;
import top.nierqiaodov.handler.SceneFileHandler;
import top.nierqiaodov.packager.Packager;
import top.nierqiaodov.packager.ResourceGenerator;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//无界面自检:验证中文支持、qsc 编解码(含语音)与 qgl 打包流程(启动参数 --selftest)
public class SelfTest {

    //测试资源与工程统一使用此前缀,结束后全部清理
    static final String TEST_PREFIX = "zzselftest";

    //执行全部自检项,返回是否全部通过
    public static boolean run() {

        boolean passed = true;

        passed &= checkLang();
        passed &= checkFont();
        passed &= checkTextWrap();
        passed &= checkCodec();
        passed &= checkPackage();

        return passed;
    }

    //中文界面文案是否齐全,并验证语言热切换
    static boolean checkLang() {

        //触发常量池初始化
        String addResource = ConstantPool.getAdd_Resource();

        boolean noMissing = Lang.missingKeys().isEmpty();
        boolean chineseText = !Lang.isChinese() || containsChinese(addResource);

        //热切换:切到英文后常量池文案应变成英文,切回中文应还原
        Lang.setLanguage(Lang.EN);
        ConstantPool.reload();
        String englishText = ConstantPool.getAdd_Resource();

        Lang.setLanguage(Lang.ZH);
        ConstantPool.reload();
        String chineseText2 = ConstantPool.getAdd_Resource();

        boolean switched = "ADD RESOURCE".equals(englishText)
                && containsChinese(chineseText2)
                && !englishText.equals(chineseText2);

        Logger.logger("language: " + Lang.getCurrent()
                + " / missing: " + Lang.missingKeys()
                + " / hot switch: " + (switched ? "OK" : "FAILED"));

        return report("lang zh", noMissing && chineseText && switched);
    }

    //中文字体是否可用(否则中文会变成方块)
    static boolean checkFont() {

        Font font = Fonts.plain(20);
        boolean passed = font.canDisplay('中') && font.canDisplay('文') && font.canDisplay('，');

        Logger.logger("font family: " + Fonts.getFamily());

        return report("font cjk", passed);
    }

    //中英混排换行:不能把标点顶到行首,也不能把西文单词拆断
    static boolean checkTextWrap() {

        BufferedImage canvas = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setFont(Fonts.plain(16));
        FontMetrics metrics = g.getFontMetrics();

        String text = "这是一段中文台词，需要在宽度不够的时候自动换行，English words should not be split in the middle.";
        List<String> lines = TextWrap.wrap(text, metrics, 260);

        boolean noLineStartsWithPunctuation = true;
        boolean widthOk = true;
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            String start = String.valueOf(line.charAt(0));
            if ("，。、；：？！）】".contains(start)) {
                noLineStartsWithPunctuation = false;
            }
            //允许行尾挂一个标点,所以放宽 20 像素
            if (metrics.stringWidth(line) > 280) {
                widthOk = false;
            }
        }

        Logger.logger("wrap lines: " + lines);
        g.dispose();

        return report("text wrap", lines.size() > 1 && noLineStartsWithPunctuation && widthOk);
    }

    //校验 qsc 单场景文本的编解码(含转义与语音)
    static boolean checkCodec() {

        //构造一个包含特殊字符与配音的场景
        Scene scene = ResourceGenerator.sceneUnpackager(
                "s1{\"b1\";\"c1:e1,c2:e1\";\"m1\";[\"第一句\",\"第二句；带分号\",\"第三句\"]}");

        scene.getLines().getLines().get(1).setVoice(new Voice("v1", null));
        scene.getLines().getLines().get(2).setText("带引号\"与反斜杠\\的台词\n第二行");

        String text = ResourceGenerator.scenePackager(scene);
        Logger.logger("qsc: " + text);

        Scene decoded = ResourceGenerator.sceneUnpackager(text);
        List<Line> lines = decoded.getLines().getLines();

        boolean passed = "s1".equals(decoded.getCode())
                && decoded.getBackground() != null && "b1".equals(decoded.getBackground().getCode())
                && decoded.getMusic() != null && "m1".equals(decoded.getMusic().getCode())
                && decoded.getActors().size() == 2
                && "c1".equals(decoded.getActors().get(0).getCharacter().getCode())
                && "e1".equals(decoded.getActors().get(0).getEmotion().getCode())
                && lines.size() == 3
                && lines.get(0).getVoice() == null
                && lines.get(1).getVoice() != null && "v1".equals(lines.get(1).getVoice().getCode())
                && decodeEquals(scene, decoded);

        //旧版四段 qsc(没有语音段)必须仍然可以解析
        Scene legacy = ResourceGenerator.sceneUnpackager("s1{\"b1\";\"c1:e1\";\"m1\";[\"只有台词\"]}");
        boolean legacyPassed = legacy.getLines().getLines().size() == 1
                && legacy.getLines().getLines().get(0).getVoice() == null;

        return report("qsc codec", passed && legacyPassed);
    }

    //逐句比较台词文本与语音
    static boolean decodeEquals(Scene first, Scene second) {

        List<Line> firstLines = first.getLines().getLines();
        List<Line> secondLines = second.getLines().getLines();

        if (firstLines.size() != secondLines.size()) {
            return false;
        }

        for (int i = 0; i < firstLines.size(); i++) {
            if (!firstLines.get(i).getText().equals(secondLines.get(i).getText())) {
                return false;
            }
        }

        return true;
    }

    //校验工程文件读写与 qgl 打包(临时资源,结束后清理)
    static boolean checkPackage() {

        List<File> createdFiles = new ArrayList<>();
        File emotionDir = FileHandler.getEmotionDir(TEST_PREFIX);

        try {
            //准备测试资源:背景图、角色表情图、音乐与语音占位文件
            createdFiles.add(writeTestImage(new File(FileHandler.getBackgroundDir(), TEST_PREFIX + "bg.png"), 640, 360));
            createdFiles.add(writeTestImage(new File(emotionDir, TEST_PREFIX + "e.png"), 300, 600));
            createdFiles.add(writeTestFile(new File(FileHandler.getMusicDir(), TEST_PREFIX + "m.ogg"), "QGal test music"));
            createdFiles.add(writeTestFile(new File(FileHandler.getVoiceDir(), TEST_PREFIX + "v.mp3"), "QGal test voice"));

            //场景:第二句带语音,验证语音能跟着台词序号进包
            Scene scene = ResourceGenerator.sceneUnpackager("s1{\"" + TEST_PREFIX + "bg\";\""
                    + TEST_PREFIX + ":" + TEST_PREFIX + "e\";\"" + TEST_PREFIX + "m\";"
                    + "[\"第一句\",\"第二句有配音\"];[\"\",\"" + TEST_PREFIX + "v\"]}");
            scene.getLines().getLines().get(1).setVoice(setCode(scene.getLines().getLines().get(1).getVoice(), TEST_PREFIX + "v"));

            File projectFile = new File(FileHandler.getSceneDir(), TEST_PREFIX + ConstantPool.getQsc_Suffix());
            SceneFileHandler.writeLines(projectFile, new ArrayList<>());
            createdFiles.add(projectFile);
            SceneFileHandler.saveScene(projectFile, scene);

            //按工程文件打包
            File outputFile = Packager.doPackager(projectFile);
            createdFiles.add(outputFile);

            //检查压缩包条目
            List<String> entries = new ArrayList<>();
            try (ZipFile zipFile = new ZipFile(outputFile)) {
                Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                while (enumeration.hasMoreElements()) {
                    entries.add(enumeration.nextElement().getName());
                }
            }
            Logger.logger("zip entries: " + entries);

            String basePath = "s1" + ConstantPool.getSlash();
            boolean passed = entries.contains(ConstantPool.getPackage_Project_File())
                    && entries.contains(ConstantPool.getPackage_Manifest_File())
                    && entries.contains(basePath + ConstantPool.getPackage_Scene_File())
                    && entries.contains(basePath + ConstantPool.getPackage_Scene_Text())
                    && entries.contains(basePath + ConstantPool.getPackage_Background_Dir() + TEST_PREFIX + "bg.png")
                    && entries.contains(basePath + ConstantPool.getPackage_Character_Dir() + "1_"
                    + TEST_PREFIX + "_" + TEST_PREFIX + "e.png")
                    && entries.contains(basePath + ConstantPool.getPackage_Music_Dir() + TEST_PREFIX + "m.ogg")
                    && entries.contains(basePath + ConstantPool.getPackage_Voice_Dir() + "2_"
                    + TEST_PREFIX + "v.mp3");

            return report("qgl package", passed);

        } catch (IOException exception) {
            Logger.error(exception.getMessage());
            return report("qgl package", false);
        } finally {
            //清理测试产物
            for (File file : createdFiles) {
                if (file != null && file.isFile() && !file.delete()) {
                    Logger.error("cleanup failed: " + file.getAbsolutePath());
                }
            }
            File[] leftFiles = emotionDir.listFiles();
            if (leftFiles != null && leftFiles.length == 0 && !emotionDir.delete()) {
                Logger.error("cleanup failed: " + emotionDir.getAbsolutePath());
            }
        }
    }

    //改写语音代号
    static Voice setCode(Voice voice, String code) {
        Voice result = (voice == null) ? new Voice() : voice;
        result.setCode(code);
        return result;
    }

    //生成一张测试图片
    static File writeTestImage(File file, int width, int height) throws IOException {

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException(ConstantPool.getMsg_Dir_Invalid() + parent.getAbsolutePath());
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(60, 60, 90));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(210, 210, 230));
        g.drawString(file.getName(), 10, 20);
        g.dispose();

        ImageIO.write(image, "png", file);

        return file;
    }

    //生成一个测试用普通文件(音频只关心字节搬运)
    static File writeTestFile(File file, String content) throws IOException {

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException(ConstantPool.getMsg_Dir_Invalid() + parent.getAbsolutePath());
        }

        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));

        return file;
    }

    //是否包含汉字
    static boolean containsChinese(String text) {

        if (text == null) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character >= '\u4e00' && character <= '\u9fa5') {
                return true;
            }
        }

        return false;
    }

    //输出单项自检结果
    static boolean report(String item, boolean passed) {
        Logger.logger("SELFTEST [" + item + "] " + (passed ? "OK" : "FAILED"));
        return passed;
    }

}
