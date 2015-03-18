/*
 * Copyright (C) 2015 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 *
 * Authors:
 * Juan Javier Cabanas
 * Álvaro Martínez Marco
 *
 */

package com.videonasocialmedia.videona.model.entities.editor;

import java.net.URI;

public abstract class Effect {

    protected URI iconUri;
    protected String name;

    public URI getIconUri() {
        return iconUri;
    }

    public String getName() {
        return name;
    }

}
