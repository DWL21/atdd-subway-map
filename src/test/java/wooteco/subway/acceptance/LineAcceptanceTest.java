package wooteco.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import wooteco.subway.dao.LineDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.dto.LineResponse;
import wooteco.subway.dto.StationResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class LineAcceptanceTest extends AcceptanceTest {
    @BeforeEach
    public void clear() {
        LineDao.clearAll();
    }

    @DisplayName("노선을 생성한다.")
    @Test
    void createLine() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "1호선");
        params.put("color", "red");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/lines")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    @DisplayName("기존에 존재하는 노선 이름으로 노선을 생성한다.")
    @Test
    void createLineWithDuplicateName() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "1호선");
        params.put("color", "red");

        RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/lines")
                .then().log().all()
                .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/lines")
                .then()
                .log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        Map<String, String> params1 = new HashMap<>();
        params1.put("name", "1호선");
        params1.put("color", "red");

        ExtractableResponse<Response> createResponse1 = RestAssured.given().log().all()
                .body(params1)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/lines")
                .then().log().all()
                .extract();

        Map<String, String> params2 = new HashMap<>();
        params2.put("name", "2호선");
        params2.put("color", "blue");

        ExtractableResponse<Response> createResponse2 = RestAssured.given().log().all()
                .body(params2)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/lines")
                .then().log().all()
                .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .get("/lines")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<Long> expectedLineIds = Arrays.asList(createResponse1, createResponse2).stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
                .collect(Collectors.toList());
        List<Long> resultLineIds = response.jsonPath().getList(".", LineResponse.class).stream()
                .map(LineResponse::getId)
                .collect(Collectors.toList());
        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    @DisplayName("노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "1호선");
        params.put("color", "red");

        ExtractableResponse<Response> createResponse = RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/lines")
                .then().log().all()
                .extract();

        // when
        String uri = createResponse.header("Location");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .delete(uri)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("노선을 조회한다.")
    @Test
    void searchLine() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "1호선");
        params.put("color", "red");

        ExtractableResponse<Response> createResponse = RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/lines")
                .then().log().all()
                .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .get("/lines/1")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        String expectedLine = createResponse.body().asString();
        String resultLine = response.body().asString();

        assertThat(resultLine).isEqualTo(expectedLine);
    }

    @DisplayName("노선을 수정한다.")
    @Test
    void editLine() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "1호선");
        params.put("color", "red");

        RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/lines")
                .then().log().all()
                .extract();

        // when
        Map<String, String> editParams = new HashMap<>();
        editParams.put("name", "2호선");
        editParams.put("color", "blue");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(editParams)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put("/lines/1")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}
