package com.ruoyi.biz.task;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 数据库自动备份定时任务
 */
@Component("dbBackupTask")
public class DbBackupTask {

    private static final Logger log = LoggerFactory.getLogger(DbBackupTask.class);

    @Value("${spring.datasource.druid.master.username}")
    private String username;

    @Value("${spring.datasource.druid.master.password}")
    private String password;

    @Value("${backup.db.path:/home/trainarchive/backup/db/}")
    private String backupPath;

    // 数据库名
    private final String dbName = "xd-trainarchive-db";

    /**
     * 执行数据库备份
     */
    public void backupDatabase() {
        log.info("========== 开始执行【数据库自动备份】定时任务 ==========");
        try {
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs(); // 自动创建备份目录
            }

            // 生成时间戳文件名，如：xd-trainarchive-db_20260305_104700.sql
            String dateStr = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = dbName + "_" + dateStr + ".sql";
            String fullPath = backupPath + fileName;

            // 构造 mysqldump 命令 (如果是 Windows 环境可以改成包含完整路径的 mysqldump.exe)
            String command = String.format("mysqldump -u%s -p%s %s -r %s",
                    username, password, dbName, fullPath);

            // 执行外部命令进行备份
            Process process = Runtime.getRuntime().exec(command);
            int processComplete = process.waitFor();

            if (processComplete == 0) {
                log.info("========== 数据库备份成功！文件已存至: {} ==========", fullPath);
            } else {
                log.error("========== 数据库备份失败！进程退出码: {} ==========", processComplete);
            }

        } catch (Exception e) {
            log.error("========== 数据库备份异常: {} ==========", e.getMessage(), e);
        }
    }
}