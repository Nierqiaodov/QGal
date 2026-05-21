package gg.nierqiaodov;

import gg.nierqiaodov.handler.FileHandler;
import gg.nierqiaodov.ui.UI;
import gg.nierqiaodov.util.ConstantPool;
import gg.nierqiaodov.util.Logger;

import static gg.nierqiaodov.handler.FileHandler.dirInit;

public class QGal {

    public static void main(String[] args) {

        Logger.logger(ConstantPool.getInfo_1());
        dirInit(FileHandler.getWorkDir());
        dirInit(FileHandler.getLines());
        dirInit(FileHandler.getBackground());
        dirInit(FileHandler.getMusic());
        dirInit(FileHandler.getCharacter());
        Logger.logger(ConstantPool.getInfo_2());
        UI.mainUi();
        Logger.logger(ConstantPool.getInfo_3());
        Logger.logger(ConstantPool.getInfo_4());

    }

}
