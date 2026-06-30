-- Activity records (connections, patches, events)
INSERT INTO activity_records (user_id, activity_type, academic_program, created_at) VALUES
('user-001', 'CONNECTION', 'ING-COMP', '2026-01-15 10:00:00'),
('user-001', 'PATCH', 'ING-COMP', '2026-02-10 14:30:00'),
('user-002', 'CONNECTION', 'ING-COMP', '2026-01-20 09:00:00'),
('user-003', 'CONNECTION', 'MED', '2026-03-05 11:00:00'),
('user-003', 'PATCH', 'MED', '2026-03-06 12:00:00'),
('user-004', 'CONNECTION', 'MED', '2026-02-15 08:00:00'),
('user-005', 'EVENT', 'DER', '2026-04-01 16:00:00'),
('user-006', 'CONNECTION', 'ING-COMP', '2026-05-10 10:00:00'),
('user-006', 'PATCH', 'ING-COMP', '2026-05-12 15:00:00'),
('user-007', 'CONNECTION', 'ADM', '2026-06-01 09:30:00');

-- Mentorships
INSERT INTO mentorships (mentor_id, mentee_id, program_code, program_name, started_at) VALUES
('user-001', 'user-004', 'ING-COMP', 'Ingeniería en Computación', '2026-02-01'),
('user-001', 'user-006', 'ING-COMP', 'Ingeniería en Computación', '2026-03-15'),
('user-005', 'user-007', 'DER', 'Derecho', '2026-04-01'),
('user-003', 'user-004', 'MED', 'Medicina', '2026-05-01');

-- Welfare checkins
INSERT INTO welfare_checkins (user_id, checkin_date, intervention_type, academic_program) VALUES
('user-001', '2026-06-01', 'COUNSELING', 'ING-COMP'),
('user-002', '2026-06-01', 'WORKSHOP', 'ING-COMP'),
('user-003', '2026-06-02', 'COUNSELING', 'MED'),
('user-001', '2026-06-08', 'WORKSHOP', 'ING-COMP'),
('user-004', '2026-06-08', NULL, 'MED'),
('user-005', '2026-06-15', 'COUNSELING', 'DER'),
('user-006', '2026-06-15', NULL, 'ING-COMP'),
('user-007', '2026-06-22', 'WORKSHOP', 'ADM');
