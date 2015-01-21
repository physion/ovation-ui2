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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Folder;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.ui.importer.FileMetadata;
import us.physion.ovation.ui.importer.ImageImporter;

public final class EntityUtilities {

    public static List<Measurement> insertMeasurements(Experiment experiment, File[] files) {
        List<File> images = getImages(files);

        DateTime[] range = getRange(files, images);
        DateTime start = range[0];
        DateTime end = range[1];

        Epoch e = experiment.insertEpoch(start,
                end,
                null,
                Maps.<String, Object>newHashMap(),
                Maps.<String, Object>newHashMap());

        return insertMeasurements(e, files, images);
    }

    public static List<Resource> insertResources(Folder folder, File[] files) {
        List<Resource> result = Lists.newLinkedList();

        for (File f : files) {
            result.add(folder.addResource(f.getName(), f.toURI().toURL(), ContentTypes.getContentType(f)));
        }

        return result;
    }


    public static List<Measurement> insertMeasurements(Epoch e, File[] files) {
        return insertMeasurements(e, files, getImages(files));
    }

    public static List<Measurement> insertMeasurements(EpochGroup epochGroup, File[] files) {
        List<File> images = getImages(files);

        DateTime[] range = getRange(files, images);
        DateTime start = range[0];
        DateTime end = range[1];

        Epoch e = epochGroup.insertEpoch(start,
                end,
                null,
                Maps.<String, Object>newHashMap(),
                Maps.<String, Object>newHashMap());

        return insertMeasurements(e, files, images);
    }

    private static List<Measurement> insertMeasurements(Epoch e, File[] files, List<File> images) {
        List<Measurement> result = Lists.newLinkedList(ImageImporter.importImageMeasurements(e, images)
                .toList()
                .toBlockingObservable()
                .lastOrDefault(Lists.<Measurement>newArrayList()));

        Set<File> others = Sets.newHashSet(files);
        others.removeAll(images);
        for (File f : others) {
            try {
                Measurement m = e.insertMeasurement(f.getName(),
                        Sets.<String>newHashSet(),
                        Sets.<String>newHashSet(),
                        f.toURI().toURL(),
                        ContentTypes.getContentType(f));

                result.add(m);
            } catch (MalformedURLException ex) {
                Toolkit.getDefaultToolkit().beep();
            } catch (IOException ex) {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        return result;
    }

    private static DateTime[] getRange(File[] files, List<File> images) {
        DateTime start = new DateTime();
        DateTime end = new DateTime();


        for (File f : images) {
            FileMetadata m = new FileMetadata(f);
            if (m.getEnd(false).isAfter(end)) {
                end = m.getEnd(false);
            }

            if (m.getStart().isBefore(start)) {
                start = m.getStart();
            }
        }

        for (File f : files) {
            DateTime lastModified = new DateTime(f.lastModified());
            if (lastModified.isAfter(end)) {
                end = lastModified;
            }

            if (start.isBefore(lastModified)) {
                start = lastModified;
            }

        }

        return new DateTime[]{start, end};
    }

    private static List<File> getImages(File[] files){
        List<File> images = Lists.newLinkedList(Iterables.filter(Lists.newArrayList(files),
                new Predicate<File>() {

                    @Override
                    public boolean apply(File input) {
                        return ImageImporter.canImport(input);
                    }
                }));

        return images;
    }
}
