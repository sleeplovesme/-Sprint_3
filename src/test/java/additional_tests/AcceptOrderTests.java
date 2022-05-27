package additional_tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import models.CreateCourier;
import models.CreateOrder;
import models.Login;
import models.LoginResponse;
import models.OrderResponse;
import models.OrdersTrackResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;

public class AcceptOrderTests {
    private String login = RandomStringUtils.randomAlphanumeric(5);
    private String password = RandomStringUtils.randomNumeric(4);
    private String firstName = RandomStringUtils.randomAlphabetic(6);

    private final String FIRST_NAME = "Naruto";
    private final String LAST_NAME = "Uchiha";
    private final String ADDRESS = "Konoha, 142 apt.";
    private final String METRO_STATION = "110";
    private final String PHONE = "+7 800 355 35 35";
    private final int RENT_TIME = 5;
    private final String DELIVERY_DATE = "2020-06-06";
    private final String COMMENT = "Saske, come back to Konoha";

    private final static String NINJA_LOGIN = "ninja";
    private final static String NINJA_PASSWORD = "1234";
    private final int ORDER_ID_62489 = 62489;
    private final int ORDER_ID_0 = 0;
    private final int COURIER_ID_0 = 0;
    private final String NOT_ENOUGH_DATA_TO_SEARCH = "Недостаточно данных для поиска";
    private final String NO_ORDER_WITH_THIS_ID = "Заказа с таким id не существует";
    private final String NO_COURIER_WITH_THIS_ID = "Курьера с таким id не существует";

    private final static boolean EXPECTED_RESULT_TRUE = true;
    private final static String[] COLOR = {"GREY"};

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Принять заказ")
    public void acceptOrder() {
        // Создание курьера
        CreateCourier courier = createObjectCourier(login, password, firstName); // создаем объект курьер (сериализация)
        Response responseCreateCourier = client.CourierClient.sendPostRequestV1Courier(courier); // отправляем POST запрос
        checkStatusCodeAndExpectedResult(responseCreateCourier, SC_CREATED, EXPECTED_RESULT_TRUE); // проверяем код статуса и ответ (true)

        // Логин курьера в системе
        Login loginObject = createObjectLogin(login, password); // создаем объект логин (сериализация)
        Response responseLoginCourier = client.CourierClient.sendPostRequestV1CourierLogin(loginObject); // отправляем POST запрос
        LoginResponse loginResponse = deserializationResponseLoginCourier(responseLoginCourier); // получаем id курьера из ответа
        int courierId = loginResponse.getId();

        // Создание заказа
        CreateOrder order = createObjectOrder(FIRST_NAME, LAST_NAME, ADDRESS, METRO_STATION, PHONE,
                RENT_TIME, DELIVERY_DATE, COMMENT, COLOR);
        Response responseCreateOrder = client.OrderClient.sendPostRequestV1Orders(order);
        OrderResponse orderResponse = deserializationResponseCreateOrder(responseCreateOrder);
        int orderTrack = orderResponse.getTrack();

        // Получить заказ по его номеру
        Response responseOrdersTrack = client.OrderClient.sendGetRequestV1OrdersTrack(orderTrack);
        OrdersTrackResponse ordersTrackResponse = deserializationResponseOrdersTrack(responseOrdersTrack);
        int orderId = ordersTrackResponse.getOrder().getId();

        // Принять заказ
        Response responseAcceptOrder = client.CourierClient.sendPutRequestV1OrdersAcceptWithOrderId(orderId, courierId);
        checkStatusCodeAndExpectedResult(responseAcceptOrder, SC_OK, EXPECTED_RESULT_TRUE);

        // Удаление курьера
        Response responseDeleteCourier = client.CourierClient.sendDeleteRequestV1Courier(courierId); // удаляем курьера по id
        checkStatusCodeAndExpectedResult(responseDeleteCourier, SC_OK, EXPECTED_RESULT_TRUE); // проверяем статус код и ответ (true)
    }

    // если не передать id курьера, запрос вернёт ошибку;
    @Test
    @DisplayName("Проверка получения ошибки \"Недостаточно данных для поиска\"")
    public void acceptOrderWithoutCourierIdReturnNotEnoughDataToSearch() {
        Response responseAcceptOrder = client.CourierClient.sendPutRequestV1OrdersAcceptWithoutCourierId(ORDER_ID_62489);
        checkStatusCodeAndErrorMessage(responseAcceptOrder, SC_BAD_REQUEST, NOT_ENOUGH_DATA_TO_SEARCH);
    }

    // если передать неверный id курьера, запрос вернёт ошибку;
    @Test
    @DisplayName("Проверка получения ошибки \"Курьера с таким id не существует\"")
    public void acceptOrderReturnNoCourierWithThisId() {
        Response responseAcceptOrder = client.CourierClient.sendPutRequestV1OrdersAcceptWithOrderId(ORDER_ID_62489, COURIER_ID_0);
        checkStatusCodeAndErrorMessage(responseAcceptOrder, SC_NOT_FOUND, NO_COURIER_WITH_THIS_ID);
    }

    // если не передать номер заказа, запрос вернёт ошибку;
    @Test
    @DisplayName("Проверка получения ошибки \"Недостаточно данных для поиска\"")
    public void acceptOrderWithoutOrderIdReturnNotEnoughDataToSearch() {
        // логинимся дефолтным курьером
        Login loginObject = createObjectLogin(NINJA_LOGIN, NINJA_PASSWORD); // создаем объект логин (сериализация)
        Response responseLoginCourier = client.CourierClient.sendPostRequestV1CourierLogin(loginObject); // отправляем POST запрос
        LoginResponse loginResponse = deserializationResponseLoginCourier(responseLoginCourier); // получаем id курьера из ответа
        int courierId = loginResponse.getId();
        Response responseAcceptOrder = client.CourierClient.sendPutRequestV1OrdersAcceptWithoutOrderId(courierId);
        checkStatusCodeAndErrorMessage(responseAcceptOrder, SC_BAD_REQUEST, NOT_ENOUGH_DATA_TO_SEARCH);
    }

    // если передать неверный номер заказа, запрос вернёт ошибку.
    @Test
    @DisplayName("Проверка получения ошибки \"Заказа с таким id не существует\"")
    public void acceptOrderWithInvalidOrderIdReturnNotEnoughDataToSearch() {
        // логинимся дефолтным курьером
        Login loginObject = createObjectLogin(NINJA_LOGIN, NINJA_PASSWORD); // создаем объект логин (сериализация)
        Response responseLoginCourier = client.CourierClient.sendPostRequestV1CourierLogin(loginObject); // отправляем POST запрос
        LoginResponse loginResponse = deserializationResponseLoginCourier(responseLoginCourier); // получаем id курьера из ответа
        int courierId = loginResponse.getId();
        Response responseAcceptOrder = client.CourierClient.sendPutRequestV1OrdersAcceptWithOrderId(ORDER_ID_0, courierId);
        checkStatusCodeAndErrorMessage(responseAcceptOrder, SC_NOT_FOUND, NO_ORDER_WITH_THIS_ID);
    }

    // Степы
    @Step("Создание объекта курьер")
    public CreateCourier createObjectCourier(String login, String password, String firstName) {
        return new CreateCourier(login, password, firstName);
    }

    @Step("Проверка соответствия кода ответа и ожидаемого результата")
    public void checkStatusCodeAndExpectedResult(Response response, int statusCode, boolean expectedResult) {
        response.then().assertThat().statusCode(statusCode).and().body("ok", equalTo(expectedResult));
    }

    @Step("Создание объекта логин")
    public Login createObjectLogin(String login, String password) {
        return new Login(login, password);
    }

    @Step("Десериализация ответа на логин курьера")
    public LoginResponse deserializationResponseLoginCourier(Response responseLoginCourier) {
        return responseLoginCourier.as(LoginResponse.class);
    }

    @Step("Десериализация ответа на создание заказа")
    public OrderResponse deserializationResponseCreateOrder(Response responseCreateOrder) {
        return responseCreateOrder.as(OrderResponse.class);
    }

    @Step("Десериализация ответа на получение заказа по его номеру")
    public OrdersTrackResponse deserializationResponseOrdersTrack(Response responseOrdersTrack) {
        return responseOrdersTrack.as(OrdersTrackResponse.class);
    }

    @Step("Создание объекта заказ")
    public CreateOrder createObjectOrder(String firstName, String lastName, String address, String metroStation, String phone,
                                         int rentTime, String deliveryDate, String comment, String[] color) {
        return new CreateOrder(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);
    }

    @Step("Проверка соответствия кода ответа и текста ошибки")
    public void checkStatusCodeAndErrorMessage(Response response, int statusCode, String errorMessage){
        response.then().assertThat().statusCode(statusCode).and().body("message",equalTo(errorMessage));
    }
}
