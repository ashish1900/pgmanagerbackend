package com.ash.main.service;

import com.ash.main.entity.PaymentUpi;
import com.ash.main.entity.UserDetailsEntity;
import com.ash.main.rpository.PaymentUpiRepository;
import com.ash.main.rpository.RepositoryO;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentUpiService {
	
	 @Value("${app.upload.dir}")
	 private String uploadDIR;

    @Autowired
    private PaymentUpiRepository upiRepository;

    @Autowired
    private RepositoryO userRepo;
    
    @Autowired
    private Cloudinary cloudinary;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    
    public PaymentUpi saveUpi(
            Long ownerId,
            String paymentType,
            String upiId,
            MultipartFile qrFile
    ) throws IOException {

        UserDetailsEntity owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        PaymentUpi upi = upiRepository
                .findByOwnerAndPaymentType(owner, paymentType)
                .orElse(new PaymentUpi());

        upi.setOwner(owner);
        upi.setPaymentType(paymentType);
        upi.setUpiId(upiId);

        if (qrFile != null && !qrFile.isEmpty()) {

            if (upi.getQrPublicId() != null && !upi.getQrPublicId().isEmpty()) {
                cloudinary.uploader().destroy(
                        upi.getQrPublicId(),
                        ObjectUtils.asMap("type", "authenticated")
                );
            }

            Map<String, Object> options = new HashMap<>();
            options.put("folder", "pg-manager/qr");
            options.put("type", "authenticated");
            options.put("invalidate", true);

            String uniquePublicId =
                    "owner_" + ownerId + "_" +
                    paymentType.toLowerCase() + "_" +
                    System.currentTimeMillis();

            options.put("public_id", uniquePublicId);

            Map upload = cloudinary.uploader()
                    .upload(qrFile.getBytes(), options);

            upi.setQrPublicId(upload.get("public_id").toString());
        }

        return upiRepository.save(upi); 
    }

    
    
    
    
    public PaymentUpi getUpiByOwnerAndType(Long ownerId, String paymentType) {

        UserDetailsEntity owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        PaymentUpi upi = upiRepository
                .findByOwnerAndPaymentType(owner, paymentType)
                .orElse(null);

        if (upi == null) return null;

        if (upi.getQrPublicId() != null && !upi.getQrPublicId().isEmpty()) {

            String signedUrl = cloudinaryService
                    .generateAuthenticatedUrl(upi.getQrPublicId());

            upi.setQrPublicId(signedUrl);
        }

        return upi;
    }

    
    
    
    
    
}
