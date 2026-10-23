package top.nierqiaodov.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import top.nierqiaodov.handler.FileHandler;
import top.nierqiaodov.handler.SceneFileHandler;
import top.nierqiaodov.packager.Packager;
import top.nierqiaodov.util.ConstantPool;
import top.nierqiaodov.util.Lang;
import top.nierqiaodov.util.Logger;
import top.nierqiaodov.util.StringCombiner;

//主编辑器界面
public class UI {

    //主窗口(语言热切换后会重建)
    static JFrame mainFrame = null;

    //附属窗口(进入子页面时登记,语言切换时一并关闭)
    static final List<JFrame> auxFrames = new ArrayList<>();

    //主界面
    public static void mainUi() {

        //初始化外观:必须在创建任何组件之前
        Theme.apply();

        buildHome();
    }

    //构建首页
    static void buildHome() {

        closeAuxFrames();

        if (mainFrame != null) {
            mainFrame.dispose();
        }

        JFrame frame = UiBuilder.frame(StringCombiner.combine(new String[]{
                ConstantPool.getApplication(), ConstantPool.getDescription(), ConstantPool.getVersion()}), 920, 680);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Logger.logger(ConstantPool.getInfo_5());
            }
        });
        mainFrame = frame;

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        //1.顶栏:应用名 + 说明 + 语言热切换按钮
        root.add(buildHomeHeader(), BorderLayout.NORTH);

        //2.中部:标语 + 三个功能卡片
        root.add(buildHomeMenu(), BorderLayout.CENTER);

        //3.底栏:工作目录与版本
        root.add(buildHomeStatus(), BorderLayout.SOUTH);

        frame.add(root);
        frame.setVisible(true);
    }

    //首页顶栏(语言热切换按钮就在这里)
    static JPanel buildHomeHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(new EmptyBorder(14, 22, 14, 18));

        JLabel appLabel = UiBuilder.title(ConstantPool.getApplication());
        appLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel descLabel = UiBuilder.dimLabel(ConstantPool.getDescription() + "  ·  v" + ConstantPool.getVersion());
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(appLabel);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(descLabel);

        //语言热切换:按钮显示"另一种语言"的名字
        JButton languageButton = UiBuilder.ghostButton(Lang.otherLanguageName());
        languageButton.setToolTipText(ConstantPool.getDialog_Language_Title());
        languageButton.addActionListener(e -> switchLanguage());

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        headerRight.setOpaque(false);
        headerRight.add(languageButton);

        header.add(titleBox, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        return header;
    }

    //首页功能卡片
    static JPanel buildHomeMenu() {

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(new EmptyBorder(26, 0, 26, 0));

        //标语
        JLabel slogan = UiBuilder.dimLabel(ConstantPool.getSlogan());
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(slogan);
        column.add(Box.createVerticalStrut(22));

        //添加资源
        MenuCard addCard = UiBuilder.menuCard(ConstantPool.getAdd_Resource(), ConstantPool.getAdd_Resource_Desc(), Theme.ACCENT);
        addCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        addCard.addActionListener(e -> {
            hideHome();
            openAddPage();
        });

        //创建场景
        MenuCard sceneCard = UiBuilder.menuCard(ConstantPool.getCreate_Scene(), ConstantPool.getCreate_Scene_Desc(), Theme.ACCENT_ALT);
        sceneCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        sceneCard.addActionListener(e -> {
            hideHome();
            SceneUI.sceneUi(mainFrame);
        });

        //打包
        MenuCard packageCard = UiBuilder.menuCard(ConstantPool.getDo_Package(), ConstantPool.getDo_Package_Desc(), Theme.ACCENT_COOL);
        packageCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        packageCard.addActionListener(e -> openPackagePage());

        column.add(addCard);
        column.add(Box.createVerticalStrut(14));
        column.add(sceneCard);
        column.add(Box.createVerticalStrut(14));
        column.add(packageCard);

        center.add(column);

        return center;
    }

    //首页底栏
    static JPanel buildHomeStatus() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.HEADER);
        footer.setBorder(new EmptyBorder(9, 22, 9, 22));

        JLabel workLabel = UiBuilder.dimLabel(ConstantPool.getHome_Status() + " " + FileHandler.getWorkDir().getAbsolutePath());
        JLabel langLabel = UiBuilder.dimLabel("v" + ConstantPool.getVersion() + "  ·  " + Lang.getCurrent());

        footer.add(workLabel, BorderLayout.WEST);
        footer.add(langLabel, BorderLayout.EAST);

        return footer;
    }

    //语言热切换:重载文案并重建首页
    static void switchLanguage() {

        Lang.toggle();
        ConstantPool.reload();
        Logger.logger(ConstantPool.getDialog_Language_Title() + " -> " + Lang.getCurrent());

        buildHome();
    }

    //隐藏首页(进入子页面)
    static void hideHome() {
        if (mainFrame != null) {
            mainFrame.setVisible(false);
        }
    }

    //显示首页
    static void showHome() {
        if (mainFrame != null) {
            mainFrame.setVisible(true);
        }
    }

    //登记附属窗口
    static void registerAux(JFrame frame) {
        auxFrames.add(frame);
    }

    //关闭全部附属窗口
    static void closeAuxFrames() {
        for (JFrame frame : new ArrayList<>(auxFrames)) {
            frame.dispose();
        }
        auxFrames.clear();
    }

    //资源添加菜单
    static void openAddPage() {

        JFrame frame = UiBuilder.frame(ConstantPool.getAdd_Resource(), 780, 720);
        registerAux(frame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        JButton backButton = UiBuilder.ghostButton("<  " + ConstantPool.getBack());
        backButton.addActionListener(e -> frame.dispose());
        root.add(UiBuilder.header(ConstantPool.getAdd_Resource(), backButton), BorderLayout.NORTH);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(new EmptyBorder(18, 24, 18, 24));

        //五种资源
        MenuCard backgroundCard = UiBuilder.menuCard(ConstantPool.getAdd_Background(), ConstantPool.getAdd_Background_Desc(), Theme.ACCENT);
        backgroundCard.addActionListener(e -> addBackground(frame));

        MenuCard characterCard = UiBuilder.menuCard(ConstantPool.getAdd_Character(), ConstantPool.getAdd_Character_Desc(), Theme.ACCENT_ALT);
        characterCard.addActionListener(e -> addCharacter(frame));

        MenuCard emotionCard = UiBuilder.menuCard(ConstantPool.getAdd_Emotion(), ConstantPool.getAdd_Emotion_Desc(), Theme.ACCENT_COOL);
        emotionCard.addActionListener(e -> addEmotion(frame));

        MenuCard musicCard = UiBuilder.menuCard(ConstantPool.getAdd_Music(), ConstantPool.getAdd_Music_Desc(), Theme.ACCENT);
        musicCard.addActionListener(e -> addMusic(frame));

        MenuCard voiceCard = UiBuilder.menuCard(ConstantPool.getAdd_Voice(), ConstantPool.getAdd_Voice_Desc(), Theme.ACCENT_ALT);
        voiceCard.addActionListener(e -> addVoice(frame));

        for (MenuCard card : new MenuCard[]{backgroundCard, characterCard, emotionCard, musicCard, voiceCard}) {
            card.setAlignmentX(Component.CENTER_ALIGNMENT);
            column.add(card);
            column.add(Box.createVerticalStrut(12));
        }

        root.add(column, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                auxFrames.remove(frame);
                showHome();
            }
        });

        frame.add(root);
        frame.setVisible(true);
    }

    //添加背景:代号 + 图片文件
    public static void addBackground(JFrame parent) {
        importWindow(parent, ConstantPool.getAdd_Background(),
                new String[]{ConstantPool.getLabel_Background()},
                new JComponent[]{UiBuilder.textField(18)},
                values -> FileHandler.getBackgroundDir());
    }

    //添加角色:角色代号 + 第一个表情代号(角色本身是 Character 目录下的一个子目录)
    public static void addCharacter(JFrame parent) {
        importWindow(parent, ConstantPool.getAdd_Character(),
                new String[]{ConstantPool.getLabel_Character(), ConstantPool.getLabel_Emotion()},
                new JComponent[]{UiBuilder.textField(18), UiBuilder.textField(18)},
                values -> FileHandler.getEmotionDir(values[0]));
    }

    //添加表情:为已有角色补充表情
    public static void addEmotion(JFrame parent) {

        String[] characterCodes = FileHandler.listDirectoryCodes(FileHandler.getCharacterDir());
        if (characterCodes.length == 0) {
            UiBuilder.warn(parent, ConstantPool.getMsg_No_Resource());
            return;
        }

        importWindow(parent, ConstantPool.getAdd_Emotion(),
                new String[]{ConstantPool.getLabel_Character(), ConstantPool.getLabel_Emotion()},
                new JComponent[]{UiBuilder.comboBox(characterCodes), UiBuilder.textField(18)},
                values -> FileHandler.getEmotionDir(values[0]));
    }

    //添加背景音乐:代号 + 音乐文件
    public static void addMusic(JFrame parent) {
        importWindow(parent, ConstantPool.getAdd_Music(),
                new String[]{ConstantPool.getLabel_Music()},
                new JComponent[]{UiBuilder.textField(18)},
                values -> FileHandler.getMusicDir());
    }

    //添加角色语音:代号 + 语音文件(一句台词一段配音)
    public static void addVoice(JFrame parent) {
        importWindow(parent, ConstantPool.getAdd_Voice(),
                new String[]{ConstantPool.getLabel_Voice()},
                new JComponent[]{UiBuilder.textField(18)},
                values -> FileHandler.getVoiceDir());
    }

    //通用资源导入窗口:labels 与 inputs 一一对应,最后一个输入项作为资源代号
    static void importWindow(JFrame parent, String title, String[] labels, JComponent[] inputs, Function<String[], File> dirResolver) {

        JFrame window = UiBuilder.frame(title, 560, 210 + labels.length * 62);
        registerAux(window);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(UiBuilder.header(title), BorderLayout.NORTH);

        //表单卡片
        RoundedPanel card = UiBuilder.card(null);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        //已选择的源文件
        final File[] selectedFile = {null};
        final JLabel fileNameLabel = UiBuilder.dimLabel(ConstantPool.getVoid_String());

        //代号等输入项
        for (int i = 0; i < labels.length; i++) {
            JLabel fieldLabel = UiBuilder.dimLabel(labels[i]);
            fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            inputs[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            inputs[i].setAlignmentX(Component.LEFT_ALIGNMENT);

            card.add(fieldLabel);
            card.add(Box.createVerticalStrut(4));
            card.add(inputs[i]);
            card.add(Box.createVerticalStrut(12));
        }

        //选择本地文件
        JButton chooseFileButton = UiBuilder.smallButton(ConstantPool.getChoose_File(), 120);
        chooseFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fileChooser.getSelectedFile();
                fileNameLabel.setText(selectedFile[0].getName());
            }
        });

        JPanel fileRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fileRow.setOpaque(false);
        fileRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileRow.add(chooseFileButton);
        fileRow.add(fileNameLabel);
        card.add(fileRow);
        card.add(Box.createVerticalStrut(14));

        //提交
        JButton submitButton = UiBuilder.primaryButton(ConstantPool.getSubmit());
        submitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitButton.addActionListener(e -> {
            //未选择文件时直接结束,不再继续执行导入逻辑
            if (selectedFile[0] == null) {
                UiBuilder.warn(window, ConstantPool.getMsg_Choose_File());
                return;
            }
            //校验输入项
            String[] values = new String[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                values[i] = readInput(inputs[i]);
                if (values[i].isEmpty()) {
                    UiBuilder.warn(window, ConstantPool.getMsg_Code_Empty());
                    return;
                }
                if (!FileHandler.isLegalCode(values[i])) {
                    UiBuilder.warn(window, ConstantPool.getMsg_Code_Illegal());
                    return;
                }
            }
            //导入资源(最后一个输入项是资源代号)
            try {
                File dir = dirResolver.apply(values);
                File targetFile = FileHandler.importResource(selectedFile[0], dir, values[values.length - 1]);
                UiBuilder.info(window, ConstantPool.getMsg_Import_Done() + targetFile.getAbsolutePath());
                window.dispose();
            } catch (IOException exception) {
                Logger.error(exception.getMessage());
                UiBuilder.error(window, exception.getMessage());
            }
        });
        card.add(submitButton);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(Theme.BG);
        holder.setBorder(new EmptyBorder(18, 22, 22, 22));
        holder.add(card, BorderLayout.CENTER);

        root.add(holder, BorderLayout.CENTER);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                auxFrames.remove(window);
            }
        });

        window.add(root);
        window.setVisible(true);
    }

    //读取一个输入控件的内容(下拉框或文本框)
    static String readInput(JComponent input) {
        if (input instanceof JComboBox) {
            Object item = ((JComboBox<?>) input).getSelectedItem();
            return (item == null) ? ConstantPool.getVoid_String() : item.toString().trim();
        }
        if (input instanceof JTextField) {
            return ((JTextField) input).getText().trim();
        }
        return ConstantPool.getVoid_String();
    }

    //打包界面:选择 qsc 工程文件,生成 qgl 压缩包
    static void openPackagePage() {

        String[] projectCodes = SceneFileHandler.listProjectCodes();
        if (projectCodes.length == 0) {
            UiBuilder.warn(mainFrame, ConstantPool.getMsg_No_Project());
            return;
        }

        JFrame window = UiBuilder.frame(ConstantPool.getDo_Package(), 620, 320);
        registerAux(window);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(UiBuilder.header(ConstantPool.getDo_Package()), BorderLayout.NORTH);

        RoundedPanel card = UiBuilder.card(null);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel projectLabel = UiBuilder.dimLabel(ConstantPool.getLabel_Project());
        projectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> projectComboBox = UiBuilder.comboBox(projectCodes);
        projectComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        projectComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        //进度提示
        JLabel messageLabel = UiBuilder.dimLabel(ConstantPool.getDo_Package_Desc());

        JButton packageButton = UiBuilder.primaryButton(ConstantPool.getDo_Package());
        packageButton.addActionListener(e -> {

            Object item = projectComboBox.getSelectedItem();
            if (item == null) {
                UiBuilder.warn(window, ConstantPool.getMsg_Choose_Project());
                return;
            }

            File projectFile = new File(FileHandler.getSceneDir(), item + ConstantPool.getQsc_Suffix());
            try {
                File outputFile = Packager.doPackager(projectFile);
                messageLabel.setText(ConstantPool.getMsg_Package_Done() + outputFile.getName());
                UiBuilder.info(window, ConstantPool.getMsg_Package_Done() + outputFile.getAbsolutePath());
            } catch (IOException exception) {
                Logger.error(exception.getMessage());
                messageLabel.setText(exception.getMessage());
                UiBuilder.error(window, exception.getMessage());
            }
        });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.add(packageButton);
        buttonRow.add(messageLabel);

        card.add(projectLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(projectComboBox);
        card.add(Box.createVerticalStrut(20));
        card.add(buttonRow);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(Theme.BG);
        holder.setBorder(new EmptyBorder(18, 22, 22, 22));
        holder.add(card, BorderLayout.CENTER);
        root.add(holder, BorderLayout.CENTER);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                auxFrames.remove(window);
            }
        });

        window.add(root);
        window.setVisible(true);
    }

}
