-- ================================================================================
-- 15. CALENDAR & ATTENDANCE (14 tables)
-- ================================================================================
-- Tables: academic_calendar, course_calendar, calendar_event, event_participant, event_reminder, attendance_session, student_attendance, attendance_record, office_hour_slot, appointment_booking, conference_session, planner_note, planner_override, conference_participant
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- AcademicCalendar: Institution-wide academic calendar
CREATE TABLE academic_calendar (
    calendar_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    calendar_name VARCHAR(100) NOT NULL,
    calendar_year INT NOT NULL,
    calendar_description TEXT,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_academic_calendar UNIQUE (tenant_id, calendar_year, calendar_name),
    CONSTRAINT ck_academic_calendar_year CHECK (calendar_year >= 1900 AND calendar_year <= 2100),
    CONSTRAINT ck_academic_calendar_name CHECK (LENGTH(calendar_name) >= 3)
);

-- CourseCalendar: Course-specific calendar and scheduling
CREATE TABLE course_calendar (
    calendar_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL UNIQUE,
    section_id UUID,
    calendar_name VARCHAR(100),
    semester VARCHAR(20),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    holidays JSONB,
    breaks JSONB,
    custom_schedule JSONB,
    is_sync_with_academic BOOLEAN DEFAULT TRUE,
    sync_status VARCHAR(50),
    last_sync_at TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_course_calendar_dates CHECK (end_date >= start_date),
    CONSTRAINT ck_course_calendar_sync_status CHECK (sync_status IS NULL OR sync_status IN ('SYNCED', 'PENDING', 'FAILED'))
);

-- CalendarEvent: Events (classes, deadlines, milestones, etc.)
CREATE TABLE calendar_event (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    section_id UUID,
    calendar_id UUID,
    event_title VARCHAR(255) NOT NULL,
    event_description TEXT,
    event_type VARCHAR(50) NOT NULL,
    event_status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    location VARCHAR(255),
    location_url VARCHAR(500),
    is_online BOOLEAN DEFAULT FALSE,
    start_date DATE NOT NULL,
    start_time TIME,
    end_date DATE,
    end_time TIME,
    duration_minutes INT,
    is_all_day BOOLEAN DEFAULT FALSE,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern VARCHAR(50),
    recurrence_end_date DATE,
    recurrence_exceptions JSONB,
    is_mandatory BOOLEAN DEFAULT TRUE,
    max_capacity INT,
    current_attendance INT DEFAULT 0,
    visibility VARCHAR(50) DEFAULT 'PUBLIC',
    allow_registration BOOLEAN DEFAULT FALSE,
    registration_deadline DATE,
    created_by UUID NOT NULL,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_calendar_event_title CHECK (LENGTH(event_title) >= 3),
    CONSTRAINT ck_calendar_event_type CHECK (event_type IN ('CLASS', 'LAB', 'LECTURE', 'SEMINAR', 'EXAM', 'DEADLINE', 'HOLIDAY', 'BREAK', 'OFFICE_HOURS', 'MEETING', 'PRESENTATION', 'MILESTONE', 'REVIEW_SESSION')),
    CONSTRAINT ck_calendar_event_status CHECK (event_status IN ('SCHEDULED', 'CONFIRMED', 'CANCELLED', 'POSTPONED', 'COMPLETED')),
    CONSTRAINT ck_calendar_event_dates CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT ck_calendar_event_capacity CHECK (max_capacity IS NULL OR max_capacity > 0),
    CONSTRAINT ck_calendar_event_attendance CHECK (current_attendance >= 0),
    CONSTRAINT ck_calendar_event_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE', 'INTERNAL', 'RESTRICTED')),
    CONSTRAINT ck_calendar_event_recurrence CHECK (is_recurring = FALSE OR recurrence_pattern IS NOT NULL)
);

-- EventParticipant: Track who is attending events
CREATE TABLE event_participant (
    participant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    registration_status VARCHAR(50) NOT NULL DEFAULT 'REGISTERED',
    rsvp_status VARCHAR(50),
    registered_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registered_by_id UUID,
    attended BOOLEAN DEFAULT FALSE,
    attendance_marked_at TIMESTAMPTZ,
    marked_by_id UUID,
    attendance_late BOOLEAN DEFAULT FALSE,
    late_minutes INT,
    notes TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_event_participant UNIQUE (event_id, user_id),
    CONSTRAINT ck_event_participant_status CHECK (registration_status IN ('REGISTERED', 'WAITLISTED', 'CANCELLED', 'NO_SHOW', 'EXCUSED')),
    CONSTRAINT ck_event_participant_rsvp CHECK (rsvp_status IS NULL OR rsvp_status IN ('ACCEPTED', 'DECLINED', 'TENTATIVE', 'NO_RESPONSE')),
    CONSTRAINT ck_event_participant_late_minutes CHECK (late_minutes IS NULL OR late_minutes >= 0)
);

-- EventReminder: Reminder notifications for events
CREATE TABLE event_reminder (
    reminder_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reminder_type VARCHAR(50) NOT NULL,
    reminder_time INT,
    reminder_unit VARCHAR(10),
    send_email BOOLEAN DEFAULT TRUE,
    send_sms BOOLEAN DEFAULT FALSE,
    send_push BOOLEAN DEFAULT TRUE,
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_event_reminder_type CHECK (reminder_type IN ('BEFORE_EVENT', 'DAY_BEFORE', 'WEEK_BEFORE', 'MONTH_BEFORE', 'CUSTOM')),
    CONSTRAINT ck_event_reminder_unit CHECK (reminder_unit IN ('MINUTES', 'HOURS', 'DAYS', 'WEEKS'))
);

-- AttendanceSession: Attendance tracking sessions (per class/event)
CREATE TABLE attendance_session (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    event_id UUID NOT NULL,
    section_id UUID NOT NULL,
    session_date DATE NOT NULL,
    session_start_time TIME,
    session_end_time TIME,
    attendance_status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    attendance_method VARCHAR(50) NOT NULL,
    attendance_code VARCHAR(10),
    attendance_code_expires_at TIMESTAMPTZ,
    is_locked BOOLEAN DEFAULT FALSE,
    locked_at TIMESTAMPTZ,
    locked_by_id UUID,
    total_enrolled INT DEFAULT 0,
    total_present INT DEFAULT 0,
    total_absent INT DEFAULT 0,
    total_late INT DEFAULT 0,
    total_excused INT DEFAULT 0,
    notes TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_attendance_session_status CHECK (attendance_status IN ('OPEN', 'IN_PROGRESS', 'CLOSED', 'PENDING', 'LOCKED')),
    CONSTRAINT ck_attendance_session_method CHECK (attendance_method IN ('MANUAL', 'QR_CODE', 'BIOMETRIC', 'RFID', 'GEOLOCATION', 'PIN_CODE'))
);

-- StudentAttendance: Individual student attendance records
CREATE TABLE student_attendance (
    attendance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    attendance_status VARCHAR(50) NOT NULL,
    marked_time TIMESTAMPTZ,
    marked_by_id UUID,
    arrival_time TIME,
    departure_time TIME,
    is_late BOOLEAN DEFAULT FALSE,
    late_minutes INT,
    is_excused BOOLEAN DEFAULT FALSE,
    excused_reason VARCHAR(255),
    excused_by_id UUID,
    excused_until DATE,
    notes TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_attendance UNIQUE (session_id, user_id),
    CONSTRAINT ck_student_attendance_status CHECK (attendance_status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED', 'PENDING', 'UNMARKED')),
    CONSTRAINT ck_student_attendance_late_minutes CHECK (late_minutes IS NULL OR late_minutes >= 0)
);

-- AttendanceRecord: Aggregated attendance records per student per course
CREATE TABLE attendance_record (
    record_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    section_id UUID NOT NULL,
    user_id UUID NOT NULL,
    total_sessions INT DEFAULT 0,
    total_present INT DEFAULT 0,
    total_absent INT DEFAULT 0,
    total_late INT DEFAULT 0,
    total_excused INT DEFAULT 0,
    total_unmarked INT DEFAULT 0,
    attendance_percentage NUMERIC(5,2),
    on_track BOOLEAN DEFAULT TRUE,
    last_attendance_date DATE,
    last_absence_date DATE,
    consecutive_absences INT DEFAULT 0,
    requires_intervention BOOLEAN DEFAULT FALSE,
    intervention_notes TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_attendance_record UNIQUE (section_id, user_id),
    CONSTRAINT ck_attendance_record_sessions CHECK (total_sessions >= 0),
    CONSTRAINT ck_attendance_record_percentage CHECK (attendance_percentage IS NULL OR (attendance_percentage >= 0 AND attendance_percentage <= 100)),
    CONSTRAINT ck_attendance_record_consecutive CHECK (consecutive_absences >= 0)
);

-- OFFICE HOURS & APPOINTMENTS (Canvas Scheduler)
CREATE TABLE office_hour_slot (
    slot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    host_user_id UUID NOT NULL,
    course_id UUID,
    section_id UUID,
    date_start TIMESTAMPTZ NOT NULL,
    date_end TIMESTAMPTZ NOT NULL,
    capacity INT NOT NULL DEFAULT 1,
    modality VARCHAR(20) NOT NULL DEFAULT 'ONLINE',
    location VARCHAR(255),
    meeting_url VARCHAR(500),
    notes TEXT,
    is_cancelled BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_office_hour_slot_dates CHECK (date_end > date_start),
    CONSTRAINT ck_office_hour_slot_capacity CHECK (capacity > 0),
    CONSTRAINT ck_office_hour_slot_modality CHECK (modality IN ('IN_PERSON','ONLINE','HYBRID'))
);

CREATE TABLE appointment_booking (
    booking_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    slot_id UUID NOT NULL,
    user_id UUID NOT NULL,
    booking_status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
    agenda TEXT,
    date_cancelled TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_appointment_booking UNIQUE (slot_id, user_id),
    CONSTRAINT ck_appointment_booking_status CHECK (booking_status IN ('BOOKED','CANCELLED','COMPLETED','NO_SHOW'))
);

-- WEB CONFERENCING (Zoom / Teams / BigBlueButton)
CREATE TABLE conference_session (
    conference_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    section_id UUID,
    event_id UUID,
    host_user_id UUID,
    provider VARCHAR(30) NOT NULL,
    provider_meeting_id VARCHAR(255),
    conference_title VARCHAR(255) NOT NULL,
    join_url VARCHAR(1000),
    host_url VARCHAR(1000),
    date_start TIMESTAMPTZ,
    date_end TIMESTAMPTZ,
    recording_file_id UUID,
    conference_status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    metadata JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_conference_session_provider CHECK (provider IN ('ZOOM','TEAMS','GOOGLE_MEET','BIGBLUEBUTTON','WEBEX','JITSI','OTHER')),
    CONSTRAINT ck_conference_session_status CHECK (conference_status IN ('SCHEDULED','LIVE','ENDED','CANCELLED')),
    CONSTRAINT ck_conference_session_dates CHECK (date_end IS NULL OR date_start IS NULL OR date_end >= date_start)
);

CREATE TABLE planner_note (
    planner_note_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    course_id UUID,
    note_title VARCHAR(255) NOT NULL,
    note_details TEXT,
    date_todo TIMESTAMPTZ NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE planner_override (
    planner_override_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    item_id UUID NOT NULL,
    is_marked_complete BOOLEAN NOT NULL DEFAULT FALSE,
    is_dismissed BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_planner_override UNIQUE (user_id, item_type, item_id),
    CONSTRAINT ck_planner_override_type CHECK (item_type IN ('ASSIGNMENT','ASSESSMENT','DISCUSSION','CONTENT','ANNOUNCEMENT','CALENDAR_EVENT','PLANNER_NOTE'))
);

CREATE TABLE conference_participant (
    conference_participant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    conference_id UUID NOT NULL,
    user_id UUID NOT NULL,
    participant_role VARCHAR(20) NOT NULL DEFAULT 'PARTICIPANT',
    date_joined TIMESTAMPTZ NOT NULL,
    date_left TIMESTAMPTZ,
    duration_seconds INT,
    CONSTRAINT ck_conference_participant_role CHECK (participant_role IN ('MODERATOR','PRESENTER','PARTICIPANT','GUEST')),
    CONSTRAINT ck_conference_participant_dates CHECK (date_left IS NULL OR date_left >= date_joined)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_academic_calendar_tenant ON academic_calendar(tenant_id);
CREATE INDEX idx_academic_calendar_year ON academic_calendar(calendar_year);
CREATE INDEX idx_academic_calendar_is_active ON academic_calendar(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_course_calendar_section ON course_calendar(section_id);
CREATE INDEX idx_course_calendar_dates ON course_calendar(start_date, end_date);
CREATE INDEX idx_calendar_event_tenant ON calendar_event(tenant_id);
CREATE INDEX idx_calendar_event_course ON calendar_event(course_id);
CREATE INDEX idx_calendar_event_section ON calendar_event(section_id);
CREATE INDEX idx_calendar_event_type ON calendar_event(event_type);
CREATE INDEX idx_calendar_event_status ON calendar_event(event_status);
CREATE INDEX idx_calendar_event_dates ON calendar_event(start_date, end_date);
CREATE INDEX idx_calendar_event_created_by ON calendar_event(created_by);
CREATE INDEX idx_calendar_event_is_mandatory ON calendar_event(is_mandatory) WHERE is_mandatory = TRUE;
CREATE INDEX idx_event_participant_event ON event_participant(event_id);
CREATE INDEX idx_event_participant_user ON event_participant(user_id);
CREATE INDEX idx_event_participant_status ON event_participant(registration_status);
CREATE INDEX idx_event_participant_rsvp ON event_participant(rsvp_status);
CREATE INDEX idx_event_participant_attended ON event_participant(attended) WHERE attended = TRUE;
CREATE INDEX idx_event_reminder_event ON event_reminder(event_id);
CREATE INDEX idx_event_reminder_user ON event_reminder(user_id);
CREATE INDEX idx_event_reminder_is_sent ON event_reminder(is_sent) WHERE is_sent = FALSE;
CREATE INDEX idx_attendance_session_event ON attendance_session(event_id);
CREATE INDEX idx_attendance_session_section ON attendance_session(section_id);
CREATE INDEX idx_attendance_session_date ON attendance_session(session_date);
CREATE INDEX idx_attendance_session_status ON attendance_session(attendance_status);
CREATE INDEX idx_student_attendance_session ON student_attendance(session_id);
CREATE INDEX idx_student_attendance_user ON student_attendance(user_id);
CREATE INDEX idx_student_attendance_status ON student_attendance(attendance_status);
CREATE INDEX idx_student_attendance_is_late ON student_attendance(is_late) WHERE is_late = TRUE;
CREATE INDEX idx_student_attendance_is_excused ON student_attendance(is_excused) WHERE is_excused = TRUE;
CREATE INDEX idx_attendance_record_section ON attendance_record(section_id);
CREATE INDEX idx_attendance_record_user ON attendance_record(user_id);
CREATE INDEX idx_attendance_record_percentage ON attendance_record(attendance_percentage);
CREATE INDEX idx_attendance_record_on_track ON attendance_record(on_track) WHERE on_track = FALSE;
CREATE INDEX idx_attendance_record_requires_intervention ON attendance_record(requires_intervention) WHERE requires_intervention = TRUE;
CREATE INDEX idx_course_calendar_tenant ON course_calendar(tenant_id);
CREATE INDEX idx_event_participant_tenant ON event_participant(tenant_id);
CREATE INDEX idx_event_reminder_tenant ON event_reminder(tenant_id);
CREATE INDEX idx_attendance_session_tenant ON attendance_session(tenant_id);
CREATE INDEX idx_student_attendance_tenant ON student_attendance(tenant_id);
CREATE INDEX idx_attendance_record_tenant ON attendance_record(tenant_id);
CREATE INDEX idx_office_hour_slot_host_start ON office_hour_slot(host_user_id, date_start);
CREATE INDEX idx_conference_session_course ON conference_session(course_id, date_start);
CREATE INDEX idx_planner_note_user_date ON planner_note(user_id, date_todo);
CREATE INDEX idx_appointment_booking_tenant_id_fk ON appointment_booking(tenant_id);
CREATE INDEX idx_appointment_booking_user_id_fk ON appointment_booking(user_id);
CREATE INDEX idx_attendance_session_locked_by_id_fk ON attendance_session(locked_by_id);
CREATE INDEX idx_calendar_event_calendar_id_fk ON calendar_event(calendar_id);
CREATE INDEX idx_conference_session_event_id_fk ON conference_session(event_id);
CREATE INDEX idx_conference_session_host_user_id_fk ON conference_session(host_user_id);
CREATE INDEX idx_conference_session_recording_file_id_fk ON conference_session(recording_file_id);
CREATE INDEX idx_conference_session_section_id_fk ON conference_session(section_id);
CREATE INDEX idx_conference_session_tenant_id_fk ON conference_session(tenant_id);
CREATE INDEX idx_event_participant_marked_by_id_fk ON event_participant(marked_by_id);
CREATE INDEX idx_event_participant_registered_by_id_fk ON event_participant(registered_by_id);
CREATE INDEX idx_office_hour_slot_course_id_fk ON office_hour_slot(course_id);
CREATE INDEX idx_office_hour_slot_section_id_fk ON office_hour_slot(section_id);
CREATE INDEX idx_office_hour_slot_tenant_id_fk ON office_hour_slot(tenant_id);
CREATE INDEX idx_student_attendance_excused_by_id_fk ON student_attendance(excused_by_id);
CREATE INDEX idx_student_attendance_marked_by_id_fk ON student_attendance(marked_by_id);
CREATE INDEX idx_conference_participant_conference_id_fk ON conference_participant(conference_id);
CREATE INDEX idx_conference_participant_tenant_id_fk ON conference_participant(tenant_id);
CREATE INDEX idx_conference_participant_user_id_fk ON conference_participant(user_id);
CREATE INDEX idx_planner_note_course_id_fk ON planner_note(course_id);
CREATE INDEX idx_planner_note_tenant_id_fk ON planner_note(tenant_id);
CREATE INDEX idx_planner_override_tenant_id_fk ON planner_override(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_academic_calendar_date_updated BEFORE UPDATE ON academic_calendar FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_course_calendar_date_updated BEFORE UPDATE ON course_calendar FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_calendar_event_date_updated BEFORE UPDATE ON calendar_event FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_attendance_session_date_updated BEFORE UPDATE ON attendance_session FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_student_attendance_date_updated BEFORE UPDATE ON student_attendance FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_attendance_record_date_updated BEFORE UPDATE ON attendance_record FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_office_hour_slot_date_updated BEFORE UPDATE ON office_hour_slot FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_appointment_booking_date_updated BEFORE UPDATE ON appointment_booking FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_conference_session_date_updated BEFORE UPDATE ON conference_session FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_planner_note_date_updated BEFORE UPDATE ON planner_note FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_planner_override_date_updated BEFORE UPDATE ON planner_override FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE academic_calendar ENABLE ROW LEVEL SECURITY;
ALTER TABLE academic_calendar FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_academic_calendar_tenant ON academic_calendar FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_calendar ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_calendar FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_calendar_tenant ON course_calendar FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE calendar_event ENABLE ROW LEVEL SECURITY;
ALTER TABLE calendar_event FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_calendar_event_tenant ON calendar_event FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE event_participant ENABLE ROW LEVEL SECURITY;
ALTER TABLE event_participant FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_event_participant_tenant ON event_participant FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE event_reminder ENABLE ROW LEVEL SECURITY;
ALTER TABLE event_reminder FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_event_reminder_tenant ON event_reminder FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE attendance_session ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance_session FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_attendance_session_tenant ON attendance_session FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE student_attendance ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_attendance FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_attendance_tenant ON student_attendance FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE attendance_record ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance_record FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_attendance_record_tenant ON attendance_record FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE office_hour_slot ENABLE ROW LEVEL SECURITY;
ALTER TABLE office_hour_slot FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_office_hour_slot_tenant ON office_hour_slot FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE appointment_booking ENABLE ROW LEVEL SECURITY;
ALTER TABLE appointment_booking FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_appointment_booking_tenant ON appointment_booking FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE conference_session ENABLE ROW LEVEL SECURITY;
ALTER TABLE conference_session FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_conference_session_tenant ON conference_session FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE planner_note ENABLE ROW LEVEL SECURITY;
ALTER TABLE planner_note FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_planner_note_tenant ON planner_note FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE planner_override ENABLE ROW LEVEL SECURITY;
ALTER TABLE planner_override FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_planner_override_tenant ON planner_override FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE conference_participant ENABLE ROW LEVEL SECURITY;
ALTER TABLE conference_participant FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_conference_participant_tenant ON conference_participant FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
