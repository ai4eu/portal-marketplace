package org.acumos.portal.be.controller;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.domain.MLPRightToUse;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.portal.be.APINames;
import org.acumos.portal.be.common.JSONTags;
import org.acumos.portal.be.common.JsonRequest;
import org.acumos.portal.be.common.JsonResponse;
import org.acumos.portal.be.common.exception.AcumosServiceException;
import org.acumos.portal.be.service.LicensingService;
import org.acumos.portal.be.transport.RightToUseDetails;
import org.acumos.portal.be.transport.RtuUser;
import org.acumos.portal.be.transport.User;
import org.acumos.portal.be.util.PortalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/")
public class LicensingServiceController extends AbstractController{

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private LicensingService licensingService;
	
	@ApiOperation(value = "Gets Solutions and Users details for the given RTU ReferenceId.", response = RightToUseDetails.class)
	@RequestMapping(value = {APINames.RTU_SOLUTION_USER_DETAILS }, method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RightToUseDetails> getRtuSolutionsAndUsers(HttpServletRequest request,
			@RequestHeader(value = "rtuReferenceId", required = true) String rtuReferenceId,
			@RequestHeader(value = "solutionName", required = false) String solutionName, HttpServletResponse response) {
		


		RightToUseDetails rightToUseDetails = new RightToUseDetails();
		MLPSolution mlpSolutionAssociatedWithRtuId = null;

		List<MLPUser> mlpUsersAssociatedWithRtuId = null;
		JsonResponse<RightToUseDetails> data = new JsonResponse<>();
		
		if(rtuReferenceId == null || rtuReferenceId.isEmpty()) {			
			data.setResponseCode(String.valueOf(HttpServletResponse.SC_BAD_REQUEST));
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("RTU ReferenceId is null");
			data.setStatus(false);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return data;		
		}
		
		try {
			if (rtuReferenceId != null && solutionName == null) {
				
				log.debug("Getting Associated Solutions and Users details for RTU ReferenceId:  ", rtuReferenceId);
				
				//Getting List of MLPRightToUse objects from CDS
				List<MLPRightToUse> rtus = licensingService.getRtusByReference(rtuReferenceId);
				
				//Extracting only RtuIds as List from MLPRightToUse
				List<Long> rtuIds =new ArrayList<Long>();
				
				/* if(rtus.size() != 0) { */
					
					for(MLPRightToUse rtu: rtus) {
						rtuIds.add(rtu.getRtuId());
					}
					
					log.debug("List of RTUIDs associated to RTU ReferenceId:  ", rtuReferenceId);
	
					// Get Solution Details associated with RtuID. On UI Solution Name and Solution Id will be displayed
					List<MLPSolution> listOfSolutions = new ArrayList<MLPSolution>();
					for (Long rtuId : rtuIds) {
						mlpSolutionAssociatedWithRtuId = licensingService.getMLPSolutions(rtuId);
						
						listOfSolutions.add(mlpSolutionAssociatedWithRtuId);
					}
					Set<MLPSolution> allSolutionSet = new HashSet<MLPSolution>(listOfSolutions);
					//Convert back to List
					List<MLPSolution> uniqueAllSolutions = new ArrayList<MLPSolution>(allSolutionSet);	
					
					rightToUseDetails.setMlpSolutionAssociatedWithRtuId(uniqueAllSolutions);
	
					// Get All Active Users
					List<RtuUser> allUsers = licensingService.getAllActiveUsers();
					
					// Get users associated with RTUID.
					List<RtuUser> usersListAssociatedWithRtuId = new ArrayList<RtuUser>();
					for (Long rtuId : rtuIds) {
						mlpUsersAssociatedWithRtuId = licensingService.getMLPUsersAssociatedWithRtuId(rtuId);
						if (!PortalUtils.isEmptyList(mlpUsersAssociatedWithRtuId)) {
							for (MLPUser mlpuser : mlpUsersAssociatedWithRtuId) {
								RtuUser user = PortalUtils.convertToRtuUser(mlpuser,true);
								usersListAssociatedWithRtuId.add(user);
							}
						}
	
					}		
						
					
					//Removing duplicate users from All users, if any
					Set<RtuUser> allUsersSet = new HashSet<RtuUser>(allUsers);
					//Convert back to List
					List<RtuUser> uniqueAllUsers = new ArrayList<RtuUser>(allUsersSet);	
					
					//Removing duplicate users related to usersListAssociatedWithRtuId
					Set<RtuUser> set = new HashSet<RtuUser>(usersListAssociatedWithRtuId);
					//Convert back to List
					List<RtuUser> uniqueUsersListAssociatedWithRtuId = new ArrayList<RtuUser>(set);
					//Removing Users associated to Rtuids from All users list which has associatedWithRtuFlag as false 
					uniqueAllUsers.removeAll(uniqueUsersListAssociatedWithRtuId);
					//Adding users associated to RtuIds to All users list with associatedWithRtuFlag as true
					uniqueAllUsers.addAll(uniqueUsersListAssociatedWithRtuId);				
					rightToUseDetails.setRtuUsers(uniqueAllUsers);
					
									
					data.setResponseBody(rightToUseDetails);
					if(rightToUseDetails.getMlpSolutionAssociatedWithRtuId().size() != 0)
						data.setResponseDetail("Solutions and Users details are fetched successfully for the given RTU ReferenceId:  "+rtuReferenceId);
					else
						data.setResponseDetail("There are no Solutions associated with the rtuReferenceId: "+rtuReferenceId);

						data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
					log.debug("Solutions and Users details are fetched successfully for the given RTU ReferenceId:  ", rtuReferenceId);
					
				/*} else {					
					log.debug("There are no RTUIDs associated with the rtuReferenceId: "+ rtuReferenceId);
					data.setResponseDetail("There are no RTUIDs associated with the rtuReferenceId: "+rtuReferenceId);				
				}*/
				
			} else if (solutionName != null) { 
				  log.debug("Getting list of solutions with exact solution name: "+ solutionName);
				  // Searching solution with exact solution name
				  Map<String, Object> solutoinNameParameter =  new HashMap<>(); 
				  solutoinNameParameter.put("name", solutionName);
			      RestPageResponse<MLPSolution> mlpSolutions =  licensingService.getMLPSolutionBySolutionName(solutoinNameParameter, false,  new RestPageRequest());
				  rightToUseDetails.setSolutionsByName(mlpSolutions); 
				  List<RtuUser> allUsers = licensingService.getAllActiveUsers();
                  rightToUseDetails.setRtuUsers(allUsers);			  
				  
				  data.setResponseBody(rightToUseDetails);
				  data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				  data.setResponseDetail("Solutions are fetched successfully for the solution name: "+solutionName);
				  log.debug("Solutions are fetched successfully for the solution name:   ", solutionName);
			  }
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.error("Exception occurred while fetching solutions and users details for RTU ReferenceId :" + rtuReferenceId, e);
		}
		return data;
	}
	
	
	
	@ApiOperation(value = "Create RTU User based on the RTU_ID, Solution_ID and list of Users of the local Acumos Instance .",  responseContainer = "List")
	@RequestMapping(value = { APINames.CREATE_RTU_USER }, method = RequestMethod.POST, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLPRightToUse>> createRtuUser(HttpServletRequest request,
			HttpServletResponse response, 
			@PathVariable("rtuRefId") String rtuRefId,
			@PathVariable("solutionId") String solutionId,
			@RequestBody JsonRequest<List<String>> userList) {
		
		log.debug( "createRtuUser={}");
		JsonResponse<List<MLPRightToUse>> responseVO = new JsonResponse<>();
		List<MLPRightToUse> responseBody = null;
		try {
			if (userList !=null && rtuRefId !=null && solutionId != null) {
				
				responseBody = licensingService.createRtuUser(rtuRefId, solutionId, userList.getBody());
				
				if(responseBody != null) {
					responseVO.setContent(responseBody );
					responseVO.setStatus(true);
					responseVO.setResponseDetail("Success");
					responseVO.setStatusCode(HttpServletResponse.SC_OK);
				}else {
					responseVO.setStatus(false);
					responseVO.setResponseDetail("Failed");
					responseVO.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
				
			}else {
				responseVO.setStatus(false);
				responseVO.setResponseDetail("Failed");
				responseVO.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			responseVO.setStatus(false);
			responseVO.setResponseDetail("Failed");
			responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
			log.error( "Exception Occurred while createRtuUser()", e);
		}
		return responseVO;
		
	}
}