/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core.config;

import java.io.File;
import java.util.List;

/**
 * Configuration monitor that periodically checks the timestamp of the configuration file and calls the
 * ConfigurationListeners when an update occurs.
 */
public class FileConfigurationMonitor implements ConfigurationMonitor {

    private static final int MASK = 0x0f;

    private final static int MIN_INTERVAL = 30;

    private final File file;

    private long lastModified;

    private final List<ConfigurationListener> listeners;

    private final int interval;

    private long nextCheck;

    private volatile int counter = 0;

    /**
     * Constructor.
     * @param file The File to monitor.
     * @param listeners The List of ConfigurationListeners to notify upon a change.
     * @param interval The monitor interval in seconds. The minimum interval is 30 seconds.
     */
    public FileConfigurationMonitor(File file, List<ConfigurationListener> listeners, int interval) {
        this.file = file;
        this.lastModified = file.lastModified();
        this.listeners = listeners;
        this.interval = (interval < MIN_INTERVAL ? MIN_INTERVAL : interval) * 1000;
        this.nextCheck = System.currentTimeMillis() + interval;
    }

    /**
     * Called to determine if the configuration has changed.
     */
    public void checkConfiguration() {
        if ((++counter & MASK) == 0) {
            long current = System.currentTimeMillis();
            synchronized(this) {
                if (current >= nextCheck) {
                    nextCheck = current + interval;
                    if (lastModified >= file.lastModified()) {
                        lastModified = file.lastModified();
                        for (ConfigurationListener listener : listeners) {
                            listener.onChange();
                        }
                    }
                }
            }
        }
    }
}
