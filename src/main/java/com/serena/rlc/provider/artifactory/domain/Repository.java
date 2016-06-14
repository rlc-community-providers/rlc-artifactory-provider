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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Artifactory Repository Object
 * @author klee@serena.com
 */
public class Repository extends ArtifactoryObject {

    private static final long serialVersionUID = 1L;

    private String type;
    private String url;

    private final static Logger logger = LoggerFactory.getLogger(Repository.class);

    public Repository() {

    }

    public Repository(String id, String type, String description, String url) {
        super.setId(id);
        super.setTitle(url);
        super.setDescription(description);
        this.setUrl(url);
        this.setType(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static List<Repository> parse(String options) {
        List<Repository> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray jsonArray = (JSONArray) parsedObject;
            for (Object object : jsonArray) {
                Repository rObj = parseSingle((JSONObject)object);
                list.add(rObj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static Repository parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            Repository repository = parseSingle((JSONObject)parsedObject);
            return repository;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static Repository parseSingle(JSONObject jsonObject) {
        Repository rObj = null;
        if (jsonObject != null) {
            rObj = new Repository(
                (String) getJSONValue(jsonObject, "key"),
                (String) getJSONValue(jsonObject, "type"),
                (String) getJSONValue(jsonObject, "description"),
                (String) getJSONValue(jsonObject, "url")
            );
        }
        return rObj;
    }

    @Override
    public String toString() {
        return "Repository{" + "key=" + getId() + ", uri=" + getUrl() + '}';
    }

}