package main_tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import models.CreateCourier;
import models.Login;
import models.LoginResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;

public class CreateCourierTests {
    private String login = RandomStringUtils.randomAlphanumeric(5);
    private String password = RandomStringUtils.randomNumeric(4);
    private String firstName = RandomStringUtils.randomAlphabetic(6);

    private final static String NOT_ENOUGH_DATA = "Недостаточно данных для создания учетной записи";
    private final static String THIS_LOGIN_IS_ALREADY_IN_USE = "Этот логин уже используется";
    private final static boolean EXPECTED_RESULT_TRUE = true;
    private final static String NINJA_LOGIN = "ninja";
    private final static String EMPTY_PASSWORD = "";

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Создание курьера")
    public void createAndDeleteNewCourier() {
        // Создание курьера
        CreateCourier courier = createObjectCourier(login, password, firstName); // создаем объект курьер (сериализация)
        Response responseCreateCourier = sendPostRequestV1Courier(courier); // отправляем POST запрос
        checkStatusCodeAndExpectedResult(responseCreateCourier, SC_CREATED, EXPECTED_RESULT_TRUE); // проверяем код статуса и ответ (true)

        // Логин курьера в системе
        Login loginObject = createObjectLogin(login, password); // создаем объект логин (сериализация)
        Response responseLoginCourier = sendPostRequestV1CourierLogin(loginObject); // отправляем POST запрос
        LoginResponse loginResponse = deserialization(responseLoginCourier); // десериализация
        int courierId = loginResponse.getId(); // получаем id курьера

        // Удаление курьера
        Response responseDeleteCourier = sendDeleteRequestV1Courier(courierId); // удаляем курьера по id
        checkStatusCodeAndExpectedResult(responseDeleteCourier, SC_OK, EXPECTED_RESULT_TRUE); // проверяем статус код и ответ (true)
    }

    @Test
    @DisplayName("Проверка получения ошибки \"Недостаточно данных для создания учетной записи\"")
    public void createCourierWithoutPasswordReturned400() {
        CreateCourier courier = createObjectCourier(NINJA_LOGIN, EMPTY_PASSWORD, firstName);
        Response response = sendPostRequestV1Courier(courier);
        checkStatusCodeAndErrorMessage(response, SC_BAD_REQUEST, NOT_ENOUGH_DATA);
    }

    @Test
    @DisplayName("Проверка получения ошибки \"Этот логин уже используется\"")
    public void createCourierAlreadyExistsReturned409 () {
        CreateCourier courier = createObjectCourier(NINJA_LOGIN, password, firstName);
        Response response = sendPostRequestV1Courier(courier);
        // Тест падает, т.к. в документации другой ожидемый текст
        checkStatusCodeAndErrorMessage(response, SC_CONFLICT, THIS_LOGIN_IS_ALREADY_IN_USE);
    }


    // Степы
    @Step("Создание объекта курьер")
    public CreateCourier createObjectCourier(String login, String password, String firstName){
        return new CreateCourier(login, password, firstName);
    }

    @Step("Отправка POST запроса на /api/v1/courier")
    public Response sendPostRequestV1Courier(CreateCourier courier){
        return given().contentType(ContentType.JSON).body(courier).post("/api/v1/courier");
    }

    @Step("Проверка соответствия кода ответа и ожидаемого результата")
    public void checkStatusCodeAndExpectedResult(Response response, int statusCode, boolean expectedResult){
        response.then().assertThat().statusCode(statusCode).and().body("ok",equalTo(expectedResult));
    }

    @Step("Проверка соответствия кода ответа и текста ошибки")
    public void checkStatusCodeAndErrorMessage(Response response, int statusCode, String errorMessage){
        response.then().assertThat().statusCode(statusCode).and().body("message",equalTo(errorMessage));
    }

    @Step("Создание объекта логин")
    public Login createObjectLogin(String login, String password){
        return new Login(login, password);
    }

    @Step("Отправка POST запроса на /api/v1/courier/login")
    public Response sendPostRequestV1CourierLogin(Login login){
        return given().contentType(ContentType.JSON).body(login).post("/api/v1/courier/login");
    }

    @Step("Десериализация ответа на логин курьера")
    public LoginResponse deserialization(Response responseLoginCourier){
        return responseLoginCourier.as(LoginResponse.class);
    }

    @Step("Отправка DELETE запроса на /api/v1/courier/id")
    public Response sendDeleteRequestV1Courier(int id){
        return given().contentType(ContentType.JSON).delete("/api/v1/courier/" + id);
    }
}
