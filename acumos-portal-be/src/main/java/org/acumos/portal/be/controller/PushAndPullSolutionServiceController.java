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

/**
 * 
 */
package org.acumos.portal.be.controller;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.domain.MLPSiteConfig;
import org.acumos.portal.be.APINames;
import org.acumos.portal.be.common.CredentialsService;
import org.acumos.portal.be.common.JsonResponse;
import org.acumos.portal.be.common.exception.AcumosServiceException;
import org.acumos.portal.be.common.exception.StorageException;
import org.acumos.portal.be.service.AdminService;
import org.acumos.portal.be.service.LicensingService;
import org.acumos.portal.be.service.PushAndPullSolutionService;
import org.acumos.portal.be.service.StorageService;
import org.acumos.portal.be.util.SanitizeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/")
public class PushAndPullSolutionServiceController extends AbstractController {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	

	@Autowired
	private StorageService storageService;
	
	@Autowired
	private LicensingService licensingService;
	
	@Autowired
	private CredentialsService credentialService;

	@Autowired
	private PushAndPullSolutionService pushAndPullSolutionService;

	@Autowired
	AdminService adminService;

	/**
	 * 
	 */

	public PushAndPullSolutionServiceController() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sends Dockerized Image Tar ball file of the Artifact for the Solution.
	 * 
	 * @param solutionId
	 *            solution ID
	 * @param artifactId
	 *            artifact ID
	 * @param revisionId
	 *            revision ID
	 * @param userId
	 *            user ID
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 */
	@ApiOperation(value = "API to download the dockerized Image Artifact of the Machine Learning Solution", response = InputStream.class, responseContainer = "List", code = 200)
	@RequestMapping(value = {
			APINames.DOWNLOADS_SOLUTIONS }, method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public void downloadSolutionArtifact(@PathVariable("solutionId") String solutionId,
			@RequestParam("artifactId") String artifactId, @RequestParam("revisionId") String revisionId,
			@RequestParam("userId") String userId, HttpServletRequest request, HttpServletResponse response) {
		try {

			/**
			 * Steps to be implemented a. Invoke Common Data Service to get the Solution &
			 * Artifact Details b. Invoke download downloadModelDockerImage() to get the
			 * Docker Image File c. Send back the file as a tar file to the UI
			 */
			
			solutionId = SanitizeUtils.sanitize(solutionId);
			
			String artifactFileName = pushAndPullSolutionService.getFileNameByArtifactId(artifactId);
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			response.setHeader("x-filename", artifactFileName);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + artifactFileName + "\"");
			response.setStatus(HttpServletResponse.SC_OK);

			pushAndPullSolutionService.downloadModelArtifact(artifactId, response);
			pushAndPullSolutionService.getSolutionDownload(solutionId, artifactId, userId);
			/*if (resource.available() > 0) {
				org.apache.commons.io.IOUtils.copy(resource, response.getOutputStream());
				response.flushBuffer();
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}*/

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error(
					"Exception Occurred downloading a artifact for a Solution in Push and Pull Solution service", e.getMessage());
		}
		// return resource;
	}

	/**
	 * Upload the model zip file to the temporary folder on server.
	 * @param file 
	 * zip file
	 * @param userId
	 * user ID 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @throws IOException 
	 */
	@ApiOperation(value = "API to Upload the model to the server")
	@RequestMapping(value = {
			APINames.UPLOAD_USER_MODEL }, method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public JsonResponse<Boolean> uploadModel(@RequestParam("file") MultipartFile file, @PathVariable("userId") String userId, 
			@RequestParam("licUploadFlag") boolean licUploadFlag, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		userId = SanitizeUtils.sanitize(userId);
		
		JsonResponse<Boolean> responseVO = new JsonResponse<>();
		String validationResponse=null;
		log.debug("uploadModel for user " + userId);

		// Check if the Onboarding is enabled in the site configuration
		MLPSiteConfig mlpSiteConfig = adminService.getSiteConfig("site_config");
		if (mlpSiteConfig != null) {
			String configJson = mlpSiteConfig.getConfigValue();
			ObjectMapper mapper = new ObjectMapper();
			try {
				Map<String, Object> configObj = mapper.readValue(configJson, Map.class);
				if (configObj != null) {
					List<Map<String, Object>> fields = (List<Map<String, Object>>) configObj.get("fields");
					for (Map<String, Object> items : fields) {
						if ("enableOnBoarding".equalsIgnoreCase((String) items.get("name"))) {
							Map<String, String> dataVal = (Map<String, String>) items.get("data");
							if (dataVal != null) {
								String val = dataVal.get("name");
								if ("Disabled".equalsIgnoreCase(val)) {
									log.info("Uploading the model is Disabled from Admin");
									response.setStatus(HttpServletResponse.SC_FORBIDDEN);
									responseVO.setStatus(false);
									responseVO.setResponseDetail("Uploading the model is Disabled from Admin");
									responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
									
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									response.getWriter().write("Uploading the model is Disabled from Admin");
								}
							}
						}

					}
				}
			} catch (JsonParseException e) {
				log.error("Exception Occurred while parsing site configuration.", e.getMessage());
				log.info("Exception Occurred while parsing site configuration. Do Nothing");
			} catch (JsonMappingException e) {
				log.error("Exception Occurred while parsing site configuration.", e.getMessage());
				log.info("Exception Occurred while parsing site configuration. Do Nothing");
			} catch (IOException e) {
				log.error("Exception Occurred while parsing site configuration.", e.getMessage());
				log.info("Exception Occurred while parsing site configuration. Do Nothing");
			}
		}
		if (StringUtils.isEmpty(userId)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.info("User Id Required to upload the model");
			responseVO.setStatus(false);
			responseVO.setResponseDetail("User Id Required to upload the model");
			responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
			
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("User Id Required to upload the model");
		}
		
		try {
			if(licUploadFlag) {
				String input= new String(file.getBytes());
				String filename = file.getOriginalFilename();
				if (!filename.endsWith(".json")) {				
					log.error("json File Required. Original File :  " + filename );
					throw new StorageException("json File Required. Original File : " + filename);
				}
				validationResponse=licensingService.validate(input);
				if(validationResponse=="SUCCESS") {
					boolean resultFlag = storageService.store(file, userId, licUploadFlag);
					responseVO.setStatus(resultFlag);
					responseVO.setResponseDetail("Success");
					responseVO.setResponseBody(resultFlag);
					responseVO.setStatusCode(HttpServletResponse.SC_OK);
				}
				else {
					responseVO.setStatus(false);
					responseVO.setResponseDetail(validationResponse);
					responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
					responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					log.error("Error Occurred during validation of license file :: "+validationResponse);
				}
			}
			else {	
				boolean resultFlag = storageService.store(file, userId, licUploadFlag);
				responseVO.setStatus(resultFlag);
				responseVO.setResponseDetail("Success");
				responseVO.setResponseBody(resultFlag);
				responseVO.setStatusCode(HttpServletResponse.SC_OK);
			}
		} catch(AcumosServiceException ae) {
			responseVO.setStatus(false);
			responseVO.setResponseDetail(ae.getMessage());
			responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(ae.getMessage());
			response.flushBuffer();
			log.error("Exception Occurred during validation of license file", ae.getMessage());
		}catch (StorageException e) {
			responseVO.setStatus(false);
			responseVO.setResponseDetail(e.getMessage());
			responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
			
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(e.getMessage());
			response.flushBuffer();
			
			log.error(
					"Exception Occurred while uploading the model in Push and Pull Solution service", e.getMessage());
		}
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error(
					"Exception Occurred while uploading the model in Push and Pull Solution service", e.getMessage());
		}
		return responseVO;
	}

	/**
	 * Sends document file of the revision for the Solution.
	 * 
	 * @param documentId
	 *            document ID
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 */
	@ApiOperation(value = "API to download the documents of the Solution", response = InputStream.class, responseContainer = "List", code = 200)
	@RequestMapping(value = { "/solution/revision/document/{documentId}" },
	method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public void downloadSolRevDocument(@PathVariable String documentId, HttpServletRequest request, HttpServletResponse response) {
		try {
			
			documentId = SanitizeUtils.sanitize(documentId);

			String documentName = pushAndPullSolutionService.getFileNameByDocumentId(documentId);
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			response.setHeader("x-filename", documentName);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + documentName + "\"");
			response.setStatus(HttpServletResponse.SC_OK);

			pushAndPullSolutionService.downloadModelDocument(documentId, response);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error(
					"Exception Occurred downloading a document for a Solution in Push and Pull Solution service", e.getMessage());
		}
	}
	
	@RequestMapping(value = { APINames.CREATE_JSON_FILE },method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse<Boolean> createJsonFile(HttpServletRequest request,@PathVariable("userId") String userId,@RequestBody String json, HttpServletResponse response)throws IOException {
		JsonResponse<Boolean> responseVO = new JsonResponse<>();
		boolean resultFlag=false;
		try{
			resultFlag=storageService.createJsonFile(json, userId);
			responseVO.setStatus(resultFlag);
			responseVO.setResponseDetail("Success");
			responseVO.setResponseBody(resultFlag);
			responseVO.setStatusCode(HttpServletResponse.SC_OK);
		} catch (StorageException e) {
			responseVO.setStatus(false);
			responseVO.setResponseDetail(e.getMessage());
			responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(e.getMessage());
			response.flushBuffer();
			
			log.error(
					"Exception Occurred while creating json file", e.getMessage());
		}
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error(
					"Exception Occurred while creating json file", e.getMessage());
		}
		return responseVO;
	}
	
	@ApiOperation(value = "API to delete license file")
	@RequestMapping(value = {APINames.DELETE_LICENSE_FILE},method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse<String> deleteLicenseFile(HttpServletRequest request,@PathVariable("userId") String userId, HttpServletResponse response)throws IOException {
		JsonResponse<String> responseVO = new JsonResponse<>();
		try {
			storageService.deleteLicenseFile(userId);
			responseVO.setStatus(true);
			responseVO.setResponseDetail("Success");
			responseVO.setResponseBody("Success");
			responseVO.setStatusCode(HttpServletResponse.SC_OK);
			log.debug("File deleted successfully");
		} catch (AcumosServiceException e) {
			responseVO.setStatus(false);
			responseVO.setResponseDetail(e.getMessage());
			responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(e.getMessage());
			log.error("Exception Occurred while deleting file", e.getMessage());
		}
		return responseVO;
	}
		
	@ApiOperation(value = "API to Upload the proto file to the server")
	@RequestMapping(value = {
			APINames.UPLOAD_PROTO_FILE }, method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public JsonResponse<Boolean> uploadProtoFile(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authorization, 
			@RequestParam("protoUploadFlag") boolean protoUploadFlag, HttpServletRequest request, HttpServletResponse response) throws IOException {
		JsonResponse<Boolean> responseVO = new JsonResponse<>();
		String userId = credentialService.getLoggedInUserId();
		try {
			if(protoUploadFlag) {
				boolean resultFlag = storageService.storeProtoFile(file, userId, protoUploadFlag);
                          	if(resultFlag) {
					responseVO.setStatus(resultFlag);
					responseVO.setResponseDetail("Success");
					responseVO.setResponseBody(resultFlag);
					responseVO.setStatusCode(HttpServletResponse.SC_OK);
                                }else {
					responseVO.setStatus(resultFlag);
					responseVO.setResponseDetail("Failed to Upload Proto File");
					responseVO.setResponseBody(resultFlag);
					responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					log.error("Failed to Upload Proto File::Flag is false");
				}
                         }else {
				responseVO.setStatus(protoUploadFlag);
				responseVO.setResponseDetail("Failed to Upload Proto File");
				responseVO.setResponseBody(protoUploadFlag);
				responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				log.error("Failed to Upload Proto File::Flag is false");
			}
				
		}catch (StorageException e) {
			responseVO.setStatus(false);
			responseVO.setResponseDetail(e.getMessage());
			responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
			
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(e.getMessage());
			response.flushBuffer();
			
			log.error(
					"Exception Occurred while uploading the proto file in Push and Pull Solution service", e.getMessage());
		}
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error(
					"Exception Occurred while uploading the proto file in Push and Pull Solution service", e.getMessage());
		}
		
			
		return responseVO;
	}
	@ApiOperation(value = "API to delete proto file")
	@RequestMapping(value = {APINames.DELETE_PROTO_FILE},method = RequestMethod.POST)
	@ResponseBody
	public JsonResponse<String> deleteProtoFile(HttpServletRequest request,@RequestHeader("Authorization") String authorization, HttpServletResponse response)throws IOException {
		JsonResponse<String> responseVO = new JsonResponse<>();
		String userId = credentialService.getLoggedInUserId();
		try {
			storageService.deleteProtoFile(userId);
			responseVO.setStatus(true);
			responseVO.setResponseDetail("Success");
			responseVO.setResponseBody("Success");
			responseVO.setStatusCode(HttpServletResponse.SC_OK);
			log.debug("File deleted successfully");
		} catch (AcumosServiceException e) {
			responseVO.setStatus(false);
			responseVO.setResponseDetail(e.getMessage());
			responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(e.getMessage());
			log.error("Exception Occurred while deleting file", e.getMessage());
		}
		return responseVO;
	}
	
}
