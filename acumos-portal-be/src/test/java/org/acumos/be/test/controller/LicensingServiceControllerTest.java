/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
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
package org.acumos.be.test.controller;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.portal.be.common.JsonRequest;
import org.acumos.portal.be.common.JsonResponse;
import org.acumos.portal.be.common.exception.AcumosServiceException;
import org.acumos.portal.be.controller.LicensingServiceController;
import org.acumos.portal.be.controller.MarketPlaceCatalogServiceController;
import org.acumos.portal.be.service.LicensingService;
import org.acumos.portal.be.service.MarketPlaceCatalogService;
import org.acumos.portal.be.service.impl.LicensingServiceImpl;
import org.acumos.portal.be.service.impl.MarketPlaceCatalogServiceImpl;
import org.acumos.portal.be.transport.MLSolution;
import org.acumos.portal.be.transport.RightToUseDetails;
import org.acumos.portal.be.transport.RtuUser;
import org.acumos.portal.be.util.PortalConstants;
import org.acumos.portal.be.util.PortalUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class LicensingServiceControllerTest {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	@InjectMocks
	LicensingServiceController licensingServiceController;
	@Mock
	LicensingService licensingService;
	@Mock
	LicensingServiceImpl licensingServiceImpl;
	@Mock
	MarketPlaceCatalogService service;
	@Mock
	MarketPlaceCatalogServiceImpl serviceImpl;
	@InjectMocks
	MarketPlaceCatalogServiceController marketPlaceController;
	
	final HttpServletResponse response = new MockHttpServletResponse();
	final HttpServletRequest request = new MockHttpServletRequest();
 


	@Test
	public void getRtuSolutionsAndUsers() {
		RestPageRequest restPageRequest = new RestPageRequest();
		RightToUseDetails rightToUseDetails = new RightToUseDetails();
		MLSolution mlsolution = getMLSolution();
		MLPSolution solution = PortalUtils.convertToMLPSolution(mlsolution);
		List<MLPSolution> solutionList = new ArrayList<MLPSolution>();
		rightToUseDetails.setMlpSolutionAssociatedWithRtuId(solutionList);
		
		RtuUser user = getUser();
		List<RtuUser> rtuUser = new ArrayList<RtuUser>();
		rtuUser.add(user);
		
		
		rightToUseDetails.setRtuUsers(rtuUser);
		
		MLPSolution solutionByName = PortalUtils.convertToMLPSolution(mlsolution);
		List<MLPSolution> solutionListByName = new ArrayList<MLPSolution>();
		solutionListByName.add(solutionByName);
		
		RestPageResponse<MLPSolution> solutionsByName = new RestPageResponse<>(solutionList, PageRequest.of(0, 1), 1);
		rightToUseDetails.setSolutionsByName(solutionsByName);

		Assert.assertNotNull(rightToUseDetails);
		
		Map<String, Object> solutoinNameParameter =  new HashMap<>(); 
		  solutoinNameParameter.put("name", "Crosssell");
		  
		  List<Long> rtuIds = new ArrayList<Long>();
		  
		  rtuIds.add(1L);
		  rtuIds.add(2L);
		  rtuIds.add(3L);
		  
		  

		JsonResponse<RightToUseDetails> notificationres = new JsonResponse<>();
		notificationres.setResponseBody(rightToUseDetails);
		
		try {
		
		when(licensingService.getMLPSolutionBySolutionName(solutoinNameParameter, true, restPageRequest)).thenReturn(solutionsByName);
		notificationres = licensingServiceController.getRtuSolutionsAndUsers(request, "A123A", "Crosssell", response);

		when(licensingService.getMLPSolutions(1L)).thenReturn(solution);
		notificationres = licensingServiceController.getRtuSolutionsAndUsers(request, "A123A", null, response);
		
		}catch(AcumosServiceException e) {
			logger.error("Exception occurred while getting getRtuSolutionsAndUsers ",e.getMessage());
		}

	}
	
	private MLSolution getMLSolution() {
		MLSolution mlsolution = new MLSolution();
		mlsolution.setSolutionId("Solution1");
		mlsolution.setName("Test_Solution data");
		mlsolution.setOwnerId("41058105-67f4-4461-a192-f4cb7fdafd34");
		mlsolution.setModelType("CL");
		mlsolution.setTookitType("DS");
		return mlsolution;
	}

	private RtuUser getUser(){
		RtuUser user = new RtuUser();
		user.setUserId("8cbeccd0-ed84-42c3-8d9a-06d5629dc7bb");
		user.setFirstName("UserFirstName");
		user.setLastName("UserLastName");
		user.setEmailId("user1@emial.com");
		user.setActive(true);
		user.setAssociatedWithRtuFlag(true);
		return user;
	}

	@Test
	public void createRtuUser() {
		String rtuRefId = "ref_01";
		String solutionId = "4cbf491b-c687-459f-9d81-e150d1a0b972";
		boolean siteWide = false;
		JsonRequest<List<String>> userList = new JsonRequest<List<String>>();
		Assert.assertNotNull(rtuRefId);
		Assert.assertNotNull(solutionId);
		Assert.assertNotNull(userList);
		licensingServiceController.createRtuUser(request, response, rtuRefId, solutionId, siteWide, userList);
		
	}

	@Test
	public void createRtuSiteWide() {
		String rtuRefId = "ref_01";
		String solutionId = "4cbf491b-c687-459f-9d81-e150d1a0b972";
		boolean siteWide = true;
		JsonRequest<List<String>> userList = new JsonRequest<List<String>>();


		licensingServiceController.createRtuUser(request, response, rtuRefId, solutionId, siteWide, userList);
		
	}
	
	@Test
	public void uploadLicense() {		
		MultipartFile dfsdf = null; 
		MultipartFile file = dfsdf ;
		String userId = "8cbeccd0-ed84-42c3-8d9a-06d5629dc7bb";
		String solutionId = "4cbf491b-c687-459f-9d81-e150d1a0b972";
		String revisionId = "2grtccd0-ed84-42c3-8d9a-06d5629dc7bb";
		String versionId = "41058105-67f4-4461-a192-f4cb7fdafd34";
		try {
			Assert.assertNotNull(userId);
			Assert.assertNotNull(solutionId);
			Assert.assertNotNull(revisionId);
			Assert.assertNotNull(versionId);
			licensingServiceController.uploadLicense(file, userId, solutionId, revisionId, versionId, request, response);			
			service.getLicenseUrl(solutionId, versionId, PortalConstants.LICENSE_ARTIFACT_TYPE, PortalConstants.LICENSE_FILENAME_PREFIX);
			
		} catch (IOException e) {
			logger.error("IOException occurred while uploadLicense ",e.getMessage());
		} catch (AcumosServiceException e) {
			logger.error("AcumosServiceException occurred while uploadLicense ",e.getMessage());
		} catch (Exception e) {
			logger.error("Exception occurred while uploadLicense ",e.getMessage());
		}
	}
}
