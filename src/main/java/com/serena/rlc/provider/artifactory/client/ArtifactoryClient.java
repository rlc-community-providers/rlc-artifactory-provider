/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.artifactory.client;

import com.serena.rlc.provider.domain.SessionData;
import com.serena.rlc.provider.artifactory.domain.*;
import com.serena.rlc.provider.artifactory.exception.ArtifactoryClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Artifactory Client
 * @author klee@serena.com
 */
@Component
public class ArtifactoryClient {
    private static final Logger logger = LoggerFactory.getLogger(ArtifactoryClient.class);

    public static String DEFAULT_HTTP_CONTENT_TYPE = "application/json";

    private String aUrl;
    private String aUsername;
    private String aPassword;
    private SessionData session;

    public ArtifactoryClient() {
    }

    public ArtifactoryClient(SessionData session, String url, String username, String password) {
        this.createConnection(session, url, username, password);
    }

    public SessionData getSession() {
        return session;
    }

    public void setSession(SessionData session) {
        this.session = session;
    }

    public String getArtifactoryUrl() {
        return aUrl;
    }

    public void setArtifactoryUrl(String url) {
        this.aUrl = url;
    }

    public String getArtifactoryUsername() {
        return aUsername;
    }

    public void setArtifactoryUsername(String username) {
        this.aUsername = username;
    }

    public String getArtifactoryPassword() {
        return aPassword;
    }

    public void setArtifactoryPassword(String password) {
        this.aPassword = password;
    }

    /**
     * Create a new connection to Artifactory.
     *
     * @param url  the url to Artifactory, e.g. https://localhost:8081/artifactorye
     * @param username  the username of the Artifactory user
     * @param password  the password/private token of the Artifactory user
     */
    public void createConnection(SessionData session, String url, String username, String password) {
        this.session = session;
        this.aUrl = url;
        this.aUsername = username;
        this.aPassword = password;
    }

    /**
     * Get a artifacts via gavc search.
     *
     * @param resultLimit  the maximum number of Work Items to return
     * @return  a list of Repositories
     * @throws ArtifactoryClientException
     */
    public List<Repository> getRepositories(Integer resultLimit) throws ArtifactoryClientException {
        logger.debug("Retrieving Repositories");
        logger.debug("Limiting results to: " + resultLimit.toString());

        String queryResponse = processGet("/api/repositories", "");
        logger.debug(queryResponse);

        List<Repository> repositories = Repository.parse(queryResponse);
        return repositories;
    }

    /**
     * Get a artifacts via gavc search.
     *
     * @param repoId  the repository id to search
     * @param groupId  the group id to search
     * @param artifactId  the artifact id to search
     * @param classId  the classifierId to search
     * @param versionId  the versionId to search
     * @param resultLimit  the maximum number of Work Items to return
     * @return  a list of Artifacts
     * @throws ArtifactoryClientException
     */
    public List<Artifact> getArtifacts(String repoId, String groupId, String artifactId, String classId, String versionId, Integer resultLimit) throws ArtifactoryClientException {
        logger.debug("Retrieving Artifacts from repo \"{}\"", repoId);
        logger.debug("Using Group Id: " + groupId);
        logger.debug("Using Artifact Id: " + artifactId);
        logger.debug("Using Class Id: " + classId);
        logger.debug("Using Version Id: " + versionId);
        logger.debug("Limiting results to: " + resultLimit.toString());

        ///api/search/gavc?g=org.acme&a=artifact&v=1.0&c=sources&repos=libs-release-local
        if (StringUtils.isEmpty(repoId)) throw new ArtifactoryClientException("Artifactory Repository not specified");
        String params = "repos=" + repoId;
        if (StringUtils.isNotEmpty(groupId)) {
            params += "&g="+groupId;
        }
        if (StringUtils.isNotEmpty(artifactId)) {
            params += "&a="+artifactId;
        }
        if (StringUtils.isNotEmpty(classId)) {
            params += "&c=" + classId;
        }
        if (StringUtils.isNotEmpty(versionId)) {
            params += "&v=" + versionId;
        }
        String queryResponse = processGet("/api/search/gavc", params);
        logger.debug(queryResponse);

        List<Artifact> artifacts = Artifact.parse(queryResponse);
        return artifacts;
    }

    /**
     * Get an artifact from its path.
     *
     * @param path  the repository id to search
     * @return  the artifact
     * @throws ArtifactoryClientException
     */
    public Artifact getArtifact(String path) throws ArtifactoryClientException {
        logger.debug("Retrieving Artifacts from path \"{}\"", path);

        ///api/storage/libs-release-local/org/apache/commons/commons-lang3/3.3.2/commons-lang3-3.3.2.jar
        if (StringUtils.isEmpty(path)) throw new ArtifactoryClientException("Artifact path not specified");
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String queryResponse = processGet("/api/storage"+path, "");
        logger.debug(queryResponse);

        Artifact artifact = Artifact.parseSingle(queryResponse);
        return artifact;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Execute a get request to Artifactory.
     *
     * @param path  the path for the specific request
     * @param parameters  parameters to send with the query
     * @return String containing the response body
     * @throws ArtifactoryClientException
     */
    protected String processGet(String path, String parameters) throws ArtifactoryClientException {
        String uri = createUrl(path, parameters);

        logger.debug("Start executing Artifactory GET request to url=\"{}\"", uri);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(uri);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(getArtifactoryUsername(), getArtifactoryPassword());
        getRequest.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false) );
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_HTTP_CONTENT_TYPE);
        getRequest.addHeader(HttpHeaders.ACCEPT, DEFAULT_HTTP_CONTENT_TYPE);
        getRequest.addHeader("X-Result-Detail", "info, properties");
        String result = "";

        try {
            HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != org.apache.http.HttpStatus.SC_OK) {
                throw createHttpError(response);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            StringBuilder sb = new StringBuilder(1024);
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            result = sb.toString();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            throw new ArtifactoryClientException("Server not available", ex);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        logger.debug("End executing Artifactory GET request to url=\"{}\" and receive this result={}", uri, result);

        return result;
    }

    /**
     * Execute a post request to Artifactory.
     *
     * @param path  the path for the specific request
     * @param parameters  parameters to send with the query
     * @param body  the body to send with the request
     * @return String containing the response body
     * @throws ArtifactoryClientException
     */
    public String processPost(String path, String parameters, String body) throws ArtifactoryClientException {
        String uri = createUrl(path, parameters);

        logger.debug("Start executing Artifactory POST request to url=\"{}\" with data: {}", uri, body);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(uri);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(getArtifactoryUsername(), getArtifactoryPassword());
        postRequest.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false) );
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_HTTP_CONTENT_TYPE);
        postRequest.addHeader(HttpHeaders.ACCEPT, DEFAULT_HTTP_CONTENT_TYPE);

        try {
            postRequest.setEntity(new StringEntity(body,"UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
            throw new ArtifactoryClientException("Error creating body for POST request", ex);
        }
        String result = "";

        try {
            HttpResponse response = httpClient.execute(postRequest);
            if (response.getStatusLine().getStatusCode() != org.apache.commons.httpclient.HttpStatus.SC_OK && response.getStatusLine().getStatusCode() != org.apache.commons.httpclient.HttpStatus.SC_CREATED &&
                    response.getStatusLine().getStatusCode() != org.apache.commons.httpclient.HttpStatus.SC_ACCEPTED) {
                throw createHttpError(response);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            StringBuilder sb = new StringBuilder(1024);
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            result = sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ArtifactoryClientException("Server not available", e);
        }

        logger.debug("End executing Artifactory POST request to url=\"{}\" and received this result={}", uri, result);

        return result;
    }

    /**
     * Create a Artifactory URL from base and path.
     *
     * @param path  the path to the request
     * @param parameters  the parameters to send with the request
     * @return a String containing a complete Artifactory path
     */
    public String createUrl(String path, String parameters) {
        String base = getArtifactoryUrl();

        // trim and encode path
        path = path.trim().replaceAll(" ", "%20");
        // if path doesn't start with "/" add it
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        //System.out.println(base + path + "?" + parameters);
        return base + path + "?" + parameters;
    }

    /**
     * Returns a Artifactory Client specific Client Exception
     * @param response  the exception to throw
     * @return
     */
    private ArtifactoryClientException createHttpError(HttpResponse response) {
        String message;
        try {
            StatusLine statusLine = response.getStatusLine();
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            StringBuffer responsePayload = new StringBuffer();
            // Read response until the end
            while ((line = rd.readLine()) != null) {
                responsePayload.append(line);
            }

            message = String.format(" request not successful: %d %s. Reason: %s", statusLine.getStatusCode(), statusLine.getReasonPhrase(), responsePayload);

            logger.debug(message);

            if (new Integer(HttpStatus.SC_UNAUTHORIZED).equals(statusLine.getStatusCode())) {
                return new ArtifactoryClientException("Artifactory: Invalid credentials provided.");
            } else if (new Integer(HttpStatus.SC_NOT_FOUND).equals(statusLine.getStatusCode())) {
                return new ArtifactoryClientException("Artifactory: Request URL not found.");
            } else if (new Integer(HttpStatus.SC_BAD_REQUEST).equals(statusLine.getStatusCode())) {
                return new ArtifactoryClientException("Artifactory: Bad request. " + responsePayload);
            }
        } catch (IOException e) {
            return new ArtifactoryClientException("Artifactory: Can't read response");
        }

        return new ArtifactoryClientException(message);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Testing API
    static public void main(String[] args) {
        ArtifactoryClient ac = new ArtifactoryClient(null, "http://localhost:8081/artifactory", "admin", "password");

        Repository firstRepository = null;
        Artifact firstArtifact = null;

        System.out.println("Retrieving Repositories...");
        List<Repository> repositories = null;
        try {
            repositories = ac.getRepositories(100);
            for (Repository r : repositories) {
                if (firstRepository == null) firstRepository = r;
                System.out.println("Found Repository: " + r.getId());
                System.out.println("Type: " + r.getType());
                System.out.println("Description: " + r.getDescription());
                System.out.println("URL: " + r.getUrl());
            }
        } catch (ArtifactoryClientException e) {
            System.out.print(e.toString());
        }

        System.out.println("Retrieving Artifacts...");
        List<Artifact> artifacts = null;
        try {
            artifacts = ac.getArtifacts("libs-release-local", "org.apache.commons", null, null, null, 100);
            for (Artifact a : artifacts) {
                if (firstArtifact == null) firstArtifact = a;
                System.out.println("Found Artifact: " + a.getId());
                System.out.println("Path: " + a.getPath());
                System.out.println("Name: " + a.getName());
                System.out.println("Version: " + a.getVersion());
                System.out.println("Repository: " + a.getRepo());
                System.out.println("Download URI: " + a.getDownloadUri());
                System.out.println("Created: " + a.getCreated());
                System.out.println("Created By: " + a.getCreatedBy());
            }
        } catch (ArtifactoryClientException e) {
            System.out.print(e.toString());
        }


    }


}
