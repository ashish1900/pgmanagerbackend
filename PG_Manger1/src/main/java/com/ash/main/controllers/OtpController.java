  package com.ash.main.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.ash.main.dto.CheckUser;
import com.ash.main.dto.GuestRequestDto;
import com.ash.main.dto.IdImageResponseDto;
import com.ash.main.dto.LeaveRquestDto;
import com.ash.main.dto.AllGuestsDto;
import com.ash.main.dto.RoomAssignmentRequest;
import com.ash.main.dto.SendOtpRequest;
import com.ash.main.dto.UserLoginDetails;
import com.ash.main.dto.VerifyAndRegister;
import com.ash.main.dto.VerifyAndRegisterG;
import com.ash.main.dto.VerifyOtpRequest;
import com.ash.main.entity.CityEntity;
import com.ash.main.entity.GuestDetailEntity;
import com.ash.main.entity.LeaveRequestEntity;
import com.ash.main.entity.NoticeEntity;
import com.ash.main.entity.PaymentTransaction;
import com.ash.main.entity.PaymentUpi;
import com.ash.main.entity.RequestStatus;
import com.ash.main.entity.RoomAssignment;
import com.ash.main.entity.StayRequestEntity;
import com.ash.main.entity.UserDetailsEntity;
import com.ash.main.rpository.CityRepository;
import com.ash.main.rpository.LeaveRequestRepository;
import com.ash.main.rpository.NoticeRepository;
import com.ash.main.rpository.PaymentTransactionRepository;
import com.ash.main.rpository.PaymentUpiRepository;
import com.ash.main.rpository.RepositoryG;
import com.ash.main.rpository.RepositoryO;
import com.ash.main.rpository.RoomAssignmentRepo;
import com.ash.main.rpository.StayRequestRepository;
import com.ash.main.security.JwtUtil;
import com.ash.main.service.CloudinaryService;
import com.ash.main.service.LeaveRequestService;
import com.ash.main.service.OtpService;
import com.ash.main.service.PaymentUpiService;
import com.ash.main.service.RoomAssignmentService;
import com.ash.main.service.ServiceO;
import com.ash.main.service.StayRequestService;
import com.cloudinary.Cloudinary;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/otp")
public class OtpController {

	@Autowired
	private RepositoryO userRepositoryO;
	
	@Autowired
	private RepositoryG userRepositoryG;
	
	@Autowired
	private CityRepository cityRepository;
	
	@Autowired
	private PaymentUpiRepository upiRepository;

	@Autowired
	private ServiceO serviceO;
	
	@Autowired
	private StayRequestService stayRequestService;
	
	@Autowired
	private LeaveRequestService leaveRequestService;
	
	@Autowired
	private CloudinaryService cloudinaryService;
	
	@Autowired
    private Cloudinary cloudinary;
	
	@Autowired
	private StayRequestRepository stayRequestRepo;
	
	
	@Autowired
	private LeaveRequestRepository leaveRequestRepo;
	
	@Autowired
	private RoomAssignmentRepo roomAssignmentRepo;

	
	@Autowired
    private JwtUtil jwtUtil;
	
	
    @Autowired
    private RoomAssignmentService roomAssignmentService;
    
    
    @Autowired
    private PaymentUpiService upiService;
    
    @Autowired
    private PaymentTransactionRepository paymentRepo;
    
    @Autowired
    private  NoticeRepository noticeRepo;

 //   private static final String RECEIPT_DIR = "C:/Users/Ashish/Desktop/pgImage/paymentReceipt/";
	

//	public static String uploadDirectory = "C:\\Users\\Ashish\\Desktop\\pgImage";
	
//	private final String uploadDir = "C:/Users/Ashish/Desktop/pgImage"; // your upload directory

    @Value("${app.upload.dir}")
    private String uploadDir;


	private final OtpService otpService;
	

	public OtpController(OtpService otpService) {

		this.otpService = otpService;
	}

	
	
	@GetMapping("/health")
    public String health() {
        return "OK";
    }
	
	
	@GetMapping("/stay-request/id-image")
	public ResponseEntity<?> getIdImage(
	        @RequestParam Long requestId,
	        @RequestParam String side,
	        Authentication authentication
	) {

	    if (authentication == null) {
	        return ResponseEntity.status(401).build();
	    }

	    String loggedMobile = authentication.getName();

	    StayRequestEntity request =
	            stayRequestRepo.findById(requestId)
	                    .orElseThrow(() -> new RuntimeException("Request not found"));

	    boolean isGuest =
	            request.getGuest().getMoNumber().equals(loggedMobile);

	    boolean isOwner =
	            request.getOwner().getMoNumber().equals(loggedMobile);

	    if (!isGuest && !isOwner) {
	        return ResponseEntity.status(403)
	                .body(Map.of("message", "Not allowed"));
	    }

	    String publicId =
	            "front".equalsIgnoreCase(side)
	                    ? request.getIdFront()
	                    : request.getIdBack();

	    String signedUrl =
	            cloudinaryService.generateAuthenticatedUrl(publicId);

	    return ResponseEntity.ok(Map.of("url", signedUrl));
	}

	
	
	
	
	@PostMapping("/upload-temp-image")
	public ResponseEntity<?> uploadTempImage(
	        @RequestParam("file") MultipartFile file) {

	    //  unique name ONLY
	    String publicId = "temp_" + UUID.randomUUID();

	    //  folder separated
	    String folder = "pg-manager/temp";

	    // upload → returns ONLY public_id
	    String imagePublicId =
	            cloudinaryService.uploadImage(file, folder, publicId);

	    //  auto delete after 5 minutes (OTP not verified case)
	    Executors.newSingleThreadScheduledExecutor().schedule(() -> {
	        try {
	            //  delete needs folder + publicId
	            cloudinaryService.deleteImage(
	                    folder + "/" + imagePublicId
	            );
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }, 5, TimeUnit.MINUTES);

	    
	    return ResponseEntity.ok(
	            Map.of("imageName", imagePublicId)
	    );
	}

	
	
	
	@PostMapping("/sendOtp")
	public ResponseEntity<String> sendOtp(@Valid @RequestBody SendOtpRequest request) {

		otpService.sendOtpToPhone(request.getMobileNumber());

		return ResponseEntity.ok("OTP sent to " + request.getMobileNumber());
	}

	
	
	@PostMapping("/verifyOtp")
	public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
		boolean isValid = otpService.validateOtp(request.getMobileNumber(), request.getOtp());
		if (isValid) {
			return ResponseEntity.ok("OTP verified successfully");
		} else {
			return new ResponseEntity<>("Invalid or expired OTP", HttpStatus.UNAUTHORIZED);
		}
	}

	
	
	@PostMapping("/check-and-send-otp")
	public ResponseEntity<String> checkMobileAndSendOtp(@RequestBody CheckUser checkUser) {

		String mobile = checkUser.getMobileNummber();
		if (mobile.startsWith("+91")) {
			mobile = mobile.substring(3);
		}

		String userType = checkUser.getUserType(); 

		boolean exists = false;
		if ("OWNER".equalsIgnoreCase(userType)) {
			exists = userRepositoryO.existsByMoNumber(mobile);
		} else if ("GUEST".equalsIgnoreCase(userType)) {
			exists = userRepositoryG.existsByMoNumber(mobile);
		}

		if (exists) {
			String sent = otpService.sendOtpToPhone(checkUser.getMobileNummber());
			return sent != null ? ResponseEntity.ok("EXISTS_AND_OTP_SENT")
					: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OTP_SEND_FAILED");
		} else {
			return ResponseEntity.ok("NEW_USER");
		}
	}
	
	
	
	
	@PostMapping("/verify-and-register")
	public ResponseEntity<?> verifyAndRegister(
	        @ModelAttribute VerifyAndRegister request,
	        @RequestParam("imageName") String tempPublicId) {

	    boolean isOtpValid = otpService.validateOtp(
	            request.getMoNumber(),
	            request.getOtp()
	    );

	    //  OTP invalid → temp image delete
	    if (!isOtpValid) {
	        cloudinaryService.deleteImage(tempPublicId);
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of(
	                        "status", "error",
	                        "message", "Invalid OTP"
	                ));
	    }

	    //  OTP valid → ONLY unique name (NO folder)
	    String profileUniqueName =
	            "profile_" + UUID.randomUUID();

	    //  temp → profile (Cloudinary handles folder)
	    String finalPublicId = cloudinaryService.uploadImageFromUrl(
	            cloudinaryService.generateUrl(tempPublicId),
	            "pg-manager/profile",
	            profileUniqueName
	    );

	    //  delete temp image
	    cloudinaryService.deleteImage(tempPublicId);

	    
	    request.setProfileImage(finalPublicId);

	    UserDetailsEntity userData =
	            serviceO.createUser(request);

	    String token =
	            jwtUtil.generateToken(userData.getMoNumber());

	    return ResponseEntity.ok(
	            Map.of(
	                    "status", "registered",
	                    "token", token,
	                    "user", userData
	            )
	    );
	}

	
	
	
	@PostMapping("/verify-and-registerG")
	public ResponseEntity<?> verifyAndRegisterG(
	        @ModelAttribute VerifyAndRegisterG request,
	        @RequestParam("imageName") String tempPublicId) {

	    boolean isOtpValid = otpService.validateOtp(
	            request.getMoNumber(),
	            request.getOtp()
	    );

	    // OTP invalid → delete temp image
	    if (!isOtpValid) {
	        cloudinaryService.deleteImage(tempPublicId);
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of(
	                        "status", "error",
	                        "message", "Invalid OTP"
	                ));
	    }

	    //  Guest final image UNIQUE NAME (NO folder here)
	    String guestUniqueName =
	            "guest_" + UUID.randomUUID();

	    //  temp → guest-profile
	    String finalPublicId =
	            cloudinaryService.uploadImageFromUrl(
	                    cloudinaryService.generateUrl(tempPublicId),
	                    "pg-manager/guest-profile",   // 👈 separate folder
	                    guestUniqueName
	            );

	    //  delete temp image
	    cloudinaryService.deleteImage(tempPublicId);

	   
	    request.setProfileImage(finalPublicId);

	    GuestDetailEntity guestData;
	    try {
	        guestData = serviceO.createUserG(request);
	    } catch (IllegalArgumentException ex) {
	        return ResponseEntity.badRequest()
	                .body(Map.of(
	                        "status", "error",
	                        "message", ex.getMessage()
	                ));
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of(
	                        "status", "error",
	                        "message", "Server error"
	                ));
	    }

	    String token =
	            jwtUtil.generateToken(guestData.getMoNumber());

	    return ResponseEntity.ok(
	            Map.of(
	                    "status", "registered",
	                    "token", token,
	                    "user", guestData
	            )
	    );
	}

	
	
	
	@GetMapping("/cities")
	public List<CityEntity> getAllCities() {
		return cityRepository.findAll();
	}
		
	
	
	
	@PostMapping("/verify-and-login")
	public ResponseEntity<?> verifyAndLogin(@RequestBody VerifyOtpRequest request) {

	    // Step 1: OTP validation
	    boolean isOtpValid = otpService.validateOtp(
	            request.getMobileNumber(),
	            request.getOtp()
	    );

	    if (!isOtpValid) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
	                "status", "error",
	                "message", "Invalid OTP"
	        ));
	    }

	    // remove +91 safely
	    String rawMobile = request.getMobileNumber()
	            .replace("+91", "")
	            .trim();

	    // Step 2: Check if user exists
	    UserDetailsEntity existingUser =
	            userRepositoryO.findByMoNumber(rawMobile);

	    //  EXISTING USER
	    if (existingUser != null) {

	        // profileImage is already Cloudinary URL
	        String token = jwtUtil.generateToken(
	                existingUser.getMoNumber()
	        );

	        return ResponseEntity.ok(Map.of(
	                "status", "login_success",
	                "message", "Login successful",
	                "token", token,
	                "user", existingUser
	        ));
	    }

	    // NEW USER
	    return ResponseEntity.ok(Map.of(
	            "status", "new_user",
	            "message", "User not found. Please register first."
	    ));
	}

	
	
	
	
	
	
    
	
	
   
	
	@PostMapping("/verify-and-loginG")
	public ResponseEntity<?> verifyAndLoginG(
	        @RequestBody VerifyOtpRequest request) {

	    // Step 1: OTP validation
	    boolean isOtpValid = otpService.validateOtp(
	            request.getMobileNumber(),
	            request.getOtp()
	    );

	    if (!isOtpValid) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of(
	                        "status", "error",
	                        "message", "Invalid OTP"
	                ));
	    }

	    // remove +91 safely
	    String rawMobile =
	            request.getMobileNumber()
	                    .replace("+91", "")
	                    .trim();

	    // Step 2: Check if guest exists
	    GuestDetailEntity existingGuest =
	            userRepositoryG.findByMoNumber(rawMobile);

	    if (existingGuest != null) {

	        String token =
	                jwtUtil.generateToken(existingGuest.getMoNumber());

	        return ResponseEntity.ok(
	                Map.of(
	                        "status", "login_success",
	                        "message", "Login successful",
	                        "token", token,
	                        "user", existingGuest
	                )
	        );
	    }

	    //  New guest
	    return ResponseEntity.ok(
	            Map.of(
	                    "status", "new_user",
	                    "message", "User not found. Please register first."
	            )
	    );
	}

	
	
	
	
	
    
    
    
    
    
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", "error",
                            "message", "Unauthorized"
                    ));
        }

        String mobileNumber =
                authentication.getPrincipal().toString();

        UserDetailsEntity user =
                userRepositoryO.findByMoNumber(mobileNumber);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", "error",
                            "message", "User not found"
                    ));
        }

        String profileImageUrl = null;

        String storedImage = user.getProfileImage();

        if (storedImage != null && !storedImage.isBlank()) {

            if (storedImage.startsWith("http")) {
                profileImageUrl = storedImage;
            } else {
                profileImageUrl =
                        cloudinaryService.generateUrl(storedImage);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("userData", user);
        response.put("profileImageUrl", profileImageUrl);

        return ResponseEntity.ok(response);
    }

    
    
    @PutMapping("/update-owner-profile")
    public ResponseEntity<?> updateOwnerProfile(
            @RequestParam(required = false) String pgName,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) MultipartFile profileImage,
            Authentication authentication) throws IOException {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", "error",
                            "message", "Unauthorized"
                    ));
        }

        String mobileNumber = authentication.getPrincipal().toString();

        UserDetailsEntity user =
                userRepositoryO.findByMoNumber(mobileNumber);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "status", "error",
                            "message", "User not found"
                    ));
        }

        serviceO.updateOwnerProfile(
                user,
                pgName,
                name,
                address,
                profileImage
        );

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "message", "Profile updated successfully"
                )
        );
    }

    
    
    
  
    
    
    

    @GetMapping("/current-guest")
    public ResponseEntity<?> getCurrentGuest(Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", "error",
                            "message", "Unauthorized"
                    ));
        }

        String mobileNumber =
                authentication.getPrincipal().toString();

        GuestDetailEntity guest =
                userRepositoryG.findByMoNumber(mobileNumber);

        if (guest == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", "error",
                            "message", "Guest not found"
                    ));
        }

        String profileImageUrl = null;

        String storedImage = guest.getProfileImage();

        if (storedImage != null && !storedImage.isBlank()) {

            if (storedImage.startsWith("http")) {
                profileImageUrl = storedImage;
            } else {
                profileImageUrl =
                        cloudinaryService.generateUrl(storedImage);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("guestData", guest);
        response.put("profileImageUrl", profileImageUrl);

        return ResponseEntity.ok(response);
    }

	
	
    
    
    
    @GetMapping("/guest-temp-address")
    public ResponseEntity<?> getGuestTempAddress(
            @RequestParam Long guestId,
            @RequestParam Long ownerId) {

        Optional<StayRequestEntity> stayReqOpt = stayRequestRepo
            .findTopByGuestIdAndOwnerIdOrderByRequestDateDesc(guestId, ownerId);

        if (stayReqOpt.isPresent()) {
            String tAddress = stayReqOpt.get().getTempAddress();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "tAddress", tAddress
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "status", "fail",
                "tAddress", "N/A"
            ));
        }
    }

	
    
    
    
 
    @GetMapping("/profileImage")
    public ResponseEntity<Void> getProfileImage(
            @RequestParam(required = false) String ownerMobile,
            Authentication authentication) {

        String mobileNumber;

        if (ownerMobile != null && !ownerMobile.isBlank()) {
            mobileNumber = ownerMobile;
        } else {
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            mobileNumber = authentication.getPrincipal().toString();
        }

        UserDetailsEntity user =
                userRepositoryO.findByMoNumber(mobileNumber);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        String storedImage = user.getProfileImage();

        if (storedImage == null || storedImage.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        String imageUrl;

        if (storedImage.startsWith("http")) {
            imageUrl = storedImage;
        } else {
            imageUrl = cloudinaryService.generateUrl(storedImage);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, imageUrl)
                .build();
    }


	
	
	
	
	
	
	
	
    @GetMapping("/profileImageG")
    public ResponseEntity<Map<String, String>> getProfileImageG(
            @RequestParam(required = false) String guestMobile,
            Authentication authentication) {

        String mobileNumber;

        if (guestMobile != null && !guestMobile.isBlank()) {
            mobileNumber = guestMobile;
        } else {
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            mobileNumber = authentication.getPrincipal().toString();
        }

        GuestDetailEntity guest =
                userRepositoryG.findByMoNumber(mobileNumber);

        if (guest == null) {
            return ResponseEntity.notFound().build();
        }

        String storedImage = guest.getProfileImage();

        if (storedImage == null || storedImage.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        String imageUrl =
                storedImage.startsWith("http")
                        ? storedImage
                        : cloudinaryService.generateUrl(storedImage);

        return ResponseEntity.ok(Map.of(
                "imageUrl", imageUrl
        ));
    }

    
    
    
    
 
	// Send stay request by guest
	@PostMapping("/sendRequest-with-id")
	public ResponseEntity<?> sendRequestWithId(
	        Authentication authentication,
	        @RequestParam Long ownerId,
	        @RequestParam String idType,
	        @RequestParam("idFront") MultipartFile idFront,
	        @RequestParam("idBack") MultipartFile idBack,
	        @RequestParam(name = "tempAddress", required = false) String tempAddress   // <-- note name = "tempAddress"
	) {

	    if (authentication == null || authentication.getPrincipal() == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("status", "error", "message", "Unauthorized"));
	    }

	    try {
	        String mobileNumber = authentication.getName();

	        StayRequestEntity request = stayRequestService.sendRequestWithId(
	                mobileNumber, ownerId, idType, idFront, idBack, tempAddress
	        );

	        return ResponseEntity.ok(Map.of(
	                "status", "success",
	                "message", "Request sent successfully with ID",
	                "requestData", request
	        ));

	    } catch (IllegalArgumentException ex) {
	        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", ex.getMessage()));
	    } catch (IllegalStateException ex) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", "error", "message", ex.getMessage()));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("status", "error", "message", e.getMessage()));
	    }
	}

	
	
	
	
	
	
	
	
	
	@PostMapping("/delete-pg-request")
	public ResponseEntity<?> deletePgRequest(
	        Authentication authentication,
	        @RequestParam String ownerMobile
	) {
	    String guestMobile = authentication.getName();

	    GuestDetailEntity guest = userRepositoryG.findByMoNumber(guestMobile);
	    UserDetailsEntity owner = userRepositoryO.findByMoNumber(ownerMobile);

	    stayRequestService.deletePgRequestByGuestIdAndOwnerId(
	            guest.getId(), owner.getId()
	    );

	    return ResponseEntity.ok(Map.of(
	            "status", "success",
	            "message", "PG request deleted successfully"
	    ));
	}

	
	
	
	
	
	
	
	
	
	
	
	//  StayRequestController.java
	@GetMapping("/request-id-image")
	public ResponseEntity<?> getRequestIdImage(@RequestParam String fileName) {
	    try {
	        File file = new File(uploadDir + "/Id_image",  fileName);

	        if (!file.exists()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(Map.of("status", "error", "message", "File not found"));
	        }

	        Path path = file.toPath();
	        byte[] imageBytes = Files.readAllBytes(path);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.IMAGE_JPEG); // or detect dynamically
	        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("status", "error", "message", e.getMessage()));
	    }
	}

    
  
    @PostMapping("/accept-and-assign/{requestId}")
    public ResponseEntity<?> acceptAndAssignRequest(
            Authentication authentication,
            @PathVariable Long requestId,
            @RequestBody RoomAssignmentRequest dto) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.ok(Map.of("status", "error", "message", "Unauthorized"));
        }

        String mobileNumber = authentication.getPrincipal().toString();
        UserDetailsEntity owner = userRepositoryO.findByMoNumber(mobileNumber);

        if (owner == null) {
            return ResponseEntity.ok(Map.of("status", "error", "message", "Owner not found"));
        }

        try {
            boolean success = stayRequestService.acceptAndAssign(requestId, owner.getId(), dto);

            if (!success) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Request not found or not authorized"));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Request accepted and room assigned successfully"
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "Something went wrong. Please try again."
            ));
        }
    }
  
    
    
    
    
    // Guest gets Owner details if request accepted
 
    @GetMapping("/accepted-pgs")
    public ResponseEntity<?> getAcceptedPgs(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.ok(Map.of("status", "error", "message", "Unauthorized"));
        }

        //  get guest mobile from authentication
        String mobileNumber;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            mobileNumber = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            mobileNumber = principal.toString();
        }

        try {
            List<Map<String, Object>> acceptedPgs = stayRequestService.getAcceptedPGs(mobileNumber);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "acceptedPgs", acceptedPgs
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
    
    
  
    
    
    @GetMapping("/owner-details")
    public ResponseEntity<?> getOwnerDetails(
            @RequestParam String ownerMobile,
            Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.ok(Map.of("status", "error", "message", "Unauthorized"));
        }

        String guestMobile = authentication.getPrincipal().toString();
        Map<String, Object> response = stayRequestService.getOwnerDetailsForGuestAndOwner(guestMobile, ownerMobile);

        return ResponseEntity.ok(response);
    }

    
    
   
    
    @GetMapping("/room-assignments")
    public ResponseEntity<?> getRoomDetails(
            @RequestParam String guestMobile,
            Principal principal) {

        String ownerMobile = principal.getName();  

        List<RoomAssignment> rooms = roomAssignmentService.getRoomsByGuestAndOwner(guestMobile, ownerMobile);

        if (!rooms.isEmpty()) {
            List<Map<String, String>> response = rooms.stream().map(r -> {
                Map<String, String> map = new HashMap<>();
                map.put("roomNumber", r.getRoomNumber());
                map.put("floorNumber", r.getFloorNumber());
                map.put("buildingNumber", r.getBuildingNumber());
                map.put("address", r.getAddress());
                return map;
            }).toList();

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body("No room assigned for this guest and owner.");
        }
    }

   
    
    
    // Owner gets all guests detail 
	@GetMapping("/all-guest")
	public ResponseEntity<?> getAllGuestForOwner(Authentication authentication) {
		if (authentication == null || authentication.getPrincipal() == null) {
			return ResponseEntity.ok(Map.of("status", "error", "message", "Unauthorized"));
		}

		String mobileNumber = authentication.getPrincipal().toString();

		System.out.println("guestRequest MobileNumber " + mobileNumber);

		UserDetailsEntity owner = userRepositoryO.findByMoNumber(mobileNumber);

		if (owner == null) {
			return ResponseEntity.ok(Map.of("status", "error", "message", "Owner not found"));
		}

		List<AllGuestsDto > allGuest = stayRequestService.getAllGuests(owner.getId());

		
		System.out.println(" AllGuest " + allGuest);

		return ResponseEntity.ok(Map.of("status", "success", "requests", allGuest));

	}

	
	
	
	
	// Owner gets all guest requests
		@GetMapping("/guest-requests")
		public ResponseEntity<?> getGuestRequestsForOwner(Authentication authentication) {
			if (authentication == null || authentication.getPrincipal() == null) {
				return ResponseEntity.ok(Map.of("status", "error", "message", "Unauthorized"));
			}

			String mobileNumber = authentication.getPrincipal().toString();

			System.out.println("guestRequest MobileNumber " + mobileNumber);

			UserDetailsEntity owner = userRepositoryO.findByMoNumber(mobileNumber);

			if (owner == null) {
				return ResponseEntity.ok(Map.of("status", "error", "message", "Owner not found"));
			}

			List<GuestRequestDto> requests = stayRequestService.getGuestRequests(owner.getId());

			
			System.out.println("GuestRequest " + requests);
			

			return ResponseEntity.ok(Map.of("status", "success", "requests", requests)); 
			

		}

	
	
	
    @GetMapping("/pg-by-city")
    public ResponseEntity<?> getPgsByCity(Authentication authentication, @RequestParam String city) {
    	
    	
    //	List<UserDetailsEntity> owners = userRepositoryO.findByCity(city);

    	CityEntity cityEntity = cityRepository.findByName(city);
    	List<UserDetailsEntity> owners = userRepositoryO.findByCity(cityEntity);

    	
        if (owners.isEmpty()) {
            return ResponseEntity.ok(Map.of("status", "error", "message", "No PGs found in this city"));
        }

        List<Map<String, Object>> pgList = owners.stream()
        	    .map(owner -> {
        	        Map<String, Object> map = new HashMap<>();
        	        map.put("pgId", owner.getId());
        	        map.put("pgName", owner.getPgName());
        	        map.put("pgAddress", owner.getAddress());
        	        map.put("ownerName", owner.getName());
        	        return map;
        	    })
        	    .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("status", "success", "pgs", pgList));


    }
    
    
    
    
    @DeleteMapping("/reject-request/{requestId}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId) {
        stayRequestService.deleteByRequestForOwner(requestId); // owner context में delete
        return ResponseEntity.ok(Map.of("status","success","message","Request deleted for owner"));
    }

    
    
    
    //  ID & Details Mismatch → Update status to "mismatch"
    @PatchMapping("/mismatch-request/{requestId}")
    public ResponseEntity<?> markMismatch(@PathVariable Long requestId) {
        stayRequestService.markAsMismatch(requestId);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Guest notified and status updated to mismatch"
        ));
    }

    
    
   
    
    //  Check mismatch status
    @GetMapping("/mismatch/{guestId}")
    public ResponseEntity<?> checkMismatch(@PathVariable Long guestId) {
        boolean isMismatch = stayRequestService.hasMismatchRequest(guestId);
        return ResponseEntity.ok(Map.of(
            "status", isMismatch ? "MISMATCH" : "NONE"
        ));
    }
    
    
    
    
    
    //  Delete mismatch request after OK click
    @DeleteMapping("/mismatch/{guestId}")
    public ResponseEntity<?> deleteMismatch(@PathVariable Long guestId) {
        stayRequestService.deleteMismatchRequest(guestId);
        return ResponseEntity.ok(Map.of(
            "status", "DELETED",
            "message", "Mismatch request deleted"
        ));
    }
    
    
    
    
    
    
    @GetMapping("/check-update-eligibility")
    public ResponseEntity<?> checkUpdateEligibility(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Unauthorized"));
        }

        String mobile = authentication.getName(); // or .toString() depending on your auth
        GuestDetailEntity guest = userRepositoryG.findByMoNumber(mobile);
        if (guest == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Guest not found"));
        }

        // Server-side check: is there any ACCEPTED stay request for this guest?
     //   boolean hasAccepted = stayRequestRepo.existsByGuestAndStatus(guest, RequestStatus.ACCEPTED);

        boolean hasAcceptedOrRemoved = stayRequestRepo.existsByGuestIdAndAcceptedOrRemoved(guest.getId());
 

        if (hasAcceptedOrRemoved) {
            return ResponseEntity.ok(Map.of(
                "status", "ineligible",
                "message", "You are already accepted by a PG owner or you have a pending request. " +
                           "If your request is rejected then you can update details and re-send with correct ID and temporary address."
            ));
        }

        return ResponseEntity.ok(Map.of("status", "eligible"));
    }

    
    
    
   
    
    
    
    @PutMapping("/update-details")
    public ResponseEntity<?> updateGuestDetails(
            Authentication authentication,
            @RequestParam String name,
            @RequestParam String pAddress,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Unauthorized"));
        }

        String mobile = authentication.getName();
        GuestDetailEntity guest = userRepositoryG.findByMoNumber(mobile);

        if (guest == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Guest not found"));
        }

        boolean blocked =
                stayRequestRepo.existsByGuestIdAndAcceptedOrRemoved(guest.getId());

        if (blocked) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "status", "ineligible",
                            "message", "Profile update not allowed while request is active."
                    ));
        }

        try {
            guest.setName(name.trim());
            guest.setPAddress(pAddress.trim());

            //  PROFILE IMAGE UPDATE (SAFE FLOW)
            if (profileImage != null && !profileImage.isEmpty()) {

                // 1️ Upload FIRST
                String newPublicId = cloudinaryService.uploadImage(
                        profileImage,
                        "pg-manager/guest-profile",
                        "guest_" + UUID.randomUUID()
                );

                // 2️ Delete OLD image ONLY AFTER successful upload
                if (guest.getProfileImage() != null && !guest.getProfileImage().isBlank()) {
                    cloudinaryService.deleteImage(guest.getProfileImage());
                }

                // 3️ Update DB
                guest.setProfileImage(newPublicId);
            }

            userRepositoryG.save(guest);

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "Guest details updated successfully"
                    )
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Update failed"));
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    //  fetch all requests for current guest
    @GetMapping("/pending-acceptd-requests")
    public ResponseEntity<?> getGuestRequests(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Unauthorized"));
        }

        String mobileNumber = authentication.getName();
        
        List<Map<String, Object>> formattedRequests = stayRequestService.getRequestsForGuest(mobileNumber);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "requests", formattedRequests
        ));
    }
    
    
    
    
    // Send Leave Request by guest
    
    @PostMapping("/send-leave-request")
    public ResponseEntity<?> sendLeaveRequest(Authentication authentication, @RequestParam String ownerMobile) {
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "error", "message", "Unauthorized"));
            }

            //  Authenticated user's mobile number (guest)
            String mobile = authentication.getName();

            //  Guest fetch from DB
            GuestDetailEntity guest = userRepositoryG.findByMoNumber(mobile);
            if (guest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Guest not found"));
            }
            
            
            
            UserDetailsEntity owner = userRepositoryO.findByMoNumber(ownerMobile);
            if (owner == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Guest not found"));
            }

            
            //  Call service with both IDs
            String message = leaveRequestService.createLeaveRequest(guest.getId(), owner.getId());
			
            return ResponseEntity.ok(Map.of("status", "success", "message", message));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    
    
    
    
    
    // Cancel leave request by guest
    @PostMapping("/delete-leave-request")
    public ResponseEntity<?> deleteLeaveRequest(
            Authentication authentication,
            @RequestParam String ownerMobile) {

        try {
            //  Authentication check
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "error", "message", "Unauthorized"));
            }

            //  Guest mobile from authentication
            String guestMobile = authentication.getName();

            //  Fetch guest entity
            GuestDetailEntity guest = userRepositoryG.findByMoNumber(guestMobile);
            if (guest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Guest not found"));
            }

            //  Fetch owner entity
            UserDetailsEntity owner = userRepositoryO.findByMoNumber(ownerMobile);
            if (owner == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Owner not found"));
            }

            //  Call service to delete leave request (unsend)
            String message = leaveRequestService.deleteLeaveRequest(
                    guest.getId(),
                    owner.getId()
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", message
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    
    
    
    
    
    
    
    
 // Owner gets all  guests detail who send leave request
 	@GetMapping("/pending-leave-request")
 	public ResponseEntity<?> getLeaveRequestForOwner(Authentication authentication) {
 		if (authentication == null || authentication.getPrincipal() == null) {
 			return ResponseEntity.ok(Map.of("status", "error", "message", "Unauthorized"));
 		}

 		String mobileNumber = authentication.getPrincipal().toString();

 		System.out.println("guestRequest MobileNumber " + mobileNumber);

 		UserDetailsEntity owner = userRepositoryO.findByMoNumber(mobileNumber);

 		if (owner == null) {
 			return ResponseEntity.ok(Map.of("status", "error", "message", "Owner not found"));
 		}

 		List<LeaveRquestDto> allGuest = stayRequestService.getLeaveRequest(owner.getId());

 		
 		System.out.println(" LeaveRequest " + allGuest);

 		return ResponseEntity.ok(Map.of("status", "success", "requests", allGuest));

 	}

    
    
 	// Accept leave request by owner 
 	
 	@PutMapping("/leave-request/{guestMobile}")
 	public ResponseEntity<?> acceptLeaveRequest(
 	        @PathVariable String guestMobile,
 	        Authentication authentication) {
 	    try {
 	        //  1. Owner Mobile from JWT Authentication
 	        String ownerMobile = authentication.getName();

 	        //  2. Fetch Guest & Owner IDs from their mobile numbers
 	       GuestDetailEntity guest = userRepositoryG.findByMoNumber(guestMobile);
 	                
 	 	   UserDetailsEntity owner = userRepositoryO.findByMoNumber(ownerMobile);

 	        if (guest == null || owner == null) {
 	            return ResponseEntity.badRequest().body(Map.of(
 	                    "status", "error",
 	                    "message", "Invalid guest or owner mobile"
 	            ));
 	        }

 	        //  3. Find StayRequestEntity
 	        StayRequestEntity stay = stayRequestRepo
 	                .findByGuestIdAndOwnerId(guest.getId(), owner.getId());

 	        //  4. Find LeaveRequestEntity
 	        LeaveRequestEntity leave = leaveRequestRepo
 	                .findByGuestIdAndOwnerId(guest.getId(), owner.getId());

 	        if (stay == null || leave == null) {
 	            return ResponseEntity.badRequest().body(Map.of(
 	                    "status", "error",
 	                    "message", "No matching stay/leave request found"
 	            ));
 	        }

 	        //  5. Update status and accepted date
 	        stay.setStatus(RequestStatus.REMOVED);
 	        leave.setStatus(RequestStatus.ACCEPTED);
 	        leave.setLAcceptedDate(LocalDateTime.now());

 	        stayRequestRepo.save(stay);
 	        leaveRequestRepo.save(leave);

 	        return ResponseEntity.ok(Map.of(
 	                "status", "success",
 	                "message", "Leave request accepted successfully!"
 	        ));
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	        return ResponseEntity.internalServerError().body(Map.of(
 	                "status", "error",
 	                "message", "Something went wrong on server"
 	        ));
 	    }
 	}

 	
 	
 	
 //  Upload or Update UPI for a specific owner
	
 	@PostMapping("/upload/{ownerId}/{type}")
 	public ResponseEntity<?> uploadUpi(
 	        @PathVariable Long ownerId,
 	        @PathVariable String type,
 	        @RequestParam("upiId") String upiId,
 	        @RequestParam(value = "qrFile", required = false) MultipartFile qrFile
 	) {
 	    try {
 	        PaymentUpi upi = upiService.saveUpi(
 	                ownerId,
 	                type.toUpperCase(),
 	                upiId,
 	                qrFile
 	        );
 	        return ResponseEntity.ok(upi);
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	        return ResponseEntity.internalServerError()
 	                .body("Failed to upload UPI info");
 	    }
 	}

    
    
 	
 	
 	
    
    
 //  Get UPI Info by Owner + Payment Type
 	@GetMapping("/payment-method/{ownerId}/{type}")
 	public ResponseEntity<?> getUpi(
 	        @PathVariable("ownerId") Long ownerId,
 	        @PathVariable("type") String paymentType) {

 	    try {
 	        PaymentUpi upi = upiService
 	                .getUpiByOwnerAndType(ownerId, paymentType.toUpperCase());

 	        if (upi == null) {
 	            return ResponseEntity.ok(Map.of(
 	                    "upiId", "",
 	                    "qrCodeUrl", "",
 	                    "paymentType", paymentType
 	            ));
 	        }

 	        Map<String, Object> response = new HashMap<>();
 	        response.put("upiId", upi.getUpiId());
 	        response.put("qrCodeUrl", upi.getQrPublicId()); // 🔐 SIGNED URL
 	        response.put("paymentType", upi.getPaymentType());

 	        return ResponseEntity.ok(response);

 	    } catch (Exception e) {
 	        e.printStackTrace();
 	        return ResponseEntity.internalServerError()
 	                .body(Map.of("error", "Error fetching UPI info"));
 	    }
 	}

    
    
    
 	@GetMapping("/payment-method/by-mobile")
 	public ResponseEntity<?> getUpiByOwnerMobileAndType(
 	        Authentication authentication,
 	        @RequestParam("ownerMobile") String ownerMobile,
 	        @RequestParam("paymentType") String paymentType) {

 	    try {
 	        //1. Guest mobile from JWT
 	        String guestMobile = authentication.getName();

 	        // 2. Fetch Guest
 	        GuestDetailEntity guest = userRepositoryG.findByMoNumber(guestMobile);
 	        if (guest == null) {
 	            return ResponseEntity.status(404)
 	                    .body(Map.of("error", "Guest not found"));
 	        }

 	        // 3. Fetch Owner
 	        UserDetailsEntity owner = userRepositoryO.findByMoNumber(ownerMobile);
 	        if (owner == null) {
 	            return ResponseEntity.status(404)
 	                    .body(Map.of("error", "Owner not found"));
 	        }

 	        // 4. Fetch StayRequest
 	        StayRequestEntity stay =
 	                stayRequestRepo.findByGuestIdAndOwnerId(
 	                        guest.getId(), owner.getId());

 	        if (stay == null) {
 	            return ResponseEntity.ok(Map.of(
 	                    "status", "NOT_FOUND",
 	                    "message", "You have no stay request with this PG.",
 	                    "upiId", "",
 	                    "qrCodeUrl", ""
 	            ));
 	        }

 	        if (stay.getStatus() != RequestStatus.ACCEPTED) {
 	            return ResponseEntity.ok(Map.of(
 	                    "status", stay.getStatus().name(),
 	                    "message", "You are removed or not accepted by this PG.",
 	                    "upiId", "",
 	                    "qrCodeUrl", ""
 	            ));
 	        }

 	        // 5. Fetch UPI (service already attaches SIGNED URL)
 	        PaymentUpi upi =
 	                upiService.getUpiByOwnerAndType(
 	                        owner.getId(),
 	                        paymentType.toUpperCase());

 	        if (upi == null) {
 	            return ResponseEntity.ok(Map.of(
 	                    "status", "NO_UPI",
 	                    "message", "No payment method found.",
 	                    "upiId", "",
 	                    "qrCodeUrl", ""
 	            ));
 	        }

 	        String qrUrl = (upi.getQrPublicId() != null)
 	                ? upi.getQrPublicId()
 	                : "";

 	        return ResponseEntity.ok(Map.of(
 	                "status", "ACCEPTED",
 	                "upiId", upi.getUpiId(),
 	                "qrCodeUrl", qrUrl,         
 	                "paymentType", upi.getPaymentType(),
 	                "ownerMobile", ownerMobile
 	        ));

 	    } catch (Exception e) {
 	        e.printStackTrace();
 	        return ResponseEntity.internalServerError()
 	                .body(Map.of("error", "Server error"));
 	    }
 	}

 	
 	
		  
 	
 	
		  
 	
	

  
 	
 	@PostMapping("/upload-payment")
 	public ResponseEntity<?> uploadPayment(
 	        @RequestParam("ownerMobile") String ownerMobile,
 	        @RequestParam("paymentType") String paymentType,
 	        @RequestParam("amount") Double amount,
 	        @RequestParam("screenshot") MultipartFile screenshot,
 	        Authentication authentication) {

 	    if (authentication == null || authentication.getPrincipal() == null) {
 	        return ResponseEntity.ok(Map.of(
 	                "status", "error",
 	                "message", "Unauthorized"
 	        ));
 	    }

 	    try {
 	        String guestMobile = authentication.getPrincipal().toString();

 	        GuestDetailEntity guest =
 	                userRepositoryG.findByMoNumber(guestMobile);

 	        UserDetailsEntity owner =
 	                userRepositoryO.findByMoNumber(ownerMobile);

 	        if (guest == null || owner == null) {
 	            return ResponseEntity.ok(Map.of(
 	                    "status", "error",
 	                    "message", "Invalid guest or owner"
 	            ));
 	        }

 	        if (screenshot == null || screenshot.isEmpty()) {
 	            return ResponseEntity.ok(Map.of(
 	                    "status", "error",
 	                    "message", "Screenshot required"
 	            ));
 	        }

 	       

 	        String folder = "pg-manager/payment-ss/" ;

 	        String uniqueName =
 	                "guest_" + guest.getId()
 	                + "_" + System.currentTimeMillis();

 	        String publicId =
 	                cloudinaryService.uploadPrivateImage(
 	                        screenshot,
 	                        folder,
 	                        uniqueName
 	                );


 	        PaymentTransaction txn = new PaymentTransaction();
 	        txn.setOwner(owner);
 	        txn.setGuest(guest);
 	        txn.setPaymentType(paymentType);
 	        txn.setAmount(amount);
 	        txn.setReceiptImage(publicId); 
 	        txn.setPaymentDate(LocalDateTime.now());
 	        txn.setStatus("PENDING");

 	        paymentRepo.save(txn);

 	        return ResponseEntity.ok(Map.of(
 	                "status", "success",
 	                "message", "Payment uploaded successfully"
 	        ));

 	    } catch (Exception e) {
 	        e.printStackTrace();
 	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
 	                .body(Map.of(
 	                        "status", "error",
 	                        "message", "Server error"
 	                ));
 	    }
 	}

 	
 	
 	
    
    
    
    
	  
	  
	 
   
 	@GetMapping("/payment-history")
 	public ResponseEntity<?> getHistory(
 	        @RequestParam(required = false) String ownerMobile,
 	        Authentication authentication) {

 	    try {
 	        if (authentication == null || authentication.getPrincipal() == null) {
 	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
 	                    .body(Map.of("error", "Unauthorized"));
 	        }

 	        String guestMobile = authentication.getPrincipal().toString();

 	        GuestDetailEntity guest =
 	                userRepositoryG.findByMoNumber(guestMobile);

 	        if (guest == null) {
 	            return ResponseEntity.ok(Map.of("history", List.of()));
 	        }

 	        List<PaymentTransaction> list;

 	        //  Optional owner filter
 	        if (ownerMobile != null && !ownerMobile.isBlank()) {
 	            list = paymentRepo
 	                    .findByGuestAndOwner_MoNumber(guest, ownerMobile);
 	        } else {
 	            list = paymentRepo.findByGuest(guest);
 	        }

 	        List<Map<String, Object>> response = new ArrayList<>();

 	        for (PaymentTransaction p : list) {

 	            Map<String, Object> item = new HashMap<>();

 	            item.put("type",
 	                    p.getPaymentType() != null
 	                            ? p.getPaymentType()
 	                            : "—");

 	            item.put("amount",
 	                    p.getAmount() != null
 	                            ? p.getAmount()
 	                            : 0);

 	            item.put("paymentDate",
 	                    p.getPaymentDate() != null
 	                            ? p.getPaymentDate()
 	                            : "—");

 	            item.put("verifiedDate",
 	                    p.getVerifiedDate() != null
 	                            ? p.getVerifiedDate()
 	                            : "—");

 	            item.put("status",
 	                    p.getStatus() != null
 	                            ? p.getStatus()
 	                            : "Pending");

 	            item.put("pgName",
 	                    (p.getOwner() != null
 	                            && p.getOwner().getPgName() != null)
 	                            ? p.getOwner().getPgName()
 	                            : "—");

 	            String receiptUrl = "/images/default.png";

 	            if (p.getReceiptImage() != null
 	                    && !p.getReceiptImage().isBlank()) {

 	                receiptUrl =
 	                        cloudinaryService
 	                                .generateAuthenticatedUrl(
 	                                        p.getReceiptImage()
 	                                );
 	            }

 	            item.put("receiptUrl", receiptUrl);

 	            response.add(item);
 	        }

 	        return ResponseEntity.ok(Map.of("history", response));

 	    } catch (Exception e) {
 	        e.printStackTrace();
 	        return ResponseEntity
 	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
 	                .body(Map.of("error", "Server error"));
 	    }
 	}

    
 	
 	
 	
 	@GetMapping("/pending-payments")
 	public ResponseEntity<?> getPendingPayments(Authentication authentication) {
 	    try {
 	        if (authentication == null || authentication.getPrincipal() == null) {
 	            return ResponseEntity
 	                    .status(HttpStatus.UNAUTHORIZED)
 	                    .body(Map.of("error", "Unauthorized"));
 	        }

 	        String ownerMobile = authentication.getPrincipal().toString();
 	        UserDetailsEntity owner = userRepositoryO.findByMoNumber(ownerMobile);

 	        if (owner == null) {
 	            return ResponseEntity.ok(Map.of("history", List.of()));
 	        }

 	        //  updated query call
 	        List<Map<String, Object>> list =
 	                paymentRepo.findPendingPaymentsWithIdProofs(owner);

 	        //  Format response
 	        List<Map<String, Object>> response = new ArrayList<>();

 	        for (Map<String, Object> p : list) {

 	            Map<String, Object> item = new HashMap<>();

 	            item.put("id", p.get("id"));
 	            item.put("guestName", p.get("guestName"));
 	            item.put("guestMobile", p.get("guestMobile"));
 	            item.put("taddress", p.get("taddress"));
 	            item.put("paddress", p.get("paddress"));
 	            item.put("type", p.get("type"));
 	            item.put("amount", p.get("amount"));
 	            item.put("paymentDate", p.get("paymentDate"));
 	            item.put("status", p.get("status"));

 	           
 	            String receiptUrl = "/images/default.png";

 	            if (p.get("receiptImage") != null) {
 	                receiptUrl =
 	                        cloudinaryService
 	                                .generateAuthenticatedUrl(
 	                                        p.get("receiptImage").toString()
 	                                );
 	            }

 	            item.put("receiptUrl", receiptUrl);

 	            //  id proofs (unchanged)
 	            item.put("idFront", p.get("idFront"));
 	            item.put("idBack", p.get("idBack"));

 	            response.add(item);
 	        }

 	        return ResponseEntity.ok(Map.of("history", response));

 	    } catch (Exception e) {
 	        e.printStackTrace();
 	        return ResponseEntity
 	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
 	                .body(Map.of("error", e.getMessage()));
 	    }
 	}

 	
 	
 	
 	
 	
    
  
 	
 	
 	@GetMapping("/payment-historyO")
 	public ResponseEntity<?> getPaymentHistotyO(Authentication authentication) {
 	    try {
 	        if (authentication == null || authentication.getPrincipal() == null) {
 	            return ResponseEntity
 	                    .status(HttpStatus.UNAUTHORIZED)
 	                    .body(Map.of("error", "Unauthorized"));
 	        }

 	        String ownerMobile = authentication.getPrincipal().toString();
 	        UserDetailsEntity owner = userRepositoryO.findByMoNumber(ownerMobile);

 	        if (owner == null) {
 	            return ResponseEntity.ok(Map.of("history", List.of()));
 	        }

 	        //  updated query call
 	        List<Map<String, Object>> list =
 	                paymentRepo.findPaymentHistoryWithIdProofs(owner);

 	        List<Map<String, Object>> response = new ArrayList<>();

 	        for (Map<String, Object> p : list) {

 	            Map<String, Object> item = new HashMap<>();

 	            item.put("id", p.get("id"));
 	            item.put("guestName", p.get("guestName"));
 	            item.put("guestId", p.get("guestId"));
 	            item.put("guestMobile", p.get("guestMobile"));
 	            item.put("taddress", p.get("taddress"));
 	            item.put("paddress", p.get("paddress"));
 	            item.put("type", p.get("type"));
 	            item.put("amount", p.get("amount"));
 	            item.put("paymentDate", p.get("paymentDate"));
 	            item.put("verifiedDate", p.get("verifiedDate"));
 	            item.put("status", p.get("status"));

 	           
 	            String receiptUrl = "/images/default.png";

 	            if (p.get("receiptImage") != null) {
 	                receiptUrl =
 	                        cloudinaryService
 	                                .generateAuthenticatedUrl(
 	                                        p.get("receiptImage").toString()
 	                                );
 	            }

 	            item.put("receiptUrl", receiptUrl);

 	            item.put("idFront", p.get("idFront"));
 	            item.put("idBack", p.get("idBack"));

 	            response.add(item);
 	        }

 	        return ResponseEntity.ok(Map.of("history", response));

 	    } catch (Exception e) {
 	        e.printStackTrace();
 	        return ResponseEntity
 	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
 	                .body(Map.of("error", e.getMessage()));
 	    }
 	}

    
    @PutMapping("/update-payment-status/{id}")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        try {
            if (authentication == null || authentication.getPrincipal() == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "error", "message", "Unauthorized"));

            PaymentTransaction payment = paymentRepo.findById(id).orElse(null);
            if (payment == null)
                return ResponseEntity.ok(Map.of("status", "error", "message", "Payment not found"));

            if (!List.of("Verified", "Rejected").contains(status))
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "Invalid status value"));

            payment.setStatus(status);
            payment.setVerifiedDate(LocalDateTime.now());
            paymentRepo.save(payment);

            return ResponseEntity.ok(Map.of("status", "success", "message", "Payment marked as " + status));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    
    
    
    
    
    
    
    
    @PostMapping("/owner/add-notice")
    public ResponseEntity<?> addNotice(
            @RequestParam("message") String message,
            Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.ok(
                Map.of("status", "error", "message", "Unauthorized")
            );
        }

        //  Logged-in owner mobile from JWT
        String ownerMobile = authentication.getPrincipal().toString();

        UserDetailsEntity owner = userRepositoryO.findByMoNumber(ownerMobile);

        if (owner == null) {
            return ResponseEntity.ok(
                Map.of("status", "error", "message", "Owner not found")
            );
        }

       // Long pgId = owner.getPgId();
        
        Long pgId = owner.getId();

        // Max 5 notice rule
        long count = noticeRepo.countByPgIdAndActiveTrue(pgId);
        if (count >= 5) {
            return ResponseEntity.ok(
                Map.of(
                    "status", "limit",
                    "message", "Maximum 5 notices allowed. Please update any existing notice."
                )
            );
        }

        NoticeEntity notice = new NoticeEntity();
        notice.setPgId(pgId);
        notice.setMessage(message);
        notice.setActive(true);

        noticeRepo.save(notice);

        return ResponseEntity.ok(
            Map.of("status", "success", "message", "Notice added successfully")
        );
    }

    
    
    
    
    
    @PutMapping("/owner/update-notice")
    public ResponseEntity<?> updateNotice(
            @RequestParam("noticeId") Long noticeId,
            @RequestParam("message") String message,
            Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.ok(
                Map.of("status", "error", "message", "Unauthorized")
            );
        }

        NoticeEntity notice = noticeRepo.findById(noticeId).orElse(null);

        if (notice == null) {
            return ResponseEntity.ok(
                Map.of("status", "error", "message", "Notice not found")
            );
        }

        notice.setMessage(message);
        noticeRepo.save(notice); // updatedAt auto update

        return ResponseEntity.ok(
            Map.of("status", "success", "message", "Notice updated successfully")
        );
    }

    
    
    @DeleteMapping("/owner/delete-notice")
    public ResponseEntity<?> deleteNotice(
            @RequestParam("noticeId") Long noticeId,
            Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.ok(
                Map.of("status", "error", "message", "Unauthorized")
            );
        }

        noticeRepo.deleteById(noticeId);

        return ResponseEntity.ok(
            Map.of("status", "success", "message", "Notice deleted")
        );
    }

    
    
   
    
    
    
    @GetMapping("/notices")
    public ResponseEntity<?> getNotices(Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.ok(
                Map.of("status", "error", "message", "Unauthorized")
            );
        }

        String mobile = authentication.getPrincipal().toString();

        //  ONLY OWNER ALLOWED
        if (!userRepositoryO.existsByMoNumber(mobile)) {
            return ResponseEntity.status(403).body(
                Map.of(
                    "status", "error",
                    "message", "Access denied. Owner only."
                )
            );
        }

        Long ownerId = userRepositoryO.findByMoNumber(mobile).getId();


        List<NoticeEntity> notices =
            noticeRepo.findByPgIdAndActiveTrueOrderByUpdatedAtDesc(ownerId);

        return ResponseEntity.ok(notices);
    }

    
    
    
    
    @GetMapping("/guest/notices")
    public ResponseEntity<?> getGuestNotices(
            @RequestParam String ownerMobile,
            Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Unauthorized"));
        }

        // 1 Guest from JWT
        String guestMobile = authentication.getPrincipal().toString();
        GuestDetailEntity guest = userRepositoryG.findByMoNumber(guestMobile);

        if (guest == null) {
            return ResponseEntity.ok(List.of());
        }

        // 2️ Verify ACCEPTED relation using SAME logic as owner-details
        StayRequestEntity acceptedRequest = stayRequestRepo
                .findAcceptedRequestforNotice(guestMobile, ownerMobile)
                .orElse(null);

        if (acceptedRequest == null) {
            //  Guest is not active for this owner
            return ResponseEntity.ok(List.of());
        }

        // 3️ Owner confirmed from accepted request
        UserDetailsEntity owner = acceptedRequest.getOwner();

        // 4️ Fetch notices for this owner
        List<NoticeEntity> notices = noticeRepo
                .findByPgIdAndActiveTrueOrderByUpdatedAtDesc(owner.getId());

        return ResponseEntity.ok(notices);
    }

    
    
    

}   
