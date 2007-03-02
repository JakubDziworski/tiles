/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.tiles.taglib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tiles.ComponentContext;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.TilesException;
import org.apache.tiles.access.TilesAccess;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

/**
 * Base tag for the tiles tags which interact with the container.
 * Provides standard support for security, and provides access
 * to the container and component context.
 * </p>
 *
 * @since Tiles 2.0
 * @version $Rev$ $Date$
 */
public abstract class ContainerTagSupport extends BodyTagSupport {
    
    /**
     * The log instance for this tag.
     */
    private static final Log LOG = LogFactory.getLog(ContainerTagSupport.class);

    private String role;
    protected TilesContainer container;
    protected ComponentContext componentContext;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * By default, all ContainerTags evaluate their body.  Subclasses may choose to be more selective.
     * In any case, children can rely upon the container and componentContext being initialized if they
     * call <code>super.doStartTag()</code>
     */
    public int doStartTag() {
        container = TilesAccess.getContainer(pageContext.getServletContext());
        startContext(pageContext);
        return EVAL_BODY_BUFFERED;
    }
    

    public int doEndTag() throws JspException {
        try {
            if (isAccessAllowed()) {
                execute();
            }
        } catch (TilesException e) {
            String message = "Error executing tag: " + e.getMessage();
            LOG.error(message, e);
            throw new JspException(message, e);
        } catch (IOException io) {
            String message = "IO Error executing tag: " + io.getMessage();
            LOG.error(message, io);
            throw new JspException(message, io);
        } finally {
            endContext(pageContext);
        }
        
        return EVAL_PAGE;
    }



    public void release() {
        super.release();
        this.role = null;
        this.container = null;
        this.componentContext = null;
    }

    protected abstract void execute() throws TilesException, JspException, IOException;

    protected boolean isAccessAllowed() {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        return (role == null || req.isUserInRole(role));
    }
    
    protected void startContext(PageContext context) {
        if (container != null) {
            componentContext = container.startContext(pageContext);
        }
    }
    
    protected void endContext(PageContext context) {
        if (componentContext != null && container != null) {
            container.endContext(pageContext);
        }
    }
}