/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */

package org.acumos.be.test.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acumos.cds.domain.MLPCatalog;
import org.acumos.cds.domain.MLPPeer;
import org.acumos.cds.domain.MLPRole;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.portal.be.common.ConfigConstants;
import org.acumos.portal.be.service.impl.CatalogServiceImpl;
import org.acumos.portal.be.transport.CatalogSearchRequest;
import org.acumos.portal.be.transport.MLCatalog;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = org.acumos.portal.be.Application.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
		ConfigConstants.cdms_client_url + "=http://localhost:8000/ccds",
		ConfigConstants.cdms_client_username + "=ccds_test", ConfigConstants.cdms_client_password + "=ccds_test" })
public class CatalogServiceImplTest {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8000));

	@Autowired
	private CatalogServiceImpl catalogService;
	
	private static final String VARIABLE = "/%s";
	private static final String CCDS_PATH = "/ccds";
	private static final String CATALOG_PATH = "/catalog";
	private static final String CCDS_CATALOG_PATH = CCDS_PATH + CATALOG_PATH;
	private static final String PAGE_REQUEST_PARAMS = "page=0&size=9&sort=modified,DESC";
	private static final String SEARCH_PATH = CCDS_CATALOG_PATH + "/search?selfPublish=false&_j=a&" + PAGE_REQUEST_PARAMS;
	private static final String CATALOG_ID_PATH = CCDS_CATALOG_PATH + VARIABLE;
	private static final String PEER_ACCESS_PATH = CCDS_PATH + "/access/peer" + VARIABLE + CATALOG_PATH;
	private static final String ADD_DROP_PEER_ACCESS_PATH = PEER_ACCESS_PATH + VARIABLE;
	private static final String SOLUTION_PATH = "/solution";
	private static final String SOLUTION_ID_PATH = SOLUTION_PATH + VARIABLE;
	private static final String CATALOG_SOLUTION_COUNT_PATH = CATALOG_ID_PATH + SOLUTION_PATH + "/count";
	private static final String CATALOG_SOLUTION_PATH = CCDS_CATALOG_PATH + SOLUTION_PATH;
	private static final String CATALOG_SOLUTION_ID_PATH = CCDS_CATALOG_PATH + SOLUTION_ID_PATH;
	private static final String ADD_DROP_SOLUTION_PATH = CATALOG_ID_PATH + SOLUTION_ID_PATH;
	private static final String USER_FAVORITE_PATH = "/user" + VARIABLE + "/favorite";
	private static final String GET_USER_FAVORITES_PATH = CCDS_CATALOG_PATH + USER_FAVORITE_PATH;
	private static final String ADD_DROP_FAVORITES_PATH = CATALOG_ID_PATH + USER_FAVORITE_PATH;
	private static final String PEER_PATH = "/peer";
	private static final String CATALOG_ACCESS_PATH = CCDS_PATH + "/access/catalog" + VARIABLE + PEER_PATH;
	
	@Test
	public void searchCatalogsTest() {
		CatalogSearchRequest catalogRequest = new CatalogSearchRequest();
		catalogRequest.setSelfPublish("false");
		catalogRequest.setPageRequest(getTestRestPageRequest());

		stubFor(get(urlEqualTo(SEARCH_PATH)).willReturn(aResponse().withStatus(HttpStatus.SC_OK)
				.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.withBody("{\"content\":" + "[{\"created\": \"2019-04-05T20:47:03Z\","
						+ "\"modified\": \"2019-04-05T20:47:03Z\","
						+ "\"catalogId\": \"12345678-abcd-90ab-cdef-1234567890ab\"," + "\"accessTypeCode\": \"PB\","
						+ "\"selfPublish\": false," + "\"name\": \"Test catalog\"," + "\"publisher\": \"Acumos\","
						+ "\"description\": null," + "\"origin\": null," + "\"url\": \"http://localhost\"}],"
						+ "\"last\":true," + "\"totalPages\":1," + "\"totalElements\":1," + "\"size\":9,"
						+ "\"number\":0," + "\"sort\":[{\"direction\":\"DESC\"," + "\"property\":\"modified\","
						+ "\"ignoreCase\":false," + "\"nullHandling\":\"NATIVE\"," + "\"ascending\":false,"
						+ "\"descending\":true}]," + "\"numberOfElements\":1," + "\"first\":true}")));

		stubFor(get(urlEqualTo(String.format(CATALOG_SOLUTION_COUNT_PATH, "12345678-abcd-90ab-cdef-1234567890ab")))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withBody("5")));

		RestPageResponse<MLCatalog> response = catalogService.searchCatalogs(catalogRequest);
		assertValidRestPageResponse(response);
		List<MLCatalog> catalogs = response.getContent();
		assertEquals(catalogs.size(), 1);
		MLCatalog catalog = catalogs.get(0);
		assertNotNull(catalog);
	}

	@Test
	public void getCatalogTest() {
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";

		stubFor(get(urlEqualTo(String.format(CATALOG_ID_PATH, catalogId))).willReturn(aResponse()
				.withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.withBody("{\"accessTypeCode\": \"PB\"," + "\"catalogId\": \"12345678-abcd-90ab-cdef-1234567890ab\","
						+ "\"created\": \"2018-12-16T12:34:56.789Z\","
						+ "\"description\": \"A catalog of test models\","
						+ "\"modified\": \"2018-12-16T12:34:56.789Z\"," + "\"name\": \"Test Catalog\","
						+ "\"origin\": \"http://test.acumos.org/api\"," + "\"publisher\": \"Acumos\","
						+ "\"url\": \"http://test.company.com/api\"}")));

		MLPCatalog catalog = catalogService.getCatalog(catalogId);
		assertEquals(catalogId, catalog.getCatalogId());
	}

	@Test
	public void createCatalogTest() {
		MLPCatalog catalog = getTestCatalog(false);

		stubFor(post(urlEqualTo(CCDS_CATALOG_PATH)).willReturn(aResponse().withStatus(HttpStatus.SC_OK)
				.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.withBody("{\"accessTypeCode\": \"PB\"," + "\"catalogId\": \"12345678-abcd-90ab-cdef-1234567890ab\","
						+ "\"created\": \"2018-12-16T12:34:56.789Z\","
						+ "\"description\": \"A catalog of test models\","
						+ "\"modified\": \"2018-12-16T12:34:56.789Z\"," + "\"name\": \"Test Catalog\","
						+ "\"origin\": \"http://test.acumos.org/api\"," + "\"publisher\": \"Acumos\","
						+ "\"url\": \"http://test.company.com/api\"}")));

		MLPCatalog out = catalogService.createCatalog(catalog);
		assertNotNull(out.getCatalogId());
		assertEquals(catalog.getAccessTypeCode(), out.getAccessTypeCode());
		assertEquals(catalog.getName(), out.getName());
		assertEquals(catalog.getPublisher(), out.getPublisher());
		assertEquals(catalog.getDescription(), out.getDescription());
		assertEquals(catalog.getUrl(), out.getUrl());
		assertEquals(catalog.getOrigin(), out.getOrigin());
		assertEquals(catalog.getCreated(), out.getCreated());
		assertEquals(catalog.getModified(), out.getModified());
	}

	@Test
	public void updateCatalogTest() {
		MLPCatalog catalog = getTestCatalog(true);

		stubFor(put(urlEqualTo(String.format(CATALOG_ID_PATH, catalog.getCatalogId()))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

		catalogService.updateCatalog(catalog);
	}

	@Test
	public void deleteCatalogTest() {
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";

		stubFor(delete(urlEqualTo(String.format(CATALOG_ID_PATH, catalogId))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

		catalogService.deleteCatalog(catalogId);
	}

	@Test
	public void getPeerAccessCatalogIdsTest() {
		String peerId = "1234-1234-1234-1234-1234";
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";

		stubFor(get(urlEqualTo(String.format(PEER_ACCESS_PATH, peerId))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody("[\"" + catalogId + "\"]")));

		List<String> catalogIds = catalogService.getPeerAccessCatalogIds(peerId);
		assertNotNull(catalogIds);
		assertEquals(catalogIds.size(), 1);
		assertEquals(catalogIds.get(0), catalogId);
	}

	@Test
	public void addPeerAccessCatalogTest() {
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";
		String peerId = "1234-1234-1234-1234-1234";
		List<String> peerIdList=new ArrayList<>();
		peerIdList.add(peerId);

		stubFor(post(urlEqualTo(String.format(ADD_DROP_PEER_ACCESS_PATH, peerId, catalogId))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

		catalogService.addPeerAccessCatalog(peerIdList, catalogId);
	}

	@Test
	public void dropPeerAccessCatalogTest() {
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";
		String peerId = "1234-1234-1234-1234-1234";
		List<String> peerIdList=new ArrayList<>();
		peerIdList.add(peerId);

		stubFor(delete(urlEqualTo(String.format(ADD_DROP_PEER_ACCESS_PATH, peerId, catalogId))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

		catalogService.dropPeerAccessCatalog(peerIdList, catalogId);
	}

	@Test
	public void getCatalogSolutionCountTest() {
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";
		Long count = new Long(7);

		stubFor(get(urlEqualTo(String.format(CATALOG_SOLUTION_COUNT_PATH, catalogId)))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withBody(count.toString())));

		Long result = catalogService.getCatalogSolutionCount(catalogId);
		assertEquals(count, result);
	}

	@Test
	public void getSolutionsInCatalogsTest() {
		String[] catalogIds = new String[2];
		catalogIds[0] = "12345678-abcd-90ab-cdef-1234567890ab";
		catalogIds[1] = "09876543-abcd-21ab-cdef-0987654321ab";

		stubFor(get(urlEqualTo(String.format(CATALOG_SOLUTION_PATH + "?ctlg=%s&ctlg=%s&" + PAGE_REQUEST_PARAMS,
				catalogIds[0], catalogIds[1]))).willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody("{\"content\":[" + "{\"created\":1535603044000," + "\"modified\":1536350829000,"
								+ "\"solutionId\":\"f226cc60-c2ec-4c2b-b05c-4a521f77e077\","
								+ "\"name\":\"TestSolution\"," + "\"metadata\":null," + "\"active\":true,"
								+ "\"modelTypeCode\":\"CL\"," + "\"toolkitTypeCode\":\"TF\"," + "\"origin\":null,"
								+ "\"userId\":\"bc961e2a-9506-4cf5-bbdb-009558b79e29\"," + "\"sourceId\":null,"
								+ "\"tags\":[{\"tag\":\"Test\"}]," + "\"viewCount\":12," + "\"downloadCount\":0,"
								+ "\"lastDownload\":1536364233000," + "\"ratingCount\":0,"
								+ "\"ratingAverageTenths\":0," + "\"featured\":false}]," + "\"last\":true,"
								+ "\"totalPages\":1," + "\"totalElements\":1," + "\"size\":9," + "\"number\":0,"
								+ "\"sort\":[{\"direction\":\"DESC\"," + "\"property\":\"modified\","
								+ "\"ignoreCase\":false," + "\"nullHandling\":\"NATIVE\"," + "\"ascending\":false,"
								+ "\"descending\":true}]," + "\"numberOfElements\":1," + "\"first\":true}")));

		RestPageResponse<MLPSolution> response = catalogService.getSolutionsInCatalogs(catalogIds,
				getTestRestPageRequest());
		assertValidRestPageResponse(response);
		List<MLPSolution> solutions = response.getContent();
		assertEquals(solutions.size(), 1);
		MLPSolution solution = solutions.get(0);
		assertNotNull(solution);
	}

	@Test
	public void getSolutionCatalogsTest() {
		String solutionId = "12345678-abcd-90ab-cdef-1234567890ab";

		stubFor(get(urlEqualTo(String.format(CATALOG_SOLUTION_ID_PATH, solutionId))).willReturn(aResponse()
				.withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.withBody("[{\"created\": \"2019-04-05T20:47:03Z\"," + "\"modified\": \"2019-04-05T20:47:03Z\","
						+ "\"catalogId\": \"12345678-abcd-90ab-cdef-1234567890ab\"," + "\"accessTypeCode\": \"PB\","
						+ "\"selfPublish\": false," + "\"name\": \"Test catalog\"," + "\"publisher\": \"Acumos\","
						+ "\"description\": null," + "\"origin\": null," + "\"url\": \"http://localhost\"}]")));

		List<MLPCatalog> catalogs = catalogService.getSolutionCatalogs(solutionId);
		assertEquals(catalogs.size(), 1);
		MLPCatalog catalog = catalogs.get(0);
		assertNotNull(catalog);
	}

	@Test
	public void addSolutionToCatalogTest() {
		String solutionId = "f226cc60-c2ec-4c2b-b05c-4a521f77e077";
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";

		stubFor(post(urlEqualTo(String.format(ADD_DROP_SOLUTION_PATH, catalogId, solutionId))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

		catalogService.addSolutionToCatalog(solutionId, catalogId);
	}

	@Test
	public void dropSolutionFromCatalogTest() {
		String solutionId = "f226cc60-c2ec-4c2b-b05c-4a521f77e077";
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";

		stubFor(delete(urlEqualTo(String.format(ADD_DROP_SOLUTION_PATH, catalogId, solutionId))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

		catalogService.dropSolutionFromCatalog(solutionId, catalogId);
	}

	@Test
	public void getUserFavoriteCatalogIdsTest() {
		String userId = "1234-1234-1234-1234-1234";
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";

		stubFor(get(urlEqualTo(String.format(GET_USER_FAVORITES_PATH, userId))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody("[\"" + catalogId + "\"]")));

		List<String> catalogIds = catalogService.getUserFavoriteCatalogIds(userId);
		assertNotNull(catalogIds);
		assertEquals(catalogIds.size(), 1);
		assertEquals(catalogIds.get(0), catalogId);
	}

	@Test
	public void addUserFavoriteCatalogTest() {
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";
		String userId = "1234-1234-1234-1234-1234";

		stubFor(post(urlEqualTo(String.format(ADD_DROP_FAVORITES_PATH, catalogId, userId))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

		catalogService.addUserFavoriteCatalog(userId, catalogId);
	}

	@Test
	public void dropUserFavoriteCatalogTest() {
		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";
		String userId = "1234-1234-1234-1234-1234";

		stubFor(delete(urlEqualTo(String.format(ADD_DROP_FAVORITES_PATH, catalogId, userId))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

		catalogService.dropUserFavoriteCatalog(userId, catalogId);
	}

	private MLPCatalog getTestCatalog(boolean hasCatalogId) {
		MLPCatalog catalog = new MLPCatalog();
		if (hasCatalogId) {
			catalog.setCatalogId("12345678-abcd-90ab-cdef-1234567890ab");
		}
		catalog.setAccessTypeCode("PB");
		catalog.setName("Test Catalog");
		catalog.setPublisher("Acumos");
		catalog.setDescription("A catalog of test models");
		catalog.setUrl("http://test.company.com/api");
		catalog.setOrigin("http://test.acumos.org/api");
		catalog.setCreated(Instant.parse("2018-12-16T12:34:56.789Z"));
		catalog.setModified(Instant.parse("2018-12-16T12:34:56.789Z"));
		return catalog;
	}

	private RestPageRequest getTestRestPageRequest() {
		Map<String, String> fieldToDirectionMap = new HashMap<>();
		fieldToDirectionMap.put("modified", "DESC");
		return new RestPageRequest(0, 9, fieldToDirectionMap);
	}

	private void assertValidRestPageResponse(RestPageResponse<?> response) {
		assertNotNull(response);
		assertEquals(response.getNumber(), 0);
		assertEquals(response.getSize(), 9);
		assertNotNull(response.getContent());
	}
	
	@Test
	public void getCatalogIdsAccessPeerTest() {

		String catalogId = "12345678-abcd-90ab-cdef-1234567890ab";

		stubFor(get(urlEqualTo(String.format(CATALOG_ACCESS_PATH, catalogId))).willReturn(
				aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody("[{\"created\": \"2019-04-05T20:47:03Z\"," + "\"modified\": \"2019-04-05T20:47:03Z\","
								+ "\"peerId\": \"12345678\"," + "\"name\": \"test\"," + "\"webUrl\": \"url\","
								+ "\"statusCode\": \"RS\"}]")));

		List<MLPPeer> peers = catalogService.getCatalogIdsAccessPeer(catalogId);
		assertEquals(peers.size(), 1);
		MLPPeer mlpeer = peers.get(0);
		assertNotNull(mlpeer);
	}
}
