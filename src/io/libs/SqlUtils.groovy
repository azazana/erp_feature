package io.libs;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;
import java.util.stream.Stream;


// Проверяет соединение к БД и наличие базы
//
// Параметры:
//  dbServer - сервер БД
//  infobase - имя базы на сервере БД
//  sqlUser - Необязательный. админ sql базы
//  sqlPwd - Необязательный. пароль админа sql базы
//
def checkDb(dbServer, infobase, sqlUser, sqlPwd) {
    utils = new Utils()

    sqlUserpath = "" 
    if (sqlUser != null && !sqlUser.isEmpty()) {
        sqlUserpath = "-U ${sqlUser}"
    } else {
        sqlUserpath = "-E"
    }

    sqlPwdPath = "" 
    if (sqlPwd != null && !sqlPwd.isEmpty()) {
        sqlPwdPath = "-P ${sqlPwd}"
    }

    returnCode = utils.cmd("sqlcmd -S ${dbServer} ${sqlUserpath} ${sqlPwdPath} -i \"${env.WORKSPACE}/copy_etalon/error.sql\" -b -v restoreddb =${infobase}");
    if (returnCode != 0) {
        utils.raiseError("Возникла ошибка при при проверке соединения к sql базе ${dbServer}\\${infobase}. Для подробностей смотрите логи")
    }
}



// Создает бекап базы по пути указанному в параметре backupPath
//
// Параметры:
//  dbServer - сервер БД
//  infobase - имя базы на сервере БД
//  backupPath - каталог бекапов
//  sqlUser - Необязательный. админ sql базы
//  sqlPwd - Необязательный. пароль админа sql базы
//
def backupDb(dbServer, infobase, backupPath, sqlUser, sqlPwd) {
    def utils = new Utils()
    try {
        def sqlUserpath = (sqlUser != null && !sqlUser.isEmpty()) ? "-U ${sqlUser}" : "-E"
        def sqlPwdPath = (sqlPwd != null && !sqlPwd.isEmpty()) ? "-P ${sqlPwd}" : ""

        def command = "sqlcmd -S ${dbServer} ${sqlUserpath} ${sqlPwdPath} -i '${env.WORKSPACE}/copy_etalon/backup.sql' -b -v backupdb=${infobase} -v bakfile='${backupPath}'"
        echo "Executing command: ${command}"
        def returnCode = utils.cmd(command)
        if (returnCode != 0) {
            utils.raiseError("Возникла ошибка при создании бекапа sql базы ${dbServer}\\${infobase}. Для подробностей смотрите логи")
        }
    } catch (Exception e) {
        echo "Exception in backupDb: ${e.message}"
        throw e
    }
}


// Создает пустую базу на сервере БД
//
// Параметры:
//  dbServer - сервер БД
//  infobase - имя базы на сервере БД
//  sqlUser - Необязательный. админ sql базы
//  sqlPwd - Необязательный. пароль админа sql базы
//
def createEmptyDb(dbServer, infobase, sqlUser, sqlPwd) {

    sqlUserpath = "" 
    if (sqlUser != null && !sqlUser.isEmpty()) {
        sqlUserpath = "-U ${sqlUser}"
    } else {
        sqlUserpath = "-E"
    }

    sqlPwdPath = "" 
    if (sqlPwd != null && !sqlPwd.isEmpty()) {
        sqlPwdPath = "-P ${sqlPwd}"
    }

    utils = new Utils()
    returnCode = utils.cmd("sqlcmd -S ${dbServer} ${sqlUserpath} ${sqlPwdPath} -i \"${env.WORKSPACE}/copy_etalon/error_create.sql\" -b -v restoreddb =${infobase}")
    if (returnCode != 0) {
        utils.raiseError("Возникла ошибка при создании пустой sql базы на  ${dbServer}\\${infobase}. Для подробностей смотрите логи")
    }
}

def getLatestBackup(backupDir) {
    try {
        def latestFile = null
        def latestModifiedTime = 0

        // Сначала проверим, существует ли директория
        if (!Files.exists(Paths.get(backupDir)) || !Files.isDirectory(Paths.get(backupDir))) {
            throw new IllegalArgumentException("Backup directory does not exist or is not a directory: ${backupDir}")
        }

        // Получаем список файлов в директории
        List<Path> files = Files.list(Paths.get(backupDir)).collect(Collectors.toList())

        if (files.isEmpty()) {
            throw new IllegalArgumentException("No backup files found in directory: ${backupDir}")
        }

        files.each { filePath ->
            def attrs = Files.readAttributes(filePath, BasicFileAttributes.class)
            def lastModifiedTime = attrs.lastModifiedTime().toMillis()

            if (latestFile == null || lastModifiedTime > latestModifiedTime) {
                latestFile = filePath
                latestModifiedTime = lastModifiedTime
            }
        }
        echo "Latest backup file: ${latestFile}"
        return ""+latestFile
    } catch (Exception e) {
        echo "Exception in getLatestBackup: ${e.message}"
        throw e
    }
}



// Восстанавливает базу из бекапа
//
// Параметры:
//  utils - экземпляр библиотеки Utils.groovy
//  dbServer - сервер БД
//  infobase - имя базы на сервере БД
//  backupPath - каталог бекапов
//  sqlUser - Необязательный. админ sql базы
//  sqlPwd - Необязательный. пароль админа sql базы
//
def restoreDb(dbServer, infobase, backupDir, sqlUser, sqlPwd) {
    utils = new Utils()

    sqlUserpath = "" 
    if (sqlUser != null && !sqlUser.isEmpty()) {
        sqlUserpath = "-U ${sqlUser}"
    } else {
        sqlUserpath = "-E"
    }

    sqlPwdPath = "" 
    if (sqlPwd != null && !sqlPwd.isEmpty()) {
        sqlPwdPath = "-P ${sqlPwd}"
    }

    def latestBackup = getLatestBackup(backupDir)
    echo latestBackup
    
    returnCode = utils.cmd("sqlcmd -S ${dbServer} ${sqlUserpath} ${sqlPwdPath} -i \"${env.WORKSPACE}/copy_etalon/restore.sql\" -b -v restoreddb =${infobase} -v bakfile=\"${latestBackup}\"")
    if (returnCode != 0) {
         utils.raiseError("Возникла ошибка при восстановлении базы из sql бекапа ${dbServer}\\${infobase}. Для подробностей смотрите логи")
    } 
    echo 'OK restore'
}


// Удаляет бекапы из сетевой шары
//
// Параметры:
//  utils - экземпляр библиотеки Utils.groovy
//  backup_path - путь к бекапам
//
def clearBackups(backup_path) {
    utils = new Utils()
    echo "Deleting file ${backup_path}..."
    returnCode = utils.cmd("oscript ${env.WORKSPACE}/one_script_tools/deleteFile.os -file\"${backup_path}\"")
    if (returnCode != 0) {
        echo "Error when deleting file: ${backup_path}"
    }    
}

def shrink_db(infobase, dbServer, backupDir, sqlUser, sqlPwd) {
    utils = new Utils()

    sqlUserpath = "" 
    if (sqlUser != null && !sqlUser.isEmpty()) {
        sqlUserpath = "-U ${sqlUser}"
    } else {
        sqlUserpath = "-E"
    }
    sqlPwdPath = "" 
    if (sqlPwd != null && !sqlPwd.isEmpty()) {
        sqlPwdPath = "-P ${sqlPwd}"
    }

    def latestBackup = getLatestBackup(backupDir)

    returnCode = utils.cmd("sqlcmd -S ${dbServer} ${sqlUserpath} ${sqlPwdPath} -i \"${env.WORKSPACE}/copy_etalon/shrink_db.sql\" -b -v restoreddb =${infobase} -v bakfile=\"${latestBackup}\"")
    if (returnCode != 0) {
         utils.raiseError("Возникла ошибка при обрезании базы из sql бекапа ${dbServer}\\${infobase}. Для подробностей смотрите логи")
    } 
 
}
def simple_db(infobase, dbServer, backupDir, sqlUser, sqlPwd){
        utils = new Utils()

    sqlUserpath = "" 
    if (sqlUser != null && !sqlUser.isEmpty()) {
        sqlUserpath = "-U ${sqlUser}"
    } else {
        sqlUserpath = "-E"
    }
    sqlPwdPath = "" 
    if (sqlPwd != null && !sqlPwd.isEmpty()) {
        sqlPwdPath = "-P ${sqlPwd}"
    }

    returnCode = utils.cmd("sqlcmd -S ${dbServer} ${sqlUserpath} ${sqlPwdPath} -i \"${env.WORKSPACE}/copy_etalon/simple.sql\" -b -v restoreddb =${infobase}")
    if (returnCode != 0) {
         utils.raiseError("Возникла ошибка при обрезании базы из sql бекапа ${dbServer}\\${infobase}. Для подробностей смотрите логи")
    } 
   
}

def after_restore(dbServer, infobase, backupDir, sqlUser, sqlPwd) {
    utils = new Utils()

    sqlUserpath = "" 
    if (sqlUser != null && !sqlUser.isEmpty()) {
        sqlUserpath = "-U ${sqlUser}"
    } else {
        sqlUserpath = "-E"
    }
    sqlPwdPath = "" 
    if (sqlPwd != null && !sqlPwd.isEmpty()) {
        sqlPwdPath = "-P ${sqlPwd}"
    }

    def latestBackup = getLatestBackup(backupDir)

    returnCode = utils.cmd("sqlcmd -S ${dbServer} ${sqlUserpath} ${sqlPwdPath} -i \"${env.WORKSPACE}/copy_etalon/after_restore.sql\" -b -v restoreddb =${infobase} -v bakfile=\"${latestBackup}\"")
    if (returnCode != 0) {
         utils.raiseError("Возникла ошибка при восстановлении последнего sql бекапа ${dbServer}\\${infobase}. Для подробностей смотрите логи")
    } 
}