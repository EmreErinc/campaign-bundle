package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.SellerEntity;
import com.finartz.intern.campaignlogic.model.request.AddSellerRequest;
import com.finartz.intern.campaignlogic.model.response.SellerResponse;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SellerServiceImpl extends BaseServiceImpl implements SellerService {
  private SellerRepository sellerRepository;

  @Autowired
  public SellerServiceImpl(SellerRepository sellerRepository,
                           AccountRepository accountRepository,
                           CampaignRepository campaignRepository,
                           ItemRepository itemRepository,
                           SalesRepository salesRepository,
                           CartRepository cartRepository,
                           VariantRepository variantRepository,
                           VariantSpecRepository variantSpecRepository,
                           SpecDataRepository specDataRepository,
                           SpecDetailRepository specDetailRepository) {
    super(accountRepository,
        sellerRepository,
        campaignRepository,
        itemRepository,
        salesRepository,
        cartRepository,
        variantRepository,
        variantSpecRepository,
        specDataRepository,
        specDetailRepository);
    this.sellerRepository = sellerRepository;
  }

  @Override
  public SellerResponse addSeller(int accountId, AddSellerRequest request) {
    if (getRoleByAccountId(accountId).equals(Role.USER)) {
      throw new ApplicationContextException("You don't have permission for this operation");
    }

    return Converters
        .sellerEntityToSellerResponse(
            sellerRepository
                .save(Converters
                    .addSellerRequestToSellerEntity(accountId, request)));
  }

  @Override
  public SellerResponse getSeller(String sellerId) {
    Optional<SellerEntity> optionalSellerEntity = sellerRepository.findById(Integer.valueOf(sellerId));

    if (!optionalSellerEntity.isPresent()) {
      throw new ApplicationContextException("Seller Not Found");
    }

    return Converters.sellerEntityToSellerResponse(optionalSellerEntity.get());
  }
}
