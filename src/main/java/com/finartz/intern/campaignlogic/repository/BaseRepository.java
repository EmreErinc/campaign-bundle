package com.finartz.intern.campaignlogic.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface BaseRepository<T, String> extends CrudRepository<T, String>{
  Optional<T> findByName(String s);
}
