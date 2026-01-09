package com.ash.main.service;

import com.cloudinary.AuthToken;
import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    
    /**
     * Upload image (PROFILE / NORMAL images)
     * @return FULL public_id (folder/name)
     */
    public String uploadImage(
            MultipartFile file,
            String folder,
            String uniqueName
    ) {
        try {
            Map<String, Object> options = Map.of(
                    "folder", folder,
                    "public_id", uniqueName,
                    "overwrite", true,
                    "resource_type", "image"
            );

            Map<?, ?> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            options
                    );

            return result.get("public_id").toString();

        } catch (Exception e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }

    /**
     * Re upload image from URL (temp → profile)
     */
    public String uploadImageFromUrl(
            String imageUrl,
            String folder,
            String uniqueName
    ) {
        try {
            Map<String, Object> options = Map.of(
                    "folder", folder,
                    "public_id", uniqueName,
                    "overwrite", true,
                    "resource_type", "image"
            );

            Map<?, ?> result =
                    cloudinary.uploader().upload(imageUrl, options);

            return result.get("public_id").toString();

        } catch (Exception e) {
            throw new RuntimeException("Cloudinary re-upload failed", e);
        }
    }

    /**
     * Delete image using FULL public_id
     */
    public void deleteImage(String fullPublicId) {
        try {
            cloudinary.uploader().destroy(
                    fullPublicId,
                    Map.of("resource_type", "image")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    
    /**
     *  Delete AUTHENTICATED ID image (Aadhaar / PAN)
     */
    public void deleteAuthenticatedIdImage(String fullPublicId) {
        try {
            Map<String, Object> options = Map.of(
                    "resource_type", "image",
                    "type", "authenticated"   // 🔴 MUST
            );

            Map result = cloudinary.uploader().destroy(fullPublicId, options);
            System.out.println("ID image delete result: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    
    
    /**
     * Generate normal secure URL (PROFILE IMAGES)
     */
    public String generateUrl(String fullPublicId) {
        return cloudinary.url()
                .publicId(fullPublicId)
                .secure(true)
                .generate();
    }

    
    
    /**
     * 🔒 Upload PRIVATE image (ID / Aadhaar / PAN)
     * @return FULL public_id
     */
    
    
    /**
     * 🔒 Upload ID image (Aadhaar / PAN) – AUTHENTICATED
     */
    public String uploadPrivateImage(
            MultipartFile file,
            String folder,
            String uniqueName
    ) {
        try {
            Map<String, Object> options = Map.of(
                    "folder", folder,
                    "public_id", uniqueName,
                    "overwrite", true,
                    "resource_type", "image",
                    "type", "authenticated"   // CHANGE HERE
            );

            Map<?, ?> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            options
                    );

            return result.get("public_id").toString();

        } catch (Exception e) {
            throw new RuntimeException("ID image upload failed", e);
        }
    }

    
    /**
     *  Generate SIGNED URL for authenticated image
     */
    public String generateAuthenticatedUrl(String fullPublicId) {

        return cloudinary.url()
                .resourceType("image")
                .type("authenticated")   
                .signed(true)            
                .secure(true)
                .generate(fullPublicId);
    }


   
    public String uploadUpiQr(
            MultipartFile file,
            Long ownerId,
            String paymentType
    ) {
        try {
            String folder = "pg-manager/upi-qr/" + ownerId;
            String uniqueName = paymentType.toLowerCase() + "_qr";

            Map<String, Object> options = Map.of(
                    "folder", folder,
                    "public_id", uniqueName,
                    "overwrite", true,
                    "resource_type", "image",
                    "type", "authenticated"   
            );

            Map<?, ?> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            options
                    );

            return result.get("public_id").toString(); 

        } catch (Exception e) {
            throw new RuntimeException("UPI QR upload failed", e);
        }
    }

    
    
   
    
}
