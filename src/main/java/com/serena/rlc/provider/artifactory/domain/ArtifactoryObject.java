/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.artifactory.domain;

import org.json.simple.JSONObject;

import java.io.Serializable;

/**
 * Base Artifactory Object
 * @author klee@serena.com
 */
public class ArtifactoryObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id; // usually uri
    private String title;
    private String description;
    private String created;
    private String createdBy;
    private String lastModified;
    private String modifiedBy;
    private String lastUpdated;

    public ArtifactoryObject() {

    }

    public ArtifactoryObject(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String created) {
        this.createdBy = created;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public static Object getJSONValue(JSONObject obj, String key) {
        Object retObj = null;
        if (obj.containsKey(key)) {
            return obj.get(key);
        }
        return retObj;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}