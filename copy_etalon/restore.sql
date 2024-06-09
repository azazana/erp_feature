DECLARE @mydb nvarchar(50);
-- Ввести имя базы данных
set @mydb = '$(restoreddb)';

DECLARE @back_file nvarchar(300);
-- ������ ������������ ����� ������
set @back_file = '$(bakfile)';

use master;

declare @ver varchar(4)
select @ver =  left(convert(nvarchar,(SERVERPROPERTY('ProductVersion'))),2)
print(@ver)
if @ver= 12
begin
CREATE TABLE #restoreFiles (LogicalName NVARCHAR(128),

                            PhysicalName NVARCHAR(260),

                            Type CHAR(1),

                            FileGroupName NVARCHAR(128),

                            Size NUMERIC(20, 0),

                            MaxSize NUMERIC(20, 0),

                            FileID BIGINT,

                            CreateLSN NUMERIC(25, 0),

                            DropLSN NUMERIC(25, 0) NULL,

                            UniqueID UNIQUEIDENTIFIER,

                            ReadOnlyLSN NUMERIC(25, 0) NULL,

                            ReadWriteLSN NUMERIC(25, 0) NULL,

                            BackupSizeInBytes BIGINT,

                            SourceBlockSize INT,

                            FileGroupID INT,

                            LogGroupGUID UNIQUEIDENTIFIER NULL,

                            DifferentialBaseLSN NUMERIC(25, 0) NULL,

                            DifferentialBaseGUID UNIQUEIDENTIFIER,

                            IsReadOnly BIT,

                            IsPresent BIT,
							TDEThumbprint varbinary(32));

DECLARE @SQLText nvarchar(max);
SET @SQLText = 'restore filelistonly FROM  DISK = '''+@back_file+'''';

INSERT  INTO #restoreFiles

exec (@SQLText);

DECLARE @logicalDataFile NVARCHAR(128),

    @logicalLogFile NVARCHAR(128);

 

SELECT  @logicalDataFile = LogicalName

FROM    #restoreFiles

WHERE   Type = 'D';

 

SELECT  @logicalLogFile = LogicalName

FROM    #restoreFiles

WHERE   Type = 'L';

-- ���������� ���������� ������������ mdf �����
DECLARE @myfile_mdf_phys nvarchar(300);
set @myfile_mdf_phys = (select physical_name from sys.master_files where db_name(database_id) = @mydb and type_desc = 'ROWS');

-- ���������� ���������� ������������ ldf �����
DECLARE @myfile_ldf_phys nvarchar(300);
set @myfile_ldf_phys = (select physical_name from sys.master_files where db_name(database_id) = @mydb and type_desc = 'LOG');

DECLARE @SQLSingle nvarchar(max);
SET @SQLSingle = 'ALTER DATABASE '+ @mydb +' SET SINGLE_USER WITH ROLLBACK IMMEDIATE';
exec (@SQLSingle);


-- ��������������� ���� ������ @mydb, ����� ����� � ���� @myfile_mdf_phys � @myfile_ldf_phys
RESTORE DATABASE @mydb FROM  DISK = @back_file WITH  FILE = 1, MOVE @logicalDataFile TO @myfile_mdf_phys,  MOVE @logicalLogFile TO @myfile_ldf_phys,  NOUNLOAD, REPLACE,   STATS = 10;


DECLARE @SQLMulti nvarchar(max);
SET @SQLMulti = 'ALTER DATABASE '+ @mydb +' SET MULTI_USER';
exec (@SQLMulti);
end

else
begin
if @ver= 13 or @ver= 15
begin
CREATE TABLE #restoreFiles2 (LogicalName NVARCHAR(128),

                            PhysicalName NVARCHAR(260),

                            Type CHAR(1),

                            FileGroupName NVARCHAR(128),

                            Size NUMERIC(20, 0),

                            MaxSize NUMERIC(20, 0),

                            FileID BIGINT,

                            CreateLSN NUMERIC(25, 0),

                            DropLSN NUMERIC(25, 0) NULL,

                            UniqueID UNIQUEIDENTIFIER,

                            ReadOnlyLSN NUMERIC(25, 0) NULL,

                            ReadWriteLSN NUMERIC(25, 0) NULL,

                            BackupSizeInBytes BIGINT,

                            SourceBlockSize INT,

                            FileGroupID INT,

                            LogGroupGUID UNIQUEIDENTIFIER NULL,

                            DifferentialBaseLSN NUMERIC(25, 0) NULL,

                            DifferentialBaseGUID UNIQUEIDENTIFIER,

                            IsReadOnly BIT,

                            IsPresent BIT,
							TDEThumbprint varbinary(32),
							SnapshotURL nvarchar(360) NULL);

--DECLARE @SQLText nvarchar(max);
SET @SQLText = 'restore filelistonly FROM  DISK = '''+@back_file+'''';

INSERT  INTO #restoreFiles2

exec (@SQLText);

--DECLARE @logicalDataFile NVARCHAR(128),

--    @logicalLogFile NVARCHAR(128);

 

SELECT  @logicalDataFile = LogicalName

FROM    #restoreFiles2

WHERE   Type = 'D';

 

SELECT  @logicalLogFile = LogicalName

FROM    #restoreFiles2

WHERE   Type = 'L';

-- ���������� ���������� ������������ mdf �����
--DECLARE @myfile_mdf_phys nvarchar(300);
set @myfile_mdf_phys = (select physical_name from sys.master_files where db_name(database_id) = @mydb and type_desc = 'ROWS');

-- ���������� ���������� ������������ ldf �����
--DECLARE @myfile_ldf_phys nvarchar(300);
set @myfile_ldf_phys = (select physical_name from sys.master_files where db_name(database_id) = @mydb and type_desc = 'LOG');

--DECLARE @SQLSingle nvarchar(max);
SET @SQLSingle = 'ALTER DATABASE '+ @mydb +' SET SINGLE_USER WITH ROLLBACK IMMEDIATE';
exec (@SQLSingle);


-- ��������������� ���� ������ @mydb, ����� ����� � ���� @myfile_mdf_phys � @myfile_ldf_phys
RESTORE DATABASE @mydb FROM  DISK = @back_file WITH  FILE = 1, MOVE @logicalDataFile TO @myfile_mdf_phys,  MOVE @logicalLogFile TO @myfile_ldf_phys,  NOUNLOAD, REPLACE,   STATS = 10;


--DECLARE @SQLMulti nvarchar(max);
SET @SQLMulti = 'ALTER DATABASE '+ @mydb +' SET MULTI_USER';
exec (@SQLMulti);
end
end
go