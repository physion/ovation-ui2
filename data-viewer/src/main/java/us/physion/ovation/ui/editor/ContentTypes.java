/*
 * Copyright (C) 2014 Physion LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package us.physion.ovation.ui.editor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

/**
 * Provides extension to content type mappings
 * @author barry
 */
public class ContentTypes {

    public static String getContentType(File file) throws IOException {
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if (contentType == null) {
            Map<String, String> customContentTypes = customTypes();

            final String extension = FilenameUtils.getExtension(file.getName());
            if (customContentTypes.containsKey(extension)) {
                contentType = customContentTypes.get(extension);
            } else {
                contentType = "application/octet-stream"; // fallback to binary
            }
        }

        return contentType;
    }

    private static Map<String, String> customTypes() {
        final Map<String, String> customContentTypes = Maps.newHashMap();
        customContentTypes.put("doc", "application/msword");
        customContentTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        customContentTypes.put("xls", "application/vnd.ms-excel");
        customContentTypes.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        customContentTypes.put("ppt", "application/vnd.ms-powerpoint");
        customContentTypes.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        customContentTypes.put("csv", "text/csv");
        customContentTypes.put("tif", "image/tiff");
        customContentTypes.put("tiff", "image/tiff");
        customContentTypes.put("lsm", "image/tiff");
        customContentTypes.put("pdf", "application/pdf");
        return customContentTypes;
    }

    public static List<String> getContentTypes() {
        List<String> result = Lists.newArrayList(
                Sets.newHashSet(customTypes().values()));

        Collections.sort(result);

        return result;
    }

}
