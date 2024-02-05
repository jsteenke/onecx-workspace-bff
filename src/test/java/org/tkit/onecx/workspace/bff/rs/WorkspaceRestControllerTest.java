package org.tkit.onecx.workspace.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.*;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.workspace.bff.rs.controllers.WorkspaceRestController;

import gen.org.tkit.onecx.workspace.bff.clients.model.*;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceRestController.class)
class WorkspaceRestControllerTest extends AbstractTest {
    @InjectMockServerClient
    MockServerClient mockServerClient;

    @BeforeEach
    void resetMockserver() {
        mockServerClient.reset();
    }

    @Test
    void createWorkspace() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        request.setName("test");
        request.setCompanyName("company1");
        request.setDescription("description1");

        Workspace response = new Workspace();
        response.setName("test");
        response.setDescription("description1");
        response.setCompanyName("company1");

        mockServerClient.when(request().withPath("/internal/workspaces").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        CreateWorkspaceRequestDTO input = new CreateWorkspaceRequestDTO();
        WorkspaceDTO dto = new WorkspaceDTO();
        dto.setDescription("description1");
        dto.setCompanyName("company1");
        dto.setName("test");
        input.setResource(dto);
        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(CreateWorkspaceResponseDTO.class);

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(request.getName(), output.getResource().getName());
        Assertions.assertEquals(request.getDescription(), output.getResource().getDescription());
        Assertions.assertEquals(request.getCompanyName(), output.getResource().getCompanyName());
    }

    @Test
    void createWorkspaceFailTest() {
        CreateWorkspaceRequest data = new CreateWorkspaceRequest();

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(data)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        CreateWorkspaceRequestDTO input = new CreateWorkspaceRequestDTO();
        WorkspaceDTO updateCreateDTO = new WorkspaceDTO();
        input.setResource(updateCreateDTO);

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(problemDetailResponse.getErrorCode(), output.getErrorCode());
    }

    @Test
    void deleteWorkspaceTest() {

        String id = "test-id-1";

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + id).withMethod(HttpMethod.DELETE))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .delete("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void getWorkspaceByIdTest() {

        Workspace data = new Workspace();
        data.setId("test-id-1");
        data.setName("test-name");
        data.setDescription("this is a test workspace");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + data.getId()).withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", data.getId())
                .get("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetWorkspaceResponseDTO.class);

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(data.getId(), output.getResource().getId());
        Assertions.assertEquals(data.getName(), output.getResource().getName());
    }

    @Test
    void getWorkspaceByIdNotFoundTest() {
        String notFoundId = "notFound";
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + notFoundId).withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", notFoundId)
                .get("/{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        Assertions.assertNotNull(output);
    }

    @Test
    void getWorkspaceByNameTest() {
        Workspace data = new Workspace();
        data.setId("test-id-1");
        data.setName("test-name");
        data.setDescription("this is a test workspace");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/name/" + data.getName()).withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("name", data.getName())
                .get("/name/{name}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetWorkspaceResponseDTO.class);

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(data.getId(), output.getResource().getId());
        Assertions.assertEquals(data.getName(), output.getResource().getName());
    }

    @Test
    void getWorkspaceByNameNotFoundTest() {
        String notFoundName = "notFound";
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/name/" + notFoundName).withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("name", notFoundName)
                .get("/name/{name}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        Assertions.assertNotNull(output);
    }

    @Test
    void searchWorkspaceByCriteriaTest() {
        WorkspaceSearchCriteria criteria = new WorkspaceSearchCriteria();
        criteria.setPageNumber(1);
        criteria.setName("test");
        criteria.setPageSize(1);

        WorkspaceAbstract w1 = new WorkspaceAbstract();
        w1.setName("test");

        WorkspacePageResult data = new WorkspacePageResult();
        data.setNumber(1);
        data.setSize(1);
        data.setTotalElements(1L);
        data.setTotalPages(1L);
        data.setStream(List.of(w1));

        SearchWorkspacesRequestDTO searchWorkspaceRequestDTO = new SearchWorkspacesRequestDTO();
        searchWorkspaceRequestDTO.setPageNumber(1);
        searchWorkspaceRequestDTO.setPageSize(1);
        searchWorkspaceRequestDTO.setName("test");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(searchWorkspaceRequestDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(SearchWorkspacesResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(data.getSize(), output.getSize());
        Assertions.assertEquals(data.getStream().size(), output.getStream().size());
        Assertions.assertEquals(data.getStream().get(0).getName(), output.getStream().get(0).getName());
    }

    @Test
    void searchWorkspaceByEmptyCriteriaTest() {

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/search").withMethod(HttpMethod.POST))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponse.class);

        Assertions.assertNotNull(output);
    }

    @Test
    void updateWorkspaceTest() {
        String testId = "testId";
        UpdateWorkspaceRequest updateWorkspace = new UpdateWorkspaceRequest();
        updateWorkspace.setDescription("test-desc");
        updateWorkspace.setName("test-workspace");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + testId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateWorkspace)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        UpdateWorkspaceRequestDTO input = new UpdateWorkspaceRequestDTO();
        WorkspaceDTO dto = new WorkspaceDTO();
        dto.setDescription("test-desc");
        dto.setName("test-workspace");
        input.setResource(dto);

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", testId)
                .body(input)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Assertions.assertNotNull(output);
    }

    @Test
    void updateWorkspaceFailTest() {
        String testId = "testId";
        UpdateWorkspaceRequest updateWorkspace = new UpdateWorkspaceRequest();
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + testId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateWorkspace)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse))
                        .withContentType(MediaType.APPLICATION_JSON));

        WorkspaceDTO updateCreateDTO = new WorkspaceDTO();
        UpdateWorkspaceRequestDTO input = new UpdateWorkspaceRequestDTO();
        input.setResource(updateCreateDTO);

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", testId)
                .body(input)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(problemDetailResponse.getErrorCode(), output.getErrorCode());
    }

    @Test
    void exportWorkspaceWithoutMenuTest() {

        ExportWorkspacesRequest request = new ExportWorkspacesRequest();
        request.setNames(Set.of("testWorkspace"));

        WorkspaceSnapshot snapshot = new WorkspaceSnapshot();
        EximWorkspace eximWorkspace = new EximWorkspace();
        eximWorkspace.setName("testWorkspace");
        eximWorkspace.setBaseUrl("/test");
        snapshot.setWorkspaces(Map.of("testWorkspace", eximWorkspace));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/exim/v1/workspace/export").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(snapshot)));

        ExportWorkspacesRequestDTO requestDTO = new ExportWorkspacesRequestDTO();
        requestDTO.setNames(Set.of("testWorkspace"));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/export")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(WorkspaceSnapshotDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(output.getWorkspaces().get("testWorkspace").getName(),
                eximWorkspace.getName());
    }

    @Test
    void exportWorkspaceIncludingMenuTest() {

        ExportWorkspacesRequest request = new ExportWorkspacesRequest();
        request.setNames(Set.of("testWorkspace", "error"));

        WorkspaceSnapshot workspaceSnapshot = new WorkspaceSnapshot();
        EximWorkspace eximWorkspace = new EximWorkspace();
        eximWorkspace.setName("testWorkspace");
        eximWorkspace.setBaseUrl("/test");
        Map<String, EximWorkspace> workspaceMap = new HashMap<>();
        workspaceMap.put("testWorkspace", eximWorkspace);
        EximWorkspace errorWorkspace = new EximWorkspace();
        errorWorkspace.setName("error");
        errorWorkspace.setBaseUrl("/error");
        workspaceMap.put("error", eximWorkspace);
        workspaceSnapshot.setWorkspaces(workspaceMap);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/exim/v1/workspace/export").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(workspaceSnapshot)));

        MenuSnapshot menuSnapshot = new MenuSnapshot();
        EximMenuStructure menu = new EximMenuStructure();
        EximWorkspaceMenuItem menuItem = new EximWorkspaceMenuItem();
        menuItem.setName("test");
        menuItem.setWorkspaceName("testWorkspace");
        menuItem.setKey("testKey");
        menu.setMenuItems(List.of(menuItem));
        menuSnapshot.setMenu(menu);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/exim/v1/workspace/" + eximWorkspace.getName() + "/menu/export")
                        .withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(menuSnapshot)));

        ExportWorkspacesRequestDTO requestDTO = new ExportWorkspacesRequestDTO();
        requestDTO.setNames(Set.of("testWorkspace", "error"));
        requestDTO.setIncludeMenus(true);

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/export")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(WorkspaceSnapshotDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(output.getWorkspaces().get("testWorkspace").getName(),
                eximWorkspace.getName());
        Assertions.assertNotNull(output.getWorkspaces().get("testWorkspace").getMenu());
        Assertions.assertEquals("testKey", output.getWorkspaces()
                .get("testWorkspace").getMenu().getMenu().getMenuItems().get(0).getKey());

    }

    @Test
    void importWorkspacesTest() {
        WorkspaceSnapshot workspaceSnapshot = new WorkspaceSnapshot();
        EximWorkspace eximWorkspace = new EximWorkspace();
        eximWorkspace.setName("test");
        eximWorkspace.setBaseUrl("/test");
        EximWorkspace eximWorkspace2 = new EximWorkspace();
        eximWorkspace2.setName("test2");
        eximWorkspace2.setBaseUrl("/test2");
        Map<String, EximWorkspace> eximWorkspaceMap = new HashMap<>();
        eximWorkspaceMap.put("test", eximWorkspace);
        eximWorkspaceMap.put("test2", eximWorkspace2);
        workspaceSnapshot.setWorkspaces(eximWorkspaceMap);

        ImportWorkspaceResponse workspaceResponse = new ImportWorkspaceResponse();
        Map<String, ImportResponseStatus> responseStatusMap = new HashMap<>();
        responseStatusMap.put("test", ImportResponseStatus.CREATED);
        responseStatusMap.put("test2", ImportResponseStatus.CREATED);
        workspaceResponse.setWorkspaces(responseStatusMap);

        MenuSnapshot menuSnapshot = new MenuSnapshot();
        EximMenuStructure eximMenuStructure = new EximMenuStructure();
        EximWorkspaceMenuItem menuitem = new EximWorkspaceMenuItem();
        menuitem.setKey("test");
        menuitem.setWorkspaceName("test");

        eximMenuStructure.setMenuItems(List.of(menuitem));
        menuSnapshot.setMenu(eximMenuStructure);

        ImportMenuResponse menuResponse = new ImportMenuResponse();
        menuResponse.setStatus(ImportResponseStatus.CREATED);
        menuResponse.setId("test");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/exim/v1/workspace/import").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(workspaceSnapshot)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(workspaceResponse)));

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/exim/v1/workspace/" + eximWorkspace.getName() + "/menu/import")
                        .withBody(JsonBody.json(menuSnapshot))
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(menuResponse)));

        mockServerClient
                .when(request().withPath("/exim/v1/workspace/" + eximWorkspace2.getName() + "/menu/import")
                        .withBody(JsonBody.json(menuSnapshot))
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode()));

        WorkspaceSnapshotDTO workspaceSnapshotDTO = new WorkspaceSnapshotDTO();
        EximWorkspaceDTO eximWorkspaceDTO = new EximWorkspaceDTO();
        eximWorkspaceDTO.setName("test");
        eximWorkspaceDTO.setBaseUrl("/test");

        EximWorkspaceDTO eximWorkspaceDTO2 = new EximWorkspaceDTO();
        eximWorkspaceDTO2.setName("test2");
        eximWorkspaceDTO2.setBaseUrl("/test2");

        MenuSnapshotDTO menuSnapshotDTO = new MenuSnapshotDTO();
        EximMenuStructureDTO eximMenuStructureDTO = new EximMenuStructureDTO();
        EximWorkspaceMenuItemDTO itemDTO = new EximWorkspaceMenuItemDTO();
        itemDTO.setWorkspaceName("test");
        itemDTO.setKey("test");

        MenuSnapshotDTO menuSnapshotDTO2 = new MenuSnapshotDTO();
        EximMenuStructureDTO eximMenuStructureDTO2 = new EximMenuStructureDTO();
        EximWorkspaceMenuItemDTO itemDTO2 = new EximWorkspaceMenuItemDTO();
        itemDTO2.setWorkspaceName("test2");
        itemDTO2.setKey("test2");

        eximMenuStructureDTO.setMenuItems(List.of(itemDTO));
        menuSnapshotDTO.setMenu(eximMenuStructureDTO);
        eximWorkspaceDTO.setMenu(menuSnapshotDTO);

        eximMenuStructureDTO2.setMenuItems(List.of(itemDTO2));
        menuSnapshotDTO2.setMenu(eximMenuStructureDTO2);
        eximWorkspaceDTO2.setMenu(menuSnapshotDTO2);
        Map<String, EximWorkspaceDTO> workspaceDTOMap = new HashMap<>();
        workspaceDTOMap.put("test", eximWorkspaceDTO);
        workspaceDTOMap.put("test2", eximWorkspaceDTO2);
        workspaceSnapshotDTO.setWorkspaces(workspaceDTOMap);

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(workspaceSnapshotDTO)
                .post("/import")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ImportWorkspaceResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(ImportResponseStatusDTO.CREATED, output.getWorkspaces().get("test"));
        Assertions.assertEquals(ImportResponseStatusDTO.CREATED, output.getWorkspaces().get("test2"));
        Assertions.assertEquals(ImportResponseStatusDTO.CREATED, output.getMenus().get("test"));
        Assertions.assertEquals(ImportResponseStatusDTO.ERROR, output.getMenus().get("test2"));

    }

    @Test
    void getThemeNamesTest() {

        ThemeInfoList infoList = new ThemeInfoList();
        ThemeInfo theme1 = new ThemeInfo();
        theme1.setName("theme1");
        theme1.setDescription("theme1");

        ThemeInfo theme2 = new ThemeInfo();
        theme2.setName("theme2");
        theme2.setDescription("theme2");

        infoList.setThemes(List.of(theme1, theme2));

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/themes/info")
                        .withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(infoList)));

        ArrayList<String> themeNames = new ArrayList<>();
        themeNames.add("theme1");
        themeNames.add("theme2");

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(themeNames)
                .get("/themes")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(List.class);

        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains("theme1"));
        Assertions.assertTrue(output.contains("theme2"));

    }
}