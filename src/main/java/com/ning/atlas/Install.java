package com.ning.atlas;

import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Node;

public class Install extends Change
{
    private final Installer installer;

    public Install(Installer installer, Root root, Node node) {
        this.installer = installer;
    }

    public void install() {

    }
}
