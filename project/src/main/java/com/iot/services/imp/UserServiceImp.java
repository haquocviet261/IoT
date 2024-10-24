package com.iot.services.imp;

import com.iot.common.utils.CommonUtils;
import com.iot.common.utils.EmailUtils;
import com.iot.common.utils.PasswordGenerator;
import com.iot.common.utils.Validation;
import com.iot.model.dto.request.ChangePasswordRequest;
import com.iot.model.dto.request.EditUserDTO;
import com.iot.model.dto.request.RegisterRequest;
import com.iot.model.dto.request.UserDTO;
import com.iot.model.dto.response.AuthenticationResponse;
import com.iot.model.dto.response.ResponseObject;
import com.iot.model.entity.Token;
import com.iot.model.entity.User;
import com.iot.repositories.TokenRepository;
import com.iot.repositories.UserRepository;
import com.iot.services.interfaces.UserService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtServiceImp jwtServiceImp;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtDecoder jwtDecoder;
    @Autowired
    HttpServletRequest request;
    @Autowired
    private UserRepository userrepository;
    @Autowired
    private EmailUtils emailUtil;

    @Override
    public ResponseEntity<ResponseObject> changePassword(ChangePasswordRequest request) {
        User user = CommonUtils.getUserInforLogin();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ResponseObject(Validation.FAIL, "The current password you entered is incorrect. Please try again.", null));
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ResponseObject(Validation.FAIL, "The new password and confirmation password do not match. Please try again.", null));
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userrepository.save(user);
        return ResponseEntity.ok(new ResponseObject(Validation.OK, "Change Password successfully !", null));
    }

    @Override
    public ResponseEntity<ResponseObject> getAllUsers() {
        List<UserDTO> userDTOS = userrepository.getAllUser();
        if (userDTOS.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(Validation.FAIL, "No users found", null));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject(Validation.OK, "Retrieved users successfully", userDTOS));
    }

    public ResponseEntity<ResponseObject> deleteUser(Long id) {
        Optional<User> user = userrepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject(Validation.FAIL, "userId" + id + " is not exist", ""));
        }
        user.get().setDeleted_at(new Date());
        user.get().setStatus("INACTIVE");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject(Validation.OK, "Deleted User" + id + " successfully!", userrepository.save(user.get())));
    }

    @Override
    public ResponseEntity<ResponseObject> editUser(EditUserDTO editUserDTO) {
        User user = CommonUtils.getUserInforLogin();
        BeanUtils.copyProperties(editUserDTO, user);
        user.setUpdated_by(user.getUser_name());
        return ResponseEntity.ok(new ResponseObject(Validation.OK, "Updated successfully!", userrepository.save(user)));
    }

    @Override
    public ResponseEntity<String> resetPassword(String email) throws MessagingException {
        Optional<User> optionalUser = userrepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email " + email + " not found !");
        }
        User user = optionalUser.get();
        String newPassword = PasswordGenerator.generateRandomPassword(8);
        emailUtil.sendEmailToResetPassword(email, user.getUser_name(), newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userrepository.save(user);
        return ResponseEntity.ok("Your password has been successfully reset! Please check your email for further instructions and your new password.");
    }

    @Override
    public ResponseEntity<String> register(String email) throws MessagingException {
        Optional<User> optionalUser = userrepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email " + email + " is exist !");
        }
        String token = jwtServiceImp.generateToken(email);
        emailUtil.confirmAccount(email, token);
        return ResponseEntity.ok("Please check email " + email);
    }

    @Override
    public ResponseEntity<String> verifyAccount(String token) throws MessagingException {
        if (jwtServiceImp.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token is expired !!!");
        }
        String email = jwtServiceImp.extractEmail(token);
        Optional<User> optionalUser = userrepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email " + email + " is exist !");
        }
        User newUser = new User();
        String username = email.split("@")[0];
        String password = PasswordGenerator.generateRandomPassword(8);
        newUser.setUser_name(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        userrepository.save(newUser);
        emailUtil.sendEmailWithInforNewAccount(email, username, password);
        return ResponseEntity.ok("Account created successfully! Please check your email for login details.");
    }

    @Override
    public ResponseEntity<ResponseObject> searchUsers(String keyword) {
        List<User> users = userRepository.searchUsers(keyword);
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(Validation.FAIL, "Not found", null));
        }
        return ResponseEntity.ok(new ResponseObject(Validation.OK, "Users found successfully !!!", users));
    }

    public ResponseEntity<String> setPassword(String email, String newPassword) {
        Optional<User> optionalUser = userrepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found!");
        }
        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userrepository.save(user);
        return ResponseEntity.ok("Set new Password successfully !");
    }

    public ResponseEntity<ResponseObject> getUserProfileById(Long userId) {
        Optional<User> optionalUser = userrepository.findById(userId);
        return optionalUser.map(user -> ResponseEntity.ok(new ResponseObject("OK", "User profile", user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject(Validation.FAIL, "userId" + userId + " is not exist", null)));
    }

    @Override
    public ResponseEntity<ResponseObject> findByUserId(Long user_id) {
        Optional<User> user = userrepository.findById(user_id);
        return user.map(value -> ResponseEntity.ok(new ResponseObject(Validation.OK, "Find user successfully", value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject(Validation.FAIL, "Cannot find user with username:" + user_id, "")));
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user_id(user.getId())
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    @Override
    public ResponseEntity<ResponseObject> addUser(RegisterRequest request) {
        List<User> list = userRepository.findAll();
        for (User value : list) {
            if (value.getUsername().equals(request.getUsername())) {
                return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseObject(Validation.FAIL, "User Name are exist !", ""));
            } else {
                if (value.getEmail().equals(request.getEmail())) {
                    return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseObject(Validation.FAIL, "Email are exist !", ""));
                } else if (request.getPhoneNumber().equals(value.getPhone_number())) {
                    return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseObject(Validation.FAIL, "Phone number are exist !", ""));
                }
            }
        }
        var user = User.builder().user_name(request.getUsername()).firstname(request.getFirstName())
                .lastname(request.getLastName()).email(request.getEmail()).address(request.getAddress())
                .phone_number(request.getPhoneNumber()).password(passwordEncoder.encode(request.getPassword())).role("USER").status("ACTIVE")
                .date_of_birth(request.getDob())
                .build();
        var savedUser = userRepository.save(user);
        var jwtToken = jwtServiceImp.generateToken(user);
        var refreshToken = jwtServiceImp.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject(Validation.OK, "Register successfully !", AuthenticationResponse.builder().accessToken(jwtToken).refresh_token(refreshToken).build()));
    }

    @Override
    public ResponseEntity<ResponseObject> authenticated(UserDTO request) {
        Optional<User> optionalUser = userRepository.findByUserName(request.getUser_name());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject(Validation.FAIL, "User Name are incorrect !", ""));
        } else if (!passwordEncoder.matches(request.getPassword(), optionalUser.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject(Validation.FAIL, "Password are incorrect !", ""));
        } else if (optionalUser.get().getStatus().equals("INACTIVE")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject(Validation.FAIL, "Inactive account !", ""));
        }
        User user = optionalUser.get();
        var jwt = jwtServiceImp.generateToken(user);
        var refreshToken = jwtServiceImp.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwt);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(String.valueOf(user.getId()), request.getPassword()));
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject(Validation.OK, "Login successfully !", AuthenticationResponse.builder().accessToken(jwt).refresh_token(refreshToken).build()));
    }

    @Override
    public ResponseEntity<ResponseObject> logout(HttpServletRequest request, HttpServletResponse response) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final Long userId;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseObject(Validation.FAIL, "Authorization header is missing!", ""));
        }
        refreshToken = authHeader.substring(7);
        userId = jwtServiceImp.extractUserId(refreshToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseObject(Validation.FAIL, "Token does not exist !", ""));
        }
        var user = this.userRepository.findById(userId)
                .orElseThrow();
        if (!jwtServiceImp.isTokenValid(refreshToken, user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseObject(Validation.FAIL, "Token invalid !", ""));
        }
        revokeAllUserTokens(user);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject(Validation.OK, "Logout Successfully !", ""));
    }

    @Override
    public ResponseEntity<ResponseObject> extractUser() {
        User user;
        String jwt;
        String refreshToken;
        String header = request.getHeader("Authorization");
        Map<String, Object> userInformation = extractUserInfoFromToken(header.substring(7));
        Optional<User> checkMailExist = userRepository.findByEmail(userInformation.get("email").toString().trim());
        if (checkMailExist.isPresent()) {
            user = checkMailExist.get();
            jwt = jwtServiceImp.generateToken(user);
            refreshToken = jwtServiceImp.generateRefreshToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwt);
            return ResponseEntity.ok(new ResponseObject("OK", "Handle authorization code and state successfully!", AuthenticationResponse.builder().accessToken(jwt).refresh_token(refreshToken).build()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject(Validation.FAIL, "Email does not exist!", ""));
    }

    public Map<String, Object> extractUserInfoFromToken(String accessToken) {
        Jwt jwt = jwtDecoder.decode(accessToken);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("picture", jwt.getClaim("picture"));
        userInfo.put("given_name", jwt.getClaim("given_name"));
        userInfo.put("family_name", jwt.getClaim("family_name"));
        userInfo.put("iat", jwt.getClaim("iat"));
        userInfo.put("exp", jwt.getClaim("exp"));
        return userInfo;
    }
}
