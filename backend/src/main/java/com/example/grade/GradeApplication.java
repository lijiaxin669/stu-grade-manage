package com.example.grade;

import com.example.grade.domain.Role;
import com.example.grade.domain.User;
import com.example.grade.domain.Permission;
import com.example.grade.repository.PermissionRepository;
import com.example.grade.repository.RoleRepository;
import com.example.grade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@EnableScheduling
@SpringBootApplication
public class GradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GradeApplication.class, args);
    }

    @Bean
    @Order(1)
    public CommandLineRunner initData(RoleRepository roleRepository,
                                      PermissionRepository permissionRepository,
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            Role superAdminRole = ensureRole(roleRepository, "ROLE_SUPER_ADMIN", "Super Admin");
            Role teacherRole = ensureRole(roleRepository, "ROLE_TEACHER", "Teacher");
            Role studentRole = ensureRole(roleRepository, "ROLE_STUDENT", "Student");

            java.util.List<Permission> allPerms = new java.util.ArrayList<>();

            allPerms.add(ensurePerm(permissionRepository, "MENU_ADMIN_OVERVIEW", "Admin Overview", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_ADMIN_USERS", "User Manage", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_ADMIN_ROLES", "Role Permission Manage", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_ADMIN_COURSES", "Course Manage", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_ADMIN_ROSTER", "Course Roster Manage", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_ADMIN_STATS", "Admin Grade Stats", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_ADMIN_EXPORT", "Admin Grade Export", "MENU"));
            Permission menuAdminAppeal = ensurePerm(permissionRepository, "MENU_ADMIN_APPEAL", "Admin Appeal Arbitration", "MENU");
            allPerms.add(menuAdminAppeal);

            allPerms.add(ensurePerm(permissionRepository, "MENU_TEACHER_OVERVIEW", "Teacher Overview", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_TEACHER_COURSES", "Teacher Courses", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_TEACHER_GRADES", "Teacher Grades", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_TEACHER_STATS", "Teacher Stats", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_TEACHER_EXPORT", "Teacher Export", "MENU"));
            Permission menuTeacherAppeal = ensurePerm(permissionRepository, "MENU_TEACHER_APPEAL", "Teacher Appeal Handle", "MENU");
            allPerms.add(menuTeacherAppeal);

            allPerms.add(ensurePerm(permissionRepository, "MENU_STUDENT_OVERVIEW", "Student Overview", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_STUDENT_COURSES", "Student Courses", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_STUDENT_GRADES", "Student Grades", "MENU"));
            allPerms.add(ensurePerm(permissionRepository, "MENU_STUDENT_PROFILE", "Student Profile", "MENU"));
            Permission menuStudentAppeal = ensurePerm(permissionRepository, "MENU_STUDENT_APPEAL", "Student Appeal", "MENU");
            allPerms.add(menuStudentAppeal);

            Permission apiUserList = ensurePerm(permissionRepository, "API_USER_LIST", "List Users", "API");
            Permission apiUserCreate = ensurePerm(permissionRepository, "API_USER_CREATE", "Create Users", "API");
            Permission apiUserReset = ensurePerm(permissionRepository, "API_USER_PASSWORD_RESET", "Reset User Password", "API");
            Permission apiUserStatus = ensurePerm(permissionRepository, "API_USER_STATUS", "Toggle User Status", "API");
            Permission apiUserDelete = ensurePerm(permissionRepository, "API_USER_DELETE", "Delete Users", "API");
            Permission apiUserUpdate = ensurePerm(permissionRepository, "API_USER_UPDATE", "Update Users", "API");
            allPerms.addAll(java.util.List.of(apiUserList, apiUserCreate, apiUserReset, apiUserStatus, apiUserDelete, apiUserUpdate));

            Permission apiCourseList = ensurePerm(permissionRepository, "API_COURSE_LIST", "List Courses", "API");
            Permission apiCourseCreate = ensurePerm(permissionRepository, "API_COURSE_CREATE", "Create Course", "API");
            Permission apiCourseUpdate = ensurePerm(permissionRepository, "API_COURSE_UPDATE", "Update Course", "API");
            Permission apiCourseDelete = ensurePerm(permissionRepository, "API_COURSE_DELETE", "Delete Course", "API");
            allPerms.addAll(java.util.List.of(apiCourseList, apiCourseCreate, apiCourseUpdate, apiCourseDelete));

            Permission apiEnrollList = ensurePerm(permissionRepository, "API_ENROLL_LIST", "List Course Roster", "API");
            Permission apiEnrollAdd = ensurePerm(permissionRepository, "API_ENROLL_ADD", "Add Student To Course", "API");
            Permission apiEnrollRemove = ensurePerm(permissionRepository, "API_ENROLL_REMOVE", "Remove Student From Course", "API");
            allPerms.addAll(java.util.List.of(apiEnrollList, apiEnrollAdd, apiEnrollRemove));

            Permission apiGradeList = ensurePerm(permissionRepository, "API_GRADE_LIST", "List Grades", "API");
            Permission apiGradeCreate = ensurePerm(permissionRepository, "API_GRADE_CREATE", "Create Grade", "API");
            Permission apiGradeUpdate = ensurePerm(permissionRepository, "API_GRADE_UPDATE", "Update Grade", "API");
            Permission apiGradeDelete = ensurePerm(permissionRepository, "API_GRADE_DELETE", "Delete Grade", "API");
            Permission apiGradeStats = ensurePerm(permissionRepository, "API_GRADE_STATS", "Grade Statistics", "API");
            Permission apiGradeExport = ensurePerm(permissionRepository, "API_GRADE_EXPORT", "Export Grades", "API");
            allPerms.addAll(java.util.List.of(apiGradeList, apiGradeCreate, apiGradeUpdate, apiGradeDelete, apiGradeStats, apiGradeExport));

            Permission apiAppealCreate = ensurePerm(permissionRepository, "API_APPEAL_CREATE", "Create Appeal", "API");
            Permission apiAppealListMine = ensurePerm(permissionRepository, "API_APPEAL_LIST_MINE", "List My Appeals", "API");
            Permission apiAppealListPending = ensurePerm(permissionRepository, "API_APPEAL_LIST_PENDING", "List Pending Appeals", "API");
            Permission apiAppealClaim = ensurePerm(permissionRepository, "API_APPEAL_CLAIM", "Claim Appeal", "API");
            Permission apiAppealDecide = ensurePerm(permissionRepository, "API_APPEAL_DECIDE", "Decide Appeal", "API");
            Permission apiAppealRequestArbitrate = ensurePerm(permissionRepository, "API_APPEAL_REQUEST_ARBITRATE", "Request Arbitration", "API");
            Permission apiAppealListArbitrating = ensurePerm(permissionRepository, "API_APPEAL_LIST_ARBITRATING", "List Arbitrating Appeals", "API");
            Permission apiAppealArbitrate = ensurePerm(permissionRepository, "API_APPEAL_ARBITRATE", "Arbitrate Appeal", "API");
            Permission apiAppealDetail = ensurePerm(permissionRepository, "API_APPEAL_DETAIL", "View Appeal Detail", "API");
            Permission apiAppealAuditLogs = ensurePerm(permissionRepository, "API_APPEAL_AUDIT_LOGS", "View Appeal Audit Logs", "API");
            allPerms.addAll(java.util.List.of(
                    apiAppealCreate, apiAppealListMine, apiAppealListPending,
                    apiAppealClaim, apiAppealDecide, apiAppealRequestArbitrate,
                    apiAppealListArbitrating, apiAppealArbitrate,
                    apiAppealDetail, apiAppealAuditLogs
            ));

            superAdminRole.getPermissions().addAll(allPerms);

            Role legacyAdminRole = roleRepository.findByCode("ROLE_ADMIN").orElse(null);
            if (legacyAdminRole != null) {
                legacyAdminRole.getPermissions().addAll(allPerms);
            }

            if (teacherRole.getPermissions().isEmpty()) teacherRole.setPermissions(new java.util.HashSet<>(java.util.List.of(
                    permissionRepository.findByCode("MENU_TEACHER_OVERVIEW").orElseThrow(),
                    permissionRepository.findByCode("MENU_TEACHER_COURSES").orElseThrow(),
                    permissionRepository.findByCode("MENU_TEACHER_GRADES").orElseThrow(),
                    permissionRepository.findByCode("MENU_TEACHER_STATS").orElseThrow(),
                    permissionRepository.findByCode("MENU_TEACHER_EXPORT").orElseThrow(),
                    menuTeacherAppeal,
                    apiCourseList, apiCourseCreate, apiCourseUpdate, apiCourseDelete,
                    apiGradeList, apiGradeCreate, apiGradeUpdate, apiGradeDelete, apiGradeStats, apiGradeExport,
                    apiEnrollList,
                    apiAppealListPending, apiAppealClaim, apiAppealDecide, apiAppealDetail, apiAppealAuditLogs
            )));

            if (studentRole.getPermissions().isEmpty()) studentRole.setPermissions(new java.util.HashSet<>(java.util.List.of(
                    permissionRepository.findByCode("MENU_STUDENT_OVERVIEW").orElseThrow(),
                    permissionRepository.findByCode("MENU_STUDENT_COURSES").orElseThrow(),
                    permissionRepository.findByCode("MENU_STUDENT_GRADES").orElseThrow(),
                    permissionRepository.findByCode("MENU_STUDENT_PROFILE").orElseThrow(),
                    menuStudentAppeal,
                    apiCourseList,
                    apiGradeList,
                    apiAppealCreate, apiAppealListMine, apiAppealRequestArbitrate, apiAppealDetail, apiAppealAuditLogs
            )));

            java.util.List<Role> rolesToSave = new java.util.ArrayList<>();
            rolesToSave.add(superAdminRole);
            rolesToSave.add(teacherRole);
            rolesToSave.add(studentRole);
            if (legacyAdminRole != null) rolesToSave.add(legacyAdminRole);
            roleRepository.saveAll(rolesToSave);

            String pwd = passwordEncoder.encode("123456");
            ensureUserMerge(userRepository, "superadmin", "Super Admin", pwd, Set.of(superAdminRole));
            ensureUserMerge(userRepository, "admin", "Admin", pwd, Set.of(superAdminRole));
            ensureUser(userRepository, "teacher", "Teacher", pwd, Set.of(teacherRole));
            ensureUser(userRepository, "student", "Student", pwd, Set.of(studentRole));
        };
    }

    private static Role ensureRole(RoleRepository roleRepository, String code, String name) {
        return roleRepository.findByCode(code).orElseGet(() -> {
            Role r = new Role();
            r.setCode(code);
            r.setName(name);
            return roleRepository.save(r);
        });
    }

    private static Permission ensurePerm(PermissionRepository permissionRepository, String code, String name, String type) {
        return permissionRepository.findByCode(code).orElseGet(() -> {
            Permission p = new Permission();
            p.setCode(code);
            p.setName(name);
            p.setType(type);
            return permissionRepository.save(p);
        });
    }

    private static void ensureUser(UserRepository userRepository, String username, String realName, String pwdHash, Set<Role> roles) {
        if (userRepository.findByUsername(username).isPresent()) return;
        User user = new User();
        user.setUsername(username);
        user.setRealName(realName);
        user.setPasswordHash(pwdHash);
        user.setStatus(1);
        user.setRoles(roles);
        userRepository.save(user);
    }

    private static void ensureUserMerge(UserRepository userRepository, String username, String realName, String pwdHash, Set<Role> roles) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setRealName(realName);
            user.setPasswordHash(pwdHash);
            user.setStatus(1);
            user.setRoles(new java.util.HashSet<>(roles));
            userRepository.save(user);
            return;
        }

        java.util.Set<Role> currentRoles = user.getRoles();
        if (currentRoles == null) {
            currentRoles = new java.util.HashSet<>();
            user.setRoles(currentRoles);
        }

        boolean changed = false;
        for (Role roleToAdd : roles) {
            boolean exists = currentRoles.stream().anyMatch(r -> r != null && r.getCode() != null && r.getCode().equals(roleToAdd.getCode()));
            if (!exists) {
                currentRoles.add(roleToAdd);
                changed = true;
            }
        }

        if (changed) {
            userRepository.save(user);
        }
    }
}
