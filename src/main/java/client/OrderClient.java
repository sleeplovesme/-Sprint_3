package client;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.CreateOrder;

import static io.restassured.RestAssured.given;

public class OrderClient {
    @Step("Отправка POST запроса на /api/v1/orders")
    public static Response sendPostRequestV1Orders(CreateOrder order){
        return given().contentType(ContentType.JSON).body(order).post("/api/v1/orders");
    }

    @Step("Отправка GET запроса на /api/v1/orders?nearestStation=[\"2\"]")
    public static Response sendGetRequestV1OrdersNearestStation(){
        return given().contentType(ContentType.JSON).get("/api/v1/orders?nearestStation=[\"2\"]");
    }

    @Step("Отправка GET запроса на /api/v1/orders/track")
    public static Response sendGetRequestV1OrdersTrack(int orderTrack) {
        return given().contentType(ContentType.JSON)
                .queryParam("t", orderTrack)
                .when()
                .get("/api/v1/orders/track");
    }
}
