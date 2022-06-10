package client;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.CreateCourier;
import models.Login;

import static io.restassured.RestAssured.given;

public class CourierClient {
    @Step("Отправка POST запроса на /api/v1/courier")
    public static Response sendPostRequestV1Courier(CreateCourier courier){
        return given().contentType(ContentType.JSON).body(courier).post("/api/v1/courier");
    }

    @Step("Отправка POST запроса на /api/v1/courier/login")
    public static Response sendPostRequestV1CourierLogin(Login login){
        return given().contentType(ContentType.JSON).body(login).post("/api/v1/courier/login");
    }

    @Step("Отправка DELETE запроса на /api/v1/courier/id")
    public static Response sendDeleteRequestV1Courier(int id){
        return given().contentType(ContentType.JSON).delete("/api/v1/courier/" + id);
    }

    @Step("Отправка DELETE запроса (без id) на /api/v1/courier")
    public static Response sendDeleteRequestWithoutCourierIdV1Courier() {
        return given().contentType(ContentType.JSON).delete("/api/v1/courier");
    }

    @Step("Отправка PUT запроса с queryParam на /api/v1/courier/id")
    public static Response sendPutRequestV1OrdersAcceptWithOrderId(int orderId, int courierId) {
        return given().contentType(ContentType.JSON)
                .queryParam("courierId", courierId)
                .when()
                .put("/api/v1/orders/accept/" + orderId);
    }

    @Step("Отправка PUT запроса без queryParam на /api/v1/courier/id")
    public static Response sendPutRequestV1OrdersAcceptWithoutCourierId(int orderId) {
        return given().contentType(ContentType.JSON)
                .when()
                .put("/api/v1/orders/accept/" + orderId);
    }

    @Step("Отправка PUT запроса без queryParam без orderId на /api/v1/courier/id")
    public static Response sendPutRequestV1OrdersAcceptWithoutOrderId(int courierId) {
        return given().contentType(ContentType.JSON)
                .when()
                .put("/api/v1/orders/accept/courierId=" + courierId);
    }

    @Step("Отправка GET запроса на /api/v1/courier/id")
    public static Response sendGetRequestV1Courier(){
        return given().contentType(ContentType.JSON).get("/api/v1/orders/track");
    }
}
