package main_tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.notNullValue;

public class GetOrdersPageByPageTests {
    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Получение списка заказов")
    public void getOrdersNearestStationReturn200() {
        Response response = client.OrderClient.sendGetRequestV1OrdersNearestStation();
        checkStatusCodeAndOrdersIsNotNull(response);
    }

    // Степы
    @Step("Проверка соответствия кода ответа и orders не равно null")
    public void checkStatusCodeAndOrdersIsNotNull(Response response){
        response.then().assertThat().statusCode(SC_OK).and().body("orders", notNullValue());
    }
}
