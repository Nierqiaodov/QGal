package gg.nierqiaodov.ui;

import gg.nierqiaodov.util.Logger;
import gg.nierqiaodov.util.StringCombiner;
import gg.nierqiaodov.util.ConstantPool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//主编辑器界面
public class UI {

    static Font buttonFont = new Font(ConstantPool.getFont(), Font.PLAIN, 20);

    static Dimension buttonSize = new Dimension(280, 60);

    public static void mainUi(){

        JFrame frame = new JFrame(StringCombiner.combine(new String[]{ ConstantPool.getApplication(), ConstantPool.getDescription(), ConstantPool.getVersion()}));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(Color.PINK);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.PINK);
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton addResourceBtn = new JButton(ConstantPool.getAdd_Resource());
        addResourceBtn.setFont(buttonFont);
        addResourceBtn.setPreferredSize(buttonSize);
        addResourceBtn.setMaximumSize(buttonSize);
        addResourceBtn.setMinimumSize(buttonSize);
        addResourceBtn.addActionListener(e -> {
            addUi(frame);
            frame.setVisible(false);
        });

        JButton createSceneBtn = new JButton(ConstantPool.getCreate_Scene());
        createSceneBtn.setFont(buttonFont);
        createSceneBtn.setPreferredSize(buttonSize);
        createSceneBtn.setMaximumSize(buttonSize);
        createSceneBtn.setMinimumSize(buttonSize);
        createSceneBtn.addActionListener(e -> {

        });

        JButton packageBtn = new JButton(ConstantPool.getDo_Package());
        packageBtn.setFont(buttonFont);
        packageBtn.setPreferredSize(buttonSize);
        packageBtn.setMaximumSize(buttonSize);
        packageBtn.setMinimumSize(buttonSize);

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(addResourceBtn);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(createSceneBtn);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(packageBtn);
        buttonPanel.add(Box.createVerticalGlue());

        panel.setLayout(new GridBagLayout());
        panel.add(buttonPanel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {Logger.logger(ConstantPool.getInfo_5());}
        });
        frame.add(panel);
        frame.setVisible(true);

    }

    public static void addUi(JFrame frame) {

        JFrame addJFrame = new JFrame(ConstantPool.getAdd_Resource());
        addJFrame.setSize(800,600);
        addJFrame.setResizable(false);
        addJFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(Color.PINK);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.PINK);
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton addBackgroundBtn = new JButton(ConstantPool.getAdd_Background());
        addBackgroundBtn.setFont(buttonFont);
        addBackgroundBtn.setPreferredSize(buttonSize);
        addBackgroundBtn.setMaximumSize(buttonSize);
        addBackgroundBtn.setMinimumSize(buttonSize);
        addBackgroundBtn.addActionListener(e -> {addResource(ConstantPool.getBackground_Directory());});

        JButton addCharacterBtn = new JButton(ConstantPool.getAdd_Character());
        addCharacterBtn.setFont(buttonFont);
        addCharacterBtn.setPreferredSize(buttonSize);
        addCharacterBtn.setMaximumSize(buttonSize);
        addCharacterBtn.setMinimumSize(buttonSize);
        addCharacterBtn.addActionListener(e -> {addResource(ConstantPool.getCharacter_Directory());});

        JButton addMusicBtn = new JButton(ConstantPool.getAdd_Music());
        addMusicBtn.setFont(buttonFont);
        addMusicBtn.setPreferredSize(buttonSize);
        addMusicBtn.setMaximumSize(buttonSize);
        addMusicBtn.setMinimumSize(buttonSize);
        addMusicBtn.addActionListener(e -> {addResource(ConstantPool.getMusic_Directory());});

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(addBackgroundBtn);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(addCharacterBtn);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(addMusicBtn);
        buttonPanel.add(Box.createVerticalGlue());

        panel.setLayout(new GridBagLayout());
        panel.add(buttonPanel);

        addJFrame.add(panel);
        addJFrame.setVisible(true);

        addJFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {frame.setVisible(true);}
        });

    }

    public static void addResource(String pathToKindOfResource) {

        JFrame addResJFrame = new JFrame(ConstantPool.getAdd_Resource());
        addResJFrame.setSize(450,100);
        addResJFrame.setLocationRelativeTo(null);
        addResJFrame.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(Color.PINK);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 15));

        JTextField textField = new JTextField(20);

        JButton fileChooserButton = getJButton(addResJFrame, textField);

        JButton actionButton = new JButton(ConstantPool.getSubmit());

        panel.add(textField);
        panel.add(fileChooserButton);
        panel.add(actionButton);

        addResJFrame.add(panel);
        addResJFrame.setVisible(true);


    }

    private static JButton getJButton(JFrame addResJFrame, JTextField textField) {
        JButton fileChooserButton = new JButton(ConstantPool.getChoose_File());
        fileChooserButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(addResJFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        return fileChooserButton;
    }

}
