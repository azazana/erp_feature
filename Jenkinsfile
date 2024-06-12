@Library("shared-libraries")
import io.libs.SqlUtils
import io.libs.ProjectHelpers
import io.libs.Utils

def sqlUtils = new SqlUtils()
def utils = new Utils()
def projectHelpers = new ProjectHelpers()
def restoreTasks = [:]
def checkPaths = [:]
def dropDbTasks = [:]
def createDbTasks = [:]
def bindReposTasks = [:]
def bindReposExtTasks = [:]
def runHandlers1cTasks = [:]

pipeline {

    parameters {
        string(defaultValue: "${env.jenkinsAgent}", description: 'Нода дженкинса, на которой запускать пайплайн. По умолчанию master', name: 'jenkinsAgent')
        string(defaultValue: "${env.server1c}", description: 'Имя сервера 1с, по умолчанию localhost', name: 'server1c')
        string(defaultValue: "${env.server1cPort}", description: 'Порт рабочего сервера 1с. По умолчанию 1540. Не путать с портом агента кластера (1541)', name: 'server1cPort')
        string(defaultValue: "${env.agent1cPort}", description: 'Порт агента кластера 1с. По умолчанию 1541', name: 'agent1cPort')
        string(defaultValue: "${env.platform1c}", description: 'Версия платформы 1с, например 8.3.12.1685. По умолчанию будет использована последня версия среди установленных', name: 'platform1c')
        string(defaultValue: "${env.serverSql}", description: 'Имя сервера MS SQL. По умолчанию localhost', name: 'serverSql')
        string(defaultValue: "${env.admin1cUser}", description: 'Имя администратора с правом открытия вншних обработок (!) для базы тестирования 1с Должен быть одинаковым для всех баз', name: 'admin1cUser')
        string(defaultValue: "${env.admin1cPwd}", description: 'Пароль администратора базы тестирования 1C. Должен быть одинаковым для всех баз', name: 'admin1cPwd')
        string(defaultValue: "${env.sqlUser}", description: 'Имя администратора сервера MS SQL. Если пустой, то используется доменная  авторизация', name: 'sqlUser')
        string(defaultValue: "${env.sqlPwd}", description: 'Пароль администратора MS SQL.  Если пустой, то используется доменная  авторизация', name: 'sqlPwd')
        string(defaultValue: "${env.templatebases}", description: 'Список баз для тестирования через запятую. Например work_erp,work_upp', name: 'templatebases')
        string(defaultValue: "${env.storages1cPath}", description: 'Необязательный. Пути к хранилищам 1С для обновления копий баз тестирования через запятую. Число хранилищ (если указаны), должно соответствовать числу баз тестирования. Например D:/temp/storage1c/erp,D:/temp/storage1c/upp', name: 'storages1cPath')
        string(defaultValue: "${env.storageUser}", description: 'Необязательный. Администратор хранилищ  1C. Должен быть одинаковым для всех хранилищ', name: 'storageUser')
        string(defaultValue: "${env.storagePwd}", description: 'Необязательный. Пароль администратора хранилищ 1c', name: 'storagePwd')
        string(defaultValue: "${env.backupDir}", description: 'Путь к бэкапам сетевым', name: 'backupDir')
        string(defaultValue: "${env.storages1cPathExt}", description: 'Путь к хранилищу расширения', name: 'storages1cPathExt')
        string(defaultValue: "${env.ext}", description: 'Название расширения', name: 'ext')
    }

    agent {
        label "${(env.jenkinsAgent == null || env.jenkinsAgent == 'null') ? "master" : env.jenkinsAgent}"
    }
    options {
        timeout(time: 8, unit: 'HOURS') 
        buildDiscarder(logRotator(numToKeepStr:'10'))
    }
    stages {
        stage("Подготовка") {
            steps {
                timestamps {
                    script {
                        templatebasesList = utils.lineToArray(templatebases.toLowerCase())
                        storages1cPathList = utils.lineToArray(storages1cPath.toLowerCase())

                        if (storages1cPathList.size() != 0) {
                            assert storages1cPathList.size() == templatebasesList.size()
                        }

                        server1c = server1c.isEmpty() ? "localhost" : server1c
                        serverSql = serverSql.isEmpty() ? "localhost" : serverSql
                        server1cPort = server1cPort.isEmpty() ? "1540" : server1cPort
                        agent1cPort = agent1cPort.isEmpty() ? "1541" : agent1cPort
                        env.sqlUser = sqlUser.isEmpty() ? "sa" : sqlUser
                        testbase = null

                        // создаем пустые каталоги
                        dir ('build') {
                            writeFile file:'dummy', text:''
                        }
                    }
                }
            }
        }
        stage("Запуск") {
            steps {
                timestamps {
                    script {

                        for (i = 0;  i < templatebasesList.size(); i++) {
                            templateDb = templatebasesList[i]
                            storage1cPath = storages1cPathList[i]
                            testbase = "${templateDb}"
                            testbaseConnString = projectHelpers.getConnString(server1c, testbase, agent1cPort)
                            backupDir = backupDir.isEmpty() ? "${env.WORKSPACE}/build/" : backupDir
                            
                            // // 1. Удаляем тестовую базу из кластера (если он там была) и очищаем клиентский кеш 1с
                            // dropDbTasks["dropDbTask_${testbase}"] = dropDbTask(
                            //     server1c, 
                            //     server1cPort, 
                            //     serverSql, 
                            //     testbase, 
                            //     admin1cUser, 
                            //     admin1cPwd,
                            //     sqluser,
                            //     sqlPwd
                            // )
                           
                            // // 2. Загружаем последний бэкап sql в тестовую
                            // restoreTasks["restoreTask_${testbase}"] = restoreTask(
                            //     serverSql, 
                            //     testbase, 
                            //     backupDir,
                            //     sqlUser,
                            //     sqlPwd
                            // )
                            // // 3. Создаем тестовую базу кластере 1С
                            // createDbTasks["createDbTask_${testbase}"] = createDbTask(
                            //     "${server1c}:${agent1cPort}",
                            //     serverSql,
                            //     platform1c,
                            //     testbase
                            // )

                            // //4. Подключаем базу к хранилищу.
                            // bindReposTasks["bindReposTask_${testbase}"] = bindReposTask(
                            //     platform1c, server1c, testbase, admin1cUser, admin1cPwd, storage1cPath, storageUser, storagePwd 
                            // ) 
                            
                            // //4. Подключаем базу к расширению хранилищу.
                            // bindReposExtTasks["bindReposExtTask_${testbase}"] = bindReposExtTask(
                            //     platform1c, server1c, testbase, admin1cUser, admin1cPwd, storages1cPathExt, storageUser, storagePwd, ext
                            // )   

                             // 5. Запускаем внешнюю обработку 1С, которая очищает базу от всплывающего окна с тем, что база перемещена при старте 1С
                            runHandlers1cTasks["runHandlers1cTask_${testbase}"] = runHandlers1cTask(
                                testbase, 
                                admin1cUser, 
                                admin1cPwd,
                                testbaseConnString
                            )
                        }

                        parallel dropDbTasks
                        parallel restoreTasks
                        parallel createDbTasks
                        parallel bindReposTasks
                        parallel bindReposExtTasks
                        parallel runHandlers1cTasks
                    }
                }
            }
        }
        stage("Тестирование ADD") {
            steps {
                timestamps {
                    script {

                        if (templatebasesList.size() == 0) {
                            return
                        }

                        platform1cLine = ""
                        if (platform1c != null && !platform1c.isEmpty()) {
                            platform1cLine = "--v8version ${platform1c}"
                        }

                        admin1cUsrLine = ""
                        if (admin1cUser != null && !admin1cUser.isEmpty()) {
                            admin1cUsrLine = "--db-user ${admin1cUser}"
                        }

                        admin1cPwdLine = ""
                        if (admin1cPwd != null && !admin1cPwd.isEmpty()) {
                            admin1cPwdLine = "--db-pwd ${admin1cPwd}"
                        }
                        // Запускаем ADD тестирование на произвольной базе, сохранившейся в переменной testbaseConnString
                        //runner run --execute ${env.WORKSPACE}/one_script_tools/vanessa-automation.epf --command \"-locktype unlock\" ${admin1cUsrLine} ${admin1cPwdLine} --ibconnection=${connString} /C'StartFeaturePlayer;VAParams=\\kami.local\files\shared\Отдел сопровождения 1С\scripts 1c_sql\tests\VAParams.json
                        // vrunner vanessa --settings tools\vrunner.json --v8version 8.3.24.1548 --ibconnection '/Slocalhost:1541\erp_test' --db-user Администратор --db-pwd 911 
                        // returnCode = utils.cmd("'C:\Program Files\1cv8\8.3.24.1548\bin\1cv8c.exe' /NАдминистратор /P911 /TestManager /Execute '\\kami.local\files\shared\Отдел сопровождения 1С\scripts 1c_sql\tests\vanessa-automation.1.2.041.1\vanessa-automation\vanessa-automation.epf' /IBConnectionString 'Srvr=""srv-sql-ls"";Ref=""erp_test"";" /C'StartFeaturePlayer;VAParams=\\kami.local\files\shared\Отдел сопровождения 1С\scripts 1c_sql\tests\VAParams.json')
                        // "runner vanessa --settings tools/vrunner.json --v8version 8.3.24.1548 --ibconnection '/Slocalhost:1541\erp_test' --db-user Администратор --db-pwd 911 --pathvanessa tools/add/vanessa-automation.epf"
                        // echo "runner vanessa --settings tools/vrunner.json ${platform1cLine} --ibconnection \"${testbaseConnString}\" ${admin1cUsrLine} ${admin1cPwdLine} --pathvanessa tools/add/vanessa-automation.epf"
                        // returnCode = utils.cmd("runner vanessa --settings tools/vrunner.json ${platform1cLine} --ibconnection \"${testbaseConnString}\" ${admin1cUsrLine} ${admin1cPwdLine} --pathvanessa tools/add/vanessa-automation.epf")
                        // "C:\Program Files\1cv8\8.3.24.1548\bin\1cv8c.exe" /NАдминистратор /P911 /TestManager /Execute "\\kami.local\files\shared\Отдел сопровождения 1С\scripts 1c_sql\tests\vanessa-automation.1.2.041.1\vanessa-automation\vanessa-automation.epf" /IBConnectionString "Srvr=""srv-sql-ls"";Ref=""erp_test"";" /C"StartFeaturePlayer;VAParams=\\kami.local\files\shared\Отдел сопровождения 1С\scripts 1c_sql\tests\VAParams.json"
                        returnCode = utils.cmd("runner vanessa --settings tools/vrunner.json ${platform1cLine} --ibconnection \"${testbaseConnString}\" ${admin1cUsrLine} ${admin1cPwdLine} --pathvanessa tools/add/bddRunner.epf")

                        if (returnCode != 0) {
                            utils.raiseError("Возникла ошибка при запуске ADD на сервере ${server1c} и базе ${testbase}")
                        }
                    }
                }
            }
        }
        stage("Шринкуем базу и делаем бэкап") {
            steps {
                timestamps {
                    script {
                        try {
                            sqlUtils.shrink_db(testbase, serverSql, backupDir, sqlUser, sqlPwd)
                        } catch (excp) {
                            echo "Error happened when shrink base ${testbase}."
                        }
                        }
                }
            }
        }
    }   
    post {
        always {
            script {
                if (currentBuild.result == "ABORTED") {
                    return
                }

                dir ('build/out/allure') {
                    writeFile file:'environment.properties', text:"Build=${env.BUILD_URL}"
                }

                allure includeProperties: false, jdk: '', results: [[path: 'build/out/allure']]
            }
        }
    }
}


def dropDbTask(server1c, server1cPort, serverSql, infobase, admin1cUser, admin1cPwd, sqluser, sqlPwd) {
    return {
        timestamps {
            stage("Удаление ${infobase}") {
                def projectHelpers = new ProjectHelpers()
                def utils = new Utils()

                projectHelpers.dropDb(server1c, server1cPort, serverSql, infobase, admin1cUser, admin1cPwd, sqluser, sqlPwd)
            }
        }
    }
}

def createDbTask(server1c, serverSql, platform1c, infobase) {
    return {
        stage("Создание базы ${infobase}") {
            timestamps {
                def projectHelpers = new ProjectHelpers()
                try {
                    projectHelpers.createDb(platform1c, server1c, serversql, infobase, null, false)
                } catch (excp) {
                    echo "Error happened when creating base ${infobase}. Probably base already exists in the ibases.v8i list. Skip the error"
                }
            }
        }
    }
}

def restoreTask(serverSql, infobase, backupDir, sqlUser, sqlPwd) {
    return {
        stage("Востановление ${infobase} бекапа") {
            timestamps {
                sqlUtils = new SqlUtils()

                sqlUtils.createEmptyDb(serverSql, infobase, sqlUser, sqlPwd)
                sqlUtils.restoreDb(serverSql, infobase, backupDir, sqlUser, sqlPwd)
            }
        }
    }
}

def runHandlers1cTask(infobase, admin1cUser, admin1cPwd, testbaseConnString) {
    return {
        stage("Запуск 1с обработки на ${infobase}") {
            timestamps {
                def projectHelpers = new ProjectHelpers()
                projectHelpers.unlocking1cBase(testbaseConnString, admin1cUser, admin1cPwd)
            }
        }
    }
}



def bindReposTask(platform1c, server1c, testbase, admin1cUser, admin1cPwd, storage1cPath, storageUser, storagePwd) {
  return {
        stage("Подключение и обновление из хранилища ${testbase}") {
            timestamps {
                prHelpers = new ProjectHelpers()

                if (storage1cPath == null || storage1cPath.isEmpty()) {
                    return
                }    
                prHelpers.bindRepo(platform1c, server1c, testbase, admin1cUser, admin1cPwd, storage1cPath, storageUser, storagePwd)

            }
        }
    }
}

def bindReposExtTask(platform1c, server1c, testbase, admin1cUser, admin1cPwd, storage1cPath, storageUser, storagePwd, ext) {
  return {
        stage("Подключение и обновление из расширения хранилища ${ext} ${testbase}") {
            timestamps {
                prHelpers = new ProjectHelpers()

                if (storage1cPath == null || storage1cPath.isEmpty()) {
                    return
                }    
                prHelpers.bindExtRepo(platform1c, server1c, testbase, admin1cUser, admin1cPwd, storage1cPath, storageUser, storagePwd, ext)

            }
        }
    }
}
