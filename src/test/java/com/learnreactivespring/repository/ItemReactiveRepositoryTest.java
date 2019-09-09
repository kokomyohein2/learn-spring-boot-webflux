package com.learnreactivespring.repository;

import com.learnreactivespring.document.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@RunWith(SpringRunner.class)
public class ItemReactiveRepositoryTest {

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    List<Item> itemList = Arrays.asList(
            new Item(null, "Samsung TV", 400.0),
            new Item(null, "LG TV", 299.99),
            new Item(null, "Apple Watch", 149.99),
            new Item("ABC", "Apple Airpods", 149.99));

    @Before
    public void setup() {

        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(itemList))
                .flatMap(itemReactiveRepository::save)
                .doOnNext((item -> {
                    System.out.println("Inserted Item is : " + item);
                }))
                .blockLast();
    }

    @Test
    public void getAllItems() {

        StepVerifier.create(itemReactiveRepository.findAll())
                .expectSubscription()
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void getItemById() {

        StepVerifier.create(itemReactiveRepository.findById("ABC"))
                .expectSubscription()
                .expectNextMatches(item -> item.getDescription().equalsIgnoreCase("apple airpods"))
                .verifyComplete();
    }

    @Test
    public void findItemByDescription() {
        StepVerifier.create(itemReactiveRepository.findByDescription("LG TV").log("findItemByDescription : "))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void saveItem() {

        Item item = new Item(null, "Google Home Mini", 30.00);
        Mono<Item> savedItem = itemReactiveRepository.save(item);

        StepVerifier.create(savedItem.log("saveItem : "))
                .expectSubscription()
                .expectNextMatches(item1 -> (item1.getId() != null && item1.getDescription().equalsIgnoreCase("google home mini")))
                .verifyComplete();

    }

    @Test
    public void updateItem() {

        double newPrice = 32.98;
        Mono<Item> updatedItem = itemReactiveRepository.findByDescription("LG TV").log("updateItem")
                .map(item -> {
                    item.setPrice(newPrice);
                    return item;
                })
                .flatMap(item -> {
                    return itemReactiveRepository.save(item);
                });

        StepVerifier.create(updatedItem)
                .expectSubscription()
                .expectNextMatches(item -> item.getPrice() == 32.98)
                .verifyComplete();
    }

    @Test
    public void deleteItemById() {

        Mono<Void> deletedItem = itemReactiveRepository.findById("ABC")
                .map(Item::getId)
                .flatMap((id) -> {
                    return itemReactiveRepository.deleteById(id);
                });

        StepVerifier.create(deletedItem.log())
                .expectSubscription()
                .verifyComplete();

        StepVerifier.create(itemReactiveRepository.findAll().log())
                .expectSubscription()
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void deleteItem() {

        Mono<Void> deletedItem = itemReactiveRepository.findByDescription("LG TV")
                .flatMap((item) -> {
                    return itemReactiveRepository.delete(item);
                });

        StepVerifier.create(deletedItem.log())
                .expectSubscription()
                .verifyComplete();

        StepVerifier.create(itemReactiveRepository.findAll().log())
                .expectSubscription()
                .expectNextCount(3)
                .verifyComplete();
    }
}
