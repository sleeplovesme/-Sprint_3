package additional_tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;

public class DeleteCourierTests {
    private final static String NOT_ENOUGH_DATA = "Недостаточно данных для удаления курьера";
    private final static String NO_COURIER_WITH_THIS_ID = "Курьера с таким id нет";
    private final static String COURIER_ID = "0";

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    // Тест с успешным удалением курьера есть в файле CreateCourierTests. Сюда его не добавлял, т.к. это копипаст

    @Test
    @DisplayName("Проверка получения ошибки \"Недостаточно данных для удаления курьера\"")
    public void deleteCourierReturned400() {
        Response responseDeleteCourier = sendDeleteRequestV1Courier();
        // Тест падает, т.к. в документации ожидаем код 400
        checkStatusCodeAndErrorMessage(responseDeleteCourier, SC_BAD_REQUEST, NOT_ENOUGH_DATA); // проверяем статус код и ответ (true)
    }

    @Test
    @DisplayName("Проверка получения ошибки \"Курьера с таким id нет\"")
    public void deleteCourierReturned404() {
        Response responseDeleteCourier = sendDeleteRequestWithIdV1CourierId(COURIER_ID); // удаляем курьера по id
        // Тест падает, т.к. в документации другой ожидемый текст
        checkStatusCodeAndErrorMessage(responseDeleteCourier, SC_NOT_FOUND, NO_COURIER_WITH_THIS_ID); // проверяем статус код и ответ (true)
    }

    // Степы
    @Step("Отправка DELETE запроса (без id) на /api/v1/courier")
    public Response sendDeleteRequestV1Courier() {
        return given().contentType(ContentType.JSON).delete("/api/v1/courier");
    }

    @Step("Отправка DELETE запроса на /api/v1/courier/id")
    public Response sendDeleteRequestWithIdV1CourierId(String id) {
        return given().contentType(ContentType.JSON).delete("/api/v1/courier/" + id);
    }

    @Step("Проверка соответствия кода ответа и текста ошибки")
    public void checkStatusCodeAndErrorMessage(Response response, int statusCode, String errorMessage) {
        response.then().assertThat().statusCode(statusCode).and().body("message", equalTo(errorMessage));
    }
}
