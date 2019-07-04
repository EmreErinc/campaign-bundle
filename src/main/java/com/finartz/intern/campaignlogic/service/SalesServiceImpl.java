package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.SalesEntity;
import com.finartz.intern.campaignlogic.model.request.SaleRequest;
import com.finartz.intern.campaignlogic.model.response.SaleResponse;
import com.finartz.intern.campaignlogic.repository.SalesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SalesServiceImpl implements SalesService {
  private SalesRepository salesRepository;

  @Autowired
  public SalesServiceImpl(SalesRepository salesRepository) {
    this.salesRepository = salesRepository;
  }

  @Override
  public SaleResponse addSale(int accountId, SaleRequest request) {
    //SalesEntity salesEntity = salesRepository.save(Converters.saleRequestToSaleEntity(request, accountId));

    List<Integer> saleIds = new ArrayList<>();

    request.getItems().forEach(saleItem ->
      saleIds.add(salesRepository.save(Converters.saleItemRequestToSaleEntity(saleItem, accountId)).getId())
    );

    //return Converters.saleEntityToSaleResponse(salesEntity);
    return SaleResponse.builder().saleIds(saleIds).build();
  }
}
