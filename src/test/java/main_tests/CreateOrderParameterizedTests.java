package main_tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import models.CreateOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class) // раннер Parameterized
public class CreateOrderParameterizedTests {
    private final String FIRST_NAME = "Naruto";
    private final String LAST_NAME = "Uchiha";
    private final String ADDRESS = "Konoha, 142 apt.";
    private final String METRO_STATION = "110";
    private final String PHONE = "+7 800 355 35 35";
    private final int RENT_TIME = 5;
    private final String DELIVERY_DATE = "2020-06-06";
    private final String COMMENT = "Saske, come back to Konoha";

    private final static String [] TWO_COLORS = {"BLACK", "GREY"};
    private final static String [] ONE_COLOR = {"GREY"};
    private final static String [] EMPTY_COLOR = {""};

    private final String [] colors; // цвета
    private final int statusCode; // код

    // конструктор с параметрами
    public CreateOrderParameterizedTests(String[] colors, int statusCode) {
        this.colors = colors;
        this.statusCode = statusCode;
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    // метод для получения данных
    @Parameterized.Parameters
    public static Object[][] getParameters() {
        return new Object[][] {
                {TWO_COLORS, SC_CREATED},
                {ONE_COLOR, SC_CREATED},
                {EMPTY_COLOR, SC_CREATED},
        };
    }

    @Test
    @DisplayName("Создание заказа")
    public void createNewOrdersReturned201() {
        CreateOrder order = createObjectOrder(FIRST_NAME, LAST_NAME, ADDRESS, METRO_STATION, PHONE,
                RENT_TIME, DELIVERY_DATE, COMMENT, colors);
        Response response = sendPostRequestV1Orders(order);
        checkStatusCodeAndIdIsNotNull(response, statusCode);

        // Задание: Необходимые тестовые данные создаются перед тестом и удаляются после того, как он выполнится.
        // Но я не могу отменить заказ, т.к. не работает ручка PUT /api/v1/orders/cancel
    }

    // Степы
    @Step("Создание объекта заказ")
    public CreateOrder createObjectOrder(String firstName, String lastName, String address, String metroStation, String phone,
                                         int rentTime, String deliveryDate, String comment, String[] color){
        return new CreateOrder(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);
    }

    @Step("Отправка POST запроса на /api/v1/orders")
    public Response sendPostRequestV1Orders(CreateOrder order){
        return given().contentType(ContentType.JSON).body(order).post("/api/v1/orders");
    }

    @Step("Проверка соответствия кода ответа и track не равен null")
    public void checkStatusCodeAndIdIsNotNull(Response response, int statusCode){
        response.then().assertThat().statusCode(statusCode).and().body("track", notNullValue());
    }
}
