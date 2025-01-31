package io.libs

// Создает базу в кластере через RAS или пакетный режим. Для пакетного режима есть возможность создать базу с конфигурацией
//
// Параметры:
//  platform - номер платформы 1С, например 8.3.12.1529
//  server1c - сервер 1c
//  serversql - сервер 1c 
//  base - имя базы на сервере 1c и sql
//  cfdt - файловый путь к dt или cf конфигурации для загрузки. Только для пакетного режима!
//  isras - если true, то используется RAS для скрипта, в противном случае - пакетный режим
//
def createDb(platform, server1c, serversql, base, cfdt, isras) {
    utils = new Utils()

    cfdtpath = ""
    if (cfdt != null && !cfdt.isEmpty()) {
        cfdtpath = "-cfdt ${cfdt}"
    }

    israspath = ""
    if (isras) {
        israspath = "-isras true"
    }

    platformLine = ""
    if (platformLine != null && !platformLine.isEmpty()) {
        platformLine = "-platform ${platform}"
    }

    returnCode = utils.cmd("oscript one_script_tools/dbcreator.os ${platformLine} -server1c ${server1c} -serversql ${serversql} -base ${base} ${cfdtpath} ${israspath}")
    if (returnCode != 0) {
        utils.raiseError("Возникла ошибка при создании базы ${base} в кластере ${serversql}")
    }
}

// Убирает в 1С базу окошки с тем, что база перемещена, интернет поддержкой, очищает настройки ванессы
//
// Параметры:
//  сonnection_string - путь к 1С базе.
//  admin1cUsr - имя админа 1С базы
//  admin1cPwd - пароль админа 1С базы
//
def unlocking1cBase(connString, admin1cUsr, admin1cPwd) {
    utils = new Utils()

    admin1cUsrLine = ""
    if (admin1cUser != null && !admin1cUser.isEmpty()) {
        admin1cUsrLine = "--db-user ${admin1cUsr}"
    }

    admin1cPwdLine = ""
    if (admin1cPwd != null && !admin1cPwd.isEmpty()) {
        admin1cPwdLine = "--db-pwd ${admin1cPwd}"
    }
    
    utils.cmd("vrunner run --execute \"${env.WORKSPACE}/one_script_tools/unlockBase1C.epf\" --command \"-locktype unlock\" ${admin1cUsrLine} ${admin1cPwdLine} --ibconnection=${connString}")
}

def getConnString(server1c, infobase, agent1cPort) {
    return "/S${server1c}:${agent1cPort}\\${infobase}"
}

// Удаляет базу из кластера через powershell.
//
// Параметры:
//  server1c - сервер 1с 
//  agentPort - порт агента кластера 1с
//  serverSql - сервер sql
//  base - база для удаления из кластера
//  admin1cUser - имя администратора 1С в кластере для базы
//  admin1cPwd - пароль администратора 1С в кластере для базы
//  sqluser - юзер sql
//  sqlPwd - пароль sql
//  fulldrop - если true, то удаляется база из кластера 1С и sql сервера
//
def dropDb(server1c, agentPort, serverSql, base, admin1cUser, admin1cPwd, sqluser, sqlPwd, fulldrop = false) {

    utils = new Utils()
    
    fulldropLine = "";
    if (fulldrop) {
        fulldropLine = "-fulldrop true"
    }

    admin1cUserLine = "";
    if (admin1cUser != null && !admin1cUser.isEmpty()) {
        admin1cUserLine = "-user ${admin1cUser}"
    }

    admin1cPwdLine = "";
    if (admin1cPwd != null && !admin1cPwd.isEmpty()) {
        admin1cPwdLine = "-passw ${admin1cPwd}"
    }

    sqluserLine = "";
    if (sqluser != null && !sqluser.isEmpty()) {
        sqluserLine = "-sqluser ${sqluser}"
    }

    sqlpasswLine = "";
    if (sqlPwd != null && !sqlPwd.isEmpty()) {
        sqlpasswLine = "-sqlPwd ${sqlPwd}"
    }

    returnCode = utils.cmd("powershell -file \"${env.WORKSPACE}/copy_etalon/drop_db.ps1\" -server1c ${server1c} -agentPort ${agentPort} -serverSql ${serverSql} -infobase ${base} ${admin1cUserLine} ${admin1cPwdLine} ${sqluserLine} ${sqlpasswLine} ${fulldropLine}")
    if (returnCode != 0) { 
        error "error when deleting base with COM ${server1c}\\${base}. See logs above fore more information."
    }
}

// Загружает в базу конфигурацию из 1С хранилища. Базу желательно подключить к хранилищу под загружаемым пользователем,
//  т.к. это даст буст по скорости загрузки.
//
// Параметры:
//
//
def loadCfgFrom1CStorage(storageTCP, storageUser, storagePwd, connString, admin1cUser, admin1cPassword, platform, unlock_code = "", extintion = "") {
    utils = new Utils()
    echo "we are in "+extintion
    echo extintion
    storagePwdLine = ""
    if (storagePwd != null && !storagePwd.isEmpty()) {
        storagePwdLine = "--storage-pwd ${storagePwd}"
    }

    platformLine = ""
    if (platform != null && !platform.isEmpty()) {
        platformLine = "--v8version ${platform}"
    }
    cmd_line = "runner loadrepo --storage-name ${storageTCP} --storage-user ${storageUser} ${storagePwdLine} --ibconnection ${connString} --db-user ${admin1cUser} --db-pwd ${admin1cPassword} ${platformLine}"
    if (extintion != "") {
        cmd_line = cmd_line +  " --extension ${extintion}"
    }
    if (unlock_code != "") {
        cmd_line = cmd_line +  " --uccode ${unlock_code}"
    }

    returnCode = utils.cmd(cmd_line)
    if (returnCode != 0) {
         utils.raiseError("Загрузка конфигурации из 1С хранилища  ${storageTCP} завершилась с ошибкой. Для подробностей смотрите логи.")
    }
}
// Обновляет базу в режиме конфигуратора. Аналог нажатия кнопки f7
//
// Параметры:
//
//  connString - строка соединения, например /Sdevadapter\template_adapter_adapter
//  platform - полный номер платформы 1с
//  admin1cUser - администратор базы
//  admin1cPassword - пароль администратора базы
//
def updateInfobase(connString, admin1cUser, admin1cPassword, platform, unlock_code="", extintion = "") {

    utils = new Utils()
    admin1cUserLine = "";
    if (!admin1cUser.isEmpty()) {
        admin1cUserLine = "--db-user ${admin1cUser}"
    }
    admin1cPassLine = "";
    if (!admin1cPassword.isEmpty()) {
        admin1cPassLine = "--db-pwd ${admin1cPassword}"
    }
    platformLine = ""
    if (platform != null && !platform.isEmpty()) {
        platformLine = "--v8version ${platform}"
    }
    cmd_line = "vrunner updatedb --ibconnection ${connString} ${admin1cUserLine} ${admin1cPassLine} ${platformLine}"
    if (unlock_code != "") {
        cmd_line = cmd_line +  " --uccode ${unlock_code}"
    }
    if (extintion != "") {
        cmd_line = cmd_line +  " --extension ${extintion}"
    }

    returnCode = utils.cmd(cmd_line)
    if (returnCode != 0) {
        utils.raiseError("Обновление базы ${connString} в режиме конфигуратора завершилось с ошибкой. Для дополнительной информации смотрите логи")
    }
}

// Обновляет расширение в режиме конфигуратора
//
// Параметры:
//
//  connString - строка соединения, например /Sdevadapter\template_adapter_adapter
//  platform - полный номер платформы 1с
//  admin1cUser - администратор базы
//  admin1cPassword - пароль администратора базы
//  unlock_code - код разблокировки
//  extintion - имя расширения
//
def updateExtension(connString, admin1cUser, admin1cPassword, platform, unlock_code="", extintion = "") {
 
    utils = new Utils()
    admin1cUserLine = "";
    if (!admin1cUser.isEmpty()) {
        admin1cUserLine = "--db-user ${admin1cUser}"
    }
    admin1cPassLine = "";
    if (!admin1cPassword.isEmpty()) {
        admin1cPassLine = "--db-pwd ${admin1cPassword}"
    }
    platformLine = ""
    if (platform != null && !platform.isEmpty()) {
        platformLine = "--v8version ${platform}"
    }
    cmd_line = "vrunner updateext ${extintion} --ibconnection ${connString} ${admin1cUserLine} ${admin1cPassLine} ${platformLine}"
    
    if (unlock_code != "") {
        cmd_line = cmd_line +  " --uccode ${unlock_code}"
    }

    returnCode = utils.cmd(cmd_line)
    if (returnCode != 0) {
        utils.raiseError("Обновление базы ${connString} в режиме конфигуратора завершилось с ошибкой. Для дополнительной информации смотрите логи")
    }

}

def runUpdatedBase(connString, admin1cUser, admin1cPassword, platform, unlock_code) {

    def utils = new Utils()
    
    def admin1cUserLine = admin1cUser.isEmpty() ? "" : "--db-user ${admin1cUser}"
    def admin1cPassLine = admin1cPassword.isEmpty() ? "" : "--db-pwd ${admin1cPassword}"
    def platformLine = (platform != null && !platform.isEmpty()) ? "--v8version ${platform}" : ""
    
    def cmd_line = "vrunner run --command \"ЗапуститьОбновлениеИнформационнойБазы;ЗавершитьРаботуСистемы;\" " +
                   "--execute \"${env.WORKSPACE}/one_script_tools\\ЗакрытьПредприятие.epf\" " +
                   "--ibconnection ${connString} ${admin1cUserLine} ${admin1cPassLine} ${platformLine}"
    
    if (!unlock_code.isEmpty()) {
        cmd_line = cmd_line + " --uccode ${unlock_code}"
    }

    def returnCode = utils.cmd(cmd_line)
    if (returnCode != 0) {
        utils.raiseError("Запуск обработчиков обновления завершен с ошибкой. Для дополнительной информации смотрите логи")
    }
}


def bindRepo(platform1c, server1c, testbase, admin1cUser, admin1cPwd, storage1cPath, storageUser, storagePwd) {
    utils = new Utils()
    returnCode = utils.cmd("oscript one_script_tools/bindRepo.os -platform ${platform1c} -server ${server1c} -base ${testbase} -user ${admin1cUser} -passw ${admin1cPwd} -storage1c ${storage1cPath} -storage1cuser ${storageUser}")
    if (returnCode != 0) {
        utils.raiseError("Возникла ошибка при подключении ${testbase}  к хранилищу")
    }
}

def bindExtRepo(platform1c, server1c, testbase, admin1cUser, admin1cPwd, storage1cPath, storageUser, storagePwd, ext) {
    utils = new Utils()
    returnCode = utils.cmd("oscript one_script_tools/bindRepoExt.os -platform ${platform1c} -server ${server1c} -base ${testbase} -user ${admin1cUser} -passw ${admin1cPwd} -storage1c ${storage1cPath} -storage1cuser ${storageUser} -extension  ${ext}")
    if (returnCode != 0) {
        utils.raiseError("Возникла ошибка при подключении ${testbase}  к расширению хранилища")
    }

}

def blockSession(platform1c, server1c, testbase, admin1cUser, admin1cPwd, unlock_code = "", clusterName="Локальный кластер", rasPort="1545") {
    utils = new Utils()
    
    cmd_line = "vrunner session lock --ras ${server1c}:${rasPort} --cluster-name \"${clusterName}\" --db ${testbase} --db-user ${admin1cUser} --db-pwd ${admin1cPwd}  --lockendclear --lockmessage \"Уважаемые пользователи, в данный момент проводится плановое обновление базы данных.\" --v8version ${platform1c}"
    
    
    if (unlock_code != "") {
        cmd_line = cmd_line +  " --uccode ${unlock_code}"
    }

    returnCode = utils.cmd(cmd_line)

    if (returnCode != 0) {
        utils.raiseError("Возникла ошибка при установик блокировки ${testbase}")
    }

}

def unBlockSession(platform1c, server1c, testbase, admin1cUser, admin1cPwd, unlock_code = "", clusterName="Локальный кластер", rasPort="1545") {
    utils = new Utils()
    
    cmd_line = "vrunner session unlock --ras ${server1c}:${rasPort} --cluster-name \"${clusterName}\" --db ${testbase} --db-user ${admin1cUser} --db-pwd ${admin1cPwd} --v8version ${platform1c}  --uccode ${unlock_code}"
    
    returnCode = utils.cmd(cmd_line)
    if (returnCode != 0) {
        utils.raiseError("Возникла ошибка при снятии блокировки ${testbase}")
    }

}

def killDesinerSession(platform1c, server1c, testbase, admin1cUser, admin1cPwd, unlock_code = "", clusterName="Локальный кластер", rasPort="1545") {
    utils = new Utils()
    
    cmd_line = "vrunner session kill --filter appid=Designer --ras ${server1c}:${rasPort} --cluster-name \"${clusterName}\" --db ${testbase} --db-user ${admin1cUser} --db-pwd ${admin1cPwd} --v8version ${platform1c} --with-nolock --try 5"
  
    if (unlock_code != "") {
        cmd_line = cmd_line +  " --uccode ${unlock_code}"
    }
    
    returnCode = utils.cmd(cmd_line)

    if (returnCode != 0) {
        utils.raiseError("Возникла ошибка при отключении пользователя конфигуратора ${testbase}")
    }

}


def killAllSession(platform1c, server1c, testbase, admin1cUser, admin1cPwd, unlock_code = "", clusterName="Локальный кластер", rasPort="1545") {
    utils = new Utils()
    
    cmd_line = "vrunner session kill --ras ${server1c}:${rasPort} --cluster-name \"${clusterName}\"  --db ${testbase} --db-user ${admin1cUser} --db-pwd ${admin1cPwd} --v8version ${platform1c}  --try 5"
    if (unlock_code != "") {
        cmd_line = cmd_line +  " --uccode ${unlock_code}"
    }
    returnCode = utils.cmd(cmd_line)
    

    if (returnCode != 0) {
        utils.raiseError("Возникла ошибка при отключении пользоватей ${testbase}")
    }

} 

def scheduledjobsUnlock(platform1c, server1c, testbase, admin1cUser, admin1cPwd, unlockCode = "", clusterName="Локальный кластер", rasPort="1545") {
    utils = new Utils()
  
    cmd_line = "vrunner scheduledjobs unlock --ras ${server1c}:${rasPort} --cluster-name \"${clusterName}\"  --db ${testbase} --db-user ${admin1cUser} --db-pwd ${admin1cPwd} --v8version ${platform1c}";
    if (unlockCode != "") {
        cmd_line = cmd_line +  " --uccode ${unlockCode}"
    }
    returnCode = utils.cmd(cmd_line)
    

    if (returnCode != 0) {
        utils.raiseError("Разблокировка регламентных заданий ${testbase}")
    }

}

def scheduledjobsLock(platform1c, server1c, testbase, admin1cUser, admin1cPwd, unlockCode = "", clusterName="Локальный кластер", rasPort="1545") {
    utils = new Utils()
    echo "scheduledjobsLock"
    cmd_line = "vrunner scheduledjobs lock --ras ${server1c}:${rasPort} --cluster-name \"${clusterName}\"  --db ${testbase} --db-user ${admin1cUser} --db-pwd ${admin1cPwd} --v8version ${platform1c}";
    echo cmd_line
    if (unlockCode != "") {
        cmd_line = cmd_line +  " --uccode ${unlockCode}"
    }
    returnCode = utils.cmd(cmd_line)
   
    if (returnCode != 0) {
        utils.raiseError("Блокировка регламентных заданий ${testbase}")
    }
}

def sendNotification(String status, String adress) {
    def subject, body

    if (status == 'SUCCESS') {
        subject = "Сборка УСПЕШНА: Задание '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
        body = """
            Сообщаем вам, что задание Jenkins ${env.JOB_NAME} выполнено УСПЕШНО.

            Название задания: ${env.JOB_NAME}
            Номер сборки: ${env.BUILD_NUMBER}
            Лог сборки: ${env.BUILD_URL}console
            URL: ${env.BUILD_URL}
            
            Вы можете просмотреть подробности и вывод консоли по следующей ссылке:
            ${env.BUILD_URL}

            С уважением,
            Ваш сервер Jenkins
            """
    } else if (status == 'FAILURE') {
        subject = "Сборка ПРОВАЛЕНА: Задание '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
        body = """
            Сообщаем вам, что задание Jenkins ${env.JOB_NAME} завершилось НЕУДАЧЕЙ.

            Название задания: ${env.JOB_NAME}
            Номер сборки: ${env.BUILD_NUMBER}
            Лог сборки: ${env.BUILD_URL}console
            URL: ${env.BUILD_URL}
            
            Вы можете просмотреть подробности и вывод консоли по следующей ссылке:
            ${env.BUILD_URL}

            С уважением,
            Ваш сервер Jenkins
            """
    }

    emailext (
        to: adress,
        subject: subject,
        body: body
    )
}