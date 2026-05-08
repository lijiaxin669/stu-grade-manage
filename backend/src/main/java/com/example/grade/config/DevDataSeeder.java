package com.example.grade.config;

import com.example.grade.domain.Course;
import com.example.grade.domain.Enrollment;
import com.example.grade.domain.Grade;
import com.example.grade.domain.Role;
import com.example.grade.domain.User;
import com.example.grade.repository.CourseRepository;
import com.example.grade.repository.EnrollmentRepository;
import com.example.grade.repository.GradeRepository;
import com.example.grade.repository.RoleRepository;
import com.example.grade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role teacherRole = roleRepository.findByCode("ROLE_TEACHER").orElseThrow();
        Role studentRole = roleRepository.findByCode("ROLE_STUDENT").orElseThrow();

        String pwd = passwordEncoder.encode("123456");

        User teacherA = ensureUser("t001", "张老师", pwd, Set.of(teacherRole));
        User teacherB = ensureUser("t002", "李老师", pwd, Set.of(teacherRole));

        List<User> students = List.of(
            ensureUser("s001", "学生01", pwd, Set.of(studentRole)),
            ensureUser("s002", "学生02", pwd, Set.of(studentRole)),
            ensureUser("s003", "学生03", pwd, Set.of(studentRole)),
	            ensureUser("s004", "学生04", pwd, Set.of(studentRole)),
	            ensureUser("s005", "学生05", pwd, Set.of(studentRole)),
	            ensureUser("s006", "学生06", pwd, Set.of(studentRole)),
	            ensureUser("s007", "学生07", pwd, Set.of(studentRole)),
	            ensureUser("s008", "学生08", pwd, Set.of(studentRole))
	        );

	        // Keep the default seeded accounts ("teacher"/"student") usable for demo linkage as well
	        User defaultTeacher = ensureUser("teacher", "Teacher", pwd, Set.of(teacherRole));
	        User defaultStudent = ensureUser("student", "Student", pwd, Set.of(studentRole));

	        Course math = ensureCourse("高等数学", "2026-Spring", 4, "基础数学课程", teacherA);
	        Course ds = ensureCourse("数据结构", "2026-Spring", 3, "线性表/树/图", teacherA);
	        Course db = ensureCourse("数据库系统", "2026-Spring", 3, "SQL/事务/索引", teacherB);
	        Course os = ensureCourse("操作系统", "2026-Spring", 3, "进程/线程/内存/文件系统", teacherB);
	        Course se = ensureCourse("软件工程", "2026-Spring", 3, "需求/设计/开发/测试/交付", defaultTeacher);

        // Roster (admin-maintained): create enrollments, but only some have grades
        enroll(math, students.get(0));
        enroll(math, students.get(1));
        enroll(math, students.get(2));
        enroll(math, students.get(3));

        enroll(ds, students.get(0));
        enroll(ds, students.get(2));
        enroll(ds, students.get(4));

        enroll(db, students.get(1));
        enroll(db, students.get(3));
        enroll(db, students.get(5));
        enroll(db, students.get(6));

	        enroll(os, students.get(2));
	        enroll(os, students.get(4));
	        enroll(os, students.get(7));

	        // Default student linkage (so logging in as "student" can see real data)
	        enroll(math, defaultStudent);
	        enroll(se, defaultStudent);

	        // Grades: strict "only recorded enrollments are counted", so seed partial grades
	        seedGrade(math, students.get(0), BigDecimal.valueOf(92.5));
	        seedGrade(math, students.get(1), BigDecimal.valueOf(78.0));
	        seedGrade(math, students.get(2), BigDecimal.valueOf(59.0));
	        seedGrade(math, defaultStudent, BigDecimal.valueOf(74.0));

        seedGrade(ds, students.get(0), BigDecimal.valueOf(88.0));
        seedGrade(ds, students.get(2), BigDecimal.valueOf(76.5));

	        seedGrade(db, students.get(1), BigDecimal.valueOf(90.0));
	        seedGrade(db, students.get(3), BigDecimal.valueOf(66.0));
	        seedGrade(se, defaultStudent, BigDecimal.valueOf(85.0));
	    }

    private User ensureUser(String username, String realName, String pwdHash, Set<Role> roles) {
        return userRepository.findByUsername(username).map(existing -> {
            Set<Role> merged = new HashSet<>();
            if (existing.getRoles() != null) merged.addAll(existing.getRoles());
            merged.addAll(roles);
            if (existing.getRoles() == null || merged.size() != existing.getRoles().size()) {
                existing.setRoles(merged);
                return userRepository.save(existing);
            }
            return existing;
        }).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setRealName(realName);
            u.setPasswordHash(pwdHash);
            u.setStatus(1);
            u.setRoles(roles);
            return userRepository.save(u);
        });
    }

    private Course ensureCourse(String name, String semester, int credits, String description, User teacher) {
        Course existing = courseRepository.findByNameAndSemester(name, semester).orElse(null);
        if (existing != null) return existing;

        Course c = new Course();
        c.setName(name);
        c.setSemester(semester);
        c.setCredits(credits);
        c.setDescription(description);
        c.setTeacher(teacher);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(c);
    }

    private void enroll(Course course, User student) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) return;
        Enrollment e = new Enrollment();
        e.setStudent(student);
        e.setCourse(course);
        enrollmentRepository.save(e);
    }

    private void seedGrade(Course course, User student, BigDecimal score) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId()).orElseThrow();
        if (gradeRepository.existsByEnrollmentId(enrollment.getId())) return;

        Grade g = new Grade();
        g.setEnrollment(enrollment);
        g.setScore(score);
        g.setType("FINAL");
        g.setCreatedAt(LocalDateTime.now());
        g.setUpdatedAt(LocalDateTime.now());
        gradeRepository.save(g);
    }
}
