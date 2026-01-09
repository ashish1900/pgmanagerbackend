package com.ash.main.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ash.main.dto.VerifyAndRegister;
import com.ash.main.dto.VerifyAndRegisterG;
import com.ash.main.entity.CityEntity;
import com.ash.main.entity.GuestDetailEntity;
import com.ash.main.entity.UserDetailsEntity;
import com.ash.main.dto.UserLoginDetails;
import com.ash.main.rpository.RepositoryO;

import io.jsonwebtoken.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


import com.ash.main.rpository.CityRepository;
import com.ash.main.rpository.RepositoryG;

@Service
public class ServiceO {

    @Autowired
    RepositoryO repositoryO;
    
    @Autowired
    RepositoryG repositoryG;

    @Autowired
    CityRepository cityRepository;
    
    @Value("${app.upload.dir}")
    private String uploadDirectory;
    
    

    @Autowired
	private CloudinaryService cloudinaryService;

    
    @Transactional
    public UserDetailsEntity createUser(VerifyAndRegister userDetails) {

        String cityName = (userDetails.getCity() == null) ? "" : userDetails.getCity().trim();
        if (cityName.isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }

        // find existing city (case-insensitive) or create new one
        CityEntity cityEntity = cityRepository.findByNameIgnoreCase(cityName)
                .orElseGet(() -> {
                    CityEntity c = new CityEntity(cityName);
                    return cityRepository.save(c);
                });

        // If owner exists (by mobile) -> update fields; otherwise create new owner
        UserDetailsEntity entity = repositoryO.findByMoNumber(userDetails.getMoNumber());
        if (entity == null) {
            entity = new UserDetailsEntity();
            entity.setMoNumber(userDetails.getMoNumber());
        }

        entity.setName(userDetails.getName());
        entity.setPgName(userDetails.getPgName());
        entity.setAddress(userDetails.getAddress());
        entity.setProfileImage(userDetails.getProfileImage());
        entity.setCity(cityEntity);

        repositoryO.save(entity);
        return entity;
    }

    
    
    
    
    @Transactional
    public void updateOwnerProfile(
            UserDetailsEntity user,
            String pgName,
            String name,
            String address,
            MultipartFile profileImage
    ) throws IOException {

        //  Required validation
        if (pgName == null || pgName.trim().isEmpty()) {
            throw new IllegalArgumentException("PG Name is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name is required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address is required");
        }

        //  Text updates
        user.setPgName(pgName.trim());
        user.setName(name.trim());
        user.setAddress(address.trim());

        //  Optional image update
        if (profileImage != null && !profileImage.isEmpty()) {

            // 1️ delete old image (if exists)
            String oldPublicId = user.getProfileImage();
            if (oldPublicId != null && !oldPublicId.isBlank()) {
                cloudinaryService.deleteImage(oldPublicId);
            }

            // 2️ upload new image
            String uniqueName =
                    "profile_" + UUID.randomUUID();

            String newPublicId =
                    cloudinaryService.uploadImage(
                            profileImage,
                            "pg-manager/profile",
                            uniqueName
                    );

            // 3️ save ONLY public_id
            user.setProfileImage(newPublicId);
        }

        repositoryO.save(user);
    }

    
    
    
    
    
    public String saveProfileImage(MultipartFile file, UserDetailsEntity user)
	        throws IOException {

	    // 1 MB limit
	    if (file.getSize() > 1024 * 1024) {
	        throw new IOException("Profile image must be less than 1 MB");
	    }

	    // Type validation
	    String contentType = file.getContentType();
	    if (contentType == null ||
	       !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
	        throw new IOException("Only JPG or PNG images are allowed");
	    }

	    Path uploadPath = Paths.get(uploadDirectory);
	    if (!Files.exists(uploadPath)) {
	        try {
				Files.createDirectories(uploadPath);
			} catch (java.io.IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	    // Delete old image
	    if (user.getProfileImage() != null) {
	        Path oldImage = uploadPath.resolve(user.getProfileImage());
	        if (Files.exists(oldImage)) {
	            try {
					Files.delete(oldImage);
				} catch (java.io.IOException e) {
					e.printStackTrace();
				}
	        }
	    }

	    String extension = file.getOriginalFilename()
	            .substring(file.getOriginalFilename().lastIndexOf("."));

	    String newFileName =
	            "owner_" + user.getId() + "_" + System.currentTimeMillis() + extension;

	    try {
			Files.copy(
			        file.getInputStream(),
			        uploadPath.resolve(newFileName),
			        StandardCopyOption.REPLACE_EXISTING
			);
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

	    return newFileName;
	}

	
    
    
    
    
    
    
    public VerifyAndRegister userLogin(UserLoginDetails userLoginDetails) {

        UserDetailsEntity userData = repositoryO.findByMoNumber(userLoginDetails.getMoNumber());

        VerifyAndRegister userDetails = null;

        if (userData != null) {
            userDetails = new VerifyAndRegister();
            userDetails.setMoNumber(userData.getMoNumber());
            userDetails.setName(userData.getName());
            userDetails.setAddress(userData.getAddress());
            userDetails.setPgName(userData.getPgName());
            // return city name for frontend convenience
            userDetails.setCity(userData.getCity() != null ? userData.getCity().getName() : null);
            userDetails.setProfileImage(userData.getProfileImage());
        }
        return userDetails;
    }
    
    
    
    
	public GuestDetailEntity createUserG(VerifyAndRegisterG userDetails) {

		GuestDetailEntity entity = new GuestDetailEntity();

		entity.setMoNumber(userDetails.getMoNumber());
		entity.setName(userDetails.getName());
		entity.setPAddress(userDetails.getPaddress());
	//	entity.setTAddress(userDetails.getTaddress());
		entity.setProfileImage(userDetails.getProfileImage());

		repositoryG.save(entity);
		
		return entity;

	}
	
	
	
	
	
	
	


}