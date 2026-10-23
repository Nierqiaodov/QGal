package top.nierqiaodov.ui;

import top.nierqiaodov.Element.Actor;
import top.nierqiaodov.Element.Background;
import top.nierqiaodov.Element.Line;
import top.nierqiaodov.Element.Lines;
import top.nierqiaodov.Element.Music;
import top.nierqiaodov.Element.Scene;
import top.nierqiaodov.Element.Voice;
import top.nierqiaodov.handler.AudioPlayer;
import top.nierqiaodov.handler.FileHandler;
import top.nierqiaodov.handler.SceneFileHandler;
import top.nierqiaodov.packager.ResourceGenerator;
import top.nierqiaodov.util.ConstantPool;
import top.nierqiaodov.util.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//场景编辑器界面:左侧实时预览,右侧编排背景/角色/音乐/语音/台词
public class SceneUI {

    //主界面(关闭本窗口后恢复显示)
    JFrame mainFrame;

    //场景编辑器窗口
    JFrame frame;

    //左侧实时预览面板
    ScenePreviewPanel previewPanel;

    //工程与场景代号
    JComboBox<String> projectComboBox;
    JTextField sceneCodeField;

    //背景、音乐、角色、表情、语音
    JComboBox<String> backgroundComboBox;
    JComboBox<String> musicComboBox;
    JComboBox<String> characterComboBox;
    JComboBox<String> emotionComboBox;
    JComboBox<String> voiceComboBox;

    //角色编排列表(列表顺序即从左到右的排列顺序)
    DefaultListModel<String> castModel = new DefaultListModel<>();
    JList<String> castList;

    //台词列表(列表顺序即播放顺序,每句可带语音)
    DefaultListModel<Line> lineModel = new DefaultListModel<>();
    JList<Line> lineList;
    JTextArea lineArea;

    //台词预览游标与进度提示
    int previewIndex = 0;
    JLabel lineInfoLabel;

    //试听用音频播放器
    AudioPlayer audioPlayer = new AudioPlayer();

    //入口:由主界面调用
    public static void sceneUi(JFrame mainFrame) {
        new SceneUI(mainFrame).show();
    }

    SceneUI(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        build();
    }

    //显示窗口
    void show() {
        frame.setVisible(true);
    }

    //搭建整个编辑窗口
    void build() {

        //初始化外观(重复调用安全)
        Theme.apply();

        frame = UiBuilder.frame(ConstantPool.getScene_Editor(), 1340, 840);
        frame.setLayout(new BorderLayout());

        //顶部:标题 + 返回按钮
        JButton backButton = UiBuilder.ghostButton("<  " + ConstantPool.getBack());
        backButton.addActionListener(e -> close());
        frame.add(UiBuilder.header(ConstantPool.getScene_Editor(), backButton), BorderLayout.NORTH);

        //左侧:实时预览卡片
        previewPanel = new ScenePreviewPanel();
        previewPanel.setLineChangedListener(index -> {
            previewIndex = index;
            syncLineInfo();
        });

        RoundedPanel previewCard = UiBuilder.card(new BorderLayout());
        previewCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel previewHeader = new JPanel(new BorderLayout());
        previewHeader.setOpaque(false);
        previewHeader.setBorder(new EmptyBorder(0, 2, 10, 2));
        previewHeader.add(UiBuilder.sectionTitle(ConstantPool.getPreview()), BorderLayout.WEST);
        lineInfoLabel = UiBuilder.dimLabel(ConstantPool.getVoid_String());
        previewHeader.add(lineInfoLabel, BorderLayout.EAST);
        previewCard.add(previewHeader, BorderLayout.NORTH);

        JPanel stage = new JPanel(new BorderLayout());
        stage.setBackground(Color.BLACK);
        stage.add(previewPanel, BorderLayout.CENTER);
        previewCard.add(stage, BorderLayout.CENTER);

        JPanel previewHolder = new JPanel(new BorderLayout());
        previewHolder.setBackground(Theme.BG);
        previewHolder.setBorder(new EmptyBorder(14, 18, 14, 10));
        previewHolder.add(previewCard, BorderLayout.CENTER);
        frame.add(previewHolder, BorderLayout.CENTER);

        //右侧:编排控制卡片列(内容较多,放进滚动面板)
        JPanel column = new JPanel();
        column.setBackground(Theme.BG);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(new EmptyBorder(14, 8, 14, 14));

        buildProjectSection(column);
        buildBackgroundSection(column);
        buildCastSection(column);
        buildMusicSection(column);
        buildLineSection(column);
        buildSaveSection(column);

        JScrollPane rightScroll = new JScrollPane(column);
        rightScroll.setPreferredSize(new Dimension(392, 840));
        rightScroll.setBorder(null);
        rightScroll.getViewport().setBackground(Theme.BG);
        rightScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);
        frame.add(rightScroll, BorderLayout.EAST);

        //关闭窗口时停止试听并回到主界面
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        //初始状态:刷新工程与资源下拉框,并推荐一个场景代号
        refreshProjectComboBox(ConstantPool.getVoid_String());
        refreshResourceComboBoxes();
        sceneCodeField.setText(SceneFileHandler.suggestSceneCode(existingProjectFile()));
        updatePreview();
    }

    //关闭窗口:停止试听并回到主界面
    void close() {
        audioPlayer.stopAll();
        if (mainFrame != null) {
            mainFrame.setVisible(true);
        }
        frame.dispose();
    }

    //新建一张带标题的控制卡片
    RoundedPanel newCard(String titleText) {

        RoundedPanel card = UiBuilder.card(null);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 14, 14, 14));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(UiBuilder.sectionTitle(titleText));
        card.add(Box.createVerticalStrut(8));

        return card;
    }

    //把卡片放到右侧列里
    void addCard(JPanel column, RoundedPanel card) {
        column.add(card);
        column.add(Box.createVerticalStrut(12));
    }

    //工程与场景代号
    void buildProjectSection(JPanel column) {

        RoundedPanel card = newCard(ConstantPool.getLabel_Project());

        projectComboBox = UiBuilder.comboBox(new String[]{});
        JButton newProjectButton = UiBuilder.smallButton(ConstantPool.getNew_Project(), 110);
        newProjectButton.addActionListener(e -> newProject());
        card.add(row(projectComboBox, newProjectButton));

        sceneCodeField = UiBuilder.textField(10);
        card.add(row(UiBuilder.dimLabel(ConstantPool.getLabel_Scene_Code()), sceneCodeField));

        JButton loadButton = UiBuilder.smallButton(ConstantPool.getLoad_Scene(), 110);
        loadButton.addActionListener(e -> loadScene());
        JButton newSceneButton = UiBuilder.smallButton(ConstantPool.getNew_Scene(), 110);
        newSceneButton.addActionListener(e -> newScene());
        card.add(row(loadButton, newSceneButton));

        addCard(column, card);
    }

    //背景(一个场景中仅可出现一次)
    void buildBackgroundSection(JPanel column) {

        RoundedPanel card = newCard(ConstantPool.getLabel_Background());

        backgroundComboBox = UiBuilder.comboBox(new String[]{ConstantPool.getNone_Option()});
        backgroundComboBox.addActionListener(e -> updatePreview());
        card.add(row(backgroundComboBox));

        addCard(column, card);
    }

    //角色及表情编排
    void buildCastSection(JPanel column) {

        RoundedPanel card = newCard(ConstantPool.getLabel_Cast());

        characterComboBox = UiBuilder.comboBox(new String[]{});
        characterComboBox.addActionListener(e -> refreshEmotionComboBox());
        emotionComboBox = UiBuilder.comboBox(new String[]{});
        emotionComboBox.addActionListener(e -> updatePreview());

        card.add(row(UiBuilder.dimLabel(ConstantPool.getLabel_Character()), characterComboBox));
        card.add(row(UiBuilder.dimLabel(ConstantPool.getLabel_Emotion()), emotionComboBox));

        JButton addButton = UiBuilder.smallButton(ConstantPool.getDo_Add(), 86);
        JButton removeButton = UiBuilder.smallButton(ConstantPool.getDo_Remove(), 86);
        JButton upButton = UiBuilder.smallButton(ConstantPool.getDo_Up(), 60);
        JButton downButton = UiBuilder.smallButton(ConstantPool.getDo_Down(), 60);

        castList = new JList<>(castModel);
        castList.setFont(Theme.bodyFont());

        addButton.addActionListener(e -> addActor());
        removeButton.addActionListener(e -> removeSelected(castModel, castList));
        upButton.addActionListener(e -> moveSelected(castModel, castList, -1));
        downButton.addActionListener(e -> moveSelected(castModel, castList, 1));

        card.add(row(addButton, removeButton, upButton, downButton));
        card.add(scrollList(castList, 104));

        addCard(column, card);
    }

    //背景音乐 + 试听
    void buildMusicSection(JPanel column) {

        RoundedPanel card = newCard(ConstantPool.getLabel_Music());

        musicComboBox = UiBuilder.comboBox(new String[]{ConstantPool.getNone_Option()});
        musicComboBox.addActionListener(e -> updatePreview());

        JButton previewMusicButton = UiBuilder.smallButton(ConstantPool.getDo_Preview_Music(), 110);
        previewMusicButton.addActionListener(e -> previewMusic());

        card.add(row(musicComboBox, previewMusicButton));

        addCard(column, card);
    }

    //台词编排、语音指派与预览推进
    void buildLineSection(JPanel column) {

        RoundedPanel card = newCard(ConstantPool.getLabel_Lines());

        lineArea = new JTextArea(3, 20);
        lineArea.setLineWrap(true);
        lineArea.setWrapStyleWord(true);
        lineArea.setFont(Theme.bodyFont());

        JScrollPane areaScroll = new JScrollPane(lineArea);
        areaScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        areaScroll.setPreferredSize(new Dimension(340, 72));
        areaScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        areaScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        card.add(areaScroll);
        card.add(Box.createVerticalStrut(6));

        //每句台词可以指派一段语音
        voiceComboBox = UiBuilder.comboBox(new String[]{ConstantPool.getNone_Option()});
        JButton previewVoiceButton = UiBuilder.smallButton(ConstantPool.getDo_Preview_Voice(), 110);
        previewVoiceButton.addActionListener(e -> previewVoice());
        card.add(row(UiBuilder.dimLabel(ConstantPool.getLabel_Voice()), voiceComboBox, previewVoiceButton));
        card.add(Box.createVerticalStrut(4));

        lineList = new JList<>(lineModel);
        lineList.setFont(Theme.bodyFont());
        //列表里把语音代号一起显示出来,例如 [v1] 你好
        lineList.setCellRenderer(new LineRenderer());
        //选中台词时同步预览,方便逐句校对
        lineList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int index = lineList.getSelectedIndex();
            if (index >= 0) {
                Line line = lineModel.get(index);
                lineArea.setText(line.getText());
                selectComboItem(voiceComboBox, (line.getVoice() == null) ? ConstantPool.getNone_Option() : line.getVoice().getCode());
                previewIndex = index;
                previewPanel.setLineIndex(previewIndex);
                syncLineInfo();
            }
        });

        JButton addButton = UiBuilder.smallButton(ConstantPool.getDo_Add(), 66);
        JButton modifyButton = UiBuilder.smallButton(ConstantPool.getDo_Modify(), 66);
        JButton removeButton = UiBuilder.smallButton(ConstantPool.getDo_Remove(), 66);

        addButton.addActionListener(e -> addLine());
        modifyButton.addActionListener(e -> modifyLine());
        removeButton.addActionListener(e -> removeSelected(lineModel, lineList));

        card.add(row(addButton, modifyButton, removeButton));
        card.add(scrollList(lineList, 132));

        JButton upButton = UiBuilder.smallButton(ConstantPool.getDo_Up(), 58);
        JButton downButton = UiBuilder.smallButton(ConstantPool.getDo_Down(), 58);
        JButton previousButton = UiBuilder.smallButton(ConstantPool.getDo_Previous(), 88);
        JButton nextButton = UiBuilder.smallButton(ConstantPool.getDo_Next(), 88);

        upButton.addActionListener(e -> moveSelected(lineModel, lineList, -1));
        downButton.addActionListener(e -> moveSelected(lineModel, lineList, 1));
        previousButton.addActionListener(e -> {
            previewPanel.previousLine();
            previewIndex = previewPanel.getLineIndex();
            syncLineInfo();
        });
        nextButton.addActionListener(e -> {
            previewPanel.nextLine();
            previewIndex = previewPanel.getLineIndex();
            syncLineInfo();
        });

        card.add(row(upButton, downButton, previousButton, nextButton));

        addCard(column, card);
    }

    //保存场景
    void buildSaveSection(JPanel column) {

        JButton saveButton = UiBuilder.primaryButton(ConstantPool.getScene_Save());
        saveButton.setFont(Theme.headingFont());
        Dimension size = new Dimension(340, 44);
        saveButton.setPreferredSize(size);
        saveButton.setMinimumSize(size);
        saveButton.setMaximumSize(size);
        saveButton.addActionListener(e -> saveScene());

        JPanel holder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        holder.setOpaque(false);
        holder.setAlignmentX(Component.LEFT_ALIGNMENT);
        holder.add(saveButton);

        column.add(holder);
        column.add(Box.createVerticalStrut(12));
    }

    //把控件放进固定高度的一行,避免 BoxLayout 下高度被拉伸
    JPanel row(Component... components) {
        JPanel panel = UiBuilder.row(components);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return panel;
    }

    //把列表放进固定高度的滚动面板
    JScrollPane scrollList(JList<?> list, int height) {
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(340, height));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return scrollPane;
    }

    //台词列表的显示:有配音时前面显示 [语音代号]
    static class LineRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText(ResourceGenerator.describeLine((Line) value));
            return label;
        }
    }

    //把界面当前状态组装成 Scene 实例(实时预览与保存共用)
    Scene buildScene() {

        Scene scene = new Scene();
        scene.setCode(sceneCodeField.getText().trim());

        //背景(一个场景中仅可出现一次)
        String backgroundCode = selectedItem(backgroundComboBox);
        if (!backgroundCode.isEmpty()) {
            Background background = new Background();
            background.setCode(backgroundCode);
            background.setFile(FileHandler.findResource(FileHandler.getBackgroundDir(), backgroundCode));
            scene.setBackground(background);
        }

        //背景音乐
        String musicCode = selectedItem(musicComboBox);
        if (!musicCode.isEmpty()) {
            Music music = new Music();
            music.setCode(musicCode);
            music.setFile(FileHandler.findResource(FileHandler.getMusicDir(), musicCode));
            scene.setMusic(music);
        }

        //角色及表情(列表顺序即从左到右的排列顺序)
        StringBuilder castText = new StringBuilder();
        for (int i = 0; i < castModel.size(); i++) {
            if (i > 0) {
                castText.append(ConstantPool.getComma());
            }
            castText.append(castModel.get(i));
        }
        List<Actor> actors = ResourceGenerator.parseActors(castText.toString());
        //补充表情图片文件,供预览绘制使用
        for (Actor actor : actors) {
            if (actor.getEmotion() != null) {
                actor.getEmotion().setFile(FileHandler.findEmotion(actor.getCharacter().getCode(), actor.getEmotion().getCode()));
            }
        }
        scene.setActors(actors);

        //台词(每句带上语音代号与语音文件)
        Lines lines = new Lines();
        for (int i = 0; i < lineModel.size(); i++) {
            Line line = lineModel.get(i);
            if (line.getVoice() != null) {
                line.getVoice().setFile(FileHandler.findResource(FileHandler.getVoiceDir(), line.getVoice().getCode()));
            }
            lines.getLines().add(line);
        }
        scene.setLines(lines);

        return scene;
    }

    //实时预览:把当前编排重新组装成场景并刷新画面
    void updatePreview() {

        Scene scene = buildScene();
        previewPanel.setScene(scene);

        int count = (scene.getLines() == null) ? 0 : scene.getLines().getLines().size();
        if (previewIndex >= count) {
            previewIndex = Math.max(0, count - 1);
        }
        previewPanel.setLineIndex(previewIndex);
        syncLineInfo();
        previewPanel.repaint();
    }

    //刷新台词进度提示
    void syncLineInfo() {

        int count = lineModel.size();
        if (previewIndex >= count) {
            previewIndex = Math.max(0, count - 1);
        }
        lineInfoLabel.setText((count == 0 ? 0 : previewIndex + 1) + ConstantPool.getSlash() + count);
    }

    //读取下拉框当前值(占位项返回空串)
    static String selectedItem(JComboBox<String> comboBox) {

        Object item = (comboBox == null) ? null : comboBox.getSelectedItem();
        if (item == null) {
            return ConstantPool.getVoid_String();
        }

        String value = item.toString().trim();

        return ConstantPool.getNone_Option().equals(value) ? ConstantPool.getVoid_String() : value;
    }

    //在下拉框内容前补一个占位项
    static String[] prependNone(String[] codes) {
        String[] items = new String[codes.length + 1];
        items[0] = ConstantPool.getNone_Option();
        System.arraycopy(codes, 0, items, 1, codes.length);
        return items;
    }

    //刷新工程下拉框(selectCode 为空时选中第一个工程)
    void refreshProjectComboBox(String selectCode) {

        String[] projectCodes = SceneFileHandler.listProjectCodes();
        projectComboBox.setModel(new DefaultComboBoxModel<>(projectCodes));

        if (!selectCode.isEmpty()) {
            projectComboBox.setSelectedItem(selectCode);
        } else if (projectCodes.length > 0) {
            projectComboBox.setSelectedIndex(0);
        }
    }

    //刷新背景、音乐、语音、角色下拉框(资源导入后调用)
    void refreshResourceComboBoxes() {

        backgroundComboBox.setModel(new DefaultComboBoxModel<>(prependNone(FileHandler.listCodes(FileHandler.getBackgroundDir()))));
        musicComboBox.setModel(new DefaultComboBoxModel<>(prependNone(FileHandler.listCodes(FileHandler.getMusicDir()))));
        voiceComboBox.setModel(new DefaultComboBoxModel<>(prependNone(FileHandler.listCodes(FileHandler.getVoiceDir()))));
        characterComboBox.setModel(new DefaultComboBoxModel<>(FileHandler.listDirectoryCodes(FileHandler.getCharacterDir())));

        //资源可能被重新导入,清空图片缓存
        previewPanel.clearImageCache();

        refreshEmotionComboBox();
    }

    //刷新表情下拉框(跟随当前选中的角色)
    void refreshEmotionComboBox() {

        String characterCode = selectedItem(characterComboBox);
        String[] emotionCodes = characterCode.isEmpty()
                ? new String[0]
                : FileHandler.listEmotionCodes(characterCode);

        emotionComboBox.setModel(new DefaultComboBoxModel<>(emotionCodes));
        updatePreview();
    }

    //当前选择的工程文件(未选择时返回 null)
    File existingProjectFile() {

        String projectCode = selectedItem(projectComboBox);
        if (projectCode.isEmpty()) {
            return null;
        }

        return new File(FileHandler.getSceneDir(), projectCode + ConstantPool.getQsc_Suffix());
    }

    //取得工程文件,没有工程时引导新建
    File requireProjectFile() {

        File projectFile = existingProjectFile();

        return (projectFile != null) ? projectFile : newProject();
    }

    //新建工程:输入工程代号并建立空工程文件
    File newProject() {

        String projectCode = UiBuilder.input(frame, ConstantPool.getMsg_Project_Name(), SceneFileHandler.suggestProjectCode());
        if (projectCode == null) {
            return null;
        }
        projectCode = projectCode.trim();
        if (!FileHandler.isLegalCode(projectCode)) {
            UiBuilder.warn(frame, ConstantPool.getMsg_Code_Illegal());
            return null;
        }

        File projectFile = new File(FileHandler.getSceneDir(), projectCode + ConstantPool.getQsc_Suffix());
        try {
            if (!projectFile.exists()) {
                //新建工程时写入一行注释,方便人工编辑时辨认
                List<String> header = new ArrayList<>();
                header.add(ConstantPool.getComment_Sign() + " " + ConstantPool.getApplication() + " " + ConstantPool.getDescription());
                SceneFileHandler.writeLines(projectFile, header);
                Logger.logger(projectFile.getAbsolutePath() + ConstantPool.getInfo_6());
            }
            refreshProjectComboBox(projectCode);
        } catch (IOException exception) {
            Logger.error(exception.getMessage());
            UiBuilder.error(frame, exception.getMessage());
            return null;
        }

        return projectFile;
    }

    //新场景:清空编排并推荐一个未占用的场景代号
    void newScene() {

        castModel.clear();
        lineModel.clear();
        lineArea.setText(ConstantPool.getVoid_String());
        backgroundComboBox.setSelectedIndex(0);
        musicComboBox.setSelectedIndex(0);
        voiceComboBox.setSelectedIndex(0);
        previewIndex = 0;
        sceneCodeField.setText(SceneFileHandler.suggestSceneCode(existingProjectFile()));
        updatePreview();
    }

    //从工程文件中读取场景并回填到界面
    void loadScene() {

        File projectFile = existingProjectFile();
        if (projectFile == null) {
            UiBuilder.warn(frame, ConstantPool.getMsg_Choose_Project());
            return;
        }

        String sceneCode = sceneCodeField.getText().trim();
        if (sceneCode.isEmpty()) {
            UiBuilder.warn(frame, ConstantPool.getMsg_Code_Empty());
            return;
        }

        try {
            Scene scene = SceneFileHandler.findScene(projectFile, sceneCode);
            if (scene == null) {
                UiBuilder.warn(frame, ConstantPool.getMsg_Scene_Not_Found());
                return;
            }
            applyScene(scene);
        } catch (IOException exception) {
            Logger.error(exception.getMessage());
            UiBuilder.error(frame, exception.getMessage());
        }
    }

    //把 Scene 实例回填到界面控件
    void applyScene(Scene scene) {

        sceneCodeField.setText(scene.getCode());

        //背景与音乐(下拉框中不存在的代号会被忽略)
        selectComboItem(backgroundComboBox, (scene.getBackground() == null) ? ConstantPool.getNone_Option() : scene.getBackground().getCode());
        selectComboItem(musicComboBox, (scene.getMusic() == null) ? ConstantPool.getNone_Option() : scene.getMusic().getCode());

        //角色及表情
        castModel.clear();
        for (Actor actor : scene.getActors()) {
            castModel.addElement(ResourceGenerator.describeActor(actor));
        }

        //台词(语音代号一并带回来)
        lineModel.clear();
        if (scene.getLines() != null) {
            for (Line line : scene.getLines().getLines()) {
                lineModel.addElement(line);
            }
        }

        lineArea.setText(ConstantPool.getVoid_String());
        selectComboItem(voiceComboBox, ConstantPool.getNone_Option());
        previewIndex = 0;
        updatePreview();
    }

    //按内容选中下拉框中的某一项
    static void selectComboItem(JComboBox<String> comboBox, String item) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).equals(item)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    //新增角色到编排列表
    void addActor() {

        String characterCode = selectedItem(characterComboBox);
        String emotionCode = selectedItem(emotionComboBox);
        if (characterCode.isEmpty() || emotionCode.isEmpty()) {
            UiBuilder.warn(frame, ConstantPool.getMsg_Cast_Empty());
            return;
        }

        castModel.addElement(characterCode + ConstantPool.getColon() + emotionCode);
        castList.setSelectedIndex(castModel.size() - 1);
        updatePreview();
    }

    //新增台词(可同时指派语音)
    void addLine() {

        String text = lineArea.getText().trim();
        if (text.isEmpty()) {
            UiBuilder.warn(frame, ConstantPool.getMsg_Line_Empty());
            return;
        }

        lineModel.addElement(buildLine(text));
        lineArea.setText(ConstantPool.getVoid_String());
        previewIndex = lineModel.size() - 1;
        lineList.setSelectedIndex(previewIndex);
        updatePreview();
    }

    //修改选中的台词(文本与语音一起改)
    void modifyLine() {

        int index = lineList.getSelectedIndex();
        if (index < 0) {
            return;
        }

        String text = lineArea.getText().trim();
        if (text.isEmpty()) {
            UiBuilder.warn(frame, ConstantPool.getMsg_Line_Empty());
            return;
        }

        lineModel.set(index, buildLine(text));
        updatePreview();
    }

    //按当前输入框与语音下拉框造一句台词
    Line buildLine(String text) {

        Line line = new Line();
        line.setText(text);

        String voiceCode = selectedItem(voiceComboBox);
        if (!voiceCode.isEmpty()) {
            Voice voice = new Voice();
            voice.setCode(voiceCode);
            voice.setFile(FileHandler.findResource(FileHandler.getVoiceDir(), voiceCode));
            line.setVoice(voice);
        }

        return line;
    }

    //试听选中台词的语音
    void previewVoice() {

        File voiceFile = null;
        int index = lineList.getSelectedIndex();

        if (index >= 0) {
            Line line = lineModel.get(index);
            voiceFile = (line.getVoice() == null) ? null : line.getVoice().getFile();
        }
        if (voiceFile == null) {
            String voiceCode = selectedItem(voiceComboBox);
            voiceFile = FileHandler.findResource(FileHandler.getVoiceDir(), voiceCode);
        }

        if (voiceFile == null) {
            UiBuilder.warn(frame, ConstantPool.getMsg_No_Voice());
            return;
        }

        audioPlayer.playVoice(voiceFile);
    }

    //试听当前场景的背景音乐
    void previewMusic() {

        String musicCode = selectedItem(musicComboBox);
        File musicFile = FileHandler.findResource(FileHandler.getMusicDir(), musicCode);

        if (musicFile == null) {
            UiBuilder.warn(frame, ConstantPool.getMsg_No_Resource());
            return;
        }

        //再点一次就停止
        if (musicFile.equals(audioPlayer.getBackgroundFile())) {
            audioPlayer.stopBackground();
            return;
        }

        audioPlayer.playBackground(musicFile);
    }

    //删除列表中选中的项
    void removeSelected(DefaultListModel<?> model, JList<?> list) {

        int index = list.getSelectedIndex();
        if (index < 0) {
            return;
        }

        model.remove(index);
        if (index < model.size()) {
            list.setSelectedIndex(index);
        }
        updatePreview();
    }

    //上移或下移选中的项(offset 为 -1 上移,1 下移)
    void moveSelected(DefaultListModel<?> model, JList<?> list, int offset) {

        int index = list.getSelectedIndex();
        int target = index + offset;
        if (index < 0 || target < 0 || target >= model.size()) {
            return;
        }

        swap(model, index, target);
        list.setSelectedIndex(target);
        updatePreview();
    }

    //交换两个元素(兼容字符串列表与台词列表)
    static <T> void swap(DefaultListModel<T> model, int index, int target) {
        T item = model.get(index);
        model.set(index, model.get(target));
        model.set(target, item);
    }

    //保存场景:同代号整行覆盖,新代号追加到工程文件末尾
    void saveScene() {

        String sceneCode = sceneCodeField.getText().trim();
        if (sceneCode.isEmpty()) {
            UiBuilder.warn(frame, ConstantPool.getMsg_Code_Empty());
            return;
        }
        if (!FileHandler.isLegalCode(sceneCode)) {
            UiBuilder.warn(frame, ConstantPool.getMsg_Code_Illegal());
            return;
        }

        //空场景不落盘
        Scene scene = buildScene();
        boolean emptyBackground = (scene.getBackground() == null);
        boolean emptyMusic = (scene.getMusic() == null);
        boolean emptyCast = scene.getActors().isEmpty();
        boolean emptyLines = (scene.getLines() == null || scene.getLines().getLines().isEmpty());
        if (emptyBackground && emptyMusic && emptyCast && emptyLines) {
            UiBuilder.warn(frame, ConstantPool.getMsg_Scene_Empty());
            return;
        }

        File projectFile = requireProjectFile();
        if (projectFile == null) {
            return;
        }

        //资源缺失时仍然允许保存(方便先编排后补素材),但把缺失情况记录下来
        List<String> missing = SceneFileHandler.resolve(scene);
        if (!missing.isEmpty()) {
            Logger.error(ConstantPool.getMsg_Missing_Resource() + missing);
        }

        try {
            SceneFileHandler.saveScene(projectFile, scene);
            UiBuilder.info(frame, ConstantPool.getMsg_Scene_Saved() + sceneCode);
            refreshProjectComboBox(FileHandler.stripSuffix(projectFile.getName()));
        } catch (IOException exception) {
            Logger.error(exception.getMessage());
            UiBuilder.error(frame, exception.getMessage());
        }
    }

}
