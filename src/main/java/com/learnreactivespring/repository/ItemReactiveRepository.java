package com.learnreactivespring.repository;

import com.learnreactivespring.document.Item;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public abstract class ItemReactiveRepository implements ReactiveMongoRepository<Item, String> {
}
