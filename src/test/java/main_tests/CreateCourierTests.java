package main_tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import models.CreateCourier;
import models.Login;
import models.LoginResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    Response responseCreateCourier;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @After
    public void clear() {
        if (responseCreateCourier.statusCode() == SC_CREATED){
            // Логин курьера в системе
            Login loginObject = createObjectLogin(login, password); // создаем объект логин (сериализация)
            Response responseLoginCourier = client.CourierClient.sendPostRequestV1CourierLogin(loginObject); // отправляем POST запрос
            LoginResponse loginResponse = deserialization(responseLoginCourier); // десериализация
            int courierId = loginResponse.getId(); // получаем id курьера

            // Удаление курьера
            Response responseDeleteCourier = client.CourierClient.sendDeleteRequestV1Courier(courierId); // удаляем курьера по id
            checkStatusCodeAndExpectedResult(responseDeleteCourier, SC_OK, EXPECTED_RESULT_TRUE); // проверяем статус код и ответ (true)
        }
    }

    @Test
    @DisplayName("Создание курьера")
    public void createAndDeleteNewCourier() {
        // Создание курьера
        CreateCourier courier = createObjectCourier(login, password, firstName); // создаем объект курьер (сериализация)
        responseCreateCourier = client.CourierClient.sendPostRequestV1Courier(courier); // отправляем POST запрос
        checkStatusCodeAndExpectedResult(responseCreateCourier, SC_CREATED, EXPECTED_RESULT_TRUE); // проверяем код статуса и ответ (true)
    }

    @Test
    @DisplayName("Проверка получения ошибки \"Недостаточно данных для создания учетной записи\"")
    public void createCourierWithoutPasswordReturned400() {
        CreateCourier courier = createObjectCourier(NINJA_LOGIN, EMPTY_PASSWORD, firstName);
        responseCreateCourier = client.CourierClient.sendPostRequestV1Courier(courier);
        checkStatusCodeAndErrorMessage(responseCreateCourier, SC_BAD_REQUEST, NOT_ENOUGH_DATA);
    }

    @Test
    @DisplayName("Проверка получения ошибки \"Этот логин уже используется\"")
    public void createCourierAlreadyExistsReturned409 () {
        CreateCourier courier = createObjectCourier(NINJA_LOGIN, password, firstName);
        responseCreateCourier = client.CourierClient.sendPostRequestV1Courier(courier);
        // Тест падает, т.к. в документации другой ожидемый текст
        checkStatusCodeAndErrorMessage(responseCreateCourier, SC_CONFLICT, THIS_LOGIN_IS_ALREADY_IN_USE);
    }


    // Степы
    @Step("Создание объекта курьер")
    public CreateCourier createObjectCourier(String login, String password, String firstName){
        return new CreateCourier(login, password, firstName);
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

    @Step("Десериализация ответа на логин курьера")
    public LoginResponse deserialization(Response responseLoginCourier){
        return responseLoginCourier.as(LoginResponse.class);
    }
}
