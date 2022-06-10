package main_tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import models.Login;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginTests {
    private final static String NINJA_LOGIN = "ninja";
    private final static String NINJA_PASSWORD = "1234";
    private final static String EMPTY_LOGIN = "";
    private final static String ACCOUNT_NOT_FOUND = "Учетная запись не найдена";
    private final static String INSUFFICIENT_LOGIN_DATA = "Недостаточно данных для входа";

    private String login = RandomStringUtils.randomAlphanumeric(5);
    private String password = RandomStringUtils.randomNumeric(4);

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Логин курьера в системе")
    public void loginReturned200() {
        Login login = createObjectLogin(NINJA_LOGIN, NINJA_PASSWORD);
        Response response = client.CourierClient.sendPostRequestV1CourierLogin(login);
        checkStatusCodeAndIdIsNotNull(response, SC_OK);
    }

    @Test
    @DisplayName("Проверка получения ошибки \"Учетная запись не найдена\"")
    public void loginWithWrongLoginReturned404() {
        Login loginCourier = createObjectLogin(login, password);
        Response response = client.CourierClient.sendPostRequestV1CourierLogin(loginCourier);
        checkStatusCodeAndErrorMessage(response, SC_NOT_FOUND, ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("Проверка получения ошибки \"Недостаточно данных для входа\"")
    public void loginWithoutLoginReturned400() {
        Login login = createObjectLogin(EMPTY_LOGIN, password);
        Response response = client.CourierClient.sendPostRequestV1CourierLogin(login);
        checkStatusCodeAndErrorMessage(response, SC_BAD_REQUEST, INSUFFICIENT_LOGIN_DATA);
    }

    // Степы
    @Step("Создание объекта логин")
    public Login createObjectLogin(String login, String password){
        return new Login(login, password);
    }

    @Step("Проверка соответствия кода ответа и id не равен null")
    public void checkStatusCodeAndIdIsNotNull(Response response, int statusCode){
        response.then().assertThat().statusCode(statusCode).and().body("id", notNullValue());
    }

    @Step("Проверка соответствия кода ответа и текста ошибки")
    public void checkStatusCodeAndErrorMessage(Response response, int statusCode, String errorMessage){
        response.then().assertThat().statusCode(statusCode).and().body("message",equalTo(errorMessage));
    }
}
