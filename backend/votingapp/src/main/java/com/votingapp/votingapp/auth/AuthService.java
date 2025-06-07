package com.votingapp.votingapp.auth;

import com.votingapp.votingapp.auth.pojo.AuthenticationResponse;
import com.votingapp.votingapp.auth.pojo.RegistrationResponse;
import com.votingapp.votingapp.faceprocessing.FaceProcessingService;
import com.votingapp.votingapp.security.JwtProvider;
import com.votingapp.votingapp.user.User;
import com.votingapp.votingapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final FaceProcessingService faceProcessingService;
    private final JwtProvider jwtProvider;

    private final double COSINE_THRESHOLD = 0.6;
    private final int ACCESS_TOKEN_EXPIRE_MINUTES = 30;
    private final String FACEAPP_SERVICE_DOMAIN = "face-scan-service";
    private final int FACEAPP_SERVICE_PORT = 8000;

    @Value("${jwt.secret}")
    private String JWT_SECRET_KEY;

    public RegistrationResponse register(String username, String fullName, MultipartFile faceImage) {
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            return RegistrationResponse.builder()
                    .success(false)
                    .message("User with such username already exists")
                    .build();
        }

        try {
            // Send face image to Python service (faceapp) to extract embedding
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Prepare multipart request
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("face_image", new ByteArrayResource(faceImage.getBytes()) {
                @Override
                public String getFilename() {
                    return faceImage.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<List<Double>> response = restTemplate.postForEntity(
                    "http://" + FACEAPP_SERVICE_DOMAIN + ":" + FACEAPP_SERVICE_PORT + "/face_scan",
                    requestEntity,
                    (Class<List<Double>>) (Class<?>) List.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return RegistrationResponse.builder()
                        .success(false)
                        .message("Something went wrong! Reason: Failed to extract face embedding from Face App")
                        .build();
            }

            // Serialize face embedding to byte array
            byte[] embeddingBytes = faceProcessingService.serializeEmbedding(response.getBody());

            // Create and save new user
            User user = new User();
            user.setUsername(username);
            user.setFullName(fullName);
            user.setFaceEmbedding(embeddingBytes);
            userRepository.save(user);

            return RegistrationResponse.builder()
                    .success(true)
                    .message("User " + username + " registered successfully")
                    .build();

        } catch (IOException e) {
            return RegistrationResponse.builder()
                    .success(false)
                    .message("Something went wrong! Reason: Failed to process face image")
                    .build();
        } catch (RestClientException e) {
            return RegistrationResponse.builder()
                    .success(false)
                    .message("Something went wrong! Reason: Failed to communicate with Face App")
                    .build();
        }
    }

    public AuthenticationResponse faceLogin(String username, MultipartFile faceImage) {
        // Find user by username
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return AuthenticationResponse.builder()
                    .success(false)
                    .message("User with such username not found")
                    .build();
        }

        User user = optionalUser.get();

        try {
            // Send face image to Python service (faceapp) to extract embedding
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Prepare multipart request
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("face_image", new ByteArrayResource(faceImage.getBytes()) {
                @Override
                public String getFilename() {
                    return faceImage.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<List<Double>> response = restTemplate.postForEntity(
                    "http://" + FACEAPP_SERVICE_DOMAIN + ":" + FACEAPP_SERVICE_PORT + "/face_scan",
                    requestEntity,
                    (Class<List<Double>>) (Class<?>) List.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return AuthenticationResponse.builder()
                        .success(false)
                        .message("Something went wrong! Reason: Failed to extract face embedding from Face App")
                        .build();
            }

            // Convert provided embedding to double array
            double[] providedEmbedding = response.getBody().stream().mapToDouble(Double::doubleValue).toArray();
            // Get stored embedding
            double[] storedEmbedding = faceProcessingService.deserializeEmbedding(user.getFaceEmbedding());

            // Calculate cosine similarity
            double similarity = faceProcessingService.calculateCosineSimilarity(providedEmbedding, storedEmbedding);

            System.out.println("similarity = " + similarity);

            if (similarity <= COSINE_THRESHOLD) {
                return AuthenticationResponse.builder()
                        .success(false)
                        .message("Face does not match")
                        .build();
            }

            String token = jwtProvider.createToken(user.getUsername());
            return AuthenticationResponse.builder()
                    .success(true)
                    .message(token)
                    .build();
        } catch (IOException e) {
            return AuthenticationResponse.builder()
                    .success(false)
                    .message("Something went wrong! Reason: Failed to process face image")
                    .build();
        } catch (RestClientException e) {
            return AuthenticationResponse.builder()
                    .success(false)
                    .message("Something went wrong! Reason: Failed to communicate with Face App")
                    .build();
        }
    }
}