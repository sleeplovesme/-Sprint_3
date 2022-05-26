package additional_tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.hamcrest.Matchers.equalTo;

public class GetOrderByTrackNumberTests {
    private final int ORDER_TRACK_0 = 0;
    private final String NOT_ENOUGH_DATA = "Недостаточно данных для поиска";
    private final String ORDER_NOT_FOUND = "Заказ не найден";

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    // Тест на получение заказа по его номеру есть в файле AcceptOrderTests. Сюда его не добавлял, т.к. это копипаст

    @Test
    @DisplayName("Проверка получения ошибки \"Недостаточно данных для поиска\"")
    public void getOrderByTrackNumberWithoutTrackNumberReturned400() {
        Response responseOrdersTrack = sendGetRequestV1Courier();
        checkStatusCodeAndErrorMessage(responseOrdersTrack, SC_BAD_REQUEST, NOT_ENOUGH_DATA);
    }

    @Test
    @DisplayName("Проверка получения ошибки \"Заказ не найден\"")
    public void getOrderByTrackNumberTrackNumber0Returned404() {
        Response responseOrdersTrack = sendGetRequestV1OrdersTrack(ORDER_TRACK_0);
        checkStatusCodeAndErrorMessage(responseOrdersTrack, SC_NOT_FOUND, ORDER_NOT_FOUND);
    }

    // Степы
    @Step("Отправка DELETE запроса на /api/v1/courier/id")
    public Response sendGetRequestV1Courier(){
        return given().contentType(ContentType.JSON).get("/api/v1/orders/track");
    }

    @Step("Отправка GET запроса на /api/v1/orders/track")
    public Response sendGetRequestV1OrdersTrack(int orderTrack) {
        return given().contentType(ContentType.JSON)
                .queryParam("t", orderTrack)
                .when()
                .get("/api/v1/orders/track");
    }

    @Step("Проверка соответствия кода ответа и текста ошибки")
    public void checkStatusCodeAndErrorMessage(Response response, int statusCode, String errorMessage){
        response.then().assertThat().statusCode(statusCode).and().body("message",equalTo(errorMessage));
    }
}
