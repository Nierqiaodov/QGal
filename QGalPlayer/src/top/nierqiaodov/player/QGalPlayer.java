package top.nierqiaodov.player;

import top.nierqiaodov.player.util.ConstantPool;
import top.nierqiaodov.player.util.Fonts;
import top.nierqiaodov.player.util.Lang;
import top.nierqiaodov.player.util.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//播放器入口:拆开 qgl 成品并在独立窗口中播放
//启动方式:
//  java -jar QGalPlayer-1.0.jar 成品.qgl
//  java -jar QGalPlayer-1.0.jar --check 成品.qgl   只做拆包自检,不开窗口
//  java -jar QGalPlayer-1.0.jar --test            直接播放 GalWork 里的成品(测试入口)
//  java -jar QGalPlayer-1.0.jar --lang en ...     以英文界面启动(播放中可按 L 热切换)
public class QGalPlayer {

    //命令行入口(不带参数时弹出成品选择框)
    public static void main(String[] args) {

        //1.先处理语言参数:必须在常量池(以及 Lang)初始化之前设置系统属性
        for (int i = 0; i < args.length - 1; i++) {
            if ("--lang".equals(args[i])) {
                System.setProperty("qgal.lang", args[i + 1]);
            }
        }

        //2.测试入口:转交给 PlayerTest,直接播放 GalWork 文件夹里的成品
        if (containsFlag(args, ConstantPool.getTest_Flag())) {
            PlayerTest.main(withoutFlag(args, ConstantPool.getTest_Flag()));
            return;
        }

        Logger.logger(ConstantPool.getInfo_1());

        //3.环境自检:不需要成品,发布门禁用
        if (containsFlag(args, ConstantPool.getSelf_Test_Flag())) {
            boolean environmentOk = SelfCheck.runEnvironment();
            System.exit(environmentOk ? 0 : 1);
        }

        //4.自检模式:验证成品能否正确拆包,不开窗口
        if (containsFlag(args, ConstantPool.getSelf_Check_Flag())) {
            File checkFile = firstFileArgument(args);
            if (checkFile == null) {
                checkFile = choosePackage();
            }
            if (checkFile == null) {
                return;
            }
            boolean passed = SelfCheck.run(checkFile);
            System.exit(passed ? 0 : 1);
        }

        //4.正常播放(第一个文件参数即成品路径,没有则弹出选择框)
        File packageFile = firstFileArgument(args);
        if (packageFile == null) {
            packageFile = choosePackage();
        }
        if (packageFile == null) {
            return;
        }

        try {
            play(packageFile);
        } catch (IOException exception) {
            Logger.error(exception.getMessage());
            JOptionPane.showMessageDialog(null, exception.getMessage(), ConstantPool.getDialog_Error(), JOptionPane.ERROR_MESSAGE);
        }
    }

    //选择 qgl 成品
    static File choosePackage() {

        Theme.apply();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(ConstantPool.getFile_Filter_Qgl(), ConstantPool.getQgl_Extension()));
        fileChooser.setDialogTitle(ConstantPool.getPlayer_Title());

        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return fileChooser.getSelectedFile();
    }

    //在独立窗口中播放成品,返回播放窗口
    public static JFrame play(File packageFile) throws IOException {

        //初始化弹窗等原生控件的外观
        Theme.apply();

        //拆包
        QGalPackage qGalPackage = PackageReader.open(packageFile);
        Logger.logger(ConstantPool.getInfo_2());
        Logger.logger("font=" + Fonts.getFamily() + " / language=" + Lang.getCurrent());

        PlayerPanel playerPanel = new PlayerPanel();

        JFrame frame = new JFrame(ConstantPool.getPlayer_Title() + " - " + qGalPackage.getProjectCode());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Theme.BG);
        frame.add(playerPanel);

        //进度回显到窗口标题
        playerPanel.setProgressListener(() -> frame.setTitle(ConstantPool.getPlayer_Title() + " - "
                + qGalPackage.getProjectCode() + " - " + playerPanel.describeProgress()));

        //关闭窗口时停止音频并清理解包目录
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                playerPanel.stopAudio();
                qGalPackage.close();
                Logger.logger(ConstantPool.getInfo_4());
            }
        });

        bindKeys(frame.getRootPane(), frame, playerPanel);

        frame.setVisible(true);
        playerPanel.load(qGalPackage);

        return frame;
    }

    //注册快捷键
    static void bindKeys(JRootPane rootPane, JFrame frame, PlayerPanel playerPanel) {

        //推进与回退
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), playerPanel::next);
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), playerPanel::next);
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), playerPanel::next);
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), playerPanel::previous);
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), playerPanel::previous);

        //重播与自动播放
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), playerPanel::restart);
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), playerPanel::toggleAutoPlay);

        //语音连播、静音、界面语言
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), playerPanel::toggleVoiceAdvance);
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), playerPanel::toggleMute);
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), playerPanel::toggleLanguage);

        //音量:主键盘的 = / -,与小键盘的 + / -
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0),
                () -> playerPanel.changeVolume(AudioPlayer.VOLUME_STEP));
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.SHIFT_DOWN_MASK),
                () -> playerPanel.changeVolume(AudioPlayer.VOLUME_STEP));
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0),
                () -> playerPanel.changeVolume(AudioPlayer.VOLUME_STEP));
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0),
                () -> playerPanel.changeVolume(-AudioPlayer.VOLUME_STEP));
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0),
                () -> playerPanel.changeVolume(-AudioPlayer.VOLUME_STEP));

        //退出
        bindKey(rootPane, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), frame::dispose);
    }

    //绑定单个按键到动作
    static void bindKey(JRootPane rootPane, KeyStroke keyStroke, Runnable action) {

        String actionKey = "qgal-" + keyStroke;
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionKey);
        rootPane.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    //是否包含某个开关参数
    static boolean containsFlag(String[] args, String flag) {

        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }

        return false;
    }

    //去掉指定开关后的参数数组
    static String[] withoutFlag(String[] args, String flag) {

        List<String> result = new ArrayList<>();
        for (String arg : args) {
            if (!flag.equals(arg)) {
                result.add(arg);
            }
        }

        return result.toArray(new String[0]);
    }

    //取第一个文件参数(跳过开关本身与开关的值)
    static File firstFileArgument(String[] args) {

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (ConstantPool.getLang_Flag().equals(arg)) {
                //跳过语言值
                i++;
                continue;
            }
            if (arg.startsWith(ConstantPool.getFlag_Prefix())) {
                continue;
            }
            return new File(arg);
        }

        return null;
    }

}
