#Использовать v8runner
#Использовать cmdline

Перем СЕРВЕР;
Перем СЕРВЕР_ПОРТ;
Перем БАЗА;
Перем ЭТО_ФАЙЛОВАЯ_БАЗА;
Перем ПОЛЬЗОВАТЕЛЬ;
Перем ПАРОЛЬ;
Перем ПЛАТФОРМА_ВЕРСИЯ;
Перем АДРЕС_ХРАНИЛИЩА;
Перем ПОЛЬЗОВАТЕЛЬ_ХРАНИЛИЩА;
Перем ПАРОЛЬ_ХРАНИЛИЩА;

Перем Конфигуратор;
Перем ЛОГ;

Функция Инициализация()
    
    ЛОГ = Логирование.ПолучитьЛог("bindRepo");
    Лог.УстановитьУровень(УровниЛога.Отладка);
    ЛОГ.Отладка("строка = " + АргументыКоманднойСтроки);
  
    Парсер = Новый ПарсерАргументовКоманднойСтроки();
    Парсер.ДобавитьИменованныйПараметр("-platform");
    Парсер.ДобавитьИменованныйПараметр("-server");
    Парсер.ДобавитьИменованныйПараметр("-base");
    Парсер.ДобавитьИменованныйПараметр("-user");
    Парсер.ДобавитьИменованныйПараметр("-passw");
    Парсер.ДобавитьИменованныйПараметр("-storage1c");
    Парсер.ДобавитьИменованныйПараметр("-storage1cuser");
    // Парсер.ДобавитьИменованныйПараметр("-storage1cpwd");
  
    Параметры = Парсер.Разобрать(АргументыКоманднойСтроки);

    ПЛАТФОРМА_ВЕРСИЯ   = Параметры["-platform"];
    СЕРВЕР            = Параметры["-server"];
    СЕРВЕР_ПОРТ       = 1541; // 1541 - по умолчанию
    БАЗА              = Параметры["-base"];
    ЭТО_ФАЙЛОВАЯ_БАЗА = Не ЗначениеЗаполнено(СЕРВЕР);
    ПОЛЬЗОВАТЕЛЬ      = Параметры["-user"];
    ПАРОЛЬ            = Параметры["-passw"];
    АДРЕС_ХРАНИЛИЩА   = Параметры["-storage1c"];
    ПОЛЬЗОВАТЕЛЬ_ХРАНИЛИЩА = Параметры["-storage1cuser"];
    ПАРОЛЬ_ХРАНИЛИЩА = "";

    ЛОГ.Отладка("ПЛАТФОРМА_ВЕРСИЯ = " + ПЛАТФОРМА_ВЕРСИЯ);
    ЛОГ.Отладка("СЕРВЕР = " + СЕРВЕР);
    ЛОГ.Отладка("БАЗА = " + БАЗА);
    ЛОГ.Отладка("ПОЛЬЗОВАТЕЛЬ = " + ПОЛЬЗОВАТЕЛЬ);
    ЛОГ.Отладка("ПАРОЛЬ = " + ПАРОЛЬ);
    ЛОГ.Отладка("АДРЕС_ХРАНИЛИЩА = " + АДРЕС_ХРАНИЛИЩА);
    ЛОГ.Отладка("ПОЛЬЗОВАТЕЛЬ_ХРАНИЛИЩА = " + ПОЛЬЗОВАТЕЛЬ_ХРАНИЛИЩА);
    ЛОГ.Отладка("ПАРОЛЬ_ХРАНИЛИЩА = " + ПАРОЛЬ_ХРАНИЛИЩА);

    Конфигуратор = Новый УправлениеКонфигуратором();
    Конфигуратор.УстановитьКонтекст(СтрокаСоединенияИБ(), ПОЛЬЗОВАТЕЛЬ, ""+ПАРОЛЬ);
    Конфигуратор.ИспользоватьВерсиюПлатформы(ПЛАТФОРМА_ВЕРСИЯ);
    
КонецФункции

Функция ПодключитьсяКХранилищу()
    Конфигуратор.ОтключитьсяОтХранилища(); 
    Лог.Информация("Unbind repo completed");
    Конфигуратор.ПодключитьсяКХранилищу(АДРЕС_ХРАНИЛИЩА, ПОЛЬЗОВАТЕЛЬ_ХРАНИЛИЩА, ""+ПАРОЛЬ_ХРАНИЛИЩА, Истина, Истина);
    Лог.Информация("Bind repo completed");
КонецФункции

Функция СтрокаСоединенияИБ() 
    Если ЭТО_ФАЙЛОВАЯ_БАЗА Тогда
        Возврат "/F" + БАЗА; 
    Иначе   
        Возврат "/IBConnectionString""Srvr=" + СЕРВЕР + ?(ЗначениеЗаполнено(СЕРВЕР_ПОРТ),":" + СЕРВЕР_ПОРТ,"") + ";Ref='"+ БАЗА + "'""";
    КонецЕсли;
КонецФункции

Инициализация();
Лог.Информация("Connecting to 1C storage...");
ПодключитьсяКХранилищу();
Лог.Информация("Sucessfuly connected to 1C storage");