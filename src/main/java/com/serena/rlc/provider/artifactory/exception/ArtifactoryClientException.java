/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.artifactory.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TFS Release Manager Client Exceptions
 * @author klee@serena.com
 */
public class ArtifactoryClientException extends Exception {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ArtifactoryClientException.class);

    public ArtifactoryClientException() {
    }

    public ArtifactoryClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtifactoryClientException(String message) {
        super(message);
    }

    public ArtifactoryClientException(Throwable cause) {
        super(cause);
    }
}
