DECLARE @DBName_To as nvarchar(40) = '$(restoreddb)';  
DECLARE @DBName_From as nvarchar(256);  

DECLARE @SQLString NVARCHAR(MAX);

-- Используем динамический SQL для смены контекста базы данных, так как USE не поддерживает переменные
SET @SQLString = N'USE ' + QUOTENAME(@DBName_To) + ';
SELECT @DBName_FromOUT = name FROM sys.master_files
WHERE database_id = DB_ID(''' + @DBName_To + ''') AND type_desc = ''LOG'';';

EXEC sp_executesql @SQLString, N'@DBName_FromOUT nvarchar(256) OUTPUT', @DBName_From OUTPUT;

-- Устанавливаем режим восстановления базы данных
SET @SQLString = 'ALTER DATABASE ' + QUOTENAME(@DBName_To) + ' SET RECOVERY SIMPLE WITH NO_WAIT';
EXEC sp_executesql @SQLString;

WAITFOR DELAY '00:00:10';

-- Сжимаем файл логов
SET @SQLString = 'USE ' + QUOTENAME(@DBName_To) + '; DBCC SHRINKFILE (N''' + @DBName_From + ''' , 2187);';
PRINT @SQLString;  -- Выводим инструкцию для проверки
EXEC sp_executesql @SQLString;
