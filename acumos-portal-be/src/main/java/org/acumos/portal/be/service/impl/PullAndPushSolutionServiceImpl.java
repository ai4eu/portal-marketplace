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

package org.acumos.portal.be.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.client.ICommonDataServiceRestClient;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPDocument;
import org.acumos.cds.domain.MLPSolutionDownload;
import org.acumos.nexus.client.NexusArtifactClient;
import org.acumos.nexus.client.data.UploadArtifactInfo;
import org.acumos.portal.be.common.exception.AcumosServiceException;
import org.acumos.portal.be.docker.DockerClientFactory;
import org.acumos.portal.be.docker.DockerConfiguration;
import org.acumos.portal.be.docker.cmd.SaveImageCommand;
import org.acumos.portal.be.service.PushAndPullSolutionService;
import org.acumos.portal.be.transport.MLSolutionDownload;
import org.acumos.portal.be.util.PortalConstants;
import org.acumos.portal.be.util.PortalUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.dockerjava.api.DockerClient;


@Service
public class PullAndPushSolutionServiceImpl extends AbstractServiceImpl implements PushAndPullSolutionService {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	

	@Autowired
	private Environment env;
	
	@Autowired
	private DockerConfiguration dockerConfiguration;
	
	private static final String ARTIFACT_TYPE_DI = "DI";

	@Override
	public File downloadModelDockerImage(String modelName, String imageName, String version) {

		log.debug("downloadModelDockerImage ={}", imageName);
		/**
		 * Steps for downloading a DockerImage
		 * a. Save the Docker Image in a File
		 * b. Return the file  back to the COntroller so that it can send back the response as tar file.
		 */
		File imageFile = null;
		/*DockerClient dockerClient = null;
		final byte[] buffer = new byte[2048];
		int read;
		try {
			//TODO make docker client singleton
			dockerClient = DockerClientFactory.getDockerClient(dockerConfiguration);
			final File tmpDir = new File(System.getProperty("tmp." + imageName + "."+ System.currentTimeMillis()));
			imageFile = new File(tmpDir, modelName + "-" + version + ".tar");
			imageFile.createNewFile();
			imageFile.deleteOnExit();
			//InputStream and outStream buffers handled by try block
			try(OutputStream imageOutput = new BufferedOutputStream(new FileOutputStream(imageFile))) {
				try(InputStream imageInput = IOUtils.toBufferedInputStream(dockerClient.saveImageCmd(imageName).exec())) {
					while ((read = imageInput.read(buffer)) > -1) {
						imageOutput.write(buffer, 0, read);
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception Occurred while Downloading Docker Image ={}", e);
		} */
		return imageFile;
	}

	/*
	 * Gets artifact to memory then returns an InputStream with contents of the buffer.
	 */
	@Override
	public InputStream downloadModelArtifact(String artifactId) {
		log.debug("downloadModelArtifact.1 begins for artifact {}",
				artifactId);
		InputStream inputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		try {
			ICommonDataServiceRestClient dataServiceRestClient = getClient();
			MLPArtifact mlpArtifact = dataServiceRestClient.getArtifact(artifactId);
			if (mlpArtifact != null && !mlpArtifact.getUri().isEmpty()) {
				if (mlpArtifact.getArtifactTypeCode().equalsIgnoreCase(ARTIFACT_TYPE_DI)) {
					DockerClient dockerClient = DockerClientFactory.getDockerClient(dockerConfiguration);
					try {
						SaveImageCommand saveImageCommand = new SaveImageCommand(mlpArtifact.getUri(), null, null, null,
								true);
						saveImageCommand.setClient(dockerClient);
						inputStream = saveImageCommand.getDockerImageStream();
						log.debug(
								"downloadModelArtifact.1 received stream for artifact {}", artifactId);
					} catch (Exception e) {
						log.error("downloadModelArtifact.1 inner failed", e.getMessage());
					} finally {
						try {
							dockerClient.close();
						} catch (IOException e) {
							log.warn("downloadModelArtifact.1 failed to close docker client", e.getMessage());
						}
					}
				} else {
					NexusArtifactClient artifactClient = getNexusClient();
					byteArrayOutputStream = artifactClient.getArtifact(mlpArtifact.getUri());
					log.debug(
							"downloadModelArtifact.1 received content for artifact {}", artifactId);
					inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
					if (byteArrayOutputStream != null) {
						byteArrayOutputStream.close();
					}
				}
			}
		} catch (Exception e) {
			log.error("downloadModelArtifact.1 outer failed", e.getMessage());
		}
		return inputStream;
	}

	/*
	 * Fetches artifact by ID and writes stream to response output stream. Docker
	 * images are streamed directly; other artifact types are first fetched to
	 * memory.
	 */
	@Override
	public void downloadModelArtifact(String artifactId, HttpServletResponse response) {
		log.debug("downloadModelArtifact.2 begins for artifact {}",
				artifactId);
		try {
			ICommonDataServiceRestClient dataServiceRestClient = getClient();
			MLPArtifact mlpArtifact = dataServiceRestClient.getArtifact(artifactId);
			if (mlpArtifact != null && !mlpArtifact.getUri().isEmpty()) {
				if (mlpArtifact.getArtifactTypeCode().equalsIgnoreCase(ARTIFACT_TYPE_DI)) {
					DockerClient dockerClient = DockerClientFactory.getDockerClient(dockerConfiguration);
					try {
						SaveImageCommand saveImageCommand = new SaveImageCommand(mlpArtifact.getUri(), null, null, null,
								true);
						saveImageCommand.setClient(dockerClient);
						// Keep the default buffer size as 8 if no buffer limit is provided
						Integer buffer = Integer.parseInt(env.getProperty("portal.feature.download_bufferSize", "8"));
						if (buffer <= 0) {
							buffer = 8;
						}
						log.debug(
								"downloadModelArtifact.2 directing docker stream to response for artifact {}", artifactId);
						saveImageCommand.getDockerImageStream(response, buffer);
					} catch (Exception e) {
						log.error("downloadModelArtifact.2 inner failed", e.getMessage());
					} finally {
						try {
							dockerClient.close();
						} catch (IOException e) {
							log.warn("downloadModelArtifact.2 failed to close docker client", e.getMessage());
						}
					}
				} else {
					NexusArtifactClient artifactClient = getNexusClient();
					ByteArrayOutputStream byteArrayOutputStream = artifactClient.getArtifact(mlpArtifact.getUri());
					log.debug(
							"downloadModelArtifact.2 copying content stream for artifact {}", artifactId);
					byteArrayOutputStream.writeTo(response.getOutputStream());
					response.flushBuffer();
					if (byteArrayOutputStream != null) {
						byteArrayOutputStream.close();
					}
				}
			}
		} catch (Exception e) {
			log.error("downloadModelArtifact.2 outer failed", e.getMessage());
		}
	}

	@Override
	public String getFileNameByArtifactId(String artifactId) {
        log.debug("getArtifactById for artifact ID {}", artifactId);

        String artifactFileName = null;
        ICommonDataServiceRestClient dataServiceRestClient = getClient();
        MLPArtifact mlpArtifact = dataServiceRestClient.getArtifact(artifactId);
        if (mlpArtifact != null) {
            String uri = mlpArtifact.getUri();
            if (!uri.isEmpty()) {
                artifactFileName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
                if(mlpArtifact.getArtifactTypeCode().equalsIgnoreCase(ARTIFACT_TYPE_DI)) {
                	artifactFileName += ".tar";
                	}
                }
            }
		return artifactFileName;
	}

	public String getFileNameByDocumentId(String documentId) {
        log.debug("getDocumentNameById for document ID {}", documentId);

        String artifactFileName = null;
        ICommonDataServiceRestClient dataServiceRestClient = getClient();
        MLPDocument mlpDocument = dataServiceRestClient.getDocument(documentId);
        if (mlpDocument != null) {
            artifactFileName = mlpDocument.getName();
        }
		return artifactFileName;
	}

	/*
	 * Fetches document by ID and writes stream to response output stream.
	 */
	@Override
	public void downloadModelDocument(String documentId, HttpServletResponse response) {
		log.debug("downloadModelDocument.2 begins for document {}",
				documentId);
		try {
			ICommonDataServiceRestClient dataServiceRestClient = getClient();
			MLPDocument mlpDocument = dataServiceRestClient.getDocument(documentId);
			if (mlpDocument != null && !mlpDocument.getUri().isEmpty()) {
				NexusArtifactClient nexusClient = getNexusClient();
				ByteArrayOutputStream byteArrayOutputStream = nexusClient.getArtifact(mlpDocument.getUri());
				log.debug("downloadModelDocument.2 copying content stream for document {}", mlpDocument);
				byteArrayOutputStream.writeTo(response.getOutputStream());
				response.flushBuffer();
				if (byteArrayOutputStream != null) {
					byteArrayOutputStream.close();
				}
			}
		} catch (Exception e) {
			log.error("downloadModelDocument.2 outer failed", e.getMessage());
		}
	}

	@Override
	public MLSolutionDownload getSolutionDownload(String solutionId, String artifactId, String userId) {
		log.debug("getSolutionDownload for solution ID {}", solutionId);
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		MLPSolutionDownload download = new MLPSolutionDownload();
		download.setSolutionId(solutionId);
		download.setArtifactId(artifactId);
		download.setUserId(userId);
		download.setDownloadDate(Instant.now());
		MLSolutionDownload mlSolutionDownload = PortalUtils
				.convertToMLSolutionDownload(dataServiceRestClient.createSolutionDownload(download));
		return mlSolutionDownload;
	}

	public boolean uploadLicense(MultipartFile file, String userId, String solutionId, String revisionId, String versionId) throws AcumosServiceException {
		boolean uploadedFile = false;
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		
		String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
		long fileSize = file.getSize();
        NexusArtifactClient nexusClient = getNexusClient();
        String groupId = String.join(".", env.getProperty("nexus.groupId"), solutionId);
        UploadArtifactInfo uploadedArtifact = null;
		try {
			uploadedArtifact = nexusClient.uploadArtifact(groupId, PortalConstants.LICENSE_FILENAME_PREFIX, versionId, fileExtension, fileSize, file.getInputStream());
		} catch (AuthenticationException e) {
			log.error("AuthenticationException failed in uploadLicense", e.getMessage());
			throw new AcumosServiceException(AcumosServiceException.ErrorCode.INVALID_TOKEN,
					"AuthenticationException failed in uploadLicense");
		} catch (AuthorizationException e) {
			log.error("AuthorizationException failed in uploadLicense", e.getMessage());
			throw new AcumosServiceException(AcumosServiceException.ErrorCode.INVALID_TOKEN,
					"AuthorizationException failed in uploadLicense");
		} catch (ConnectionException e) {
			log.error("ConnectionException failed in uploadLicense", e.getMessage());
			throw new AcumosServiceException(AcumosServiceException.ErrorCode.INVALID_TOKEN,
					"ConnectionException failed in uploadLicense");
		} catch (TransferFailedException e) {
			log.error("TransferFailedException failed in uploadLicense", e.getMessage());
			throw new AcumosServiceException(AcumosServiceException.ErrorCode.INVALID_TOKEN,
					"TransferFailedException failed in uploadLicense");
		} catch (ResourceDoesNotExistException e) {
			log.error("ResourceDoesNotExistException failed in uploadLicense", e.getMessage());
			throw new AcumosServiceException(AcumosServiceException.ErrorCode.INVALID_TOKEN,
					"ResourceDoesNotExistException failed in uploadLicense");
		} catch (IOException e) {
			log.error("IOException failed in uploadLicense", e.getMessage());
			throw new AcumosServiceException(AcumosServiceException.ErrorCode.INVALID_TOKEN,
					"IOException failed in uploadLicense");
		}
		
        if(uploadedArtifact !=null && !PortalUtils.isEmptyOrNullString(uploadedArtifact.getArtifactId())) {
        	log.info("uploadedFile or not---->>"+uploadedFile);
        	uploadedFile = true;
        }
		if(uploadedFile) {
			log.info("uploadedFile or not---->>"+uploadedFile);
			
			List<MLPArtifact> artifactList = dataServiceRestClient.getSolutionRevisionArtifacts(solutionId, revisionId).stream().filter(
					mlpArtifact -> mlpArtifact.getArtifactTypeCode().equalsIgnoreCase(PortalConstants.LICENSE_ARTIFACT_TYPE) && 
					mlpArtifact.getName().contains(PortalConstants.LICENSE_FILENAME)).collect(Collectors.toList());		
			 
			if(artifactList.size() == 0) {
				String uri = String.join("/",  Arrays.asList(env.getProperty("nexus.groupId").split("\\.")).stream().collect(Collectors.joining("/")), solutionId, PortalConstants.LICENSE_FILENAME_PREFIX, versionId, PortalConstants.LICENSE_FILENAME_PREFIX+"-"+ versionId+ PortalConstants.LICENSE_EXT);
				
				MLPArtifact modelArtifact = new MLPArtifact();
				modelArtifact.setName(PortalConstants.LICENSE_FILENAME);
				modelArtifact.setDescription(PortalConstants.LICENSE_FILENAME);
				modelArtifact.setVersion(versionId);
				modelArtifact.setArtifactTypeCode(PortalConstants.LICENSE_ARTIFACT_TYPE);			
				modelArtifact.setUserId(userId);
				modelArtifact.setUri(uri);			
				modelArtifact.setSize((int)file.getSize());
				modelArtifact = dataServiceRestClient.createArtifact(modelArtifact);				
				dataServiceRestClient.addSolutionRevisionArtifact(solutionId, revisionId, modelArtifact.getArtifactId());
			}
		}
		
		return uploadedFile;
	}
}
