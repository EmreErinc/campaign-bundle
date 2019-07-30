# Campaign Bundle for e-commerce applications

This project for control and manage e-commerce applications "campaign bundle". And project includes base level e-commerce application skills.

  
**You can do with this application:**
 * Create user account
 * Create seller account
 * Add product to your seller account
 * Add variant section to product
 * Add campaign to your product
 * Add item to your cart
 * Update your cart item and item count
 * Remove product from your cart
 * Base level sale operation
 
  
> **When user add campaign item to cart**, application controls campaign prerequisites, calculate and add sufficient gift to user's cart. 
Additionally **when user want to list products**, application controls user's account for that he/she can view campaign or not.
Variant control mechanism controls per unique item on cart, not variant section.

***

# Project Includes:
 * Spring Boot
 * MySQL
 * Mongo DB
 * H2 (for testing)
 * Swagger
 * JWT
 * Unit Test (38)
 * Integration Test (22)
 
***

For run project with maven
```
mvn spring-boot:run
```

For build project
```
mvn clean install
```

For skip tests use `-Dmaven.test.skip=true` parameter
```
mvn clean install -Dmaven.test.skip=true
```  

***

**Projects runs on** [http://localhost:8080](http://localhost:8080)  
**Swagger runs on** [http://localhost:8080/swagger-ui.html#/](http://localhost:8080/swagger-ui.html#/)

Additionally i use for databases local docker containers. 

***
# Endpoint list  

|type|url                                 |description                  |token required|
|----|------------------------------------|-----------------------------|--------------|
|POST|localhost:8080/register             |user account register        |false         |
|POST|localhost:8080/seller/register      |seller account register      |false         |
|POST|localhost:8080/login                |login for user and seller    |false         |
|POST|localhost:8080/seller               |create seller                |true          |
|GET |localhost:8080/cart                 |get user cart                |true          |
|POST|localhost:8080/cart/add             |add item to user cart        |true          |
|POST|localhost:8080/cart/inc             |increment item count         |true          |
|POST|localhost:8080/cart/dec             |decrement item count         |true          |
|POST|localhost:8080/cart/remove          |remove item from cart        |true          |
|POST|localhost:8080/sale                 |sale of given users cart     |true          |
|POST|localhost:8080/campaign             |add campaign to given item   |true          |
|GET |localhost:8080/campaign/{cid}       |get details of campaign      |true          |
|GET |localhost:8080/campaign/seller/{sid}|get seller's campaigns       |true          |
|POST|localhost:8080/item                 |add product to seller account|true          |
|GET |localhost:8080/item                 |list products                |true/false    |
|GET |localhost:8080/item/seller/{sid}    |get given seller's products  |true/false    |
|GET |localhost:8080/item/{pid}           |get product details          |true/false    |
|GET |localhost:8080/stock/{pid}          |get given product's stock    |true          |
|POST|localhost:8080/stock                |add stock to given product   |true          |

> Get requests about **item context** required token for check user's product status. If user do not give a token, application knows its **guest user** and shows all products with campaigns without prerequisites.

***

You can view my presentation about this application [Campaign Bundle Presentation](https://drive.google.com/file/d/1xz1dMn45Fe0tVb49xy33yN-MIBhACH8x/view?usp=sharing)

***

You can view my old posts that includes how to configure docker on local machines;

- [Spring Boot - Docker - MongoDB Example](https://github.com/EmreErinc/spring-mongodb-docker-example)  
- [Spring Boot - Docker - Mysql Example](https://github.com/EmreErinc/spring-mysql-docker-example)
