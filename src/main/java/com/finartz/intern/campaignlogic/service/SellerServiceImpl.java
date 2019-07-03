package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.SellerEntity;
import com.finartz.intern.campaignlogic.model.request.AddSellerRequest;
import com.finartz.intern.campaignlogic.model.response.SellerResponse;
import com.finartz.intern.campaignlogic.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SellerServiceImpl implements SellerService {
  private SellerRepository sellerRepository;

  @Autowired
  public SellerServiceImpl(SellerRepository sellerRepository) {
    this.sellerRepository = sellerRepository;
  }

  @Override
  public SellerResponse addSeller(String accountId, AddSellerRequest request) {
    //TODO should add privilege controls

    return Converters
        .sellerEntityToSellerResponse(
            sellerRepository
                .save(Converters
                    .addSellerRequestToSellerEntity(request)));
  }

  @Override
  public SellerResponse getSeller(String sellerId) {
    //TODO should add privilege controls

    Optional<SellerEntity> optionalSellerEntity = sellerRepository.findById(sellerId);

    if (!optionalSellerEntity.isPresent()) {
      throw new ApplicationContextException("Seller Not Found");
    }

    return Converters.sellerEntityToSellerResponse(optionalSellerEntity.get());
  }
}
