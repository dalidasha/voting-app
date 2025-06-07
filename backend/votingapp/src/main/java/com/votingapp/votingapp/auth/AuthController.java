package com.votingapp.votingapp.auth;

import com.votingapp.votingapp.auth.pojo.AuthenticationResponse;
import com.votingapp.votingapp.auth.pojo.RegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public RegistrationResponse register(
            @RequestParam("username") String username,
            @RequestParam("fullName") String fullName,
            @RequestParam("faceImage") MultipartFile faceImage
    ) {
        return authService.register(username, fullName, faceImage);
    }

    @PostMapping("/face_login")
    public AuthenticationResponse faceLogin(@RequestParam("username") String username,
                                            @RequestParam("faceImage") MultipartFile faceImage) {
        return authService.faceLogin(username, faceImage);
    }
}