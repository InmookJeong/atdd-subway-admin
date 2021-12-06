package nextstep.subway.line;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.util.UrlConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // when
        // 지하철 역 생성 요청
        Long stationId1 = 지하철_역_아이디_가져오기(지하철_역_생성요청("사당역"));
        Long stationId2 = 지하철_역_아이디_가져오기(지하철_역_생성요청("신도림"));
        int distance = 0;

        // 지하철_노선_생성_요청
        LineRequest defaultLine = 지하철_노선요청_객체_가져오기("2호선", "bg-green-600", stationId1, stationId2, distance);
        ExtractableResponse<Response> response = 지하철_노선_생성요청(defaultLine);

        // then
        // 지하철_노선_생성됨
        응답_코드_검증(response, HttpStatus.CREATED.value());
        응답_헤더_정보_존재여부_검증(response);
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLine2() {
        // given
        // 지하철 역 생성 요청
        Long stationId1 = 지하철_역_아이디_가져오기(지하철_역_생성요청("사당역"));
        Long stationId2 = 지하철_역_아이디_가져오기(지하철_역_생성요청("신도림"));
        int distance = 0;

        // 지하철_노선_등록되어_있음
        LineRequest defaultLine = 지하철_노선요청_객체_가져오기("2호선", "bg-green-600", stationId1, stationId2, distance);
        지하철_노선_생성요청(defaultLine);

        // when
        // 지하철_노선_생성_요청
        ExtractableResponse<Response> response = 지하철_노선_생성요청(defaultLine);

        // then
        // 지하철_노선_생성_실패됨
        // INTERNAL_SERVER_ERROR : 500
        System.out.println("status : " + response.statusCode());
        응답_코드_검증(response, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        // 지하철 2호선 역 생성 요청
        Long stationId1 = 지하철_역_아이디_가져오기(지하철_역_생성요청("사당역"));
        Long stationId2 = 지하철_역_아이디_가져오기(지하철_역_생성요청("신도림"));
        Long stationId3 = 지하철_역_아이디_가져오기(지하철_역_생성요청("강남역"));
        Long stationId4 = 지하철_역_아이디_가져오기(지하철_역_생성요청("판교역"));
        int distance = 0;

        // 지하철_노선_등록되어_있음 : 2호선
        LineRequest defaultLine = 지하철_노선요청_객체_가져오기("2호선", "bg-green-600", stationId1, stationId2, distance);
        ExtractableResponse<Response> createLineResponse1 = 지하철_노선_생성요청(defaultLine);

        // 지하철_노선_등록되어_있음 : 신분당선
        ExtractableResponse<Response> createLineResponse2 = 지하철_노선_생성요청(지하철_노선요청_객체_가져오기("신분당선", "bg-red-600", stationId3, stationId4, distance));

        // when
        // 지하철_노선_목록_조회_요청
        ExtractableResponse<Response> response = 지하철_노선_목록_조회요청();

        // then
        // 지하철_노선_목록_응답됨
        // 지하철_노선_목록_포함됨
        응답_코드_검증(response, HttpStatus.OK.value());
        List<Long> expectedLineIds = Arrays.asList(createLineResponse1, createLineResponse2).stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
                .collect(Collectors.toList());
        List<Long> resultLineIds = response.jsonPath().getList(".", LineResponse.class).stream()
                .map(it -> it.getId())
                .collect(Collectors.toList());
        지하철_노선_목록_포함여부_검증(resultLineIds, expectedLineIds);
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void getLine() {
        // given
        // 지하철 역 생성 요청
        Long stationId1 = 지하철_역_아이디_가져오기(지하철_역_생성요청("사당역"));
        Long stationId2 = 지하철_역_아이디_가져오기(지하철_역_생성요청("신도림"));
        int distance = 0;

        // 지하철_노선_등록되어_있음
        LineRequest defaultLine = 지하철_노선요청_객체_가져오기("2호선", "bg-green-600", stationId1, stationId2, distance);
        ExtractableResponse<Response> createLineResponse = 지하철_노선_생성요청(defaultLine);

        // 생성 요청 Response로부터 Line 정보 가져오기.
        LineResponse createdLine = API_응답에서_노선_응답정보_가져오기(createLineResponse);

        // when
        // 지하철_노선_조회_요청
        ExtractableResponse<Response> getLineResponse = 지하철_노선_정보_조회요청(createdLine.getId());

        // then
        // 지하철_노선_응답됨
        응답_코드_검증(getLineResponse, HttpStatus.OK.value());
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        // 지하철 역 생성 요청
        Long stationId1 = 지하철_역_아이디_가져오기(지하철_역_생성요청("사당역"));
        Long stationId2 = 지하철_역_아이디_가져오기(지하철_역_생성요청("신도림"));
        int distance = 0;

        // 지하철_노선_등록되어_있음
        LineRequest defaultLine = 지하철_노선요청_객체_가져오기("2호선", "bg-green-600", stationId1, stationId2, distance);
        ExtractableResponse<Response> createLineResponse = 지하철_노선_생성요청(defaultLine);

        LineResponse createdLine = API_응답에서_노선_응답정보_가져오기(createLineResponse);

        Map<String, String> updateParams = new HashMap<>();
        updateParams.put("name", "1호선");
        updateParams.put("color", "bg-blue-600");

        // when
        // 지하철_노선_수정_요청
        ExtractableResponse<Response> updateLineResponse = 지하철_노선_수정요청(createdLine.getId(), updateParams);

        LineResponse updatedLine = API_응답에서_노선_응답정보_가져오기(updateLineResponse);

        // then
        // 지하철_노선_수정됨
        assertAll(
                () -> 응답_코드_검증(updateLineResponse, HttpStatus.OK.value()),
                () -> 수정된_노선_ID와_기존에_생성된_노선_ID가_같은지_검증(createdLine, updatedLine) ,
                () -> 수정된_노선_이름이_수정_요청한_이름과_같은지_검증(updatedLine, (String) updateParams.get("name")),
                () -> 수정된_노선_색상이_수정_요청한_색상과_같은지_검증(updatedLine, (String) updateParams.get("color"))
        );
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        // 지하철 역 생성 요청
        Long stationId1 = 지하철_역_아이디_가져오기(지하철_역_생성요청("사당역"));
        Long stationId2 = 지하철_역_아이디_가져오기(지하철_역_생성요청("신도림"));
        int distance = 0;

        // 지하철_노선_등록되어_있음
        LineRequest defaultLine = 지하철_노선요청_객체_가져오기("2호선", "bg-green-600", stationId1, stationId2, distance);
        ExtractableResponse<Response> createLineResponse = 지하철_노선_생성요청(defaultLine);

        LineResponse createdLine = API_응답에서_노선_응답정보_가져오기(createLineResponse);

        // when
        // 지하철_노선_제거_요청
        ExtractableResponse<Response> updateLineResponse = 지하철_노선_삭제요청(createdLine.getId());

        // then
        // 지하철_노선_삭제됨
        응답_코드_검증(updateLineResponse, HttpStatus.NO_CONTENT.value());
    }

    /**
     * 지하철 Line 생성 요청
     * @param line
     * @return
     */
    private ExtractableResponse<Response> 지하철_노선_생성요청(LineRequest line) {
        return RestAssured.given().log().all()
                .body(line)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post(UrlConstants.LINE_DEFAULT_URL)
                .then().log().all()
                .extract();
    }

    /**
     * 지하철 Line 목록 조회 요청
     * @return
     */
    private ExtractableResponse<Response> 지하철_노선_목록_조회요청() {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(UrlConstants.LINE_DEFAULT_URL)
                .then().log().all()
                .extract();
    }

    /**
     * 지하철 Line 정보 조회 요청
     * @param lineId
     * @return
     */
    private ExtractableResponse<Response> 지하철_노선_정보_조회요청(Long lineId) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(UrlConstants.LINE_DEFAULT_URL + "/" + lineId)
                .then().log().all()
                .extract();
    }

    /**
     * 지하철 Line 정보 수정 요청
     * @param lineId
     * @param updateParams
     * @return
     */
    private ExtractableResponse<Response> 지하철_노선_수정요청(Long lineId, Map<String, String> updateParams) {
        return RestAssured.given().log().all()
                .body(updateParams)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put(UrlConstants.LINE_DEFAULT_URL + "/" + lineId)
                .then().log().all()
                .extract();
    }

    /**
     * 지하철 Line 삭제
     * @param lineId
     * @return
     */
    private ExtractableResponse<Response> 지하철_노선_삭제요청(Long lineId) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete(UrlConstants.LINE_DEFAULT_URL + "/" + lineId)
                .then().log().all()
                .extract();
    }

    /**
     * 지하철 LineRequestion(객체) 가져오기
     * @param name
     * @param color
     * @return
     */
    private LineRequest 지하철_노선요청_객체_가져오기(String name, String color, Long upstreamId, Long donwStreamId, int distance) {
        return new LineRequest(name, color, upstreamId, donwStreamId, distance);
    }

    /**
     * 지하철 노선 생성,조회,삭제 등의 API 요청 후 응답 정보 가져오기
     * @param response
     * @return
     */
    private LineResponse API_응답에서_노선_응답정보_가져오기(ExtractableResponse<Response> response) {
        return response.jsonPath().getObject(".", LineResponse.class);
    }

    private ExtractableResponse<Response> 지하철_역_생성요청(String stationName) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("name", stationName);
        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post(UrlConstants.STATION_DEFAULT_URL)
                .then().log().all()
                .extract();
    }

    private Long 지하철_역_아이디_가져오기(ExtractableResponse<Response> response) {
        return response.jsonPath().getObject(".", StationResponse.class).getId();
    }

    /* API 검증 */
    private void 응답_코드_검증(ExtractableResponse<Response> response, int statusCode) {
        System.out.println("http status : " + response.statusCode());
        System.out.println("int statuscode : " + statusCode);
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    private void 응답_헤더_정보_존재여부_검증(ExtractableResponse<Response> response) {
        assertThat(response.header("Location")).isNotBlank();
    }

    private void 지하철_노선_목록_포함여부_검증(List<Long> resultLineIds, List<Long> expectedLineIds) {
        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    private void 수정된_노선_ID와_기존에_생성된_노선_ID가_같은지_검증(LineResponse createdLine, LineResponse updatedLine) {
        assertThat(updatedLine.getId()).isEqualTo(createdLine.getId());
    }

    private void 수정된_노선_이름이_수정_요청한_이름과_같은지_검증(LineResponse updatedLine, String requestName) {
        assertThat(updatedLine.getName()).isEqualTo(requestName);
    }

    private void 수정된_노선_색상이_수정_요청한_색상과_같은지_검증(LineResponse updatedLine, String requestColor) {
        assertThat(updatedLine.getColor()).isEqualTo(requestColor);
    }
}
