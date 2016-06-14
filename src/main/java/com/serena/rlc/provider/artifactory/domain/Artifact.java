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

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 *  Artifact Object
 * @author klee@serena.com
 */
public class Artifact extends ArtifactoryObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Artifact.class);

    private String repo;
    private String path;
    private String downloadUri;
    private String mimeType;
    private String size;
    private String name;
    private String version;

    public Artifact() {

    }

    public Artifact(String id, String path, String downloadUri) {
        super.setId(id);
        super.setTitle(path);
        super.setDescription(downloadUri);
        this.setPath(path);
        this.setDownloadUri(downloadUri);
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }
    
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Artifact> parse(String options) {
        List<Artifact> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray jsonArray = (JSONArray) getJSONValue((JSONObject) parsedObject, "results");
            for (Object object : jsonArray) {
                Artifact aObj = parseSingle((JSONObject)object);
                list.add(aObj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static Artifact parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            Artifact artifact = parseSingle((JSONObject) parsedObject);
            return artifact;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static Artifact parseSingle(JSONObject jsonObject) {
        Artifact aObj = null;
        if (jsonObject != null) {
            aObj = new Artifact((String) getJSONValue(jsonObject, "uri"),
                    (String) jsonObject.get("path"),
                    (String) jsonObject.get("downloadUri")
            );
            aObj.setRepo((String) jsonObject.get("repo"));
            aObj.setCreated((String) jsonObject.get("created"));
            aObj.setCreatedBy((String) jsonObject.get("createdBy"));
            aObj.setLastModified((String) jsonObject.get("lastModified"));
            aObj.setModifiedBy((String) jsonObject.get("modifiedBy"));
            aObj.setLastUpdated((String) jsonObject.get("lastUpdated"));
            aObj.setMimeType((String) jsonObject.get("mimeType"));
            aObj.setSize((String) jsonObject.get("size"));
            try {
                URI uri = new URI((String) getJSONValue(jsonObject, "uri"));
                aObj.setName(uri.getName());
                String[] segments = uri.getPath().split("/");
                aObj.setVersion(segments[segments.length-2]);
            } catch (URIException ex) {
                logger.error("Error while parsing input JSON - " + (String) getJSONValue(jsonObject, "uri"), ex);
            }
        }
        return aObj;
    }

    @Override
    public String toString() {
        return "Artifact{" + "repo=" + getRepo() + ", downloadUri=" + getDownloadUri() + '}';
    }

}
