package okky.team.chawchaw.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okky.team.chawchaw.config.auth.PrincipalDetails;
import okky.team.chawchaw.user.dto.*;
import okky.team.chawchaw.utils.dto.DefaultResponseVo;
import okky.team.chawchaw.utils.message.ResponseFileMessage;
import okky.team.chawchaw.utils.message.ResponseUserMessage;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Environment env;

    @PostMapping("users/signup")
    public ResponseEntity createUser(@RequestBody CreateUserDto createUserDto,
                                     HttpServletResponse response){


        String provider = createUserDto.getProvider();

        if (provider.equals("kakao") || provider.equals("facebook")) {
            createUserDto.setPassword(UUID.randomUUID().toString());
        }

        Long userId = userService.createUser(createUserDto);

        if (provider.equals("kakao") || provider.equals("facebook")) {
            userService.uploadImage(createUserDto.getImageUrl(), userId);
        }

        return new ResponseEntity(DefaultResponseVo.res(ResponseUserMessage.CREATED_SUCCESS, true), HttpStatus.CREATED);
    }

    @GetMapping("users/email/duplicate/{email}")
    public ResponseEntity<Boolean> duplicateEmail(@PathVariable String email){
        Boolean result = userService.duplicateEmail(email);

        if (result)
            return new ResponseEntity(DefaultResponseVo.res(ResponseUserMessage.EMAIL_EXIST, true), HttpStatus.OK);
        else
            return new ResponseEntity(DefaultResponseVo.res(ResponseUserMessage.EMAIL_NOT_EXIST, false), HttpStatus.OK);

    }

    @GetMapping("users")
    public ResponseEntity<List<UserCardDto>> getUserCards(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                          @ModelAttribute FindUserVo findUserVo,
                                                          HttpServletRequest request) {

        findUserVo.getExclude().add(principalDetails.getId());

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("exclude")) {
                for (String s : cookie.getValue().split("/")) {
                    findUserVo.getExclude().add(Long.parseLong(s));
                }
            }
        }

        findUserVo.setSchool(principalDetails.getSchool());

        try {

            List<UserCardDto> result = userService.findUserCards(findUserVo);

            if (result.isEmpty())
                return new ResponseEntity(DefaultResponseVo.res(ResponseUserMessage.FIND_NOT_EXIST, false), HttpStatus.OK);
            else
                return new ResponseEntity(DefaultResponseVo.res(ResponseUserMessage.FIND_SUCCESS, true, result), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity(DefaultResponseVo.res(ResponseUserMessage.FIND_FAIL, false), HttpStatus.OK);
        }


    }

    @GetMapping("users/{userId}")
    public ResponseEntity<UserDetailsDto> getUserDetails(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                         @PathVariable Long userId) {
        UserDetailsDto result = userService.findUserDetails(userId, principalDetails.getId());
        if (result != null)
            return new ResponseEntity(DefaultResponseVo.res(
                    ResponseUserMessage.FIND_SUCCESS,
                    true,
                    result
            ), HttpStatus.OK);
        else
            return new ResponseEntity(DefaultResponseVo.res(ResponseUserMessage.FIND_FAIL, false), HttpStatus.OK);
    }

    @DeleteMapping("users")
    public ResponseEntity deleteUser(@AuthenticationPrincipal PrincipalDetails principalDetails){
        userService.deleteUser(principalDetails.getUsername());

        return new ResponseEntity(DefaultResponseVo.res(ResponseUserMessage.DELETE_SUCCESS, true), HttpStatus.OK);
    }

    @PostMapping("users/profile")
    public ResponseEntity updateUserProfile(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                            @RequestBody UpdateUserDto updateUserDto) {
        updateUserDto.setId(principalDetails.getId());

        Boolean result = userService.updateProfile(updateUserDto);

        if (result)
            return new ResponseEntity(DefaultResponseVo.res(ResponseUserMessage.UPDATE_SUCCESS, true), HttpStatus.OK);
        else
            return new ResponseEntity(DefaultResponseVo.res(ResponseUserMessage.UPDATE_FAIL, false), HttpStatus.OK);

    }

    @GetMapping("/users/image")
    public ResponseEntity findUserImage(@RequestParam String imageUrl) {
        byte[] result = null;
        HttpHeaders header = new HttpHeaders();

        try {
            File file = new File(env.getProperty("user.profile.image.path") + File.separator + imageUrl);
            header.add("Content-Type", Files.probeContentType(file.toPath()));
            result = FileCopyUtils.copyToByteArray(file);

        } catch (Exception e) {
            return new ResponseEntity(DefaultResponseVo.res(ResponseFileMessage.FIND_FAIL, false), HttpStatus.OK);
        }
        return new ResponseEntity(result, header, HttpStatus.OK);
    }

    @PostMapping("/users/image")
    public ResponseEntity uploadUserImage(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                          @RequestParam MultipartFile file) {
            String result = userService.uploadImage(file, principalDetails.getId());
            if (!result.isEmpty())
                return new ResponseEntity(DefaultResponseVo.res(ResponseFileMessage.UPLOAD_SUCCESS, true, result), HttpStatus.OK);
            else
                return new ResponseEntity(DefaultResponseVo.res(ResponseFileMessage.UPLOAD_FAIL, false), HttpStatus.OK);
    }

    @DeleteMapping("/users/image")
    public ResponseEntity deleteUserImage(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        String result = userService.deleteImage(principalDetails.getImageUrl(), principalDetails.getId());
        if (!result.isEmpty())
            return new ResponseEntity(DefaultResponseVo.res(ResponseFileMessage.DELETE_SUCCESS, true, result), HttpStatus.OK);
        else
            return new ResponseEntity(DefaultResponseVo.res(ResponseFileMessage.DELETE_FAIL, false), HttpStatus.OK);
    }

}
