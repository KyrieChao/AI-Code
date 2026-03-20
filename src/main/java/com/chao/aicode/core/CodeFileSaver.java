package com.chao.aicode.core;

/**
 * 代码文件保存器
 */
/*
@Deprecated
public class CodeFileSaver {

    */
/**
 * 文件保存方法
 *//*

    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output/";

    */
/**
 * 保存HTML网页代码
 *
 * @param htmlCodeResult HTML代码结果
 * @return 保存的文件
 *//*

    public static File saveHTMLCodeResult(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);

    }

    */
/**
 * 保存多文件代码
 *
 * @param multiFileCodeResult 多文件代码结果
 * @return 保存的文件
 *//*

    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath, "index.html", multiFileCodeResult.getHtmlCode());
        writeToFile(baseDirPath, "style.css", multiFileCodeResult.getCssCode());
        writeToFile(baseDirPath, "script.js", multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }

    */
/**
 * 构建文件的唯一路径 tmp/code_output/bizType_雪花 ID
 *
 * @param bizType 代码生成类型
 * @return 唯一路径
 *//*

    private static String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String DirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(DirPath);
        return DirPath;
    }

    */
/**
 * 保存单个文件
 *
 * @param dirPath  文件保存的目录
 * @param filename 文件名
 * @param content  文件内容
 *//*

    public static void writeToFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
*/
