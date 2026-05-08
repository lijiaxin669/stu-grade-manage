package com.example.grade.service;

import com.example.grade.common.ErrorCode;
import com.example.grade.common.PageResult;
import com.example.grade.domain.Role;
import com.example.grade.domain.User;
import com.example.grade.dto.user.UserCreateDto;
import com.example.grade.dto.user.UserDto;
import com.example.grade.dto.user.UserQueryDto;
import com.example.grade.dto.user.UserUpdateDto;
import com.example.grade.exception.BusinessException;
import com.example.grade.repository.RoleRepository;
import com.example.grade.repository.UserRepository;
import com.example.grade.repository.CourseRepository;
import com.example.grade.repository.EnrollmentRepository;
import com.example.grade.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public PageResult<UserDto> listUsers(UserQueryDto query) {
        Pageable pageable = PageRequest.of(query.getPageNum() - 1, query.getPageSize(), Sort.by("id").descending());
        
        Page<User> page = userRepository.findAll((root, cq, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            
            if (query.getRoleCode() != null && !query.getRoleCode().isEmpty()) {
                // Join roles
                predicates.add(cb.equal(root.join("roles").get("code"), query.getRoleCode()));
            }
            
            if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                String likePattern = "%" + query.getKeyword() + "%";
                predicates.add(cb.or(
                    cb.like(root.get("username"), likePattern),
                    cb.like(root.get("realName"), likePattern)
                ));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
        
        List<UserDto> list = page.getContent().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
            
        return new PageResult<>(page.getTotalElements(), list, query.getPageNum(), query.getPageSize());
    }

    @Transactional
    public void createUser(CustomUserDetails currentUser, UserCreateDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException(ErrorCode.USER_EXIST);
        }

        // Only allow creating TEACHER/STUDENT via this API.
        if (!"ROLE_TEACHER".equals(dto.getRoleCode()) && !"ROLE_STUDENT".equals(dto.getRoleCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "仅支持创建教师/学生账号");
        }
        
        Role role = roleRepository.findByCode(dto.getRoleCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "Invalid Role Code: " + dto.getRoleCode()));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        // Default password for new users
        user.setPasswordHash(passwordEncoder.encode("123456")); 
        user.setStatus(1); // Active
        user.setRoles(Set.of(role));
        
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long userId) {
        User user = getById(userId);
        user.setPasswordHash(passwordEncoder.encode("123456"));
        userRepository.save(user);
    }

    @Transactional
    public void toggleStatus(Long userId, Integer status) {
        if (status != 0 && status != 1) throw new BusinessException(ErrorCode.PARAM_ERROR);
        User user = getById(userId);
        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long userId, UserUpdateDto dto) {
        User user = getById(userId);

        java.util.Set<String> roleCodes = user.getRoles() == null ? java.util.Set.of()
            : user.getRoles().stream()
                .filter(r -> r != null && r.getCode() != null)
                .map(com.example.grade.domain.Role::getCode)
                .collect(java.util.stream.Collectors.toSet());

        if (roleCodes.contains("ROLE_SUPER_ADMIN") || roleCodes.contains("ROLE_ADMIN")) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "禁止修改超级管理员账号");
        }

        boolean isTeacher = roleCodes.contains("ROLE_TEACHER");
        boolean isStudent = roleCodes.contains("ROLE_STUDENT");
        if (!isTeacher && !isStudent) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "仅支持修改教师/学生账号");
        }

        user.setRealName(dto.getRealName());

        if (dto.getRoleCode() != null && !dto.getRoleCode().isBlank()) {
            if (!"ROLE_TEACHER".equals(dto.getRoleCode()) && !"ROLE_STUDENT".equals(dto.getRoleCode())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "仅支持设置角色为教师/学生");
            }

            if (!roleCodes.contains(dto.getRoleCode())) {
                // Safety checks when switching role
                if (isTeacher && "ROLE_STUDENT".equals(dto.getRoleCode())) {
                    if (courseRepository.existsByTeacherId(userId)) {
                        throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "该教师存在课程，禁止改为学生");
                    }
                }
                if (isStudent && "ROLE_TEACHER".equals(dto.getRoleCode())) {
                    if (enrollmentRepository.existsByStudentId(userId)) {
                        throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "该学生存在选课记录，禁止改为教师");
                    }
                }

                Role role = roleRepository.findByCode(dto.getRoleCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "Invalid Role Code: " + dto.getRoleCode()));
                user.setRoles(new java.util.HashSet<>(java.util.Set.of(role)));
            }
        }

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getById(userId);

        java.util.Set<String> roleCodes = user.getRoles() == null ? java.util.Set.of()
            : user.getRoles().stream()
                .filter(r -> r != null && r.getCode() != null)
                .map(com.example.grade.domain.Role::getCode)
                .collect(java.util.stream.Collectors.toSet());

        if (roleCodes.contains("ROLE_SUPER_ADMIN") || roleCodes.contains("ROLE_ADMIN")) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "禁止删除超级管理员账号");
        }

        boolean isTeacher = roleCodes.contains("ROLE_TEACHER");
        boolean isStudent = roleCodes.contains("ROLE_STUDENT");
        if (!isTeacher && !isStudent) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "仅支持删除教师/学生账号");
        }

        if (isTeacher && courseRepository.existsByTeacherId(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "该教师存在课程，禁止删除");
        }
        if (isStudent && enrollmentRepository.existsByStudentId(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "该学生存在选课记录，禁止删除");
        }

        // Avoid FK constraint issues on join table
        user.setRoles(new java.util.HashSet<>());
        userRepository.save(user);
        userRepository.delete(user);
    }

    @Transactional
    public void setRoles(Long userId, List<String> roleCodes) {
        User user = getById(userId);
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "roleCodes cannot be empty");
        }

        Set<Role> roles = roleCodes.stream()
            .map(code -> roleRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "Invalid Role Code: " + code)))
            .collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
    }

    private User getById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        if (!user.getRoles().isEmpty()) {
            dto.setRoleCode(user.getRoles().iterator().next().getCode()); 
        }
        return dto;
    }
}
