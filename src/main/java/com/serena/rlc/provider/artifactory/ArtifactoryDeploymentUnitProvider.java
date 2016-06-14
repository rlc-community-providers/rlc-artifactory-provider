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

import com.serena.rlc.provider.annotations.*;
import com.serena.rlc.provider.artifactory.domain.Artifact;
import com.serena.rlc.provider.artifactory.exception.ArtifactoryClientException;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.spi.IDeployUnitProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


/**
 * Artifactory Deployment Unit Provider
 * @author klee@serena.com
 */
public class ArtifactoryDeploymentUnitProvider extends ArtifactoryBaseServiceProvider implements IDeployUnitProvider {

    final static Logger logger = LoggerFactory.getLogger(ArtifactoryDeploymentUnitProvider.class);

    //================================================================================
    // Configuration Properties
    // -------------------------------------------------------------------------------
    // The configuration properties are marked with the @ConfigProperty annotaion
    // and will be displayed in the provider administration page when creating a 
    // configuration of this plugin for use.
    //================================================================================

    @ConfigProperty(name = "deploy_unit_provider_name", displayName = "Deployment Unit Provider Name",
            description = "provider name",
            defaultValue = "Artifactory Deployment Unit Provider",
            dataType = DataType.TEXT)
    private String providerName;

    @ConfigProperty(name = "deploy_unit_provider_description", displayName = "Deployment Unit Provider Description",
            description = "provider description",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String providerDescription;

    @ConfigProperty(name = "deploy_unit_result_limit", displayName = "Result Limit",
            description = "Result limit for find deployment units action",
            defaultValue = "200",
            dataType = DataType.TEXT)
    private String deployUnitResultLimit;

    @Override
    public String getProviderName() {
        return this.providerName;
    }

    @Autowired(required = false)
    public void setProviderName(String providerName) {
        if (StringUtils.isNotEmpty(providerName)) {
            providerName = providerName.trim();
        }
        this.providerName = providerName;
    }

    @Override
    public String getProviderDescription() {
        return this.providerDescription;
    }

    @Autowired(required = false)
    public void setProviderDescription(String providerDescription) {
        if (StringUtils.isNotEmpty(providerDescription)) {
            providerDescription = providerDescription.trim();
        }
        this.providerDescription = providerDescription;
    }

    public String getDeployUnitResultLimit() {
        return deployUnitResultLimit;
    }

    @Autowired(required = false)
    public void setDeployUnitResultLimit(String deployUnitResultLimit) {
        this.deployUnitResultLimit = deployUnitResultLimit;
    }

    //================================================================================
    // Services Methods
    // -------------------------------------------------------------------------------
    //================================================================================

    @Override
    @Service(name = FIND_DEPLOY_UNITS, displayName = "Find Deploy Units", description = "Find Artifactory Artifact Versions.")
        @Params(params = {
            @Param(fieldName = ARTIFACT_REPO, displayName = "Repository", description = "Artifactory Repository", required = true, dataType = DataType.SELECT),
            @Param(fieldName = ARTIFACT_GROUP, displayName = "Group Filter", description = "Artifact Group Filter", required = true, dataType = DataType.TEXT),
            @Param(fieldName = ARTIFACT_CLASS, displayName = "Class Filter", description = "Artifact Class Filter", required = false, dataType = DataType.TEXT),
            @Param(fieldName = ARTIFACT_NAME, displayName = "Artifact Filter", description = "Artifact Filter", required = false, dataType = DataType.TEXT),
            @Param(fieldName = ARTIFACT_VERSION, displayName = "Version Filter", description = "Artifact Version Filter", required = false, dataType = DataType.TEXT)
    })
    public ProviderInfoResult findDeployUnits(List<Field> properties, Long startIndex, Long resultCount) throws ProviderException {
        List<ProviderInfo> list = new ArrayList<ProviderInfo>();

        Field field = Field.getFieldByName(properties, ARTIFACT_REPO);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + ARTIFACT_REPO);
        String repoId = field.getValue();
        String repoName = field.getDisplayValue();

        field = Field.getFieldByName(properties, ARTIFACT_GROUP);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + ARTIFACT_GROUP);
        String groupFilter = field.getValue();

        field = Field.getFieldByName(properties, ARTIFACT_NAME);
        String artifactFilter = field.getValue();

        field = Field.getFieldByName(properties, ARTIFACT_CLASS);
        String classFilter = field.getValue();

        field = Field.getFieldByName(properties, ARTIFACT_VERSION);
        String versionFilter = field.getValue();

        setArtifactoryClientConnectionDetails();

        List<Artifact> artifacts = null;
        try {
            logger.debug("Retrieving Artifact Versions for Repository: {} Group: {} Artifact: {} Class: {} Version: {}",
                    repoId, groupFilter, artifactFilter, classFilter, versionFilter);
            artifacts = getArtifactoryClient().getArtifacts(repoId, groupFilter, artifactFilter, classFilter, versionFilter, Integer.valueOf(getDeployUnitResultLimit()));
            for (Artifact a : artifacts) {
                list.add(getProviderInfo(a, repoId));
            }
        } catch (ArtifactoryClientException ex) {
            logger.error("Error retrieving Artifacts: {}", ex.getMessage());
        }

        return new ProviderInfoResult(0, list.size(), list.toArray(new ProviderInfo[list.size()]));
    }

    @Override
    @Service(name = GET_DEPLOY_UNIT, displayName = "Get Deploy Unit", description = "Get Artifactory Version as a Deployment Unit")
        @Params(params = {
            @Param(fieldName = ARTIFACT_PATH, displayName = "Artifact Path", description = "Artifact Path", required = false, deployUnit = true)
    })
    public ProviderInfo getDeployUnit(Field property) throws ProviderException {

        String path = property.getId();
        if (StringUtils.isEmpty(path))
            throw new ProviderException("Missing required field: " + ARTIFACT_PATH);

        setArtifactoryClientConnectionDetails();

        Artifact artifact = null;
        try {
            logger.debug("Retrieving Artifact from path: {}" + path);
            artifact = getArtifactoryClient().getArtifact(path);
        } catch (ArtifactoryClientException ex) {
            logger.error("Error retrieving Artifact: {}", ex.getMessage());
        }

        if (artifact == null) {
            return null;
        }

        return getProviderInfo(artifact, artifact.getRepo());
    }

    //================================================================================
    // Getter Methods
    // -------------------------------------------------------------------------------
    //================================================================================


    //

    private ProviderInfo getProviderInfo(Artifact artifact, String repoId) {
        ProviderInfo providerInfo = new ProviderInfo(artifact.getId(), artifact.getName(), "Artifact", artifact.getVersion(), artifact.getDownloadUri());
        providerInfo.setDescription(artifact.getName());

        // http://localhost:8081/artifactory/webapp/#/artifacts/browser/tree/General/libs-release-local/org/apache/commons/commons-lang3/3.3.2/commons-lang3-3.3.2.jar
        providerInfo.setUrl(getArtifactoryUrl() + "/webapp/#/artifacts/browse/tree/General/" +
            repoId + artifact.getPath());

        List<Field> fields = new ArrayList<Field>();
        Field field;

        if (artifact.getRepo() != null) {
            field = new Field("repository", "Repository");
            field.setValue(artifact.getRepo());
            fields.add(field);
        }

        if (artifact.getCreated() != null) {
            field = new Field("created", "Created");
            field.setValue(artifact.getCreated());
            fields.add(field);
        }

        if (artifact.getCreatedBy() != null) {
            field = new Field("createdBy", "Created By");
            field.setValue(artifact.getCreatedBy());
            fields.add(field);
        }

        if (artifact.getName() != null) {
            field = new Field("name", "Name");
            field.setValue(artifact.getVersion());
            fields.add(field);
        }

        if (artifact.getVersion() != null) {
            field = new Field("version", "Version");
            field.setValue(artifact.getVersion());
            fields.add(field);
        }

        if (artifact.getSize() != null) {
            field = new Field("size", "Size");
            field.setValue(artifact.getVersion());
            fields.add(field);
        }

        // TODO: add more fields

        providerInfo.setProperties(fields);

        return providerInfo;
    }

    //

    @Override
    public FieldInfo getFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        if (fieldName.equalsIgnoreCase(ARTIFACT_REPO)) {
            return getRepositoryFieldValues(fieldName, properties);
        }

        throw new ProviderException("Unsupported get values for field name: " + fieldName);
    }

    @Override
    public void setArtifactoryClientConnectionDetails() {
        getArtifactoryClient().createConnection(getSession(), getArtifactoryUrl(), getServiceUser(), getServicePassword());
    }

    @Override
    public ServiceInfo getServiceInfo(String service) throws ProviderException {
        return AnnotationUtil.getServiceInfo(this.getClass(), service);
    }

    @Override
    public ServiceInfoResult getServices() throws ProviderException {
        List<ServiceInfo> services = AnnotationUtil.getServices(this.getClass());

        return new ServiceInfoResult(0, services.size(), services.toArray(new ServiceInfo[services.size()]));
    }

    @Override
    public FieldValuesGetterFunction findFieldValuesGetterFunction(String fieldName) throws ProviderException {
        return AnnotationUtil.findFieldValuesGetterFunction(this.getClass(), fieldName);
    }

    @Override
    public FieldValuesGetterFunctionResult findFieldValuesGetterFunctions() throws ProviderException {
        List<FieldValuesGetterFunction> getters = AnnotationUtil.findFieldValuesGetterFunctions(this.getClass());

        return new FieldValuesGetterFunctionResult(0, getters.size(), getters.toArray(new FieldValuesGetterFunction[getters.size()]));
    }


    @Override
    public ConfigurationPropertyResult getConfigurationProperties() throws ProviderException {
        List<ConfigurationProperty> configProps = AnnotationUtil.getConfigurationProperties(this.getClass(), this);

        return new ConfigurationPropertyResult(0, configProps.size(), configProps.toArray(new ConfigurationProperty[configProps.size()]));
    }

}
