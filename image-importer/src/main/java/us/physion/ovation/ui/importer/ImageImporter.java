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

package us.physion.ovation.ui.importer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import rx.Observable;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochContainer;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.exceptions.OvationException;

/**
 * Import procedure for image file(s)
 */
public class ImageImporter {

    public static Observable<Measurement> importImagesEpoch(EpochContainer container, Iterable<File> files) {
        List<FileMetadata> metadata = Lists.newArrayList();
        for (File f : files) {
            metadata.add(new FileMetadata(f));
        }


        List<Measurement> result = Lists.newLinkedList();
        for (FileMetadata fileMetadata : metadata) {

            Epoch epoch = container.insertEpoch(fileMetadata.getStart(),
                    fileMetadata.getEnd(false),
                    null,
                    fileMetadata.getEpochProtocolParameters(),
                    fileMetadata.getDeviceParameters());

            for (Map<String, Object> m : fileMetadata.getMeasurements()) {
                epoch.insertMeasurement(String.format("Image %d", (Integer) m.get(FileMetadata.IMAGE_NUMBER)),
                        Sets.<String>newHashSet(),
                        Sets.newHashSet((Set<String>) m.get(FileMetadata.DEVICE_NAMES)),
                        (URL) m.get(FileMetadata.IMAGE_URL),
                        (String) m.get(FileMetadata.MIME_TYPE));
            }
        }

        return Observable.from(result);
    }

    public static Observable<Measurement> importImageMeasurements(Epoch epoch, Iterable<File> files) {
        List<FileMetadata> metadata = Lists.newArrayList();
        for (File f : files) {
            metadata.add(new FileMetadata(f));
        }

        List<Measurement> result = Lists.newLinkedList();
        for (FileMetadata fileMetadata : metadata) {

            for (Map.Entry<String, Object> e : fileMetadata.getEpochProtocolParameters().entrySet()) {
                epoch.addProtocolParameter(e.getKey(), e.getValue());
            }

            for (Map.Entry<String, Object> e : fileMetadata.getDeviceParameters().entrySet()) {
                epoch.addDeviceParameter(e.getKey(), e.getValue());
            }

            for (Map<String, Object> m : fileMetadata.getMeasurements()) {
                try {
                    result.add(epoch.insertMeasurement(String.format("Image %d", (Integer) m.get(FileMetadata.IMAGE_NUMBER)),
                            Sets.<String>newHashSet(),
                            Sets.<String>newHashSet(),
                            new URL((String) m.get(FileMetadata.IMAGE_URL)),
                            (String) m.get(FileMetadata.MIME_TYPE)));
                } catch (MalformedURLException ex) {
                    throw new OvationException(ex);
                }
            }
        }

        return Observable.from(result);
    }

    public static Set<String> importableExtensions = Sets.newHashSet("tif", "tiff", "lsm");

    public static boolean canImport(File f) {
        return importableExtensions.contains(FilenameUtils.getExtension(f.getName()).toLowerCase());
    }
}
