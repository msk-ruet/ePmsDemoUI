//Service
OvertimeSetup saved = repository.save(entity);


ApiDTO responseDTO = attendanceHolidaySetupService.save(null,attendanceHolidaySetupDAO,user_id);

//service- repository call
public ApiDTO save(Long id, AttendanceHolidaySetupDAO attendanceHolidaySetupDAO, String user_id) {
        Map<String, Object> data = attendanceHolidaySetupRepository.spAttendanceHolidaySetupSave(id,
                attendanceHolidaySetupDAO.getName(),
                attendanceHolidaySetupDAO.getDate(),
                attendanceHolidaySetupDAO.getRemark(),
                attendanceHolidaySetupDAO.getYear(),
                attendanceHolidaySetupDAO.getActive(),
                user_id, "E");

        if (Integer.parseInt(data.get("out_message_code").toString()) > 0) {
            throw new ServiceBusinessException(data.get("out_message_description").toString());
        }
        AttendanceHolidaySetup attendanceHolidaySetup = attendanceHolidaySetupRepository.findById(Long.parseLong(data.get("out_id").toString()))
                .orElseThrow(() -> new ServiceNotFoundException("Data not Found!!"));

        AttendanceHolidaySetupDTO attendanceHolidaySetupDTO = AttendanceHolidaySetupMapper.convertToDTO(attendanceHolidaySetup);
        ApiDTO<AttendanceHolidaySetupDTO> responseDTO = ApiDTO
                .<AttendanceHolidaySetupDTO>builder()
                .status(true)
                .message(data.get("out_message_description").toString())
                .data(attendanceHolidaySetupDTO)
                .build();
        return responseDTO;
    }
	
	
	//Repository -Store Procedure Call
	@Procedure(name = "attendance_holiday_setup_save")
    Map<String, Object> spAttendanceHolidaySetupSave(@Param("id") Long id,
                                                     @Param("name") String name,
                                                     @Param("date") LocalDate date,
                                                     @Param("remark") String remark,
                                                     @Param("year") Integer year,
                                                     @Param("active") Boolean active,
                                                     @Param("user") String user,
                                                     @Param("operation") String operation);
													 
													 
//Domain-Store Procedure Declaration													 
@NamedStoredProcedureQuery(
        name = "attendance_holiday_setup_save", procedureName = "SP_UM_HR_Attendance_Holiday_Setup_Save",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "name", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "date", type = LocalDate.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "remark", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "year", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "active", type = Boolean.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "user", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "operation", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "out_id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "out_message_code", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "out_message_description", type = String.class)
        }
)

//Database- (Store procedure) SP_UM_HR_Attendance_Holiday_Setup_Save  
CREATE PROCEDURE SP_UM_HR_Attendance_Holiday_Setup_Save
    @in_id BIGINT,
    @in_name VARCHAR(200),
    @in_date DATE,
    @in_remark VARCHAR(200),
    @in_year INT,
    @in_active BIT,
    @in_user VARCHAR(150),
    @in_operation VARCHAR(1),
    @out_id BIGINT OUTPUT,
    @out_message_code INT OUTPUT,
    @out_message_description VARCHAR(255) OUTPUT
AS
BEGIN
    SET @out_id = NULL;
    SET @out_message_code = 1;
    SET @out_message_description = '';

    BEGIN TRY
        IF @in_operation = 'E'
        BEGIN

             -- Name empty or null Check
             IF @in_name IS NULL OR @in_name = ''
             BEGIN
                 SET @out_message_code = 1;
                 SET @out_message_description = 'Name should not be NULL';
                 GOTO stop_level;
             END

             -- Date empty or null Check
             IF @in_date IS NULL
             BEGIN
                 SET @out_message_code = 1;
                 SET @out_message_description = 'Date should not be NULL';
                 GOTO stop_level;
             END

             -- Year empty or null Check
             IF @in_year IS NULL
             BEGIN
                 SET @out_message_code = 1;
                 SET @out_message_description = 'Year should not be NULL';
                 GOTO stop_level;
             END

             -- Active empty or null Check
             IF @in_active IS NULL
             BEGIN
                 SET @out_message_code = 1;
                 SET @out_message_description = 'Active should not be NULL';
                 GOTO stop_level;
             END


            IF @in_id IS NULL
            BEGIN

                BEGIN TRANSACTION;
                -- Data Save
                INSERT INTO UM_HR_Attendance_Holiday_Setup ( name,  date,  remark,  year,  active,  created_by, updated_by)
                VALUES ( @in_name,  @in_date,  @in_remark,  @in_year,  @in_active,  @in_user, @in_user);

                SELECT @out_id = SCOPE_IDENTITY();
                SET @out_message_code = 0;
                SET @out_message_description = 'Record created successfully';
                COMMIT TRANSACTION;
            END
            ELSE
            BEGIN
                -- Id Exists Check
                IF NOT EXISTS (SELECT 1 FROM UM_HR_Attendance_Holiday_Setup WHERE id = @in_id)
                BEGIN
                    SET @out_message_code = 1;
                    SET @out_message_description = 'Record Id not found.';
                    GOTO stop_level;
                END


                BEGIN TRANSACTION;
                -- Data Update
                UPDATE UM_HR_Attendance_Holiday_Setup SET  name = @in_name,
                         date = @in_date,
                         remark = @in_remark,
                         year = @in_year,
                         active = @in_active,

                         updated_by = @in_user,
                         updated_at = GETDATE()
                WHERE id = @in_id;
                SET @out_id = @in_id;
                SET @out_message_code = 0;
                SET @out_message_description = 'Record updated successfully';
                COMMIT TRANSACTION;
            END
        END
        ELSE IF @in_operation = 'D'
        BEGIN
            IF @in_id IS NULL
            BEGIN
                SET @out_message_code = 1;
                SET @out_message_description = 'Record Id should not be NULL';
                GOTO stop_level;
            END

            IF NOT EXISTS (SELECT 1 FROM UM_HR_Attendance_Holiday_Setup WHERE id = @in_id)
            BEGIN
                SET @out_message_code = 1;
                SET @out_message_description = 'Record Id not found';
                GOTO stop_level;
            END

            BEGIN TRANSACTION;
            -- Data Delete
            DELETE FROM UM_HR_Attendance_Holiday_Setup WHERE id = @in_id;

            SET @out_message_code = 0;
            SET @out_message_description = 'Record deleted successfully';
            COMMIT TRANSACTION;
        END
        ELSE
        BEGIN
            SET @out_message_code = 1;
            SET @out_message_description = 'Operation not found.';
        END
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @out_message_code = 1;
        SET @out_message_description = ERROR_MESSAGE();
    END CATCH

    stop_level:
END
go

grant alter, execute on dbo.SP_UM_HR_Attendance_Holiday_Setup_Save to []
go

grant alter, control, execute, take ownership, view definition on dbo.SP_UM_HR_Attendance_Holiday_Setup_Save to hr
go


													 