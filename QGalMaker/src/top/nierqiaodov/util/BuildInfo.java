package top.nierqiaodov.util;

import java.io.InputStream;
import java.util.Properties;

//构建信息:版本号、应用名、构建时间
//由 Maven 在构建时把 src/build.properties 过滤后写进 classpath,
//这里运行时读回来,保证界面显示的版本与 pom 完全一致
public class BuildInfo {

    //默认值(直接跑 target/classes 或没经过资源过滤时用)
    static final String DEFAULT_VERSION = "1.0.0";
    static final String DEFAULT_NAME = "QGalMaker";

    //构建信息文件
    static final String FILE = "/build.properties";

    //版本号
    static final String version;
    static final String name;
    static final String buildTime;

    static {
        Properties properties = new Properties();
        try (InputStream inputStream = BuildInfo.class.getResourceAsStream(FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception exception) {
            //读不到就用默认值,不影响启动
            properties = new Properties();
        }
        version = properties.getProperty("version", DEFAULT_VERSION).trim();
        name = properties.getProperty("name", DEFAULT_NAME).trim();
        buildTime = properties.getProperty("build.time", "").trim();
    }

    //版本号
    public static String version() {
        return version;
    }

    //应用名
    public static String name() {
        return name;
    }

    //构建时间
    public static String buildTime() {
        return buildTime;
    }

}
