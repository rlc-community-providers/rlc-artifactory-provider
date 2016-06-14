/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.artifactory;

import com.serena.rlc.provider.annotations.ConfigProperty;
import com.serena.rlc.provider.annotations.Getter;
import com.serena.rlc.provider.artifactory.client.ArtifactoryClient;
import com.serena.rlc.provider.artifactory.domain.Repository;
import com.serena.rlc.provider.artifactory.exception.ArtifactoryClientException;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.spi.IBaseServiceProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


/**
 * Artifactory Base Service Provider
 * @author klee@serena.com
 */
public abstract class ArtifactoryBaseServiceProvider implements IBaseServiceProvider {

    final static Logger logger = LoggerFactory.getLogger(ArtifactoryBaseServiceProvider.class);

    final static String ARTIFACT_REPO = "artifactRepo";
    final static String ARTIFACT_GROUP = "artifactGroup";
    final static String ARTIFACT_CLASS = "artifactClass";
    final static String ARTIFACT_NAME = "artifactName";
    final static String ARTIFACT_VERSION = "artifactVersion";
    final static String ARTIFACT_PATH = "artifactPath";

    //================================================================================
    // Configuration Properties
    // -------------------------------------------------------------------------------
    // The configuration properties are marked with the @ConfigProperty annotaion
    // and will be displayed in the provider administration page when creating a 
    // configuration of this plugin for use.
    //================================================================================

    @ConfigProperty(name = "artifactory_url", displayName = "Artifactory URL",
            description = "Artifactory Server URL.",
            defaultValue = "http://localhost:8081/artifactory",
            dataType = DataType.TEXT)
    private String artifactoryUrl;

    @ConfigProperty(name = "artifactory_default_repository", displayName = "Default Repository",
            description = "Default Artifactory repository.",
            defaultValue = "libs-release-local",
            dataType = DataType.TEXT)
    private String defaultRepository;

    @ConfigProperty(name = "artifactory_serviceuser", displayName = "User Name",
            description = "Artifactory service username.",
            defaultValue = "admin",
            dataType = DataType.TEXT)
    private String serviceUser;

    @ConfigProperty(name = "artifactory_servicepassword", displayName = "Password/Token",
            description = "Artifactory service password/token",
            defaultValue = "",
            dataType = DataType.PASSWORD)
    private String servicePassword;

    private SessionData session;
    private Long providerId;
    private String providerUuid;
    private String providerNamespaceId;

    @Autowired
    ArtifactoryClient artifactoryClient;

    public SessionData getSession() {
        return session;
    }

    @Override
    public void setSession(SessionData session) {
        this.session = null;
    }

    @Override
    public Long getProviderId() {
        return providerId;
    }

    @Override
    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    @Override
    public String getProviderNamespaceId() {
        return providerNamespaceId;
    }

    @Override
    public void setProviderNamespaceId(String providerNamespaceId) {
        this.providerNamespaceId = providerNamespaceId;
    }

    @Override
    public String getProviderUuid() {
        return providerUuid;
    }

    @Override
    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
    }

    public String getArtifactoryUrl() {
        return artifactoryUrl;
    }

    @Autowired(required = false)
    public void setArtifactoryUrl(String artifactoryUrl) {
        if (StringUtils.isNotEmpty(artifactoryUrl)) {
            this.artifactoryUrl = artifactoryUrl.replaceAll("^\\s+", "");
        } else {
            this.artifactoryUrl = "http://localhost:8081/artifactory";
        }
    }

    public String getDefaultRepository() {
        return defaultRepository;
    }

    @Autowired(required = false)
    public void setDefaultRepository(String defaultRepository) {
        if (!StringUtils.isEmpty(defaultRepository)) {
            defaultRepository = defaultRepository.trim();
        } else {
            defaultRepository = "libs-release-local";
        }

        this.defaultRepository = defaultRepository;
    }

    public String getServiceUser() {
        return serviceUser;
    }

    @Autowired(required = false)
    public void setServiceUser(String serviceUser) {
        if (!StringUtils.isEmpty(serviceUser)) {
            this.serviceUser = serviceUser.replaceAll("^\\s+", "");
        }
    }

    public String getServicePassword() {
        return servicePassword;
    }

    @Autowired(required = false)
    public void setServicePassword(String servicePassword) {
        if (!StringUtils.isEmpty(servicePassword)) {
            this.servicePassword = servicePassword.replaceAll("^\\s+", "");
        }
    }

    //================================================================================
    // Getter Methods
    // -------------------------------------------------------------------------------
    // These methods are used to get the field values. The @Getter annotation is used
    // by the system to generate a user interface and pass the correct parameters to
    // to the provider
    //================================================================================

    @Getter(name = ARTIFACT_REPO, displayName = "Repository", description = "Get Artifactory Repository.")
    public FieldInfo getRepositoryFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        setArtifactoryClientConnectionDetails();

        try {
            List<Repository> repositories = getArtifactoryClient().getRepositories(200);
            if (repositories == null || repositories.size() < 1) {
                return null;
            }

            List<FieldValueInfo> values = new ArrayList<>();
            FieldValueInfo value;
            for (Repository repo : repositories) {
                value = new FieldValueInfo(repo.getId(), repo.getId());
                value.setDescription(repo.getDescription());
                values.add(value);
            }

            fieldInfo.setValues(values);
            return fieldInfo;
        } catch (ArtifactoryClientException e) {
            throw new ProviderException(e.getLocalizedMessage());
        }
    }

    //================================================================================
    // Additional Public Methods
    //================================================================================

    public ArtifactoryClient getArtifactoryClient() {
        if (artifactoryClient == null) {
            artifactoryClient = new ArtifactoryClient();
        }

        return artifactoryClient;
    }

    public void setArtifactoryClientConnectionDetails() {
        getArtifactoryClient().createConnection(getSession(), getArtifactoryUrl(), getServiceUser(), getServicePassword());
    }

}
