package com.finartz.intern.campaignlogic.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BaseRepository<T, S> extends CrudRepository<T, S> {
  Optional<T> findById(int id);
}
