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

-------------------------------------------
-- ТЕЛО СКРИПТА

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

SET @SQLString = 

'

-- Очищаем версии объектов, вх, исх почту
TRUNCATE Table _InfoRg41901;--версии объектов
TRUNCATE Table _InfoRg41901X1;
TRUNCATE Table _InfoRg43608;--замеры времени
TRUNCATE Table _InfoRg92352; --лог напоминаний
TRUNCATE Table _InfoRg92352X1; --лог напоминаний
TRUNCATE Table InfoRg108340;-- использовагние отчетов
TRUNCATE Table InfoRg50033; -- сведенья о файлах
TRUNCATE Table _InfoRg85131; --–егистр—ведений.ѕЋ_”ведомлени¤
TRUNCATE Table _InfoRg85131X1; --–егистр—ведений.ѕЋ_”ведомлени¤
TRUNCATE Table _InfoRg101074; --–егистр—ведений. јћ»_ќчередьќбмен—ервисна¤—лужба
TRUNCATE TABLE _Document1440
TRUNCATE TABLE _Document1440_VT37653
TRUNCATE TABLE _Document1440_VT37658
TRUNCATE TABLE _Document1440_VT37663
TRUNCATE TABLE _Document1440_VT37668
TRUNCATE TABLE _Document1440_VT37675
TRUNCATE TABLE _Document1440_VT37680
TRUNCATE TABLE _Document1440_VT37685
TRUNCATE TABLE _Document1440_VT87805
TRUNCATE TABLE _Document1441
TRUNCATE TABLE _Document1441_VT37723
TRUNCATE TABLE _Document1441_VT37728
TRUNCATE TABLE _Document1441_VT37733
TRUNCATE TABLE _Document1441_VT37738
TRUNCATE TABLE _Document1441_VT37743
TRUNCATE TABLE _Document1441_VT37750
TRUNCATE TABLE _Document1441_VT37755
TRUNCATE TABLE _Document1441_VT37758
TRUNCATE TABLE _Document1441_VT87824
TRUNCATE TABLE _Reference872
TRUNCATE TABLE _Reference872_VT80402
TRUNCATE TABLE _Reference872_VT80390
TRUNCATE TABLE _DocumentJournal83098

'

PRINT @SQLString
EXEC sp_executesql @SQLString

SET @SQLString = 
	'DBCC SHRINKFILE (N''' + @DBName_From + ''' , 2187);'				
-- Выводим и выполняем полученную инструкцию
PRINT @SQLString
EXEC sp_executesql @SQLString



-------------------------------------------
-- ТЕЛО СКРИПТА
use master

-- Формируем строку для исполнения
SET @SQLString = 
	N'BACKUP DATABASE [' + @DBName_To + ']
	TO DISK = N''' + @Path + '\' + @DBName_From + '_fresh_cut_.bak''		  
	WITH NOFORMAT, NOINIT,
	SKIP, NOREWIND, NOUNLOAD, STATS = 10'

-- Выводим и выполняем полученную инструкцию
PRINT @SQLString
EXEC sp_executesql @SQLString