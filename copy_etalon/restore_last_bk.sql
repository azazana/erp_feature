-- НАСТРАИВАЕМЫЕ ПЕРЕМЕННЫЕ
-- База данных назначения
DECLARE @DBName_To as nvarchar(40) = '$(restoreddb)';
-- База данных источник						
DECLARE @DBName_From as nvarchar(40) = 'erp_w_001'
DECLARE @backupfile as nvarchar(500) =  '$(bakfile)'
DECLARE @Path as nvarchar(400) = 'B:\BACKUP'

-------------------------------------------
-- СЛУЖЕБНЫЕ ПЕРЕМЕННЫЕ	
DECLARE @SQLString NVARCHAR(4000)
DECLARE @physicalName NVARCHAR(500), @logicalName NVARCHAR(500)
--test

-------------------------------------------

use master

DECLARE @KillCommand nvarchar(max)
SET @KillCommand = ''

SELECT @KillCommand = @KillCommand + 'KILL ' + CONVERT(varchar(5), session_id) + ';'
FROM sys.dm_exec_sessions
WHERE database_id  = db_id(@DBName_To)

EXEC(@KillCommand)

USE erp_test;

SET @SQLString = 
'ALTER DATABASE CURRENT SET RECOVERY SIMPLE WITH NO_WAIT'
EXEC sp_executesql @SQLString

WAITFOR DELAY '00:00:10'

SET @SQLString = 
	'DBCC SHRINKFILE (N''' + @DBName_From + '_log'' , 2187);'				
-- Выводим и выполняем полученную инструкцию
PRINT @SQLString
EXEC sp_executesql @SQLString